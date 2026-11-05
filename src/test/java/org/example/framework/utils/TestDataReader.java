package org.example.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads test data from src/test/resources/testdata/saucedemo.properties. */
public final class TestDataReader {
    private static final String DATA_FILE = "/testdata/saucedemo.properties";
    private static final Properties DATA = loadProperties();

    private TestDataReader() {
    }

    public static String get(String key) {
        String value = DATA.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing test data property: " + key);
        }
        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestDataReader.class.getResourceAsStream(DATA_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Test data resource not found: " + DATA_FILE);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
