package com.orangehrm.base;

import com.orangehrm.exceptions.FrameworkException;
import com.orangehrm.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

/**
 * Thread-safe driver factory.
 *
 * MULTI-THREADING concepts demonstrated:
 *  - ThreadLocal<WebDriver>  -> each thread gets its own driver instance
 *  - synchronized methods    -> critical sections guarded
 *  - Static variables        -> shared across the JVM
 *
 * CI/Jenkins support:
 *  - System property -Dheadless=true overrides config.properties
 *  - System property -Dbrowser=chrome overrides config.properties
 *  - Headless Chrome on Linux Jenkins agents needs --no-sandbox
 *    and --disable-dev-shm-usage to avoid crashes
 */
public final class DriverFactory {

    // Each thread gets its own WebDriver -> the core of parallel-safe Selenium
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    // Private constructor - utility class, no instances allowed
    private DriverFactory() {
        throw new UnsupportedOperationException("Utility class - cannot instantiate");
    }

    /**
     * 'synchronized' ensures only one thread initialises a browser at a time.
     * Without it, parallel WebDriverManager calls can race on driver downloads.
     */
    public static synchronized WebDriver initDriver(String browser) {
        // System property -Dheadless=true wins over config.properties.
        // This is how Jenkins forces headless mode regardless of dev config.
        boolean headless = isHeadless();

        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions co = new ChromeOptions();
                if (headless) {
                    co.addArguments("--headless=new");
                    // CI essentials - without these, Chrome crashes on Linux Jenkins agents
                    co.addArguments("--no-sandbox");
                    co.addArguments("--disable-dev-shm-usage");
                    co.addArguments("--window-size=1920,1080");
                    co.addArguments("--disable-gpu");
                }
                co.addArguments("--remote-allow-origins=*", "--disable-notifications");
                driver = new ChromeDriver(co);
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions fo = new FirefoxOptions();
                if (headless) {
                    fo.addArguments("-headless");
                    fo.addArguments("--width=1920");
                    fo.addArguments("--height=1080");
                }
                driver = new FirefoxDriver(fo);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions eo = new EdgeOptions();
                if (headless) {
                    eo.addArguments("--headless=new");
                    eo.addArguments("--no-sandbox");
                    eo.addArguments("--disable-dev-shm-usage");
                    eo.addArguments("--window-size=1920,1080");
                }
                driver = new EdgeDriver(eo);
            }
            default -> throw new FrameworkException("Unsupported browser: " + browser);
        }

        // Headless mode doesn't need maximize - we set explicit window-size above
        if (!headless) {
            driver.manage().window().maximize();
        }
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getInt("implicit.wait")));

        DRIVER.set(driver);
        return driver;
    }

    /**
     * Checks both system property (-Dheadless=true) and config.properties.
     * System property takes precedence - this is how Jenkins overrides
     * local dev settings without touching config files.
     */
    private static boolean isHeadless() {
        String sysProp = System.getProperty("headless");
        if (sysProp != null && !sysProp.isBlank()) {
            return Boolean.parseBoolean(sysProp);
        }
        return ConfigReader.getBoolean("headless");
    }

    public static WebDriver getDriver() {
        WebDriver d = DRIVER.get();
        if (d == null) {
            throw new FrameworkException(
                "Driver not initialised for thread: " + Thread.currentThread().getName());
        }
        return d;
    }

    public static synchronized void quitDriver() {
        WebDriver d = DRIVER.get();
        if (d != null) {
            d.quit();
            DRIVER.remove();   // critical -> prevents memory leaks across thread reuse
        }
    }
}
