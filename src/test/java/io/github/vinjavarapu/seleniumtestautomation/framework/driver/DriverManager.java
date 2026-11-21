package io.github.vinjavarapu.seleniumtestautomation.framework.driver;

import org.openqa.selenium.WebDriver;
import java.util.Objects;

/** Keeps each test thread's browser separate for parallel test execution. */
public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No browser is running on this test thread. Call setDriver first.");
        }
        return driver;
    }

    public static void setDriver(WebDriver driver) {
        Objects.requireNonNull(driver, "driver must not be null");
        if (DRIVER.get() != null) {
            throw new IllegalStateException("A WebDriver is already attached to this test thread");
        }
        DRIVER.set(driver);
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
