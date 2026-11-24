package io.github.vinjavarapu.seleniumtestautomation.framework.pages;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BasePage;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Page object for SauceDemo's single-product detail view. */
public class ProductDetailsPage extends BasePage {
    private final By name = By.cssSelector(".inventory_details_name");
    private final By description = By.cssSelector(".inventory_details_desc");
    private final By price = By.cssSelector(".inventory_details_price");
    private final By image = By.cssSelector(".inventory_details_img");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
        visibleElement(name);
        TestStepReporter.record("Product details page is displayed");
    }

    public String getName() { return readText(name); }
    public String getDescription() { return readText(description); }
    public String getPrice() { return readText(price); }
    public boolean isImageVisible() { return isVisible(image); }
    public boolean isImageLoaded() {
        WebElement productImage = visibleElement(image);
        return Boolean.parseBoolean(productImage.getDomProperty("complete"))
                && Integer.parseInt(productImage.getDomProperty("naturalWidth")) > 0;
    }
    public void backToProducts() {
        TestStepReporter.record("Navigating back to products");
        driver.navigate().back();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("inventory.html"));
        driver.navigate().refresh();
        wait.until(driver -> !driver.findElements(By.cssSelector(".inventory_item_name")).isEmpty());
    }
}
