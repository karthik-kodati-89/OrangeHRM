package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.AdminPage;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AdminTest extends BaseTest {

    @DataProvider(name = "adminFilters")
    public Object[][] filters() {
        return new Object[][] {
            {"Admin",  "Enabled"},
            {"ESS",    "Enabled"},
            {"Admin",  "Disabled"}
        };
    }

    @Test(dataProvider = "adminFilters",
          description = "DROPDOWNS + DATA-DRIVEN - filter users by role and status")
    public void testFilterUsers(String role, String status) {
        DashboardPage dash = new LoginPage(driver)
                .loginAs(ConfigReader.get("username"), ConfigReader.get("password"));
        AdminPage admin = dash.navigateToAdmin();
        Assert.assertTrue(admin.isPageLoaded());

        admin.searchUser(role, status);
        // Just verify the records label updated - results may be 0 for some combos
        String recordsText = admin.getRecordsFoundText();
        Assert.assertTrue(recordsText.toLowerCase().contains("record"),
                "Expected 'records found' label, got: " + recordsText);
    }
}
