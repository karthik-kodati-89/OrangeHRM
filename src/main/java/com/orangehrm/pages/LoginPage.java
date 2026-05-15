package com.orangehrm.pages;

import com.orangehrm.base.BasePage;
import com.orangehrm.interfaces.IPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * INHERITANCE  : extends BasePage
 * INTERFACES   : implements IPage
 * 'super'      : invokes BasePage(driver) constructor
 * DYNAMIC POLY : overrides isPageLoaded()
 */
public class LoginPage extends BasePage implements IPage {

    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton   = By.cssSelector("button[type='submit']");
    private final By errorAlert    = By.cssSelector(".oxd-alert-content-text");
    private final By logo          = By.cssSelector(".orangehrm-login-branding > img");

    public LoginPage(WebDriver driver) {
        super(driver);              // 'super' calls BasePage constructor
    }

    // ============== ACTIONS ==============
    public DashboardPage loginAs(String user, String pass) {
        type(usernameInput, user);
        type(passwordInput, pass);
        click(loginButton);
        return new DashboardPage(driver);
    }

    public String getErrorMessage() {
        return getText(errorAlert);
    }

    // ============== OVERRIDES ==============
    @Override
    public boolean isPageLoaded() {           // abstract method - dynamic polymorphism
        return isDisplayed(logo);
    }

    @Override
    public String getPageTitle() { return driver.getTitle(); }

    @Override
    public String getPageUrl() { return driver.getCurrentUrl(); }

    @Override
    public boolean isAt() { return isPageLoaded(); }
}
