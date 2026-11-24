package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;

public class RetryAnalyzerTest {
    private String originalRetryCount;

    @BeforeMethod
    public void captureRetryCount() {
        originalRetryCount = System.getProperty("retryCount");
        System.setProperty("retryCount", "1");
        RetryAnalyzer.clearThreadState();
    }

    @AfterMethod(alwaysRun = true)
    public void restoreRetryCount() {
        RetryAnalyzer.clearThreadState();
        if (originalRetryCount == null) {
            System.clearProperty("retryCount");
        } else {
            System.setProperty("retryCount", originalRetryCount);
        }
    }

    @Test
    public void isolatesRetryBudgetPerDataProviderInvocation() {
        RetryAnalyzer analyzer = new RetryAnalyzer();

        ITestResult firstRow = mockResult("SuiteA", "sampleDataTest", new Object[] {"row1"}, false);
        Assert.assertTrue(analyzer.retry(firstRow), "First failure for row1 should schedule a retry");
        Assert.assertTrue(RetryAnalyzer.consumeRetriedOutcome(), "Thread-local retry flag should be set");
        Assert.assertFalse(RetryAnalyzer.consumeRetriedOutcome(), "consumeRetriedOutcome should clear the flag");
        Assert.assertFalse(analyzer.retry(firstRow), "Second failure for row1 should exhaust retryCount=1");

        ITestResult secondRow = mockResult("SuiteA", "sampleDataTest", new Object[] {"row2"}, false);
        Assert.assertTrue(analyzer.retry(secondRow), "row2 should still receive its own retry budget");
    }

    private static ITestResult mockResult(String contextName, String methodName, Object[] parameters, boolean success) {
        ITestContext context = (ITestContext) Proxy.newProxyInstance(
                ITestContext.class.getClassLoader(),
                new Class<?>[] {ITestContext.class},
                (proxy, method, args) -> "getName".equals(method.getName()) ? contextName : null);

        ITestNGMethod ngMethod = (ITestNGMethod) Proxy.newProxyInstance(
                ITestNGMethod.class.getClassLoader(),
                new Class<?>[] {ITestNGMethod.class},
                (proxy, method, args) -> "getQualifiedName".equals(method.getName())
                        ? "SampleTests." + methodName
                        : null);

        return (ITestResult) Proxy.newProxyInstance(
                ITestResult.class.getClassLoader(),
                new Class<?>[] {ITestResult.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isSuccess" -> success;
                    case "getName" -> methodName;
                    case "getParameters" -> parameters;
                    case "getTestContext" -> context;
                    case "getMethod" -> ngMethod;
                    default -> null;
                });
    }
}
