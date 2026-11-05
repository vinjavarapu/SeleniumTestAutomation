package org.example.framework.base;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Shared Selenium actions and explicit waits for page objects. */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger logger;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.logger = LogManager.getLogger(getClass());
        long timeoutSeconds = Long.parseLong(System.getProperty("explicitWaitSeconds", "10"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    protected WebElement visibleElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator, String actionDescription) {
        logger.info("{}", actionDescription);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void enterText(By locator, String value, String actionDescription) {
        logger.info("{}", actionDescription);
        WebElement field = visibleElement(locator);
        field.clear();
        field.sendKeys(value);
    }

    protected String readText(By locator) {
        return visibleElement(locator).getText();
    }

    protected boolean isVisible(By locator) {
        try {
            return visibleElement(locator).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException exception) {
            return false;
        }
    }
}
