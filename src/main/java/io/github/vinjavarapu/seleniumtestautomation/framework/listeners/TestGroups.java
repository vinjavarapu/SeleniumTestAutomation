package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

/** Constants for TestNG test groups to standardize categorization. */
public final class TestGroups {
    // Execution scope
    public static final String SMOKE = "smoke";
    public static final String REGRESSION = "regression";

    // Severity levels
    public static final String CRITICAL = "critical";
    public static final String HIGH = "high";
    public static final String MEDIUM = "medium";
    public static final String LOW = "low";

    // Feature areas
    public static final String LOGIN = "login";
    public static final String CART = "cart";
    public static final String CHECKOUT = "checkout";
    public static final String INVENTORY = "inventory";
    public static final String PAYMENT = "payment";
    public static final String PRODUCT = "product";

    private TestGroups() {
    }
}
