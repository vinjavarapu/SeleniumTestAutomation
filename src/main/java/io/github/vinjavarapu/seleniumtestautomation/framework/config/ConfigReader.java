package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
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

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = getOptional(key, String.valueOf(defaultValue));
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean for '" + key + "': " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue, String constraint) {
        String value = getOptional(key, String.valueOf(defaultValue));
        int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer for '" + key + "': " + value, exception);
        }
        validateIntConstraint(key, intValue, constraint);
        return intValue;
    }

    public static long getLong(String key, long defaultValue, String constraint) {
        String value = getOptional(key, String.valueOf(defaultValue));
        long longValue;
        try {
            longValue = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid long for '" + key + "': " + value, exception);
        }
        validateLongConstraint(key, longValue, constraint);
        return longValue;
    }

    public static Duration getDuration(String key, Duration defaultValue, String constraint) {
        String value = getOptional(key, String.valueOf(defaultValue.getSeconds()));
        long seconds;
        try {
            seconds = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid duration (seconds) for '" + key + "': " + value, exception);
        }
        Duration duration = Duration.ofSeconds(seconds);
        validateDurationConstraint(key, duration, constraint);
        return duration;
    }

    private static void validateIntConstraint(String key, int value, String constraint) {
        if ("positive".equals(constraint) && value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero, but was: " + value);
        } else if ("non-negative".equals(constraint) && value < 0) {
            throw new IllegalArgumentException(key + " must be zero or greater, but was: " + value);
        }
    }

    private static void validateLongConstraint(String key, long value, String constraint) {
        if ("positive".equals(constraint) && value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero, but was: " + value);
        } else if ("non-negative".equals(constraint) && value < 0) {
            throw new IllegalArgumentException(key + " must be zero or greater, but was: " + value);
        }
    }

    private static void validateDurationConstraint(String key, Duration value, String constraint) {
        if ("positive".equals(constraint) && value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException(key + " must be greater than zero, but was: " + value.getSeconds() + "s");
        } else if ("non-negative".equals(constraint) && value.isNegative()) {
            throw new IllegalArgumentException(key + " must be zero or greater, but was: " + value.getSeconds() + "s");
        }
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
        String envName = toEnvironmentVariableName(key);
        return System.getenv(envName);
    }

    private static String toEnvironmentVariableName(String camelCaseKey) {
        return camelCaseKey.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }
}
