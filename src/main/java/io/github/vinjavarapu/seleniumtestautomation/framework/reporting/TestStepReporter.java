package io.github.vinjavarapu.seleniumtestautomation.framework.reporting;

import org.testng.Assert;

import java.util.function.Consumer;

/** Optional per-test step reporting bridge; page objects do not depend on ExtentReports. */
public final class TestStepReporter {
    private static final ThreadLocal<Consumer<String>> REPORTER = new ThreadLocal<>();
    private static final ThreadLocal<Consumer<String>> PASS_REPORTER = new ThreadLocal<>();

    private TestStepReporter() {
    }

    public static void register(Consumer<String> reporter) {
        REPORTER.set(reporter);
    }

    public static void register(Consumer<String> reporter, Consumer<String> passReporter) {
        REPORTER.set(reporter);
        PASS_REPORTER.set(passReporter);
    }

    public static void record(String description) {
        Consumer<String> reporter = REPORTER.get();
        if (reporter != null) reporter.accept(description);
    }

    public static void pass(String description) {
        Consumer<String> reporter = PASS_REPORTER.get();
        if (reporter != null) reporter.accept(description);
    }

    public static void assertTrue(boolean condition) {
        Assert.assertTrue(condition);
        pass("Assertion passed: condition is true");
    }

    public static void assertTrue(boolean condition, String description) {
        Assert.assertTrue(condition, description);
        pass(description);
    }

    public static void assertFalse(boolean condition) {
        Assert.assertFalse(condition);
        pass("Assertion passed: condition is false");
    }

    public static void assertFalse(boolean condition, String description) {
        Assert.assertFalse(condition, description);
        pass(description);
    }

    public static void assertEquals(Object actual, Object expected) {
        Assert.assertEquals(actual, expected);
        pass("Assertion passed: actual value matches expected value");
    }

    public static void assertEquals(Object actual, Object expected, String description) {
        Assert.assertEquals(actual, expected, description);
        pass(description);
    }

    public static void clear() {
        REPORTER.remove();
        PASS_REPORTER.remove();
    }
}
