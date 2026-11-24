package io.github.vinjavarapu.seleniumtestautomation.framework.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.EnvironmentPreflight;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverFactory;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverManager;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.utils.WaitHelper;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Common browser lifecycle for TestNG test classes. */
public abstract class BaseTest {
    private static final Set<String> VERIFIED_TARGETS = ConcurrentHashMap.newKeySet();
    protected final Logger logger = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        FrameworkConfig.validate();
        runPreflightIfEnabled();
        logger.info("Starting {} browser (headless={})", FrameworkConfig.browser(), FrameworkConfig.headless());

        WebDriver driver = null;
        boolean registered = false;
        try {
            driver = DriverFactory.createDriver();
            DriverManager.setDriver(driver);
            registered = true;
            org.openqa.selenium.Capabilities capabilities = ((org.openqa.selenium.remote.RemoteWebDriver) driver)
                    .getCapabilities();
            Object driverVersion = getDriverVersion(capabilities);
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

    private Object getDriverVersion(org.openqa.selenium.Capabilities capabilities) {
        Object version = nestedCapability(capabilities.getCapability("chrome"), "chromedriverVersion");
        if (version == null) version = capabilities.getCapability("moz:geckodriverVersion");
        if (version == null) version = nestedCapability(capabilities.getCapability("msedge"), "msedgedriverVersion");
        return version;
    }

    private Object nestedCapability(Object capability, String name) {
        return capability instanceof Map<?, ?> values ? values.get(name) : null;
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

    private void runPreflightIfEnabled() {
        if (!FrameworkConfig.preflightCheck()) {
            return;
        }
        String baseUrl = FrameworkConfig.baseUrl();
        if (VERIFIED_TARGETS.add(baseUrl)) {
            EnvironmentPreflight.verifyReachable(baseUrl, Duration.ofSeconds(FrameworkConfig.preflightTimeoutSeconds()));
        }
    }
}
