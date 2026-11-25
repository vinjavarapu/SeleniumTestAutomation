package io.github.vinjavarapu.seleniumtestautomation.framework.tests;

import io.github.vinjavarapu.seleniumtestautomation.framework.data.TestDataLoader;
import io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata.CheckoutValidation;
import io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata.RejectedLogin;
import io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata.SortOrder;
import org.testng.annotations.DataProvider;

/** Centralized TestNG data providers for all test scenarios. */
public final class DataProviders {
    private DataProviders() {
    }

    @DataProvider(name = "rejectedLogins")
    public static Object[][] rejectedLogins() {
        return TestDataLoader.toDataProvider(TestDataLoader.load("rejected-logins.csv", RejectedLogin.class));
    }

    @DataProvider(name = "sortOrders")
    public static Object[][] sortOrders() {
        return TestDataLoader.toDataProvider(TestDataLoader.load("sort-orders.csv", SortOrder.class));
    }

    @DataProvider(name = "checkoutValidation")
    public static Object[][] checkoutValidation() {
        return TestDataLoader.toDataProvider(
                TestDataLoader.load("checkout-validation.csv", CheckoutValidation.class));
    }
}
