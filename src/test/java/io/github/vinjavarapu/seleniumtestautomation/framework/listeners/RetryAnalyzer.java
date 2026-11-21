package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Retries failed tests up to the configured limit. */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private static final ThreadLocal<Boolean> retryPending = ThreadLocal.withInitial(() -> false);
    private int retryCount = 0;

    public static boolean consumeRetriedOutcome() {
        boolean wasRetried = retryPending.get();
        retryPending.remove();
        return wasRetried;
    }

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = FrameworkConfig.retryCount();
        if (!result.isSuccess() && retryCount < maxRetries) {
            retryCount++;
            retryPending.set(true);
            logger.warn("First attempt failed for {}. Scheduling retry {}/{}; original failure: {}",
                    result.getName(), retryCount, maxRetries,
                    result.getThrowable() == null ? "unknown" : result.getThrowable().toString());
            return true;
        }
        if (!result.isSuccess()) retryPending.remove();
        return false;
    }
}
