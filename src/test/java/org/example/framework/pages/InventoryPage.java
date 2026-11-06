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
    private final By sortSelect = By.cssSelector("[data-test='product-sort-container']");
    private final By menuButton = By.id("react-burger-menu-btn");
    private final By closeMenuButton = By.id("react-burger-cross-btn");
    private final By logoutLink = By.id("logout_sidebar_link");
    private final By resetLink = By.id("reset_sidebar_link");
    private final By allItemsLink = By.id("inventory_sidebar_link");
    private final By aboutLink = By.id("about_sidebar_link");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return driver.findElements(inventoryContainer).stream().anyMatch(WebElement::isDisplayed);
    }

    public List<String> getProductNames() {
        return driver.findElements(productNames).stream().map(WebElement::getText).toList();
    }

    public boolean containsProduct(String productName) {
        return getProductNames().contains(productName);
    }

    public List<String> getProductPrices() {
        return driver.findElements(By.cssSelector(".inventory_item_price")).stream()
                .map(WebElement::getText).toList();
    }

    public List<String> getProductImageSources() {
        return driver.findElements(By.cssSelector(".inventory_item_img img")).stream()
                .map(image -> image.getAttribute("src")).toList();
    }

    public List<Boolean> getProductImagesLoaded() {
        return driver.findElements(By.cssSelector(".inventory_item_img img")).stream()
                .map(image -> Boolean.parseBoolean(image.getDomProperty("complete"))
                        && !"0".equals(image.getDomProperty("naturalWidth"))).toList();
    }

    public void sortBy(String optionValue) {
        new org.openqa.selenium.support.ui.Select(visibleElement(sortSelect)).selectByValue(optionValue);
    }

    public void removeProductFromInventory(String productName) {
        String id = productId(productName);
        click(By.cssSelector("[data-test='remove-" + id + "']"), "Removing product from inventory: " + productName);
    }

    public void openProductDetails(String productName) {
        List<WebElement> links = driver.findElements(productNames);
        links.stream().filter(link -> link.getText().equals(productName)).findFirst()
                .orElseThrow(() -> new AssertionError("Product not found in inventory: " + productName)).click();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory-item.html"));
    }

    public void openMenu() { click(menuButton, "Opening the side menu"); }
    public void closeMenu() { click(closeMenuButton, "Closing the side menu"); }
    public boolean isMenuVisible() {
        return driver.findElements(closeMenuButton).stream().anyMatch(WebElement::isDisplayed);
    }
    public void logout() { click(logoutLink, "Logging out"); }
    public void resetAppState() { click(resetLink, "Resetting application state"); }
    public void clickAllItems() {
        click(allItemsLink, "Selecting All Items");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }
    public void clickAbout() { click(aboutLink, "Opening Sauce Labs About page"); }

    public boolean hasRemoveButton(String productName) {
        return driver.findElements(By.cssSelector("[data-test='remove-" + productId(productName) + "']")).size() > 0;
    }

    private String productId(String productName) {
        return productName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    public void addProductToCart(String productName) {
        String productId = productId(productName);
        By addButton = By.cssSelector("[data-test='add-to-cart-" + productId + "']");
        click(addButton, "Adding product to cart: " + productName);
    }

    public int getCartItemCount() {
        if (driver.findElements(cartBadge).isEmpty()) {
            return 0;
        }
        return Integer.parseInt(readText(cartBadge));
    }

    public void openCart() {
        clickCheckoutControl(cartLink, "Opening the shopping cart");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
    }
}
