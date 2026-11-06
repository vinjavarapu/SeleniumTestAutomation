package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

/** Page object for the inventory screen displayed after a successful login. */
public class InventoryPage extends BasePage {
    private final By inventoryContainer = By.cssSelector("[data-test='inventory-container']");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By cartLink = By.cssSelector("[data-test='shopping-cart-link']");
    private final By productNames = By.cssSelector(".inventory_item_name");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isVisible(inventoryContainer);
    }

    public List<String> getProductNames() {
        return driver.findElements(productNames).stream().map(WebElement::getText).toList();
    }

    public boolean containsProduct(String productName) {
        return getProductNames().contains(productName);
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
        clickCheckoutControl(cartLink, "Opening the shopping cart");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
    }
}
