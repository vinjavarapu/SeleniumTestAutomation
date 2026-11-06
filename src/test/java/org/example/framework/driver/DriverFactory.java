package org.example.framework.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.framework.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/** Factory for creating local or remote WebDriver instances based on config. */
public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String remoteUrl = FrameworkConfig.remoteUrl();
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            return createRemoteDriver(remoteUrl);
        }
        return createLocalDriver();
    }

    private static WebDriver createLocalDriver() {
        String browserName = FrameworkConfig.browser();
        boolean headless = FrameworkConfig.headless();

        return switch (browserName.trim().toLowerCase()) {
            case "chrome" -> createChromeDriver(headless);
            case "firefox" -> createFirefoxDriver(headless);
            case "edge" -> createEdgeDriver(headless);
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browserName + "'. Choose chrome, firefox, or edge.");
        };
    }

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
            if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
                options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
            }
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        return new EdgeDriver(options);
    }

    private static WebDriver createRemoteDriver(String remoteUrl) {
        try {
            String browserName = FrameworkConfig.browser();
            boolean headless = FrameworkConfig.headless();

            var options = switch (browserName.trim().toLowerCase()) {
                case "chrome" -> {
                    ChromeOptions opts = new ChromeOptions();
                    if (headless) opts.addArguments("--headless=new");
                    yield opts;
                }
                case "firefox" -> {
                    FirefoxOptions opts = new FirefoxOptions();
                    if (headless) opts.addArguments("-headless");
                    yield opts;
                }
                case "edge" -> {
                    EdgeOptions opts = new EdgeOptions();
                    if (headless) opts.addArguments("--headless=new");
                    yield opts;
                }
                default -> throw new IllegalArgumentException(
                        "Unsupported browser '" + browserName + "'. Choose chrome, firefox, or edge.");
            };

            return new RemoteWebDriver(new URL(remoteUrl), options);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid remote URL: " + remoteUrl, exception);
        }
    }
}
