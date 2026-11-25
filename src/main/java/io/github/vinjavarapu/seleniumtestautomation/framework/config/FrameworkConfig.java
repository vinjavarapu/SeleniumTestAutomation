package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import java.net.URI;
import java.util.Locale;

/** Typed access and startup validation for framework configuration. */
public final class FrameworkConfig {
    private FrameworkConfig() {
    }

    public static String baseUrl() {
        return ConfigReader.get("baseUrl");
    }

    public static String browser() {
        return ConfigReader.getOptional("browser", "chrome");
    }

    public static boolean headless() {
        return ConfigReader.getBoolean("headless", true);
    }

    public static long explicitWaitSeconds() {
        return ConfigReader.getLong("explicitWaitSeconds", 10, "positive");
    }

    public static int retryCount() {
        return ConfigReader.getInt("retryCount", 0, "non-negative");
    }

    public static String remoteUrl() {
        return ConfigReader.getOptional("remoteUrl", "");
    }

    public static String reportName() {
        return ConfigReader.getOptional("reportName", "Test Execution Report");
    }

    public static String environment() {
        return ConfigReader.environment();
    }

    public static boolean preflightCheck() {
        return ConfigReader.getBoolean("preflightCheck", false);
    }

    public static long preflightTimeoutSeconds() {
        return ConfigReader.getLong("preflightTimeoutSeconds", 10, "positive");
    }

    public static String testDataFile() {
        return ConfigReader.getOptional("testDataFile", "/testdata/saucedemo.properties");
    }

    public static void validate() {
        validateEnvironmentBaseUrl(environment(), ConfigReader.hasExplicitValue("baseUrl"));

        String browser = browser().toLowerCase(Locale.ROOT);
        if (!browser.equals("chrome") && !browser.equals("firefox") && !browser.equals("edge")) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. Choose chrome, firefox, or edge.");
        }
        validateHttpUrl("baseUrl", baseUrl());
        explicitWaitSeconds();
        retryCount();
        headless();
        preflightCheck();
        preflightTimeoutSeconds();

        String remote = remoteUrl();
        if (!remote.isBlank()) validateHttpUrl("remoteUrl", remote);
    }

    static void validateEnvironmentBaseUrl(String environment, boolean hasExplicitBaseUrl) {
        if ("staging".equalsIgnoreCase(environment) && !hasExplicitBaseUrl) {
            throw new IllegalStateException(
                    "Staging requires an explicit baseUrl via -DbaseUrl or the BASE_URL environment variable");
        }
    }

    private static void validateHttpUrl(String key, String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(key + " must be an absolute HTTP(S) URL with a host", exception);
        }
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || uri.getHost() == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException(key + " must be an absolute HTTP(S) URL with a host");
        }
    }
}
