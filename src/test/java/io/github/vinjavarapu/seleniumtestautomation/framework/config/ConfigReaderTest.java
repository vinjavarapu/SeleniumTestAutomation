package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigReaderTest {
    private String originalBaseUrl;

    @BeforeMethod
    public void captureBaseUrlProperty() {
        originalBaseUrl = System.getProperty("baseUrl");
        System.clearProperty("baseUrl");
    }

    @AfterMethod(alwaysRun = true)
    public void restoreBaseUrlProperty() {
        if (originalBaseUrl == null) {
            System.clearProperty("baseUrl");
        } else {
            System.setProperty("baseUrl", originalBaseUrl);
        }
    }

    @Test
    public void recognizesExplicitBaseUrlProperty() {
        System.setProperty("baseUrl", "https://client.example.test");

        Assert.assertTrue(ConfigReader.hasExplicitValue("baseUrl"));
    }

    @Test
    public void ignoresBlankBaseUrlProperty() {
        System.setProperty("baseUrl", "  ");

        Assert.assertFalse(ConfigReader.hasExplicitValue("baseUrl"));
    }

    @Test
    public void stagingRequiresAnExplicitBaseUrl() {
        Assert.expectThrows(IllegalStateException.class,
                () -> FrameworkConfig.validateEnvironmentBaseUrl("staging", false));
    }

    @Test
    public void nonStagingEnvironmentCanUseItsConfiguredDefault() {
        FrameworkConfig.validateEnvironmentBaseUrl("qa", false);
    }
}
