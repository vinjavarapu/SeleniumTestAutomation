package io.github.vinjavarapu.seleniumtestautomation.framework.pages;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BasePage;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

/** Page object for SauceDemo's login screen. */
public class LoginPage extends BasePage {
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        logger.info("Opening login page");
        TestStepReporter.record("Opening the SauceDemo login page");
        driver.get(FrameworkConfig.baseUrl());
        visibleElement(usernameField);
    }

    public void enterUsername(String username) {
        enterText(usernameField, username, "Entering username");
    }

    public void enterPassword(String password) {
        // Log the action only. Never write the password value into test logs.
        enterText(passwordField, password, "Entering password");
    }

    public void clickLogin() {
        click(loginButton, "Clicking the login button");
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public String getErrorMessage() {
        TestStepReporter.record("Reading the login validation message");
        return readText(errorMessage);
    }

    public boolean isLoginFormVisible() {
        return isVisible(usernameField) && isVisible(passwordField) && isVisible(loginButton);
    }

    public String getPasswordInputType() {
        return visibleElement(passwordField).getAttribute("type");
    }

    public String tabFromUsername() {
        TestStepReporter.record("Moving focus from username to the next control with Tab");
        visibleElement(usernameField).sendKeys(Keys.TAB);
        return activeElementId();
    }

    public String tabFromUsernameWithValue(String username) {
        TestStepReporter.record("Entering username and tabbing to the password field");
        visibleElement(usernameField).sendKeys(username, Keys.TAB);
        return activeElementId();
    }

    public void enterPasswordAndSubmitWithEnter(String password) {
        TestStepReporter.record("Entering password and submitting the login form with Enter");
        visibleElement(passwordField).sendKeys(password, Keys.ENTER);
    }

    public String tabFromPassword() {
        TestStepReporter.record("Moving focus from password to the login button with Tab");
        visibleElement(passwordField).sendKeys(Keys.TAB);
        return activeElementId();
    }

    public boolean isFocusedElementFocusVisible() {
        return isActiveElementFocusVisible();
    }

    public boolean hasFocusedElementRenderedIndicator() {
        return hasActiveElementRenderedFocusIndicator();
    }
}
