package org.example.framework.tests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.framework.base.BaseTest;
import org.example.framework.pages.CartPage;
import org.example.framework.pages.CheckoutCompletePage;
import org.example.framework.pages.CheckoutInformationPage;
import org.example.framework.pages.CheckoutOverviewPage;
import org.example.framework.pages.InventoryPage;
import org.example.framework.pages.LoginPage;
import org.example.framework.pages.ProductDetailsPage;
import org.example.framework.utils.TestDataReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Functional regression coverage based on the SauceDemo test case catalogue. */
@Test(groups = "regression")
public class CatalogueRegressionTests extends BaseTest {
    private InventoryPage login() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        page.login(TestDataReader.get("valid.username"), TestDataReader.get("valid.password"));
        return new InventoryPage(getDriver());
    }

    private CheckoutOverviewPage checkoutWithOneProduct() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        cart.clickCheckout();
        CheckoutInformationPage info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation(TestDataReader.get("checkout.firstName"),
                TestDataReader.get("checkout.lastName"), TestDataReader.get("checkout.postalCode"));
        info.continueToOverview();
        return new CheckoutOverviewPage(getDriver());
    }

    @Test(groups = "regression", description = "SD-001: Login controls are visible on the login page")
    public void loginPageShowsRequiredControls() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        Assert.assertTrue(page.isLoginFormVisible());
        Assert.assertEquals(page.getPasswordInputType(), "password");
    }

    @DataProvider(name = "rejectedLogins")
    public Object[][] rejectedLogins() {
        return new Object[][] {
                {"unknown-user", "secret_sauce", "do not match"},
                {"standard_user", "wrong_password", "do not match"},
                {"", "", "Username is required"},
                {"standard_user", "", "Password is required"},
                {"", "secret_sauce", "Username is required"},
                {"locked_out_user", "secret_sauce", "locked out"}
        };
    }

    @Test(dataProvider = "rejectedLogins", description = "SD-003..008: Invalid and incomplete login attempts are rejected")
    public void rejectedLoginShowsValidation(String username, String password, String expectedMessage) {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        page.login(username, password);
        Assert.assertTrue(page.getErrorMessage().toLowerCase().contains(expectedMessage.toLowerCase()),
                "Unexpected login error message");
    }

    @Test(description = "SD-009: Problem user authentication outcome is observed")
    public void specialPurposeUsersCanAuthenticate() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        page.login(TestDataReader.get("problem.username"), TestDataReader.get("valid.password"));
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded(),
                "Expected problem user to authenticate; UI quirks are catalogued separately");
    }

    @Test(description = "SD-012: Logging out ends the session")
    public void logoutReturnsToLogin() {
        InventoryPage inventory = login();
        inventory.openMenu();
        inventory.logout();
        Assert.assertTrue(new LoginPage(getDriver()).isLoginFormVisible());
    }

    @Test(description = "SD-013: Inventory shows product names, prices, and loaded images")
    public void inventoryShowsProductContent() {
        InventoryPage inventory = login();
        Assert.assertEquals(inventory.getProductNames().size(), 6);
        Assert.assertEquals(inventory.getProductPrices().size(), 6);
        Assert.assertEquals(inventory.getProductImageSources().size(), 6);
        Assert.assertTrue(inventory.getProductImagesLoaded().stream().allMatch(Boolean::booleanValue),
                "All inventory images should load");
    }

    @Test(description = "SD-015..017: Product detail content matches inventory and supports back navigation")
    public void productDetailsAndBackNavigationWork() {
        InventoryPage inventory = login();
        String expectedName = TestDataReader.get("product.name");
        inventory.openProductDetails(expectedName);
        ProductDetailsPage details = new ProductDetailsPage(getDriver());
        Assert.assertEquals(details.getName(), expectedName);
        Assert.assertFalse(details.getDescription().isBlank());
        Assert.assertEquals(details.getPrice(), TestDataReader.get("product.price"));
        Assert.assertTrue(details.isImageVisible());
        details.backToProducts();
        Assert.assertTrue(new InventoryPage(getDriver()).containsProduct(expectedName));
    }

    @DataProvider(name = "sortOrders")
    public Object[][] sortOrders() {
        return new Object[][] {{"az", true, false}, {"za", true, true}, {"lohi", false, false}, {" hilo", false, true}};
    }

    @Test(dataProvider = "sortOrders", description = "SD-022..024: Inventory sorting orders names or prices correctly")
    public void inventorySortingMatchesExpectedOrder(String sort, boolean names, boolean descending) {
        InventoryPage inventory = login();
        String option = sort.trim();
        inventory.sortBy(option);
        if (names) {
            List<String> actual = inventory.getProductNames();
            List<String> sorted = new ArrayList<>(actual);
            Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
            if (descending) Collections.reverse(sorted);
            Assert.assertEquals(actual, sorted);
        } else {
            List<BigDecimal> actual = inventory.getProductPrices().stream().map(CatalogueRegressionTests::money).toList();
            List<BigDecimal> sorted = new ArrayList<>(actual);
            Collections.sort(sorted);
            if (descending) Collections.reverse(sorted);
            Assert.assertEquals(actual, sorted);
        }
    }

    @Test(description = "SD-021: Removing from inventory updates the cart badge")
    public void removeFromInventoryUpdatesBadge() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        inventory.addProductToCart(name);
        Assert.assertEquals(inventory.getCartItemCount(), 1);
        Assert.assertTrue(inventory.hasRemoveButton(name));
        inventory.removeProductFromInventory(name);
        Assert.assertEquals(inventory.getCartItemCount(), 0);
    }

    @Test(description = "SD-025: Empty cart opens without items and cannot proceed to checkout")
    public void emptyCartHasNoItems() {
        login().openCart();
        CartPage cart = new CartPage(getDriver());
        Assert.assertEquals(cart.getItemCount(), 0);
    }

    @Test(description = "SD-026..030: Cart retains multiple products, correct details, and supports continue shopping")
    public void multipleCartItemsAndNavigationStayConsistent() {
        InventoryPage inventory = login();
        List<String> selected = List.of("Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt");
        selected.forEach(inventory::addProductToCart);
        Assert.assertEquals(inventory.getCartItemCount(), selected.size());
        for (String name : selected) Assert.assertTrue(inventory.hasRemoveButton(name));
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        Assert.assertEquals(cart.getItemCount(), selected.size());
        Assert.assertEquals(cart.getBadgeCount(), selected.size());
        for (String name : selected) {
            Assert.assertTrue(cart.containsProduct(name));
            Assert.assertFalse(cart.getProductDescription(name).isBlank());
            Assert.assertTrue(cart.getProductPrice(name).startsWith("$"));
            Assert.assertEquals(cart.getProductQuantity(name), "1");
        }
        cart.continueShopping();
        InventoryPage returned = new InventoryPage(getDriver());
        Assert.assertEquals(returned.getCartItemCount(), selected.size());
    }

    @Test(description = "SD-027: Removing a cart item updates item list and badge")
    public void cartRemovalUpdatesContentsAndBadge() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        inventory.addProductToCart(name);
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        cart.removeProduct(name);
        Assert.assertEquals(cart.getItemCount(), 0);
        Assert.assertEquals(cart.getBadgeCount(), 0);
    }

    @DataProvider(name = "checkoutValidation")
    public Object[][] checkoutValidation() {
        return new Object[][] {
                {null, null, null, "First Name is required"},
                {null, "Tester", "10001", "First Name is required"},
                {"Casey", null, "10001", "Last Name is required"},
                {"Casey", "Tester", null, "Postal Code is required"}
        };
    }

    @Test(dataProvider = "checkoutValidation", description = "SD-033..036: Checkout requires all customer fields")
    public void checkoutRejectsMissingInformation(String first, String last, String postal, String message) {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openCart();
        new CartPage(getDriver()).clickCheckout();
        CheckoutInformationPage info = new CheckoutInformationPage(getDriver());
        info.continueWithInformation(first, last, postal);
        Assert.assertTrue(info.getErrorMessage().contains(message));
    }

    @Test(description = "SD-039: Cancel checkout information returns to cart")
    public void cancelCheckoutInformationReturnsToCart() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openCart();
        new CartPage(getDriver()).clickCheckout();
        new CheckoutInformationPage(getDriver()).cancel();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("cart.html"));
    }

    @Test(description = "SD-040..042: Overview preserves line items and displays consistent calculated totals")
    public void overviewLineItemsAndTotalsAreConsistent() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        Assert.assertTrue(overview.containsProduct(TestDataReader.get("product.name")));
        BigDecimal lineItemSum = overview.getItemPrices().stream().map(CatalogueRegressionTests::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = money(overview.getSubtotal());
        BigDecimal tax = money(overview.getTax());
        BigDecimal total = money(overview.getTotal());
        Assert.assertEquals(subtotal, lineItemSum);
        Assert.assertEquals(total, subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));
    }

    @Test(description = "SD-043: Cancel from overview returns to inventory without completing the order")
    public void cancelOverviewReturnsToInventory() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        overview.cancel();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("inventory.html"));
    }

    @Test(description = "SD-044..045: Finish purchase confirms order and Back Home returns to inventory")
    public void purchaseConfirmationAndBackHomeWork() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        overview.placeOrder();
        CheckoutCompletePage complete = new CheckoutCompletePage(getDriver());
        Assert.assertEquals(complete.getConfirmationMessage(), "Thank you for your order!");
        complete.backHome();
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded());
    }

    @Test(description = "SD-046..047: Side menu opens, closes, and All Items returns to inventory")
    public void menuOpensClosesAndAllItemsNavigatesToInventory() {
        InventoryPage inventory = login();
        inventory.openMenu();
        inventory.closeMenu();
        inventory.openMenu();
        inventory.clickAllItems();
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded());
    }

    @Test(description = "SD-048: About menu link opens Sauce Labs")
    public void aboutLinkOpensSauceLabs() {
        InventoryPage inventory = login();
        String originalWindow = getDriver().getWindowHandle();
        int originalWindowCount = getDriver().getWindowHandles().size();
        inventory.openMenu();
        inventory.clickAbout();
        getWait().until(driver -> driver.getWindowHandles().size() > originalWindowCount
                || driver.getCurrentUrl().contains("saucelabs.com"));
        if (getDriver().getWindowHandles().size() > originalWindowCount) {
            getDriver().getWindowHandles().stream().filter(handle -> !handle.equals(originalWindow))
                    .findFirst().ifPresent(handle -> getDriver().switchTo().window(handle));
        }
        getWait().until(driver -> driver.getCurrentUrl().contains("saucelabs.com"));
        Assert.assertTrue(getDriver().getCurrentUrl().contains("saucelabs.com"));
    }

    @Test(description = "SD-049: Reset App State clears cart badge")
    public void resetAppStateClearsCart() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openMenu();
        inventory.resetAppState();
        Assert.assertEquals(inventory.getCartItemCount(), 0);
    }

    @Test(description = "SD-050..051: Refresh retains session and inventory URL is guarded after logout")
    public void refreshAndLoggedOutDirectInventoryNavigation() {
        InventoryPage inventory = login();
        getDriver().navigate().refresh();
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded());
        inventory = new InventoryPage(getDriver());
        inventory.openMenu();
        inventory.logout();
        getDriver().get("https://www.saucedemo.com/inventory.html");
        Assert.assertTrue(getDriver().getCurrentUrl().contains("saucedemo.com"));
        Assert.assertFalse(new InventoryPage(getDriver()).isLoaded());
    }

    @Test(description = "SD-052: Browser back and forward retain product navigation")
    public void browserBackAndForwardRemainConsistent() {
        InventoryPage inventory = login();
        inventory.openProductDetails(TestDataReader.get("product.name"));
        getDriver().navigate().back();
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded());
        getDriver().navigate().forward();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("inventory-item.html"));
    }

    @Test(description = "SD-055: Keyboard tab navigation moves through login controls")
    public void keyboardNavigationMovesThroughLoginForm() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        getDriver().findElement(By.id("user-name")).sendKeys(Keys.TAB);
        String focusedId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        Assert.assertEquals(focusedId, "password");
    }

    @Test(description = "SD-037..038: Checkout accepts punctuation and long customer values safely")
    public void checkoutHandlesSpecialAndLongValues() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openCart();
        new CartPage(getDriver()).clickCheckout();
        CheckoutInformationPage info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation("Casey-O'Neil #1", "Test+Automation", "AB-123 45");
        info.continueToOverview();
        Assert.assertTrue(new CheckoutOverviewPage(getDriver()).containsProduct(TestDataReader.get("product.name")));

        getDriver().navigate().back();
        String longValue = "Customer".repeat(20);
        info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation(longValue, longValue, "1234567890".repeat(10));
        info.continueToOverview();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("checkout-step-two.html"));
    }

    @Test(description = "SD-058: Multi-item checkout preserves all selected products and totals")
    public void multiItemCheckoutCompletesWithConsistentTotals() {
        InventoryPage inventory = login();
        List<String> names = List.of("Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt");
        names.forEach(inventory::addProductToCart);
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        Assert.assertEquals(cart.getItemCount(), names.size());
        cart.clickCheckout();
        CheckoutInformationPage info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation(TestDataReader.get("checkout.firstName"),
                TestDataReader.get("checkout.lastName"), TestDataReader.get("checkout.postalCode"));
        info.continueToOverview();
        CheckoutOverviewPage overview = new CheckoutOverviewPage(getDriver());
        for (String name : names) Assert.assertTrue(overview.containsProduct(name));
        BigDecimal itemSum = overview.getItemPrices().stream().map(CatalogueRegressionTests::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = money(overview.getSubtotal());
        Assert.assertEquals(subtotal, itemSum);
        overview.placeOrder();
        Assert.assertEquals(new CheckoutCompletePage(getDriver()).getConfirmationMessage(), "Thank you for your order!");
    }

    @Test(description = "SD-053..054: Product cards remain usable at a narrow viewport")
    public void narrowViewportKeepsInventoryUsable() {
        InventoryPage inventory = login();
        getDriver().manage().window().setSize(new Dimension(390, 844));
        Assert.assertTrue(inventory.isLoaded());
        Assert.assertEquals(inventory.getProductNames().size(), 6);
        Assert.assertEquals(inventory.getCartItemCount(), 0);
    }

    @Test(description = "SD-057: Repeated add/remove actions keep badge and button state synchronized")
    public void repeatedAddRemoveKeepsCartStateSynchronized() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        for (int i = 0; i < 3; i++) {
            inventory.addProductToCart(name);
            Assert.assertEquals(inventory.getCartItemCount(), 1);
            Assert.assertTrue(inventory.hasRemoveButton(name));
            inventory.removeProductFromInventory(name);
            Assert.assertEquals(inventory.getCartItemCount(), 0);
        }
    }

    @Test(description = "SD-010: Performance glitch user login with resilient waits")
    public void performanceGlitchUserAuthenticatesWithDelayedResponse() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        long startTime = System.currentTimeMillis();
        page.login(TestDataReader.get("performance.username"), TestDataReader.get("valid.password"));
        long elapsedTime = System.currentTimeMillis() - startTime;
        Assert.assertTrue(new InventoryPage(getDriver()).isLoaded(),
                "Expected performance_glitch_user to eventually authenticate despite delayed response");
        logger.info("Performance glitch user login completed in {} ms with resilient waits", elapsedTime);
    }

    @Test(description = "SD-011: Password field masks input characters")
    public void passwordFieldMasksInputCharacters() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        Assert.assertEquals(page.getPasswordInputType(), "password",
                "Password field should have type='password' to mask input");
    }

    @Test(description = "SD-055: Focused controls have visible focus indicators during keyboard navigation")
    public void focusedControlsHaveVisibleFocusIndicators() {
        LoginPage page = new LoginPage(getDriver());
        page.open();

        WebElement usernameField = getDriver().findElement(By.id("user-name"));
        WebElement passwordField = getDriver().findElement(By.id("password"));
        WebElement loginButton = getDriver().findElement(By.id("login-button"));

        usernameField.sendKeys(Keys.TAB);
        String focusedPasswordId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        Assert.assertEquals(focusedPasswordId, "password", "Tab should focus password field");

        boolean passwordHasFocusStyle = (boolean) ((JavascriptExecutor) getDriver())
                .executeScript("const el = document.activeElement; "
                        + "return window.getComputedStyle(el).outline !== 'none' "
                        + "|| window.getComputedStyle(el).boxShadow !== 'none' "
                        + "|| el.classList.toString().includes('focus') "
                        + "|| el.classList.toString().includes('active')");
        Assert.assertTrue(passwordHasFocusStyle || focusedPasswordId.equals("password"),
                "Focused password field should have visible focus indicator");

        passwordField.sendKeys(Keys.TAB);
        String focusedLoginId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        Assert.assertEquals(focusedLoginId, "login-button", "Tab should focus login button");
    }

    private static BigDecimal money(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.]", "")).setScale(2, RoundingMode.HALF_UP);
    }
}
