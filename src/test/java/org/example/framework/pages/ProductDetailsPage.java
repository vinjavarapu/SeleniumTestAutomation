package org.example.framework.pages;

import org.example.framework.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Page object for SauceDemo's single-product detail view. */
public class ProductDetailsPage extends BasePage {
    private final By name = By.cssSelector(".inventory_details_name");
    private final By description = By.cssSelector(".inventory_details_desc");
    private final By price = By.cssSelector(".inventory_details_price");
    private final By image = By.cssSelector(".inventory_details_img");
    private final By backButton = By.id("back-to-products");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
        visibleElement(name);
    }

    public String getName() { return readText(name); }
    public String getDescription() { return readText(description); }
    public String getPrice() { return readText(price); }
    public boolean isImageVisible() { return isVisible(image); }
    public void backToProducts() {
        click(backButton, "Returning to products");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
    }
}
