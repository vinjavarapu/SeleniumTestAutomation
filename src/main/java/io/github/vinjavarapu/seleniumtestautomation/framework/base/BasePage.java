package io.github.vinjavarapu.seleniumtestautomation.framework.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

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
        String currentValue = field.getDomProperty("value");
        if (!value.equals(currentValue)) {
            throw new IllegalStateException("Text entry did not persist for field " + locator);
        }
    }

    protected String readText(By locator) {
        return visibleElement(locator).getText();
    }

    protected int readBadgeCount(By locator) {
        return wait.until(currentDriver -> {
            List<WebElement> badges = currentDriver.findElements(locator);
            if (badges.isEmpty()) {
                return 0;
            }
            try {
                WebElement badge = badges.get(0);
                if (!badge.isDisplayed()) {
                    return null;
                }
                String text = badge.getText();
                return text == null || text.isBlank() ? null : Integer.parseInt(text.trim());
            } catch (StaleElementReferenceException exception) {
                return null;
            }
        });
    }

    protected boolean isVisible(By locator) {
        try {
            return visibleElement(locator).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException exception) {
            return false;
        }
    }

    protected boolean isImageLoaded(WebElement image) {
        if (!Boolean.parseBoolean(image.getDomProperty("complete"))) {
            return false;
        }
        String naturalWidth = image.getDomProperty("naturalWidth");
        if (naturalWidth == null || naturalWidth.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(naturalWidth.trim()) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    protected String activeElementId() {
        Object id = ((JavascriptExecutor) driver)
                .executeScript("return document.activeElement ? document.activeElement.id : null");
        return id == null ? "" : id.toString();
    }

    protected boolean isActiveElementFocusVisible() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver)
                .executeScript("return document.activeElement !== null && document.activeElement.matches(':focus-visible')"));
    }

    protected boolean hasActiveElementRenderedFocusIndicator() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver)
                .executeScript("if (!document.activeElement) return false; "
                        + "const style = window.getComputedStyle(document.activeElement); "
                        + "return (style.outlineStyle !== 'none' && parseFloat(style.outlineWidth) > 0) "
                        + "|| style.boxShadow !== 'none' "
                        + "|| document.activeElement.classList.toString().includes('focus') "
                        + "|| document.activeElement.classList.toString().includes('active');"));
    }

    protected void waitForUrlContains(String expectedUrl) {
        wait.urlContains(expectedUrl);
    }

    protected void waitForInvisibility(By locator) {
        wait.invisible(locator);
    }

    protected void addCookie(Cookie cookie) {
        driver.manage().addCookie(cookie);
    }

    protected void deleteAllCookies() {
        driver.manage().deleteAllCookies();
    }

    protected void setLocalStorageItem(String key, String value) {
        ((JavascriptExecutor) driver).executeScript("window.localStorage.setItem(arguments[0], arguments[1]);",
                key, value);
    }

    protected String getLocalStorageItem(String key) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return window.localStorage.getItem(arguments[0]);", key);
        return result == null ? null : result.toString();
    }

    protected void clearWebStorage() {
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
    }
}
