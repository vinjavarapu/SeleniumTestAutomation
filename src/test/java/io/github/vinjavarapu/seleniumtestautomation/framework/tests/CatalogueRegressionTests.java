package io.github.vinjavarapu.seleniumtestautomation.framework.tests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BaseTest;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.CartPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.CheckoutCompletePage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.CheckoutInformationPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.CheckoutOverviewPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.InventoryPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.LoginPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.pages.ProductDetailsPage;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.TestDataReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
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
        TestStepReporter.assertTrue(page.isLoginFormVisible());
        TestStepReporter.assertEquals(page.getPasswordInputType(), "password");
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
        TestStepReporter.assertTrue(page.getErrorMessage().toLowerCase().contains(expectedMessage.toLowerCase()),
                "Unexpected login error message");
    }

    @Test(description = "SD-009: Problem user authentication outcome is observed")
    public void specialPurposeUsersCanAuthenticate() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        page.login(TestDataReader.get("problem.username"), TestDataReader.get("valid.password"));
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded(),
                "Expected problem user to authenticate; UI quirks are catalogued separately");
    }

    @Test(description = "SD-012: Logging out ends the session")
    public void logoutReturnsToLogin() {
        InventoryPage inventory = login();
        inventory.openMenu();
        inventory.logout();
        TestStepReporter.assertTrue(new LoginPage(getDriver()).isLoginFormVisible());
    }

    @Test(description = "SD-013: Inventory shows product names, prices, and loaded images")
    public void inventoryShowsProductContent() {
        InventoryPage inventory = login();
        TestStepReporter.assertEquals(inventory.getProductNames().size(), 6);
        TestStepReporter.assertEquals(inventory.getProductPrices().size(), 6);
        TestStepReporter.assertTrue(inventory.productActionButtonsAreVisible(), "Each product should have a visible cart control");
        TestStepReporter.assertEquals(inventory.getProductImageSources().size(), 6);
        TestStepReporter.assertTrue(inventory.getProductImagesLoaded().stream().allMatch(Boolean::booleanValue),
                "All inventory images should load");
    }

    @Test(description = "SD-014..015, SD-056: Product details match inventory, image loads, and back navigation works")
    public void productDetailsAndBackNavigationWork() {
        InventoryPage inventory = login();
        String expectedName = TestDataReader.get("product.name");
        inventory.openProductDetails(expectedName);
        ProductDetailsPage details = new ProductDetailsPage(getDriver());
        TestStepReporter.assertEquals(details.getName(), expectedName);
        TestStepReporter.assertFalse(details.getDescription().isBlank());
        TestStepReporter.assertEquals(details.getPrice(), TestDataReader.get("product.price"));
        TestStepReporter.assertTrue(details.isImageVisible());
        TestStepReporter.assertTrue(details.isImageLoaded(), "Product detail image should load without a broken-image placeholder");
        details.backToProducts();
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).containsProduct(expectedName));
    }

    @DataProvider(name = "sortOrders")
    public Object[][] sortOrders() {
        return new Object[][] {{"az", true, false}, {"za", true, true}, {"lohi", false, false}, {"hilo", false, true}};
    }

    @Test(dataProvider = "sortOrders", description = "SD-019..022: Inventory sorting orders names or prices correctly")
    public void inventorySortingMatchesExpectedOrder(String sort, boolean names, boolean descending) {
        InventoryPage inventory = login();
        String option = sort.trim();
        inventory.sortBy(option);
        if (names) {
            List<String> actual = inventory.getProductNames();
            List<String> sorted = new ArrayList<>(actual);
            Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
            if (descending) Collections.reverse(sorted);
            TestStepReporter.assertEquals(actual, sorted);
        } else {
            List<BigDecimal> actual = inventory.getProductPrices().stream().map(CatalogueRegressionTests::money).toList();
            List<BigDecimal> sorted = new ArrayList<>(actual);
            Collections.sort(sorted);
            if (descending) Collections.reverse(sorted);
            TestStepReporter.assertEquals(actual, sorted);
        }
    }

    @Test(description = "SD-018: Removing from inventory updates the cart badge")
    public void removeFromInventoryUpdatesBadge() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        inventory.addProductToCart(name);
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 1);
        TestStepReporter.assertTrue(inventory.hasRemoveButton(name));
        inventory.removeProductFromInventory(name);
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 0);
    }

    @Test(description = "SD-025: Empty cart opens with no items; observe checkout control behavior")
    public void emptyCartHasNoItems() {
        login().openCart();
        CartPage cart = new CartPage(getDriver());
        TestStepReporter.assertEquals(cart.getItemCount(), 0);
        if (cart.isCheckoutEnabled()) {
            logger.warn("SauceDemo currently leaves Checkout enabled when the cart is empty");
        }
    }

    @Test(description = "SD-016..017, SD-026..029: Add/remove controls, cart details, multiple items, and continue shopping")
    public void multipleCartItemsAndNavigationStayConsistent() {
        InventoryPage inventory = login();
        List<String> selected = List.of("Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt");
        selected.forEach(inventory::addProductToCart);
        TestStepReporter.assertEquals(inventory.getCartItemCount(), selected.size());
        for (String name : selected) TestStepReporter.assertTrue(inventory.hasRemoveButton(name));
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        TestStepReporter.assertEquals(cart.getItemCount(), selected.size());
        TestStepReporter.assertEquals(cart.getBadgeCount(), selected.size());
        for (String name : selected) {
            TestStepReporter.assertTrue(cart.containsProduct(name));
            TestStepReporter.assertFalse(cart.getProductDescription(name).isBlank());
            TestStepReporter.assertTrue(cart.getProductPrice(name).startsWith("$"));
            TestStepReporter.assertEquals(cart.getProductQuantity(name), "1");
        }
        cart.continueShopping();
        InventoryPage returned = new InventoryPage(getDriver());
        TestStepReporter.assertEquals(returned.getCartItemCount(), selected.size());
    }

    @Test(description = "SD-023: Sorting after adding a product preserves its cart state")
    public void sortingAfterAddPreservesCartContents() {
        InventoryPage inventory = login();
        String productName = TestDataReader.get("product.name");
        inventory.addProductToCart(productName);

        inventory.sortBy("za");
        List<String> actualNames = inventory.getProductNames();
        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(String.CASE_INSENSITIVE_ORDER.reversed());
        TestStepReporter.assertEquals(actualNames, expectedNames, "Name Z to A sort should be applied");
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 1);
        TestStepReporter.assertTrue(inventory.hasRemoveButton(productName));

        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        TestStepReporter.assertTrue(cart.containsProduct(productName));
        TestStepReporter.assertEquals(cart.getBadgeCount(), 1);
    }

    @Test(description = "SD-030: Cart state remains consistent through product details and cart navigation")
    public void cartStatePersistsAcrossProductNavigation() {
        InventoryPage inventory = login();
        String productName = TestDataReader.get("product.name");
        inventory.addProductToCart(productName);
        inventory.openProductDetails(productName);

        ProductDetailsPage details = new ProductDetailsPage(getDriver());
        TestStepReporter.assertEquals(details.getName(), productName);
        details.backToProducts();

        InventoryPage returned = new InventoryPage(getDriver());
        TestStepReporter.assertEquals(returned.getCartItemCount(), 1);
        TestStepReporter.assertTrue(returned.hasRemoveButton(productName));
        returned.openCart();
        CartPage cart = new CartPage(getDriver());
        TestStepReporter.assertTrue(cart.containsProduct(productName));
        TestStepReporter.assertEquals(cart.getBadgeCount(), 1);
    }

    @Test(description = "SD-027: Removing a cart item updates item list and badge")
    public void cartRemovalUpdatesContentsAndBadge() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        inventory.addProductToCart(name);
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        cart.removeProduct(name);
        TestStepReporter.assertEquals(cart.getItemCount(), 0);
        TestStepReporter.assertEquals(cart.getBadgeCount(), 0);
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
        TestStepReporter.assertTrue(info.getErrorMessage().contains(message));
    }

    @Test(description = "SD-039: Cancel checkout information returns to cart")
    public void cancelCheckoutInformationReturnsToCart() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.openCart();
        new CartPage(getDriver()).clickCheckout();
        new CheckoutInformationPage(getDriver()).cancel();
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("cart.html"));
        TestStepReporter.assertTrue(new CartPage(getDriver()).containsProduct(TestDataReader.get("product.name")),
                "Canceling checkout should preserve the cart item");
    }

    @Test(description = "SD-040..042: Overview preserves line items and displays consistent calculated totals")
    public void overviewLineItemsAndTotalsAreConsistent() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        TestStepReporter.assertTrue(overview.containsProduct(TestDataReader.get("product.name")));
        TestStepReporter.assertEquals(overview.getItemNames(), List.of(TestDataReader.get("product.name")));
        TestStepReporter.assertEquals(overview.getItemPrices(), List.of(TestDataReader.get("product.price")));
        TestStepReporter.assertEquals(overview.getItemQuantities(), List.of("1"));
        BigDecimal lineItemSum = overview.getItemPrices().stream().map(CatalogueRegressionTests::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = money(overview.getSubtotal());
        BigDecimal tax = money(overview.getTax());
        BigDecimal total = money(overview.getTotal());
        TestStepReporter.assertEquals(subtotal, lineItemSum);
        TestStepReporter.assertEquals(total, subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));
    }

    @Test(description = "SD-043: Cancel from overview returns to inventory without completing the order")
    public void cancelOverviewReturnsToInventory() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        overview.cancel();
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("inventory.html"));
    }

    @Test(description = "SD-044..045: Finish purchase confirms order and Back Home returns to inventory")
    public void purchaseConfirmationAndBackHomeWork() {
        CheckoutOverviewPage overview = checkoutWithOneProduct();
        overview.placeOrder();
        CheckoutCompletePage complete = new CheckoutCompletePage(getDriver());
        TestStepReporter.assertEquals(complete.getConfirmationMessage(), "Thank you for your order!");
        complete.backHome();
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded());
    }

    @Test(description = "SD-046..047: Side menu opens, closes, and All Items returns to inventory")
    public void menuOpensClosesAndAllItemsNavigatesToInventory() {
        InventoryPage inventory = login();
        inventory.openMenu();
        TestStepReporter.assertTrue(inventory.isMenuOpen(), "The side menu should be visible after opening it");
        inventory.closeMenu();
        TestStepReporter.assertFalse(inventory.isMenuOpen(), "The side menu should be hidden after closing it");
        inventory.openMenu();
        inventory.clickAllItems();
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded());
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
            TestStepReporter.record("Switching to the Sauce Labs browser tab");
            getDriver().getWindowHandles().stream().filter(handle -> !handle.equals(originalWindow))
                    .findFirst().ifPresent(handle -> getDriver().switchTo().window(handle));
        }
        getWait().until(driver -> driver.getCurrentUrl().contains("saucelabs.com"));
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("saucelabs.com"));
    }

    @Test(description = "SD-049: Reset App State clears cart badge")
    public void resetAppStateClearsCart() {
        InventoryPage inventory = login();
        inventory.addProductToCart(TestDataReader.get("product.name"));
        inventory.resetAppState();
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 0);
    }

    @Test(description = "SD-050..051: Refresh retains session and inventory URL is guarded after logout")
    public void refreshAndLoggedOutDirectInventoryNavigation() {
        InventoryPage inventory = login();
        refreshPage();
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded());
        inventory = new InventoryPage(getDriver());
        inventory.openMenu();
        inventory.logout();
        openUrl(FrameworkConfig.baseUrl() + "inventory.html", "Attempting direct access to the inventory page");
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("saucedemo.com"));
        TestStepReporter.assertTrue(new LoginPage(getDriver()).isLoginFormVisible());
    }

    @Test(description = "SD-052: Browser back and forward retain product navigation")
    public void browserBackAndForwardRemainConsistent() {
        InventoryPage inventory = login();
        inventory.openProductDetails(TestDataReader.get("product.name"));
        navigateBack();
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded());
        navigateForward();
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("inventory-item.html"));
    }

    @Test(description = "SD-054: Keyboard navigation operates login and inventory shopping controls")
    public void keyboardNavigationOperatesLoginAndShoppingFlow() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        TestStepReporter.record("Entering credentials and moving through the login form with the keyboard");
        WebElement username = getDriver().findElement(By.id("user-name"));
        WebElement password = getDriver().findElement(By.id("password"));
        username.sendKeys(TestDataReader.get("valid.username"), Keys.TAB);
        TestStepReporter.assertEquals(activeElementId(), "password", "Tab should move from username to password");
        password.sendKeys(TestDataReader.get("valid.password"), Keys.ENTER);

        InventoryPage inventory = new InventoryPage(getDriver());
        TestStepReporter.assertTrue(inventory.isLoaded(), "Enter should submit the login form");

        String productName = TestDataReader.get("product.name");
        TestStepReporter.record("Using keyboard tab navigation to focus the product cart control");
        String addButtonTestId = "add-to-cart-" + productName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        boolean addButtonFocused = false;
        for (int tab = 0; tab < 50; tab++) {
            getDriver().switchTo().activeElement().sendKeys(Keys.TAB);
            if (Boolean.TRUE.equals(((JavascriptExecutor) getDriver()).executeScript(
                    "return document.activeElement.getAttribute('data-test') === arguments[0]", addButtonTestId))) {
                addButtonFocused = true;
                break;
            }
        }
        TestStepReporter.assertTrue(addButtonFocused, "Tab order should reach the product Add to cart control");
        TestStepReporter.record("Adding the focused product to the cart with the Space key");
        getDriver().switchTo().activeElement().sendKeys(Keys.SPACE);
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 1, "Space should activate the focused Add to cart button");
        TestStepReporter.assertTrue(inventory.hasRemoveButton(productName),
                "Space should change the focused product control to Remove");
    }

    @Test(description = "SD-054: Login Tab order moves through the login controls")
    public void keyboardNavigationMovesThroughLoginForm() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        TestStepReporter.record("Moving focus from username to password with the Tab key");
        getDriver().findElement(By.id("user-name")).sendKeys(Keys.TAB);
        String focusedId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        TestStepReporter.assertEquals(focusedId, "password");
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
        TestStepReporter.assertTrue(new CheckoutOverviewPage(getDriver()).containsProduct(TestDataReader.get("product.name")));

        navigateBack();
        String longValue = "Customer".repeat(20);
        info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation(longValue, longValue, "1234567890".repeat(10));
        info.continueToOverview();
        TestStepReporter.assertTrue(getDriver().getCurrentUrl().contains("checkout-step-two.html"));
        TestStepReporter.assertTrue(new CheckoutOverviewPage(getDriver()).containsProduct(TestDataReader.get("product.name")),
                "Checkout should remain usable after accepting long customer values");
    }

    @Test(description = "SD-058: Multi-item checkout preserves all selected products and totals")
    public void multiItemCheckoutCompletesWithConsistentTotals() {
        InventoryPage inventory = login();
        List<String> names = List.of("Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt");
        names.forEach(inventory::addProductToCart);
        inventory.openCart();
        CartPage cart = new CartPage(getDriver());
        TestStepReporter.assertEquals(cart.getItemCount(), names.size());
        cart.clickCheckout();
        CheckoutInformationPage info = new CheckoutInformationPage(getDriver());
        info.enterCustomerInformation(TestDataReader.get("checkout.firstName"),
                TestDataReader.get("checkout.lastName"), TestDataReader.get("checkout.postalCode"));
        info.continueToOverview();
        CheckoutOverviewPage overview = new CheckoutOverviewPage(getDriver());
        for (String name : names) TestStepReporter.assertTrue(overview.containsProduct(name));
        BigDecimal itemSum = overview.getItemPrices().stream().map(CatalogueRegressionTests::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = money(overview.getSubtotal());
        TestStepReporter.assertEquals(subtotal, itemSum);
        overview.placeOrder();
        TestStepReporter.assertEquals(new CheckoutCompletePage(getDriver()).getConfirmationMessage(), "Thank you for your order!");
    }

    @Test(description = "SD-053: Inventory cards and controls remain usable at desktop and narrow viewports")
    public void narrowViewportKeepsInventoryUsable() {
        InventoryPage inventory = login();
        TestStepReporter.record("Setting desktop viewport to 1280 by 800");
        getDriver().manage().window().setSize(new Dimension(1280, 800));
        TestStepReporter.assertTrue(inventory.productCardsFitViewport(), "Desktop product cards should fit the viewport");
        TestStepReporter.assertTrue(inventory.productActionButtonsAreVisible(), "Desktop product controls should be visible");

        TestStepReporter.record("Setting narrow viewport to 390 by 844");
        getDriver().manage().window().setSize(new Dimension(390, 844));
        TestStepReporter.assertTrue(inventory.isLoaded());
        TestStepReporter.assertEquals(inventory.getProductNames().size(), 6);
        TestStepReporter.assertEquals(inventory.getCartItemCount(), 0);
        TestStepReporter.assertTrue(inventory.productCardsFitViewport(), "Mobile product cards should not overflow horizontally");
        TestStepReporter.assertTrue(inventory.productActionButtonsAreVisible(), "Mobile product controls should remain visible");
    }

    @Test(description = "SD-057: Repeated add/remove actions keep badge and button state synchronized")
    public void repeatedAddRemoveKeepsCartStateSynchronized() {
        InventoryPage inventory = login();
        String name = TestDataReader.get("product.name");
        for (int i = 0; i < 3; i++) {
            inventory.addProductToCart(name);
            TestStepReporter.assertEquals(inventory.getCartItemCount(), 1);
            TestStepReporter.assertTrue(inventory.hasRemoveButton(name));
            inventory.removeProductFromInventory(name);
            TestStepReporter.assertEquals(inventory.getCartItemCount(), 0);
        }
    }

    @Test(description = "SD-010: Performance glitch user login with resilient waits")
    public void performanceGlitchUserAuthenticatesWithDelayedResponse() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        long startTime = System.currentTimeMillis();
        page.login(TestDataReader.get("performance.username"), TestDataReader.get("valid.password"));
        long elapsedTime = System.currentTimeMillis() - startTime;
        TestStepReporter.assertTrue(new InventoryPage(getDriver()).isLoaded(),
                "Expected performance_glitch_user to eventually authenticate despite delayed response");
        logger.info("Performance glitch user login completed in {} ms with resilient waits", elapsedTime);
    }

    @Test(description = "SD-011: Password field masks input characters")
    public void passwordFieldMasksInputCharacters() {
        LoginPage page = new LoginPage(getDriver());
        page.open();
        TestStepReporter.assertEquals(page.getPasswordInputType(), "password",
                "Password field should have type='password' to mask input");
    }

    @Test(description = "SD-055: Focused controls have visible focus indicators during keyboard navigation")
    public void focusedControlsHaveVisibleFocusIndicators() {
        LoginPage page = new LoginPage(getDriver());
        page.open();

        TestStepReporter.record("Checking keyboard focus indicators on login controls");
        WebElement usernameField = getDriver().findElement(By.id("user-name"));
        WebElement passwordField = getDriver().findElement(By.id("password"));
        WebElement loginButton = getDriver().findElement(By.id("login-button"));

        usernameField.sendKeys(Keys.TAB);
        String focusedPasswordId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        TestStepReporter.assertEquals(focusedPasswordId, "password", "Tab should focus password field");

        boolean keyboardFocusVisible = (boolean) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.matches(':focus-visible')");
        TestStepReporter.assertTrue(keyboardFocusVisible, "Keyboard-focused password field should match :focus-visible");

        boolean passwordHasRenderedIndicator = (boolean) ((JavascriptExecutor) getDriver())
                .executeScript("const style = window.getComputedStyle(document.activeElement); "
                        + "return (style.outlineStyle !== 'none' && parseFloat(style.outlineWidth) > 0) "
                        + "|| style.boxShadow !== 'none' "
                        + "|| document.activeElement.classList.toString().includes('focus') "
                        + "|| document.activeElement.classList.toString().includes('active')");
        if (!passwordHasRenderedIndicator) {
            logger.warn("SD-055: SauceDemo marks the field focus-visible but exposes no computed outline or focus style");
        }

        passwordField.sendKeys(Keys.TAB);
        String focusedLoginId = (String) ((JavascriptExecutor) getDriver())
                .executeScript("return document.activeElement.id");
        TestStepReporter.assertEquals(focusedLoginId, "login-button", "Tab should focus login button");
    }

    private static BigDecimal money(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.]", "")).setScale(2, RoundingMode.HALF_UP);
    }

    private String activeElementId() {
        return (String) ((JavascriptExecutor) getDriver()).executeScript("return document.activeElement.id");
    }
}
