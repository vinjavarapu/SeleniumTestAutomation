package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.example.framework.config.FrameworkConfig;
import org.openqa.selenium.By;
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
        driver.get(FrameworkConfig.baseUrl());
        try {
            visibleElement(usernameField);
        } catch (org.openqa.selenium.TimeoutException firstLoadFailure) {
            logger.warn("Login form did not load on the first request; refreshing once");
            driver.navigate().refresh();
            visibleElement(usernameField);
        }
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
        return readText(errorMessage);
    }

    public boolean isLoginFormVisible() {
        return isVisible(usernameField) && isVisible(passwordField) && isVisible(loginButton);
    }

    public String getPasswordInputType() {
        return visibleElement(passwordField).getAttribute("type");
    }
}
