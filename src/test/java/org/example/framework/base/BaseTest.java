package org.example.framework.base;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.framework.config.FrameworkConfig;
import org.example.framework.driver.DriverFactory;
import org.example.framework.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Common browser lifecycle for TestNG test classes. */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        logger.info("Starting {} browser (headless={})", FrameworkConfig.browser(), FrameworkConfig.headless());

        try {
            WebDriver driver = DriverFactory.createDriver();
            DriverManager.setDriver(driver);

            long implicitWaitSeconds = FrameworkConfig.implicitWaitSeconds();
            if (implicitWaitSeconds > 0) {
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
            }
            driver.manage().window().maximize();
        } catch (RuntimeException exception) {
            logger.error("Could not start browser", exception);
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

    protected WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(FrameworkConfig.explicitWaitSeconds()));
    }
}
