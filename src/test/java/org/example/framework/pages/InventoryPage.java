package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for the inventory screen displayed after a successful login. */
public class InventoryPage extends BasePage {
    private final By inventoryContainer = By.cssSelector("[data-test='inventory-container']");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By cartLink = By.cssSelector("[data-test='shopping-cart-link']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isVisible(inventoryContainer);
    }

    public void addProductToCart(String productName) {
        String productId = productName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        By addButton = By.cssSelector("[data-test='add-to-cart-" + productId + "']");
        click(addButton, "Adding product to cart: " + productName);
    }

    public int getCartItemCount() {
        if (!isVisible(cartBadge)) {
            return 0;
        }
        return Integer.parseInt(readText(cartBadge));
    }

    public void openCart() {
        click(cartLink, "Opening the shopping cart");
    }
}
