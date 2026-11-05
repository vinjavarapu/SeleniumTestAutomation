package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for SauceDemo's login screen. */
public class LoginPage extends BasePage {
    private static final String URL = "https://www.saucedemo.com/";

    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        logger.info("Opening SauceDemo login page");
        driver.get(URL);
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
        return readText(errorMessage);
    }
}
