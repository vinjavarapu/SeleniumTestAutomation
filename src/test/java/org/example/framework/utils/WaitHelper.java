package org.example.framework.utils;

import java.time.Duration;
import java.util.function.Function;

import org.example.framework.config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Explicit synchronization helpers. No implicit wait or fixed sleep is used. */
public final class WaitHelper {
    private final WebDriverWait wait;

    public WaitHelper(WebDriver driver) {
        this(driver, Duration.ofSeconds(FrameworkConfig.explicitWaitSeconds()));
    }

    WaitHelper(WebDriver driver, Duration timeout) {
        this.wait = new WebDriverWait(driver, timeout);
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement present(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public boolean invisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean urlContains(String value) {
        return wait.until(ExpectedConditions.urlContains(value));
    }

    /** Allows page objects to express domain-specific synchronization conditions. */
    public <T> T until(Function<WebDriver, T> condition) {
        return wait.until(condition);
    }
}
