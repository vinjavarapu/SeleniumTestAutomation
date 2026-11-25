package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/** Metadata annotation for test methods to provide structured information for reporting and retry. */
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface TestInfo {
    /** Test case ID (e.g., "SD-001"). */
    String id() default "";

    /** Feature area this test covers (e.g., "login", "cart"). */
    String feature() default "";

    /** Severity level of this test (e.g., "critical", "high"). */
    String severity() default "";

    /** Test owner or team (e.g., "qa-team", "platform"). */
    String owner() default "";

    /** Number of retries for this specific test; overrides global retryCount. */
    int retries() default -1;
}
