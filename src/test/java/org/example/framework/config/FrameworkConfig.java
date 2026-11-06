package org.example.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Typed configuration singleton. Loads from env-specific properties files with system property overrides.
 * Precedence: system property > env file > default.
 */
public final class FrameworkConfig {
    private static final String ENV = System.getProperty("env", "qa");
    private static final String CONFIG_FILE = String.format("/config/%s.properties", ENV);
    private static final Properties DEFAULTS;
    private static final Properties ENV_CONFIG;

    static {
        DEFAULTS = loadResource("/config/default.properties");
        ENV_CONFIG = loadResource(CONFIG_FILE);
    }

    private FrameworkConfig() {
    }

    private static Properties loadResource(String resourcePath) {
        Properties props = new Properties();
        try (InputStream input = FrameworkConfig.class.getResourceAsStream(resourcePath)) {
            if (input != null) {
                props.load(input);
            }
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
        return Boolean.parseBoolean(getOptional("headless", "true"));
    }

    public static long explicitWaitSeconds() {
        return Long.parseLong(getOptional("explicitWaitSeconds", "10"));
    }

    public static long implicitWaitSeconds() {
        return Long.parseLong(getOptional("implicitWaitSeconds", "0"));
    }

    public static int retryCount() {
        return Integer.parseInt(getOptional("retryCount", "1"));
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
}
