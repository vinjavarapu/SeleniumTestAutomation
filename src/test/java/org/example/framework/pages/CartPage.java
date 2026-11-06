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
    private final By cartItems = By.cssSelector(".cart_item");
    private final By checkoutButton = By.id("checkout");
    private final By continueShoppingButton = By.id("continue-shopping");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    public CartPage(WebDriver driver) {
        super(driver);
        visibleElement(cartList);
    }

    public boolean containsProduct(String productName) {
        List<WebElement> names = driver.findElements(cartItemNames);
        return names.stream().anyMatch(element -> element.getText().equals(productName));
    }

    public int getItemCount() {
        return driver.findElements(cartItems).size();
    }

    public int getBadgeCount() {
        return driver.findElements(cartBadge).isEmpty() ? 0 : Integer.parseInt(readText(cartBadge));
    }

    public boolean isCheckoutEnabled() {
        return driver.findElement(checkoutButton).isEnabled();
    }

    public void continueShopping() {
        click(continueShoppingButton, "Continuing shopping");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }

    public String getProductDescription(String productName) {
        return itemFor(productName).findElement(By.cssSelector(".inventory_item_desc")).getText();
    }

    public String getProductPrice(String productName) {
        return itemFor(productName).findElement(By.cssSelector(".inventory_item_price")).getText();
    }

    public String getProductQuantity(String productName) {
        return itemFor(productName).findElement(By.cssSelector(".cart_quantity")).getText();
    }

    public void removeProduct(String productName) {
        String productId = productName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        int previousCount = getItemCount();
        By removeButton = By.cssSelector("[data-test='remove-" + productId + "']");
        clickCheckoutControl(removeButton, "Removing product from cart: " + productName);
        wait.until(driver -> getItemCount() == previousCount - 1);
    }

    private WebElement itemFor(String productName) {
        return driver.findElements(cartItems).stream()
                .filter(item -> item.findElement(By.cssSelector(".inventory_item_name")).getText().equals(productName))
                .findFirst().orElseThrow(() -> new AssertionError("Product not found in cart: " + productName));
    }

    public void clickCheckout() {
        clickCheckoutControl(checkoutButton, "Starting checkout");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("checkout-step-one.html"));
    }
}
