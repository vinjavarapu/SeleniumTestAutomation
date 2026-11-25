package io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata;

import io.github.vinjavarapu.seleniumtestautomation.framework.utils.TestDataReader;

/** Centralized access to SauceDemo test data properties. */
public final class SauceDemoData {
    private SauceDemoData() {
    }

    public static String validUsername() {
        return TestDataReader.get("valid.username");
    }

    public static String validPassword() {
        return TestDataReader.get("valid.password");
    }

    public static String productName() {
        return TestDataReader.get("product.name");
    }

    public static String productPrice() {
        return TestDataReader.get("product.price");
    }

    public static String checkoutFirstName() {
        return TestDataReader.get("checkout.first.name");
    }

    public static String checkoutLastName() {
        return TestDataReader.get("checkout.last.name");
    }

    public static String checkoutPostalCode() {
        return TestDataReader.get("checkout.postal.code");
    }

    public static String expectedErrorMessage() {
        return TestDataReader.get("expected.error.message");
    }
}
