# OrangeHRM Selenium Java Framework

A Selenium 4 + TestNG framework against `https://opensource-demo.orangehrmlive.com`, designed to demonstrate every OOPS, multi-threading, and Selenium concept, **and run on Jenkins**.

## How to run locally

```bash
# Prerequisites: JDK 17+, Maven 3.8+, Chrome installed
mvn clean test                              # run everything in testng.xml
mvn test -Dtest=LoginTest                   # run one class
mvn test -Dtest=PIMTest#testEmployeeTableHeaders   # run one method

# Override config via system properties (CI-style)
mvn test -Dbrowser=firefox -Dheadless=true
```

## How to run on Jenkins

### Step 1 — Install Jenkins and required plugins

Install Jenkins, then under **Manage Jenkins → Plugins → Available**:

- Pipeline
- Git
- Maven Integration
- JDK Tool
- **Allure Jenkins Plugin** (essential for report rendering)
- HTML Publisher (optional, for ExtentReports)

Restart Jenkins after installing.

### Step 2 — Configure global tools

Go to **Manage Jenkins → Tools** and add the following installations. The names below are referenced verbatim in the `Jenkinsfile` — keep them identical or update the Jenkinsfile.

| Tool                | Name in Jenkins | Notes                                                |
| ------------------- | --------------- | ---------------------------------------------------- |
| JDK                 | `JDK17`         | Auto-install or point at an existing JDK 17+         |
| Maven               | `Maven3`        | Auto-install Maven 3.9.x                             |
| Allure Commandline  | `Allure`        | Auto-install Allure 2.27.0                           |

### Step 3 — Install Chrome on the Jenkins agent

If your agent is Linux:

```bash
sudo apt update
sudo apt install -y google-chrome-stable
```

Jenkins running as a system service has no display, so `--headless=true` is mandatory. The `DriverFactory` already adds `--no-sandbox` and `--disable-dev-shm-usage` when headless mode is on — these are essential on CI agents.

### Step 4 — Push the framework to Git

```bash
git init
git add .
git commit -m "OrangeHRM automation framework"
git remote add origin https://github.com/<you>/orangehrm-automation.git
git push -u origin main
```

### Step 5 — Create the pipeline job in Jenkins

1. Jenkins dashboard → **New Item** → enter a name → select **Pipeline** → OK
2. Scroll to **Pipeline** section
3. **Definition**: `Pipeline script from SCM`
4. **SCM**: Git
5. **Repository URL**: your repo URL
6. **Credentials**: add username/token if the repo is private
7. **Branch**: `*/main`
8. **Script Path**: `Jenkinsfile`
9. Save

### Step 6 — Run it

Click **Build with Parameters**. You can pick:

- `BROWSER` — `chrome` / `firefox` / `edge`
- `HEADLESS` — `true` (always for CI) / `false`
- `SUITE_FILE` — defaults to `testng.xml`

After the build finishes, you'll see:

- **Test Result** link (TestNG → JUnit XML from surefire)
- **Allure Report** link (full step-by-step report with attachments)
- Archived screenshots and logs under **Build Artifacts**

### Step 7 — Schedule or auto-trigger

In the job config under **Build Triggers**:

- **Poll SCM**: `H/5 * * * *` (check Git every 5 minutes)
- **Build periodically**: `H 2 * * *` (run nightly at 2 AM)
- **GitHub hook trigger for GITScm polling** for instant push triggers (requires a webhook configured on the repo)

## Jenkins ↔ Framework wiring

The system property bridge is the key piece. Anything Jenkins passes with `-D` propagates into the framework:

| Jenkins parameter   | Maven flag           | Where it's read                         |
| ------------------- | -------------------- | --------------------------------------- |
| `BROWSER`           | `-Dbrowser=chrome`   | `ConfigReader.get("browser")`           |
| `HEADLESS`          | `-Dheadless=true`    | `DriverFactory.isHeadless()`            |
| `SUITE_FILE`        | `-DsuiteXmlFile=...` | Surefire plugin                         |

System properties take precedence over `config.properties`, so local devs and CI can have different defaults without touching the file.

## Concept → File map

### 1. OOPS principles

