package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/** Reads test data from runtime environment overrides or configured classpath property files. */
public final class TestDataReader {
    private static final Map<String, Properties> CACHE = new ConcurrentHashMap<>();

    private TestDataReader() {
    }

    public static String get(String key) {
        return get(FrameworkConfig.testDataFile(), key);
    }

    public static String get(String resourcePath, String key) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        Objects.requireNonNull(key, "key must not be null");
        String environmentKey = "TESTDATA_" + key.replaceAll("[^A-Za-z0-9]", "_").toUpperCase(Locale.ROOT);
        String value = System.getenv(environmentKey);
        if (value == null || value.isBlank()) {
            value = CACHE.computeIfAbsent(resourcePath, TestDataReader::loadProperties).getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing test data property: " + key);
        }
        return value.trim();
    }

    private static Properties loadProperties(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream input = TestDataReader.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Test data resource not found: " + resourcePath);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load test data resource: " + resourcePath, exception);
        }
    }
}
