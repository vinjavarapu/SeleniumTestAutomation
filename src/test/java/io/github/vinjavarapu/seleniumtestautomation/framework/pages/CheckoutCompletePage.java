package io.github.vinjavarapu.seleniumtestautomation.framework.pages;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BasePage;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for the order confirmation screen. */
public class CheckoutCompletePage extends BasePage {
    private final By confirmationMessage = By.cssSelector("[data-test='complete-header']");
    private final By backHomeButton = By.id("back-to-products");

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
        visibleElement(confirmationMessage);
        TestStepReporter.record("Order confirmation page is displayed");
    }

    public String getConfirmationMessage() {
        return readText(confirmationMessage);
    }

    public void backHome() {
        click(backHomeButton, "Returning home after order completion");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }
}
