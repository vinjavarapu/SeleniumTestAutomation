package io.github.vinjavarapu.seleniumtestautomation.framework.reporting;

import java.util.function.Consumer;

/** Optional per-test step reporting bridge; page objects do not depend on ExtentReports. */
public final class TestStepReporter {
    private static final ThreadLocal<Consumer<String>> REPORTER = new ThreadLocal<>();

    private TestStepReporter() {
    }

    public static void register(Consumer<String> reporter) {
        REPORTER.set(reporter);
    }

    public static void record(String description) {
        Consumer<String> reporter = REPORTER.get();
        if (reporter != null) reporter.accept(description);
    }

    public static void clear() {
        REPORTER.remove();
    }
}
