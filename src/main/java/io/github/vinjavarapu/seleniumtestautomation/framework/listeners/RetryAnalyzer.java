package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Retries failed test invocations up to the configured limit, isolated per method and parameters. */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger(RetryAnalyzer.class);
    private static final ThreadLocal<Boolean> RETRY_PENDING = ThreadLocal.withInitial(() -> false);
    private final ConcurrentMap<String, AtomicInteger> retryCounts = new ConcurrentHashMap<>();

    public static boolean consumeRetriedOutcome() {
        boolean wasRetried = RETRY_PENDING.get();
        RETRY_PENDING.remove();
        return wasRetried;
    }

    public static void clearThreadState() {
        RETRY_PENDING.remove();
    }

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = FrameworkConfig.retryCount();
        String key = invocationKey(result);
        AtomicInteger attempts = retryCounts.computeIfAbsent(key, ignored -> new AtomicInteger(0));
        if (!result.isSuccess() && attempts.get() < maxRetries) {
            int attempt = attempts.incrementAndGet();
            RETRY_PENDING.set(true);
            LOGGER.warn("Attempt {} failed for {}. Scheduling retry {}/{}; failure: {}",
                    attempt, result.getName(), attempt, maxRetries,
                    result.getThrowable() == null ? "unknown" : result.getThrowable().toString());
            return true;
        }
        retryCounts.remove(key);
        if (!result.isSuccess()) {
            RETRY_PENDING.remove();
        }
        return false;
    }

    static String invocationKey(ITestResult result) {
        String contextName = result.getTestContext() == null ? "" : result.getTestContext().getName();
        String methodName = result.getMethod() == null
                ? String.valueOf(result.getName())
                : result.getMethod().getQualifiedName();
        return contextName + "::" + methodName + "::" + Arrays.deepToString(result.getParameters());
    }
}
