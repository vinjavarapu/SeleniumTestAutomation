package io.github.vinjavarapu.seleniumtestautomation.framework.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverFactory;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverManager;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.WaitHelper;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Common browser lifecycle for TestNG test classes. */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        logger.info("Starting {} browser (headless={})", FrameworkConfig.browser(), FrameworkConfig.headless());

        WebDriver driver = null;
        boolean registered = false;
        try {
            driver = DriverFactory.createDriver();
            DriverManager.setDriver(driver);
            registered = true;
            org.openqa.selenium.Capabilities capabilities = ((org.openqa.selenium.remote.RemoteWebDriver) driver)
                    .getCapabilities();
            Object driverVersion = capabilities.getCapability("chrome.chromedriverVersion");
            if (driverVersion == null) driverVersion = capabilities.getCapability("moz:geckodriverVersion");
            if (driverVersion == null) driverVersion = capabilities.getCapability("msedge.msedgedriverVersion");
            logger.info("Started browser={} version={} driverVersion={}", capabilities.getBrowserName(),
                    capabilities.getBrowserVersion(), driverVersion == null ? "not reported" : driverVersion);

            driver.manage().window().maximize();
        } catch (RuntimeException exception) {
            logger.error("Could not start browser", exception);
            try {
                if (registered) {
                    DriverManager.quitDriver();
                } else {
                    if (driver != null) driver.quit();
                    DriverManager.quitDriver();
                }
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        try {
            logger.info("Closing browser");
            DriverManager.quitDriver();
        } catch (IllegalStateException exception) {
            logger.debug("No browser was available during teardown");
        }
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected WaitHelper getWait() {
        return new WaitHelper(getDriver());
    }

    protected void navigateBack() {
        TestStepReporter.record("Navigating back in browser history");
        getDriver().navigate().back();
    }

    protected void navigateForward() {
        TestStepReporter.record("Navigating forward in browser history");
        getDriver().navigate().forward();
    }

    protected void refreshPage() {
        TestStepReporter.record("Refreshing the current page");
        getDriver().navigate().refresh();
    }

    protected void openUrl(String url, String description) {
        TestStepReporter.record(description);
        getDriver().get(url);
    }

}
