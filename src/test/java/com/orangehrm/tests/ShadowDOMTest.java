package com.orangehrm.tests;

import com.orangehrm.base.DriverFactory;
import com.orangehrm.pages.ShadowDomPage;
import com.orangehrm.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * SHADOW DOM test.
 * Does not extend BaseTest because it points at a different URL than
 * OrangeHRM. Reuses DriverFactory + ShadowDomPage from the framework.
 */
public class ShadowDOMTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver(ConfigReader.get("browser"));
        // selenium.dev uses shadow DOM in its components
        driver.get("https://www.selenium.dev/selenium/web/shadow_dom/");
    }

    @AfterMethod
    public void tearDown() { DriverFactory.quitDriver(); }

    @Test(description = "Pierce a shadow root via the BasePage helper")
    public void testReadShadowText() {
        ShadowDomPage page = new ShadowDomPage(driver);
        // The selenium test fixture exposes a custom-checkbox-element
        // with an internal #content paragraph.
        WebElement target = page.findInShadowManual(
                "#shadowHost", "#checkbox");
        Assert.assertTrue(target.isDisplayed(),
                "Shadow DOM element should be visible");
    }
}
