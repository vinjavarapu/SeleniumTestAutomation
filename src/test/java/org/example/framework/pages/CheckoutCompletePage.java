package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for the order confirmation screen. */
public class CheckoutCompletePage extends BasePage {
    private final By confirmationMessage = By.cssSelector("[data-test='complete-header']");

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
        visibleElement(confirmationMessage);
    }

    public String getConfirmationMessage() {
        return readText(confirmationMessage);
    }
}
