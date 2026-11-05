package org.example.framework.pages;

import java.util.List;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Page object for the shopping cart. */
public class CartPage extends BasePage {
    private final By cartList = By.cssSelector(".cart_list");
    private final By cartItemNames = By.cssSelector(".cart_item .inventory_item_name");
    private final By checkoutButton = By.id("checkout");

    public CartPage(WebDriver driver) {
        super(driver);
        visibleElement(cartList);
    }

    public boolean containsProduct(String productName) {
        List<WebElement> names = driver.findElements(cartItemNames);
        return names.stream().anyMatch(element -> element.getText().equals(productName));
    }

    public void clickCheckout() {
        clickCheckoutControl(checkoutButton, "Starting checkout");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("checkout-step-one.html"));
    }
}
