package io.github.vinjavarapu.seleniumtestautomation.framework.pages;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BasePage;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
    private final By menuContainer = By.cssSelector(".bm-menu-wrap");
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
                .map(this::isImageLoaded)
                .toList();
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
        TestStepReporter.record("Sorting inventory using option: " + optionValue);
        new org.openqa.selenium.support.ui.Select(visibleElement(sortSelect)).selectByValue(optionValue);
    }

    public void removeProductFromInventory(String productName) {
        String id = productId(productName);
        int previousCount = getCartItemCount();
        click(By.cssSelector("[data-test='remove-" + id + "']"), "Removing product from inventory: " + productName);
        wait.until(driver -> getCartItemCount() == previousCount - 1);
    }

    public void openProductDetails(String productName) {
        By productLink = By.linkText(productName);
        WebElement link = wait.clickable(productLink);
        TestStepReporter.record("Opening product details for: " + productName);
        link.click();
        wait.until(currentDriver -> currentDriver.getCurrentUrl().contains("inventory-item.html")
                && currentDriver.findElements(By.cssSelector(".inventory_details_name")).stream()
                .anyMatch(name -> name.isDisplayed() && name.getText().equals(productName)));
    }

    public void openMenu() {
        if (!isMenuOpen()) {
            waitForElementUncovered(menuButton);
            click(menuButton, "Opening the side menu");
        }
        wait.until(driver -> "false".equals(driver.findElement(menuContainer).getAttribute("aria-hidden")));
        waitForMenuAnimation();
    }

    private void waitForMenuAnimation() {
        wait.until(driver -> {
            WebElement menu = driver.findElement(menuContainer);
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].getAnimations().every(animation => "
                            + "animation.playState !== 'running' && animation.playState !== 'pending');",
                    menu));
        });
    }

    private void waitForElementUncovered(By locator) {
        wait.until(driver -> {
            WebElement element = driver.findElement(locator);
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "const element = arguments[0]; const rect = element.getBoundingClientRect(); "
                            + "const top = document.elementFromPoint(rect.left + rect.width / 2, "
                            + "rect.top + rect.height / 2); return top === element || element.contains(top);",
                    element));
        });
    }

    public boolean isMenuOpen() {
        return driver.findElements(menuContainer).stream()
                .anyMatch(menu -> "false".equals(menu.getAttribute("aria-hidden")));
    }

    public void closeMenu() {
        waitForMenuAnimation();
        waitForElementUncovered(closeMenuButton);
        click(closeMenuButton, "Closing the side menu");
        wait.until(driver -> "true".equals(driver.findElement(menuContainer).getAttribute("aria-hidden")));
        waitForMenuAnimation();
        waitForElementUncovered(menuButton);
    }

    public void logout() {
        openMenu();
        click(logoutLink, "Logging out");
        wait.until(driver -> {
            String currentUrl = driver.getCurrentUrl();
            return currentUrl != null && !currentUrl.contains("inventory.html");
        });
    }
    public void resetAppState() {
        openMenu();
        WebElement reset = visibleElement(resetLink);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", reset);
        visibleElement(resetLink);
        click(resetLink, "Resetting application state");
        wait.until(driver -> getCartItemCount() == 0);
    }
    public void clickAllItems() {
        openMenu();
        click(allItemsLink, "Selecting All Items");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }
    public String getAboutLinkUrl() {
        openMenu();
        return visibleElement(aboutLink).getAttribute("href");
    }

    public boolean hasRemoveButton(String productName) {
        return driver.findElements(By.cssSelector("[data-test='remove-" + productId(productName) + "']")).size() > 0;
    }

    static String productSlug(String productName) {
        return productName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private String productId(String productName) {
        return productSlug(productName);
    }

    public boolean focusProductCartControlWithTab(String productName) {
        TestStepReporter.record("Using keyboard tab navigation to focus the product cart control");
        String addButtonTestId = "add-to-cart-" + productSlug(productName);
        for (int tab = 0; tab < 50; tab++) {
            driver.switchTo().activeElement().sendKeys(Keys.TAB);
            if (Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "return document.activeElement !== null && document.activeElement.getAttribute('data-test') === arguments[0]",
                    addButtonTestId))) {
                return true;
            }
        }
        return false;
    }

    public void activateFocusedControlWithSpace() {
        TestStepReporter.record("Activating the focused control with the Space key");
        driver.switchTo().activeElement().sendKeys(Keys.SPACE);
    }

    public void addProductToCart(String productName) {
        String productId = productId(productName);
        By addButton = By.cssSelector("[data-test='add-to-cart-" + productId + "']");
        int previousCount = getCartItemCount();
        click(addButton, "Adding product to cart: " + productName);
        wait.until(driver -> getCartItemCount() == previousCount + 1);
    }

    public int getCartItemCount() {
        return readBadgeCount(cartBadge);
    }

    public void openCart() {
        click(cartLink, "Opening the shopping cart");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
    }
}
