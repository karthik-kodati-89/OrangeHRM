package com.orangehrm.tests;

import com.orangehrm.base.DriverFactory;
import com.orangehrm.pages.IFramePage;
import com.orangehrm.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IFrameTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initDriver(ConfigReader.get("browser"));
        driver.get("https://the-internet.herokuapp.com/iframe");
    }

    @AfterMethod
    public void tearDown() { DriverFactory.quitDriver(); }

    @Test(description = "Switch into iframe, type, switch back")
    public void testIframeWriteAndRead() {
        IFramePage page = new IFramePage(driver);
        page.writeInsideIframe("Hello from Selenium iframe!");
        String content = page.readIframeContent();
        Assert.assertTrue(content.contains("Hello"), "iframe text not persisted");
    }
}
