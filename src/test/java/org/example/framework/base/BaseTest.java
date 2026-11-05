package org.example.framework.base;

import java.time.Duration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Method;

import org.example.framework.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import org.example.framework.utils.ExtentReportManager;
import org.testng.ITestResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.bonigarcia.wdm.WebDriverManager;

/** Common browser lifecycle for TestNG test classes. */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void startBrowser(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        ExtentReportManager.startTest(method.getName(), testAnnotation == null ? "" : testAnnotation.description());
        String browserName = System.getProperty("browser", "chrome");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        logger.info("Starting {} browser (headless={})", browserName, headless);

        try {
            WebDriver driver = createDriver(browserName, headless);
            DriverManager.setDriver(driver);

            long implicitWaitSeconds = Long.parseLong(System.getProperty("implicitWaitSeconds", "0"));
            if (implicitWaitSeconds > 0) {
                // Explicit waits are preferred. Keep this at 0 unless a project requirement needs implicit waits.
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
            }
            driver.manage().window().maximize();
        } catch (RuntimeException exception) {
            logger.error("Could not start the {} browser", browserName, exception);
            throw exception;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser(ITestResult result) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (result.getStatus() == ITestResult.FAILURE) {
                logger.error("Test '{}' failed on URL {}", result.getName(), driver.getCurrentUrl());
                ExtentReportManager.fail(result.getThrowable());
                ExtentReportManager.attachScreenshot(
                        ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES), "Failure screenshot");
                saveFailureScreenshot(driver, result.getName());
            } else if (result.getStatus() == ITestResult.SUCCESS) {
                ExtentReportManager.pass("Test completed successfully");
            } else if (result.getStatus() == ITestResult.SKIP) {
                ExtentReportManager.skipped(result.getThrowable() == null
                        ? "Test was skipped" : result.getThrowable().toString());
            }
        } catch (IllegalStateException exception) {
            logger.debug("No browser was available during teardown");
        } finally {
            logger.info("Closing browser");
            DriverManager.quitDriver();
            ExtentReportManager.clearCurrentTest();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void writeExtentReport() {
        ExtentReportManager.flush();
        logger.info("Extent report saved to {}", ExtentReportManager.reportPath().toAbsolutePath());
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait getWait() {
        long timeoutSeconds = Long.parseLong(System.getProperty("explicitWaitSeconds", "10"));
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
    }

    private WebDriver createDriver(String browserName, boolean headless) {
        return switch (browserName.trim().toLowerCase()) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new", "--window-size=1920,1080");
                yield new org.openqa.selenium.chrome.ChromeDriver(options);
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                yield new org.openqa.selenium.firefox.FirefoxDriver(options);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                yield new org.openqa.selenium.edge.EdgeDriver(options);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browserName + "'. Choose chrome, firefox, or edge.");
        };
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
}
