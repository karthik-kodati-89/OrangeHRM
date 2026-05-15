package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * MULTI-THREADING / PARALLEL test.
 *
 * Run with: mvn test -DsuiteXmlFile=testng.xml
 * In testng.xml the suite uses parallel="methods" thread-count="3".
 *
 * Each method runs on its own thread. ThreadLocal<WebDriver> in
 * DriverFactory ensures each thread gets its own browser instance and
 * the driver references never leak across threads.
 *
 * The 'synchronized' methods in DriverFactory protect the
 * download/initialisation critical section.
 */
public class ParallelLoginTest extends BaseTest {

    @Test
    public void parallelTest1() { runLogin("Test-1"); }

    @Test
    public void parallelTest2() { runLogin("Test-2"); }

    @Test
    public void parallelTest3() { runLogin("Test-3"); }

    private void runLogin(String tag) {
        System.out.println("[" + tag + "] running on thread: "
                + Thread.currentThread().getName());

        DashboardPage dash = new LoginPage(driver)
                .loginAs(ConfigReader.get("username"), ConfigReader.get("password"));
        Assert.assertTrue(dash.isPageLoaded(), "[" + tag + "] dashboard load failed");
    }
}
