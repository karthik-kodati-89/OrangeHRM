package com.orangehrm.pages;

import com.orangehrm.base.BasePage;
import com.orangehrm.interfaces.IPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage implements IPage {

    private final By header        = By.cssSelector(".oxd-topbar-header-breadcrumb h6");
    private final By userDropdown  = By.cssSelector(".oxd-userdropdown-tab");
    private final By logoutLink    = By.xpath("//a[normalize-space()='Logout']");

    // Sidebar menu - generic locator (uses parameterised XPath)
    private By sidebarMenu(String menuName) {
        return By.xpath("//span[normalize-space()='" + menuName + "']");
    }

    public DashboardPage(WebDriver driver) { super(driver); }

    public String getHeaderText()           { return getText(header); }

    public PIMPage navigateToPIM()          { click(sidebarMenu("PIM"));   return new PIMPage(driver); }
    public AdminPage navigateToAdmin()      { click(sidebarMenu("Admin")); return new AdminPage(driver); }

    public LoginPage logout() {
        click(userDropdown);
        click(logoutLink);
        return new LoginPage(driver);
    }

    @Override public boolean isPageLoaded() { return isDisplayed(header); }
    @Override public String getPageTitle()  { return driver.getTitle(); }
    @Override public String getPageUrl()    { return driver.getCurrentUrl(); }
    @Override public boolean isAt()         { return getPageUrl().contains("/dashboard"); }
}
