package com.orangehrm.pages;

import com.orangehrm.base.BasePage;
import com.orangehrm.exceptions.FrameworkException;
import com.orangehrm.interfaces.IPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * Demonstrates DROPDOWN handling.
 *
 * OrangeHRM uses CUSTOM (non-<select>) dropdowns built with divs - the
 * Selenium 'Select' class will NOT work on them. Both styles are shown.
 */
public class AdminPage extends BasePage implements IPage {

    private final By userRoleDropdown   = By.xpath("(//div[@class='oxd-select-text-input'])[1]");
    private final By statusDropdown     = By.xpath("(//div[@class='oxd-select-text-input'])[2]");
    private final By dropdownOptions    = By.cssSelector(".oxd-select-dropdown .oxd-select-option");
    private final By searchBtn          = By.xpath("//button[normalize-space()='Search']");
    private final By resetBtn           = By.xpath("//button[normalize-space()='Reset']");
    private final By recordsFoundLabel  = By.cssSelector(".orangehrm-horizontal-padding span");

    public AdminPage(WebDriver driver) { super(driver); }

    /** Select an option from OrangeHRM's custom dropdown. */
    public void selectFromCustomDropdown(By dropdown, String optionText) {
        click(dropdown);                                   // open the dropdown
        List<WebElement> options = getElements(dropdownOptions);
        for (WebElement opt : options) {
            if (opt.getText().trim().equalsIgnoreCase(optionText)) {
                opt.click();
                return;
            }
        }
        throw new FrameworkException("Dropdown option not found: " + optionText);
    }

    public void selectUserRole(String role)        { selectFromCustomDropdown(userRoleDropdown, role); }
    public void selectStatus(String status)        { selectFromCustomDropdown(statusDropdown, status); }

    public void searchUser(String role, String status) {
        selectUserRole(role);
        selectStatus(status);
        click(searchBtn);
    }

    public void resetFilters() { click(resetBtn); }
    public String getRecordsFoundText() { return getText(recordsFoundLabel); }

    @Override public boolean isPageLoaded() { return isDisplayed(searchBtn); }
    @Override public String getPageTitle()  { return driver.getTitle(); }
    @Override public String getPageUrl()    { return driver.getCurrentUrl(); }
    @Override public boolean isAt()         { return getPageUrl().contains("/admin"); }
}
