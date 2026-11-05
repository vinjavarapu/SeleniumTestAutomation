package org.example.framework.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page object for the inventory screen displayed after a successful login. */
public class InventoryPage {
    private final WebDriverWait wait;
    private final By inventoryContainer = By.cssSelector("[data-test='inventory-container']");

    public InventoryPage(WebDriver driver) {
        long timeoutSeconds = Long.parseLong(System.getProperty("explicitWaitSeconds", "10"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public boolean isLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer)).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException exception) {
            return false;
        }
    }
}
