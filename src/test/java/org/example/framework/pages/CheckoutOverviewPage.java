package org.example.framework.pages;

import java.util.List;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Page object for reviewing an order before placing it. */
public class CheckoutOverviewPage extends BasePage {
    private final By summaryContainer = By.cssSelector(".checkout_summary_container");
    private final By itemNames = By.cssSelector(".cart_item .inventory_item_name");
    private final By finishButton = By.id("finish");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
        visibleElement(summaryContainer);
    }

    public boolean containsProduct(String productName) {
        List<WebElement> names = driver.findElements(itemNames);
        return names.stream().anyMatch(element -> element.getText().equals(productName));
    }

    public void placeOrder() {
        clickCheckoutControl(finishButton, "Placing the order");
    }
}
