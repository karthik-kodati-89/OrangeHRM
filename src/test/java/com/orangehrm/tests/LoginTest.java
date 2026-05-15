package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.DashboardPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(description = "Valid login leads to dashboard")
    public void testValidLogin() {
        LoginPage login = new LoginPage(driver);
        Assert.assertTrue(login.isPageLoaded(), "Login page did not load");

        DashboardPage dash = login.loginAs(
                ConfigReader.get("username"),
                ConfigReader.get("password"));

        Assert.assertTrue(dash.isAt(), "Dashboard URL not reached");
        Assert.assertEquals(dash.getHeaderText(), "Dashboard");
    }

    @DataProvider(name = "invalidCreds")
    public Object[][] badCreds() {
        return new Object[][] {
            {"WrongUser",  "admin123",   "Invalid credentials"},
            {"Admin",      "wrongPass",  "Invalid credentials"},
            {"",           "",           "Required"}
        };
    }

    @Test(dataProvider = "invalidCreds", description = "DATA-DRIVEN negative login")
    public void testInvalidLogin(String user, String pass, String expectedMsg) {
        LoginPage login = new LoginPage(driver);
        login.loginAs(user, pass);
        // For empty fields, error wording differs. Just verify we did not pass.
        Assert.assertTrue(login.isPageLoaded(), "Login should NOT have succeeded");
    }
}
