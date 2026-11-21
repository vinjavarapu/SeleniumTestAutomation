package io.github.vinjavarapu.seleniumtestautomation.framework.unittests;

import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class FrameworkConfigTest {
    private static final String[] PROPERTY_KEYS = {
            "baseUrl", "browser", "headless", "explicitWaitSeconds", "retryCount", "remoteUrl"
    };

    private final Map<String, String> originalProperties = new HashMap<>();

    @BeforeMethod
    public void captureSystemProperties() {
        for (String key : PROPERTY_KEYS) {
            originalProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void restoreSystemProperties() {
        for (String key : PROPERTY_KEYS) {
            String value = originalProperties.get(key);
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        }
        originalProperties.clear();
    }

    @Test
    public void acceptsValidLocalBrowserConfiguration() {
        System.setProperty("baseUrl", "https://shop.example.test/");
        System.setProperty("browser", "firefox");

        FrameworkConfig.validate();

        Assert.assertEquals(FrameworkConfig.baseUrl(), "https://shop.example.test/");
        Assert.assertEquals(FrameworkConfig.browser(), "firefox");
    }

    @Test
    public void acceptsValidRemoteGridConfiguration() {
        System.setProperty("remoteUrl", "http://grid.example.test:4444");

        FrameworkConfig.validate();

        Assert.assertEquals(FrameworkConfig.remoteUrl(), "http://grid.example.test:4444");
    }

    @Test
    public void rejectsUnsupportedBrowser() {
        System.setProperty("browser", "safari");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }

    @Test
    public void rejectsInvalidApplicationUrl() {
        System.setProperty("baseUrl", "file:///tmp/site");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }

    @Test
    public void rejectsInvalidGridUrl() {
        System.setProperty("remoteUrl", "not-a-url");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }

    @Test
    public void rejectsNonPositiveWaitTimeout() {
        System.setProperty("explicitWaitSeconds", "0");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }

    @Test
    public void rejectsNegativeRetryCount() {
        System.setProperty("retryCount", "-1");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }

    @Test
    public void rejectsInvalidBoolean() {
        System.setProperty("headless", "sometimes");

        Assert.expectThrows(IllegalArgumentException.class, FrameworkConfig::validate);
    }
}
