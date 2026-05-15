package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.keywords.KeywordEngine;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.pages.PIMPage;
import com.orangehrm.utils.ConfigReader;
import com.orangehrm.utils.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * HYBRID FRAMEWORK demo.
 *
 *   POM             -> page classes encapsulate locators + actions
 *   DATA-DRIVEN     -> @DataProvider feeds parameters
 *   KEYWORD-DRIVEN  -> Excel-defined keyword flow runs alongside
 *
 * Combine the strengths: business-readable keyword flows for repetitive
 * actions, POM for complex assertions, data-driven for coverage.
 */
public class HybridTest extends BaseTest {

    @DataProvider(name = "employees")
    public Object[][] employees() {
        return ExcelReader.getSheetData(
                "src/test/resources/testdata/LoginData.xlsx", "Employees");
    }

    @Test(dataProvider = "employees",
          description = "POM + Data-Driven combined hybrid scenario")
    public void testEmployeeSearchHybrid(String empId, String expected) {
        // POM phase
        DashboardPage dash = new LoginPage(driver)
                .loginAs(ConfigReader.get("username"), ConfigReader.get("password"));
        PIMPage pim = dash.navigateToPIM();

        boolean found = pim.findEmployeeAcrossAllPages(empId);
        Assert.assertEquals(String.valueOf(found), expected,
                "Mismatch for empId: " + empId);
    }

    @Test(description = "Hybrid: keyword-driven login + POM-driven assertions")
    public void testKeywordLoginThenPomAssertion() {
        KeywordEngine engine = new KeywordEngine(driver);
        engine.execute("type",  "name=username", ConfigReader.get("username"));
        engine.execute("type",  "name=password", ConfigReader.get("password"));
        engine.execute("click", "css=button[type='submit']", "");

        // Hand off to POM for richer post-conditions
        DashboardPage dash = new DashboardPage(driver);
        Assert.assertTrue(dash.isPageLoaded(), "Dashboard not visible");
    }
}
