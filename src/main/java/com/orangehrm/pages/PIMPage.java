package com.orangehrm.pages;

import com.orangehrm.base.BasePage;
import com.orangehrm.interfaces.IPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates:
 *   - TABLE handling (employee list grid)
 *   - PAGINATION (page-number buttons + Next/Prev)
 */
public class PIMPage extends BasePage implements IPage {

    private final By searchEmployeeName = By.cssSelector("input[placeholder='Type for hints...']");
    private final By searchButton       = By.xpath("//button[normalize-space()='Search']");
    private final By tableRows          = By.cssSelector(".oxd-table-card");
    private final By tableHeaderCells   = By.cssSelector(".oxd-table-header-cell");
    private final By rowCells           = By.cssSelector(".oxd-table-cell");
    private final By paginationButtons  = By.cssSelector(".oxd-pagination__page-item button");
    private final By nextPageBtn        = By.cssSelector("button[class*='next']");
    private final By totalRecordsLabel  = By.cssSelector(".orangehrm-horizontal-padding span");

    public PIMPage(WebDriver driver) { super(driver); }

    public void searchByEmployeeName(String name) {
        type(searchEmployeeName, name);
        click(searchButton);
    }

    // ============= TABLE READING =============
    /**
     * Reads the employee grid and returns each row's cell values.
     * Skips the leading checkbox cell (index 0).
     */
    public List<List<String>> getEmployeeTableData() {
        List<List<String>> table = new ArrayList<>();
        List<WebElement> rows = getElements(tableRows);

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(rowCells);
            List<String> rowData = new ArrayList<>();
            // Skip cell 0 (checkbox) and last cell (action buttons) for clean data
            for (int i = 1; i < cells.size() - 1; i++) {
                rowData.add(cells.get(i).getText().trim());
            }
            table.add(rowData);
        }
        return table;
    }

    public List<String> getTableHeaders() {
        List<String> headers = new ArrayList<>();
        for (WebElement h : getElements(tableHeaderCells)) headers.add(h.getText().trim());
        return headers;
    }

    /** Locates an employee by ID across the visible page. */
    public boolean isEmployeeInTable(String empId) {
        for (List<String> row : getEmployeeTableData()) {
            if (!row.isEmpty() && row.get(0).equalsIgnoreCase(empId)) return true;
        }
        return false;
    }

    // ============= PAGINATION =============
    public int getTotalPages() {
        // Pagination buttons: [1][2][3]... Last button is Next, exclude it.
        List<WebElement> btns = driver.findElements(paginationButtons);
        return Math.max(1, btns.size());
    }

    public void goToPage(int pageNumber) {
        List<WebElement> btns = driver.findElements(paginationButtons);
        for (WebElement btn : btns) {
            if (btn.getText().trim().equals(String.valueOf(pageNumber))) {
                scrollIntoView(btn);
                btn.click();
                return;
            }
        }
        throw new IllegalArgumentException("Page " + pageNumber + " not visible in pagination.");
    }

    public void clickNext() { click(nextPageBtn); }

    /**
     * Real-world pagination scenario:
     * iterate every page until target empId is found, return true on hit.
     */
    public boolean findEmployeeAcrossAllPages(String empId) {
        int totalPages = getTotalPages();
        for (int page = 1; page <= totalPages; page++) {
            if (page > 1) goToPage(page);
            if (isEmployeeInTable(empId)) return true;
        }
        return false;
    }

    public String getTotalRecordsText() { return getText(totalRecordsLabel); }

    @Override public boolean isPageLoaded() { return isDisplayed(searchButton); }
    @Override public String getPageTitle()  { return driver.getTitle(); }
    @Override public String getPageUrl()    { return driver.getCurrentUrl(); }
    @Override public boolean isAt()         { return getPageUrl().contains("/pim"); }
}
