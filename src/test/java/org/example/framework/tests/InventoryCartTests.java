package org.example.framework.tests;

import org.example.framework.base.BaseTest;
import org.example.framework.pages.CartPage;
import org.example.framework.pages.InventoryPage;
import org.example.framework.pages.LoginPage;
import org.example.framework.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InventoryCartTests extends BaseTest {
    private InventoryPage loginToInventory() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(TestDataReader.get("valid.username"), TestDataReader.get("valid.password"));
        InventoryPage inventoryPage = new InventoryPage(getDriver());
        Assert.assertTrue(inventoryPage.isLoaded(), "Expected the inventory page after login");
        return inventoryPage;
    }

    @Test(description = "Inventory displays products including the configured product")
    public void inventoryProductsAreDisplayed() {
        InventoryPage inventoryPage = loginToInventory();
        Assert.assertEquals(inventoryPage.getProductNames().size(), 6, "Expected six products in inventory");
        Assert.assertTrue(inventoryPage.containsProduct(TestDataReader.get("product.name")),
                "Expected the configured product in inventory");
    }

    @Test(description = "A product can be added to and removed from the cart")
    public void productCanBeAddedAndRemoved() {
        String productName = TestDataReader.get("product.name");
        InventoryPage inventoryPage = loginToInventory();
        inventoryPage.addProductToCart(productName);
        Assert.assertEquals(inventoryPage.getCartItemCount(), 1, "Expected one item after adding a product");

        inventoryPage.openCart();
        CartPage cartPage = new CartPage(getDriver());
        Assert.assertTrue(cartPage.containsProduct(productName), "Expected selected product in cart");
        cartPage.removeProduct(productName);
        Assert.assertEquals(cartPage.getItemCount(), 0, "Expected an empty cart after removing the product");
    }

    @Test(description = "Cart shows the selected product name, description, price, and quantity")
    public void cartShowsProductDetails() {
        String productName = TestDataReader.get("product.name");
        InventoryPage inventoryPage = loginToInventory();
        inventoryPage.addProductToCart(productName);
        inventoryPage.openCart();
        CartPage cartPage = new CartPage(getDriver());

        Assert.assertTrue(cartPage.containsProduct(productName), "Expected selected product in cart");
        Assert.assertFalse(cartPage.getProductDescription(productName).isBlank(), "Expected a product description");
        Assert.assertEquals(cartPage.getProductPrice(productName), TestDataReader.get("product.price"));
        Assert.assertEquals(cartPage.getProductQuantity(productName), "1");
    }
}
