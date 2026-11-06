package org.example.framework.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.framework.driver.DriverManager;
import org.example.framework.utils.ExtentReportManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Manages test reporting via ExtentReports and captures screenshots on failure. */
public class TestListener implements ITestListener, ISuiteListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("Starting test: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentReportManager.startTest(result.getName(), result.getMethod().getDescription());
        logger.info("Test started: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getName());
        ExtentReportManager.pass("Test completed successfully");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {} on URL {}", result.getName(),
                getDriverUrl());
        ExtentReportManager.fail(result.getThrowable());
        captureAndAttachScreenshot(result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String reason = result.getThrowable() == null ? "Test was skipped" : result.getThrowable().toString();
        logger.warn("Test skipped: {}", result.getName());
        ExtentReportManager.skipped(reason);
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Finished test suite: {}", context.getName());
        ExtentReportManager.clearCurrentTest();
    }

    @Override
    public void onStart(org.testng.ISuite suite) {
        logger.info("Starting suite: {}", suite.getName());
    }

    @Override
    public void onFinish(org.testng.ISuite suite) {
        logger.info("Finishing suite: {}", suite.getName());
        ExtentReportManager.flush();
        logger.info("Extent report saved to {}", ExtentReportManager.reportPath().toAbsolutePath());
    }

    private void captureAndAttachScreenshot(String testName) {
        try {
            WebDriver driver = DriverManager.getDriver();
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            ExtentReportManager.attachScreenshot(screenshot, "Failure screenshot");
            saveFailureScreenshot(driver, testName);
        } catch (IllegalStateException exception) {
            logger.debug("No driver available for screenshot");
        }
    }

    private void saveFailureScreenshot(WebDriver driver, String testName) {
        try {
            Path screenshotDirectory = Path.of("target", "screenshots");
            Files.createDirectories(screenshotDirectory);
            Path screenshotPath = screenshotDirectory.resolve(testName + ".png");
            Files.copy(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath(),
                    screenshotPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved failure screenshot to {}", screenshotPath.toAbsolutePath());
        } catch (IOException | RuntimeException exception) {
            logger.warn("Could not save failure screenshot", exception);
        }
    }

    private String getDriverUrl() {
        try {
            return DriverManager.getDriver().getCurrentUrl();
        } catch (IllegalStateException exception) {
            return "N/A";
        }
    }
}
