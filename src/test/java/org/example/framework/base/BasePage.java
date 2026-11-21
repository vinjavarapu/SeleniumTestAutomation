package org.example.framework.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.framework.reporting.TestStepReporter;
import org.example.framework.utils.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Shared Selenium actions and explicit waits for page objects. */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WaitHelper wait;
    protected final Logger logger;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.logger = LogManager.getLogger(getClass());
        this.wait = new WaitHelper(driver);
    }

    protected WebElement visibleElement(By locator) {
        return wait.visible(locator);
    }

    protected void click(By locator, String actionDescription) {
        logger.info("{}", actionDescription);
        TestStepReporter.record(actionDescription);
        WebElement element = wait.clickable(locator);
        element.click();
    }

    protected void enterText(By locator, String value, String actionDescription) {
        logger.info("{}", actionDescription);
        TestStepReporter.record(actionDescription);
        WebElement field = visibleElement(locator);
        String existingValue = field.getDomProperty("value");
        if (existingValue != null && !existingValue.isEmpty()) {
            field.clear();
        }
        field.sendKeys(value);
        if (!value.equals(field.getDomProperty("value"))) {
            logger.warn("Keyboard entry did not persist for {}; applying the input event fallback", locator);
            ((JavascriptExecutor) driver).executeScript(
                    "const input = arguments[0]; const value = arguments[1];"
                            + "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, value);"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('change', { bubbles: true }));",
                    field, value);
        }
        String currentValue = field.getDomProperty("value");
        if (!value.equals(currentValue)) {
            throw new IllegalStateException("Text entry did not persist for field " + locator);
        }
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

    protected void waitForUrlContains(String expectedUrl) {
        wait.urlContains(expectedUrl);
    }

    protected void waitForInvisibility(By locator) {
        wait.invisible(locator);
    }
}
