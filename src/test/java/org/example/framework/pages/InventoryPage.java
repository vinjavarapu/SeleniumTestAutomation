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
        try {
            wait.until(d -> d.findElements(productNames).size() > 0);
            return driver.findElements(inventoryContainer).stream().anyMatch(WebElement::isDisplayed);
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public List<String> getProductNames() {
        wait.until(d -> d.findElements(productNames).size() > 0);
        return driver.findElements(productNames).stream().map(WebElement::getText).toList();
    }

    public boolean containsProduct(String productName) {
        return getProductNames().contains(productName);
    }

    public List<String> getProductPrices() {
        wait.until(d -> d.findElements(productNames).size() > 0);
        return driver.findElements(By.cssSelector(".inventory_item_price")).stream()
                .map(WebElement::getText).toList();
    }

    public List<String> getProductImageSources() {
        wait.until(d -> d.findElements(productNames).size() > 0);
        return driver.findElements(By.cssSelector(".inventory_item_img img")).stream()
                .map(image -> image.getAttribute("src")).toList();
    }

    public List<Boolean> getProductImagesLoaded() {
        wait.until(d -> d.findElements(productNames).size() > 0);
        return driver.findElements(By.cssSelector(".inventory_item_img img")).stream()
                .map(image -> Boolean.parseBoolean(image.getDomProperty("complete"))
                        && !"0".equals(image.getDomProperty("naturalWidth"))).toList();
    }

    public boolean productCardsFitViewport() {
        return Boolean.TRUE.equals(((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "const width = document.documentElement.clientWidth; "
                        + "const cards = [...document.querySelectorAll('.inventory_item')]; "
                        + "return cards.length > 0 && document.documentElement.scrollWidth <= width "
                        + "&& cards.every(card => { const r = card.getBoundingClientRect(); "
                + "return r.left >= 0 && r.right <= width; }) "
                + "&& cards.every((card, i) => cards.slice(i + 1).every(other => { "
                + "const a = card.getBoundingClientRect(), b = other.getBoundingClientRect(); "
                + "return a.right <= b.left || b.right <= a.left || a.bottom <= b.top || b.bottom <= a.top; }));"));
    }

    public boolean productActionButtonsAreVisible() {
        List<WebElement> buttons = driver.findElements(By.cssSelector(
                ".inventory_item button[data-test^='add-to-cart-'], "
                        + ".inventory_item button[data-test^='remove-']"));
        return buttons.size() == getProductNames().size() && buttons.stream().allMatch(WebElement::isDisplayed);
    }

    public void sortBy(String optionValue) {
        new org.openqa.selenium.support.ui.Select(visibleElement(sortSelect)).selectByValue(optionValue);
    }

    public void removeProductFromInventory(String productName) {
        String id = productId(productName);
        int previousCount = getCartItemCount();
        click(By.cssSelector("[data-test='remove-" + id + "']"), "Removing product from inventory: " + productName);
        wait.until(driver -> getCartItemCount() == previousCount - 1);
    }

    public void openProductDetails(String productName) {
        wait.until(d -> d.findElements(productNames).stream()
                .anyMatch(link -> link.getText().equals(productName)));
        List<WebElement> links = driver.findElements(productNames);
        links.stream().filter(link -> link.getText().equals(productName)).findFirst()
                .orElseThrow(() -> new AssertionError("Product not found in inventory: " + productName)).click();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory-item.html"));
    }

    public void openMenu() {
        click(menuButton, "Opening the side menu");
    }
    public boolean isMenuOpen() {
        return "false".equals(driver.findElement(By.cssSelector(".bm-menu-wrap")).getAttribute("aria-hidden"));
    }
    public void closeMenu() {
        click(closeMenuButton, "Closing the side menu");
    }
    public void logout() {
        click(logoutLink, "Logging out");
        wait.until(driver -> !driver.getCurrentUrl().contains("inventory.html"));
    }
    public void resetAppState() {
        click(resetLink, "Resetting application state");
        wait.until(driver -> getCartItemCount() == 0);
    }
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
        int previousCount = getCartItemCount();
        click(addButton, "Adding product to cart: " + productName);
        wait.until(driver -> getCartItemCount() == previousCount + 1);
    }

    public int getCartItemCount() {
        if (driver.findElements(cartBadge).isEmpty()) {
            return 0;
        }
        return Integer.parseInt(readText(cartBadge));
    }

    public void openCart() {
        click(cartLink, "Opening the shopping cart");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
    }
}
