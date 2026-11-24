package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Loads immutable test configuration with environment and JVM overrides. */
public final class ConfigReader {
    private static final String ENVIRONMENT = environmentFromSystemProperty();
    private static final Properties DEFAULTS = loadRequired("/config.properties");
    private static final Properties ENVIRONMENT_OVERRIDES = loadRequired(
            "/config/" + ENVIRONMENT + ".properties");

    private ConfigReader() {
    }

    public static String environment() {
        return ENVIRONMENT;
    }

    static boolean hasExplicitValue(String key) {
        return isSet(System.getProperty(key)) || isSet(environmentValue(key));
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (isSet(value)) return value.trim();

        value = environmentValue(key);
        if (isSet(value)) return value.trim();

        value = ENVIRONMENT_OVERRIDES.getProperty(key);
        if (isSet(value)) return value.trim();

        value = DEFAULTS.getProperty(key);
        if (isSet(value)) return value.trim();

        throw new IllegalStateException("Missing configuration property: " + key);
    }

    public static String getOptional(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (isSet(value)) return value.trim();

        value = environmentValue(key);
        if (isSet(value)) return value.trim();

        value = ENVIRONMENT_OVERRIDES.getProperty(key);
        if (isSet(value)) return value.trim();

        value = DEFAULTS.getProperty(key);
        return isSet(value) ? value.trim() : defaultValue;
    }

    private static String environmentFromSystemProperty() {
        String value = System.getProperty("env");
        return isSet(value) ? value.trim().toLowerCase(Locale.ROOT) : "qa";
    }

    private static Properties loadRequired(String resource) {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Configuration resource not found: " + resource);
            }
            properties.load(input);
            if (properties.isEmpty()) {
                throw new IllegalStateException("Configuration resource is empty: " + resource);
            }
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load configuration resource: " + resource, exception);
        }
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    private static String environmentValue(String key) {
        Map<String, String> keyNames = Map.of(
                "baseUrl", "BASE_URL",
                "browser", "BROWSER",
                "headless", "HEADLESS",
                "explicitWaitSeconds", "EXPLICIT_WAIT_SECONDS",
                "retryCount", "RETRY_COUNT",
                "remoteUrl", "REMOTE_URL",
                "reportName", "REPORT_NAME",
                "preflightCheck", "PREFLIGHT_CHECK",
                "preflightTimeoutSeconds", "PREFLIGHT_TIMEOUT_SECONDS",
                "testDataFile", "TEST_DATA_FILE"
        );
        String environmentName = keyNames.get(key);
        return environmentName == null ? null : System.getenv(environmentName);
    }
}
