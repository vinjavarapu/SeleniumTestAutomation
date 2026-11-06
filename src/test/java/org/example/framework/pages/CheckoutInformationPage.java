package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for the checkout contact and address form. */
public class CheckoutInformationPage extends BasePage {
    private final By firstNameField = By.id("first-name");
    private final By lastNameField = By.id("last-name");
    private final By postalCodeField = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By cancelButton = By.id("cancel");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public CheckoutInformationPage(WebDriver driver) {
        super(driver);
        logger.info("Checkout information page state: url={}, title={}", driver.getCurrentUrl(), driver.getTitle());
        visibleElement(firstNameField);
    }

    public void enterCustomerInformation(String firstName, String lastName, String postalCode) {
        enterText(firstNameField, firstName, "Entering checkout first name");
        enterText(lastNameField, lastName, "Entering checkout last name");
        enterText(postalCodeField, postalCode, "Entering checkout postal code");
    }

    public void continueToOverview() {
        clickCheckoutControl(continueButton, "Continuing to the checkout overview");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("checkout-step-two.html"));
    }

    public void continueWithInformation(String firstName, String lastName, String postalCode) {
        if (firstName != null) enterText(firstNameField, firstName, "Entering checkout first name");
        if (lastName != null) enterText(lastNameField, lastName, "Entering checkout last name");
        if (postalCode != null) enterText(postalCodeField, postalCode, "Entering checkout postal code");
        clickCheckoutControl(continueButton, "Continuing checkout");
    }

    public String getErrorMessage() { return readText(errorMessage); }

    public void cancel() {
        clickCheckoutControl(cancelButton, "Canceling checkout");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
    }
}
