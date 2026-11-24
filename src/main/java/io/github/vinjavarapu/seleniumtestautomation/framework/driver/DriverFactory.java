package io.github.vinjavarapu.seleniumtestautomation.framework.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

/** Factory for creating local or remote WebDriver instances based on config. */
public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        FrameworkConfig.validate();
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
        String chromeDriverPath = System.getProperty("webdriver.chrome.driver");
        ChromeDriverService service = null;
        if (chromeDriverPath != null && !chromeDriverPath.isBlank()) {
            service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(chromeDriverPath.trim()))
                    .build();
        } else {
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = createChromeOptions(headless);
        return service == null ? new ChromeDriver(options) : new ChromeDriver(service, options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        return new FirefoxDriver(createFirefoxOptions(headless));
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        return new EdgeDriver(createEdgeOptions(headless));
    }

    private static ChromeOptions createChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        String chromeBinaryPath = System.getProperty("webdriver.chrome.binary");
        if (chromeBinaryPath != null && !chromeBinaryPath.isBlank()) {
            options.setBinary(chromeBinaryPath.trim());
        }
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
            if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
                options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
            }
        }
        return options;
    }

    private static FirefoxOptions createFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless", "--width=1920", "--height=1080");
        }
        return options;
    }

    private static EdgeOptions createEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    private static WebDriver createRemoteDriver(String remoteUrl) {
        try {
            String browserName = FrameworkConfig.browser();
            boolean headless = FrameworkConfig.headless();

            Capabilities options = switch (browserName.trim().toLowerCase()) {
                case "chrome" -> createChromeOptions(headless);
                case "firefox" -> createFirefoxOptions(headless);
                case "edge" -> createEdgeOptions(headless);
                default -> throw new IllegalArgumentException(
                        "Unsupported browser '" + browserName + "'. Choose chrome, firefox, or edge.");
            };

            return new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
        } catch (MalformedURLException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid remote URL: " + remoteUrl, exception);
        }
    }
}
