package org.example.framework.tests;

import org.example.framework.base.BaseTest;
import org.example.framework.pages.CartPage;
import org.example.framework.pages.CheckoutCompletePage;
import org.example.framework.pages.CheckoutInformationPage;
import org.example.framework.pages.CheckoutOverviewPage;
import org.example.framework.pages.InventoryPage;
import org.example.framework.pages.LoginPage;
import org.example.framework.utils.TestDataReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckoutTests extends BaseTest {
    @Test(description = "A customer can purchase a product from login through order confirmation")
    public void customerCanCompleteCheckout() {
        String productName = TestDataReader.get("product.name");
        logger.info("Starting end-to-end checkout for product: {}", productName);

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        loginPage.login(
                TestDataReader.get("valid.username"),
                TestDataReader.get("valid.password"));

        InventoryPage inventoryPage = new InventoryPage(getDriver());
        Assert.assertTrue(inventoryPage.isLoaded(), "Expected the inventory page after login");
        inventoryPage.addProductToCart(productName);
        Assert.assertEquals(inventoryPage.getCartItemCount(), 1, "Expected one item in the cart badge");

        inventoryPage.openCart();
        CartPage cartPage = new CartPage(getDriver());
        Assert.assertTrue(cartPage.containsProduct(productName), "Expected selected product in the cart");
        cartPage.clickCheckout();

        CheckoutInformationPage informationPage = new CheckoutInformationPage(getDriver());
        informationPage.enterCustomerInformation(
                TestDataReader.get("checkout.firstName"),
                TestDataReader.get("checkout.lastName"),
                TestDataReader.get("checkout.postalCode"));
        informationPage.continueToOverview();

        CheckoutOverviewPage overviewPage = new CheckoutOverviewPage(getDriver());
        Assert.assertTrue(overviewPage.containsProduct(productName),
                "Expected the selected product on the order overview");
        overviewPage.placeOrder();

        CheckoutCompletePage completePage = new CheckoutCompletePage(getDriver());
        Assert.assertEquals(completePage.getConfirmationMessage(), "Thank you for your order!");
        logger.info("End-to-end checkout completed successfully");
    }
}
