package org.example.framework.listeners;

import org.example.framework.config.FrameworkConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Retries failed tests up to the configured limit. */
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = FrameworkConfig.retryCount();
        if (!result.isSuccess() && retryCount < maxRetries) {
            retryCount++;
            return true;
        }
        return false;
    }
}