| Concept | Where to look |
|---|---|
| **Class & Object** | every `.java` file; `new LoginPage(driver)` in tests |
| **Abstraction (abstract class)** | `BasePage.java` — declares abstract `isPageLoaded()` |
| **Interface** | `IPage.java`, `IKeywordAction.java` (functional interface used with lambdas in `KeywordEngine`) |
| **Constructor** | `BasePage(WebDriver)`, `LoginPage(WebDriver)` etc. |
| **`this` keyword** | `BasePage` constructor — `this.driver = driver` resolves param-vs-field shadowing |
| **`super` keyword** | every page class — `super(driver)` invokes `BasePage` constructor |
| **Static polymorphism (overloading)** | `BasePage#click(By)`, `click(WebElement)`, `click(By, int)`; same for `type` |
| **Dynamic polymorphism (overriding)** | every page overrides `isPageLoaded()`, `getPageTitle()`, `isAt()` |
| **Variable scopes** | `BasePage` has *instance* (`driver`, `wait`), *static* (`DEFAULT_TIMEOUT`), *local* (`localWait` inside `click(By, int)`), *parameter* (`driver`) |

### 2. Exception handling & multi-threading

| Concept | Where |
|---|---|
| **Custom exception** | `FrameworkException` (unchecked) |
| **try-catch-finally** | `IFramePage` (frame switch back in `finally`), `ConfigReader` static block |
| **try-with-resources** | `ConfigReader`, `ExcelReader` (auto-close streams) |
| **`throws` propagation** | `ExcelReader` wraps `IOException` into `FrameworkException` |
| **`ThreadLocal`** | `DriverFactory.DRIVER` — per-thread WebDriver |
| **`synchronized`** | `DriverFactory#initDriver`, `quitDriver` — guards driver setup |
| **Parallel execution** | `testng.xml` `parallel="methods" thread-count="3"`, demoed by `ParallelLoginTest` |

### 3. Selenium concepts

| Concept | File(s) |
|---|---|
| **POM** | `pages/` — every class is a page object |
| **Tables** | `PIMPage#getEmployeeTableData`, `getTableHeaders` |
| **Pagination** | `PIMPage#goToPage`, `findEmployeeAcrossAllPages` |
| **Dropdowns (custom Angular)** | `AdminPage#selectFromCustomDropdown` |
| **Dropdowns (native `<select>`)** | `BasePage#selectByVisibleText` (uses `Select` class) |
| **Data Driven** | `LoginTest#testInvalidLogin` (`@DataProvider`), `HybridTest#testEmployeeSearchHybrid` (Excel via `ExcelReader`) |
| **Keyword Driven** | `KeywordEngine` + `KeywordDrivenTest` + `KeywordTests.xlsx` |
| **Hybrid** | `HybridTest` — POM + Data-Driven + Keyword-Driven all in one |
| **iFrames** | `IFramePage`, `IFrameTest` |
| **Shadow DOM** | `BasePage#findInShadow`, `ShadowDomPage`, `ShadowDOMTest` |

## Architecture

```
src/main/java/com/orangehrm/
├── base/         BasePage (abstract), DriverFactory (ThreadLocal + CI flags), BaseTest
├── interfaces/   IPage, IKeywordAction
├── pages/        Page Objects (Login, Dashboard, PIM, Admin, IFrame, ShadowDom)
├── keywords/     KeywordEngine
├── utils/        ConfigReader (sys-prop overrides), ExcelReader
└── exceptions/   FrameworkException

src/test/java/com/orangehrm/tests/
├── LoginTest, PIMTest, AdminTest        ── feature tests
├── KeywordDrivenTest, HybridTest        ── framework-style tests
├── IFrameTest, ShadowDOMTest            ── concept tests
└── ParallelLoginTest                    ── multi-threading demo

Jenkinsfile        ── declarative pipeline (build/test/report stages)
```

## What changed for Jenkins

| File                              | Change                                                              |
| --------------------------------- | ------------------------------------------------------------------- |
| `pom.xml`                         | Added `allure-testng` dep + AspectJ weaver argLine in surefire      |
| `DriverFactory.java`              | Reads `-Dheadless` system property; adds CI Chrome flags             |
| `ConfigReader.java`               | System properties (`-Dbrowser=...`) now override `config.properties` |
| `Jenkinsfile`                     | **New** — declarative pipeline with parameters and Allure publish    |
| `src/test/resources/allure.properties` | **New** — points Allure at `target/allure-results`              |
| `.gitignore`                      | **New** — excludes `target/`, IDE files, logs, screenshots          |

## Common Jenkins gotchas

- **Chrome not installed on the agent** — `sudo apt install google-chrome-stable`
- **Headless missing `--no-sandbox`** — fixed in `DriverFactory` but worth flagging
- **Allure plugin missing** — install via *Manage Jenkins → Plugins*
- **Tool names mismatch** — `JDK17` / `Maven3` / `Allure` must match `tools {}` block in Jenkinsfile
- **Permissions on `/var/lib/jenkins/workspace/`** — Jenkins-as-service vs. Jenkins-as-user run as different OS users; pick one
- **Corporate proxy** — Maven and WebDriverManager both need proxy config (`~/.m2/settings.xml` and JVM `-Dhttps.proxyHost=...`)
