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

    public CheckoutInformationPage(WebDriver driver) {
        super(driver);
        visibleElement(firstNameField);
    }

    public void enterCustomerInformation(String firstName, String lastName, String postalCode) {
        enterText(firstNameField, firstName, "Entering checkout first name");
        enterText(lastNameField, lastName, "Entering checkout last name");
        enterText(postalCodeField, postalCode, "Entering checkout postal code");
    }

    public void continueToOverview() {
        click(continueButton, "Continuing to the checkout overview");
    }
}
