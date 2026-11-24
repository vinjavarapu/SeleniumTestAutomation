package io.github.vinjavarapu.seleniumtestautomation.framework.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverManager;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.BrowserDiagnostics;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.ExtentReportManager;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.UrlRedactor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuiteListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Manages test reporting via ExtentReports and captures screenshots on failure. */
public class TestListener implements ITestListener, ISuiteListener {
    private static final Logger LOGGER = LogManager.getLogger(TestListener.class);
    private static final Pattern SENSITIVE_PARAMETER = Pattern.compile(
            ".*(password|secret|token|credential).*", Pattern.CASE_INSENSITIVE);

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("Starting test: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String testName = result.getName();
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            testName += " " + formatParameters(method, parameters);
        }

        String[] groups = result.getMethod().getGroups();
        if (groups == null || groups.length == 0) {
            groups = new String[] {defaultCategory(method)};
        }
        ExtentReportManager.startTest(testName, result.getMethod().getDescription(), groups);
        ThreadContext.put("testName", result.getName());
        TestStepReporter.register(ExtentReportManager::step, ExtentReportManager::pass);
        TestStepReporter.record("Starting test execution");
        LOGGER.info("Test started: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        boolean retried = RetryAnalyzer.consumeRetriedOutcome() || result.wasRetried();
        LOGGER.info(retried ? "Test passed after retry: {}" : "Test passed: {}", result.getName());
        try {
            ExtentReportManager.pass(retried
                    ? "Test passed after a retry; see the earlier failed attempt in this report and CI logs"
                    : "Test completed successfully");
        } finally {
            TestStepReporter.clear();
            ThreadContext.remove("testName");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RetryAnalyzer.clearThreadState();
        LOGGER.error("Test failed: {} on URL {}", result.getName(), getDriverUrl());
        ExtentReportManager.fail(result.getThrowable());
        captureFailureEvidence(result.getName());
        TestStepReporter.clear();
        ThreadContext.remove("testName");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String reason = result.getThrowable() == null ? "Test was skipped" : result.getThrowable().toString();
        if (result.wasRetried()) {
            LOGGER.warn("Attempt failed and will be retried: {} on URL {}", result.getName(), getDriverUrl());
            ExtentReportManager.skipped("Attempt failed and scheduled for retry: " + reason);
            captureFailureEvidence(result.getName() + "-retry");
        } else {
            RetryAnalyzer.clearThreadState();
            LOGGER.warn("Test skipped: {}", result.getName());
            ExtentReportManager.skipped(reason);
        }
        TestStepReporter.clear();
        ThreadContext.remove("testName");
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("Finished test suite: {}", context.getName());
        ExtentReportManager.clearCurrentTest();
    }

    @Override
    public void onStart(org.testng.ISuite suite) {
        LOGGER.info("Starting suite: {}", suite.getName());
    }

    @Override
    public void onFinish(org.testng.ISuite suite) {
        LOGGER.info("Finishing suite: {}", suite.getName());
        ExtentReportManager.flush();
        LOGGER.info("Extent report saved to {}", ExtentReportManager.reportPath().toAbsolutePath());
    }

    private void captureFailureEvidence(String testName) {
        try {
            WebDriver driver = DriverManager.getDriver();
            String screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            ExtentReportManager.attachScreenshot(screenshot, "Failure screenshot");
            String invocation = uniqueArtifactPrefix(testName);
            saveFailureScreenshot(driver, invocation);
            for (Path artifact : BrowserDiagnostics.captureFailureArtifacts(driver, invocation)) {
                ExtentReportManager.step("Captured failure diagnostic: " + artifact.getFileName());
            }
        } catch (IllegalStateException exception) {
            LOGGER.debug("No driver available for failure diagnostics");
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not capture or attach failure diagnostics", exception);
        }
    }

    private String uniqueArtifactPrefix(String testName) {
        String invocation = Integer.toHexString(System.identityHashCode(Thread.currentThread()))
                + "-" + System.nanoTime();
        String safeName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safeName + "-" + invocation;
    }

    private String defaultCategory(Method method) {
        String category = method.getDeclaringClass().getSimpleName();
        if (category.endsWith("Tests")) category = category.substring(0, category.length() - "Tests".length());
        if (category.endsWith("Regression")) {
            category = category.substring(0, category.length() - "Regression".length());
        }
        if (category.equals("InventoryCart")) return "Cart";
        if (category.endsWith("Test")) category = category.substring(0, category.length() - "Test".length());
        return category.isBlank() ? "Uncategorized" : category;
    }

    private String formatParameters(Method method, Object[] parameters) {
        Object[] reportParameters = parameters.clone();
        java.lang.reflect.Parameter[] methodParameters = method.getParameters();
        for (int i = 0; i < Math.min(reportParameters.length, methodParameters.length); i++) {
            if (methodParameters[i].isNamePresent()
                    && SENSITIVE_PARAMETER.matcher(methodParameters[i].getName()).matches()) {
                reportParameters[i] = "[REDACTED]";
            }
        }
        return Arrays.deepToString(reportParameters);
    }

    private void saveFailureScreenshot(WebDriver driver, String artifactPrefix) {
        try {
            Path screenshotDirectory = Path.of("target", "screenshots");
            Files.createDirectories(screenshotDirectory);
            Path screenshotPath = screenshotDirectory.resolve(artifactPrefix + ".png");
            Files.copy(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath(),
                    screenshotPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Saved failure screenshot to {}", screenshotPath.toAbsolutePath());
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not save failure screenshot", exception);
        }
    }

    private String getDriverUrl() {
        try {
            return UrlRedactor.redact(DriverManager.getDriver().getCurrentUrl());
        } catch (IllegalStateException exception) {
            return "N/A";
        }
    }
}
