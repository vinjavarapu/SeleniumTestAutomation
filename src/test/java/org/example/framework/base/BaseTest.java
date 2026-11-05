package org.example.framework.base;

import java.time.Duration;

import org.example.framework.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import io.github.bonigarcia.wdm.WebDriverManager;

/** Common browser lifecycle for TestNG test classes. */
public abstract class BaseTest {
    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        WebDriver driver = createDriver(System.getProperty("browser", "chrome"));
        DriverManager.setDriver(driver);

        long implicitWaitSeconds = Long.parseLong(System.getProperty("implicitWaitSeconds", "0"));
        if (implicitWaitSeconds > 0) {
            // Explicit waits are preferred. Keep this at 0 unless a project requirement needs implicit waits.
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
        }
        driver.manage().window().maximize();
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        DriverManager.quitDriver();
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait getWait() {
        long timeoutSeconds = Long.parseLong(System.getProperty("explicitWaitSeconds", "10"));
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
    }

    private WebDriver createDriver(String browserName) {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        return switch (browserName.trim().toLowerCase()) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
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
}
