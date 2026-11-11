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
    private final By cancelButton = By.id("cancel");
    private final By itemPrices = By.cssSelector(".cart_item .inventory_item_price");
    private final By subtotal = By.cssSelector(".summary_subtotal_label");
    private final By tax = By.cssSelector(".summary_tax_label");
    private final By total = By.cssSelector(".summary_total_label");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
        visibleElement(summaryContainer);
    }

    public boolean containsProduct(String productName) {
        List<WebElement> names = driver.findElements(itemNames);
        return names.stream().anyMatch(element -> element.getText().equals(productName));
    }

    public List<String> getItemNames() {
        return driver.findElements(itemNames).stream().map(WebElement::getText).toList();
    }

    public void placeOrder() {
        click(finishButton, "Placing the order");
    }

    public List<String> getItemPrices() {
        return driver.findElements(itemPrices).stream().map(WebElement::getText).toList();
    }
    public List<String> getItemQuantities() {
        return driver.findElements(By.cssSelector(".cart_item .cart_quantity")).stream()
                .map(WebElement::getText).toList();
    }
    public String getSubtotal() { return readText(subtotal); }
    public String getTax() { return readText(tax); }
    public String getTotal() { return readText(total); }
    public void cancel() {
        click(cancelButton, "Canceling from order overview");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }
}
