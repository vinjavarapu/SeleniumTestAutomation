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
        String value = ConfigReader.getOptional("headless", "true");
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean for headless: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public static long explicitWaitSeconds() {
        long value = Long.parseLong(ConfigReader.getOptional("explicitWaitSeconds", "10"));
        if (value <= 0) throw new IllegalArgumentException("explicitWaitSeconds must be greater than zero");
        return value;
    }

    public static int retryCount() {
        int retries;
        try {
            retries = Integer.parseInt(ConfigReader.getOptional("retryCount", "0"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("retryCount must be a whole number", exception);
        }
        if (retries < 0) throw new IllegalArgumentException("retryCount must be zero or greater");
        return retries;
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
