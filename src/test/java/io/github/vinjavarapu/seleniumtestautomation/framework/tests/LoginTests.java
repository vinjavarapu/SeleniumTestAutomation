package io.github.vinjavarapu.seleniumtestautomation.framework.tests;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BaseTest;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.InventoryPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.LoginPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {
    @Test(groups = "smoke", description = "A valid SauceDemo user can sign in")
    public void validUserCanLogIn() {
        logger.info("Starting valid-login test");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(
                TestDataReader.get("valid.username"),
                TestDataReader.get("valid.password"));

        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded(),
                "Expected the inventory page after a successful login");
        logger.info("Valid-login test passed");
    }

    @Test(groups = "smoke", description = "Invalid SauceDemo credentials are rejected")
    public void invalidUserSeesLoginError() {
        logger.info("Starting invalid-login test");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(
                TestDataReader.get("invalid.username"),
                TestDataReader.get("invalid.password"));

        Assert.assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"),
                "Expected SauceDemo to show a login error for invalid credentials");
        logger.info("Invalid-login test passed");
    }
}
