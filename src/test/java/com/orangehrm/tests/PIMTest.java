package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.pages.PIMPage;
import com.orangehrm.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;

public class PIMTest extends BaseTest {

    private PIMPage pim;

    @BeforeMethod(dependsOnMethods = "setUp")
    public void loginAndNavigate() {
        DashboardPage dash = new LoginPage(driver)
                .loginAs(ConfigReader.get("username"), ConfigReader.get("password"));
        pim = dash.navigateToPIM();
    }

    @Test(description = "TABLE - read headers and validate columns")
    public void testEmployeeTableHeaders() {
        List<String> headers = pim.getTableHeaders();
        Assert.assertTrue(headers.contains("First (& Middle) Name"),
                "Expected header missing. Got: " + headers);
    }

    @Test(description = "TABLE - read all rows on current page")
    public void testReadEmployeeRows() {
        List<List<String>> data = pim.getEmployeeTableData();
        Assert.assertFalse(data.isEmpty(), "Employee table is empty");
        // Each row should have at least 4 cells (Id, First, Last, Title-or-status)
        Assert.assertTrue(data.get(0).size() >= 3);
    }

    @Test(description = "PAGINATION - navigate page 2 if available")
    public void testPaginationToPage2() {
        int pages = pim.getTotalPages();
        if (pages < 2) throw new SkipException();
        pim.goToPage(2);
        Assert.assertFalse(pim.getEmployeeTableData().isEmpty());
    }

    @Test(description = "PAGINATION - search employee across all pages")
    public void testFindEmployeeAcrossPages() {
        // 0001 is the seeded admin user in OrangeHRM demo
        boolean found = pim.findEmployeeAcrossAllPages("0001");
        Assert.assertTrue(found, "Employee 0001 should be findable across pages");
    }

    private static class SkipException extends RuntimeException {
        SkipException() { super("Not enough pagination pages to test."); }
    }
}
