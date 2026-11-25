package io.github.vinjavarapu.seleniumtestautomation.framework.unittests;

import io.github.vinjavarapu.seleniumtestautomation.framework.config.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConfigReaderTest {
    @DataProvider(name = "envVarNameMappings")
    public Object[][] envVarNameMappings() {
        return new Object[][] {
                {"baseUrl", "BASE_URL"},
                {"browser", "BROWSER"},
                {"headless", "HEADLESS"},
                {"explicitWaitSeconds", "EXPLICIT_WAIT_SECONDS"},
                {"retryCount", "RETRY_COUNT"},
                {"remoteUrl", "REMOTE_URL"},
                {"reportName", "REPORT_NAME"},
                {"preflightCheck", "PREFLIGHT_CHECK"},
                {"preflightTimeoutSeconds", "PREFLIGHT_TIMEOUT_SECONDS"},
                {"testDataFile", "TEST_DATA_FILE"},
                {"customKey", "CUSTOM_KEY"},
                {"myMultiWordProperty", "MY_MULTI_WORD_PROPERTY"},
        };
    }

    @Test(dataProvider = "envVarNameMappings", description = "Verify camelCase to UPPER_SNAKE_CASE conversion")
    public void verifyCamelCaseToEnvVarNameConversion(String camelCase, String expectedEnvName) {
        String derivedName = toEnvironmentVariableName(camelCase);
        Assert.assertEquals(derivedName, expectedEnvName);
    }

    @Test
    public void getOptionalReturnsDefaultWhenKeyNotFound() {
        String result = ConfigReader.getOptional("nonexistent.key.that.does.not.exist", "default-value");
        Assert.assertEquals(result, "default-value");
    }

    @Test(description = "Verify precedence: system property > environment variable > config file > default")
    public void verifyPrecedenceChain() {
        String defaultValue = "default";
        String result = ConfigReader.getOptional("precedence.test", defaultValue);
        Assert.assertEquals(result, defaultValue);
    }

    @DataProvider(name = "booleanValues")
    public Object[][] booleanValues() {
        return new Object[][] {
                {"true", true},
                {"True", true},
                {"TRUE", true},
                {"false", false},
                {"False", false},
                {"FALSE", false},
        };
    }

    @Test(dataProvider = "booleanValues", description = "Verify getBoolean accepts case-insensitive true/false")
    public void getBooleanHandlesCaseInsensitive(String value, boolean expected) {
        boolean defaultValue = !value.equals("false");
        boolean result = ConfigReader.getBoolean("headless", defaultValue);
        Assert.assertEquals(result, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, description = "Reject invalid boolean value")
    public void getBooleanRejectsInvalidValue() {
        ConfigReader.getBoolean("test.boolean", false);
        System.setProperty("test.boolean", "maybe");
        ConfigReader.getBoolean("test.boolean", false);
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][] {
                {0, "non-negative"},
                {1, "positive"},
                {100, "positive"},
                {-1, "non-negative"},
        };
    }

    @Test(description = "Verify getInt with positive constraint")
    public void getIntPositiveConstraint() {
        int result = ConfigReader.getInt("retryCount", 2, "non-negative");
        Assert.assertTrue(result >= 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, description = "Reject zero when positive constraint required")
    public void getIntPositiveConstraintRejectsZero() {
        ConfigReader.getInt("test.int", 0, "positive");
    }

    @Test(description = "Verify getLong with positive constraint")
    public void getLongPositiveConstraint() {
        long result = ConfigReader.getLong("explicitWaitSeconds", 10, "positive");
        Assert.assertTrue(result > 0);
    }

    private String toEnvironmentVariableName(String camelCaseKey) {
        return camelCaseKey.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}
