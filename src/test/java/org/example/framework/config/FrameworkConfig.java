package org.example.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Immutable process-level configuration. Run different environments in separate JVMs
 * (for example, separate CI matrix jobs); parallel TestNG methods within a JVM share
 * one environment configuration but get independent browser sessions.
 */
public final class FrameworkConfig {
    private static final String ENV = environmentFromSystemProperty();
    private static final String CONFIG_FILE = String.format("/config/%s.properties", ENV);
    private static final Properties DEFAULTS;
    private static final Properties ENV_CONFIG;

    static {
        DEFAULTS = loadResource("/config/default.properties");
        ENV_CONFIG = loadResource(CONFIG_FILE);
    }

    private FrameworkConfig() {
    }

    private static String environmentFromSystemProperty() {
        String environment = System.getProperty("env");
        if (environment == null || environment.isBlank()) {
            return "qa";
        }
        return environment.trim().toLowerCase(Locale.ROOT);
    }

    private static Properties loadResource(String resourcePath) {
        Properties props = new Properties();
        try (InputStream input = FrameworkConfig.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Configuration resource not found: " + resourcePath);
            }
            props.load(input);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(
                    "Could not load config resource: " + resourcePath);
        }
        return props;
    }

    private static String get(String key) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = ENV_CONFIG.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = DEFAULTS.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        throw new IllegalStateException("Missing config property: " + key);
    }

    private static String getOptional(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = ENV_CONFIG.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = DEFAULTS.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return defaultValue;
    }

    public static String baseUrl() {
        return get("baseUrl");
    }

    public static String browser() {
        return getOptional("browser", "chrome");
    }

    public static boolean headless() {
        String value = getOptional("headless", "true");
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean for headless: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public static long explicitWaitSeconds() {
        return positiveLong("explicitWaitSeconds", 10, true);
    }

    public static long implicitWaitSeconds() {
        return positiveLong("implicitWaitSeconds", 0, false);
    }

    public static int retryCount() {
        int retries = Integer.parseInt(getOptional("retryCount", "0"));
        if (retries < 0) {
            throw new IllegalArgumentException("retryCount must be zero or greater");
        }
        return retries;
    }

    public static String remoteUrl() {
        return getOptional("remoteUrl", "");
    }

    public static String reportName() {
        return getOptional("reportName", "Test Execution Report");
    }

    public static String environment() {
        return ENV;
    }

    private static long positiveLong(String key, long defaultValue, boolean allowZero) {
        long value = Long.parseLong(getOptional(key, Long.toString(defaultValue)));
        if (value < 0 || (!allowZero && value == 0)) {
            throw new IllegalArgumentException(key + (allowZero ? " must be zero or greater" : " must be greater than zero"));
        }
        return value;
    }
}
