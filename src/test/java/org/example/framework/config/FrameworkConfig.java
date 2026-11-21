package org.example.framework.config;

/** Typed configuration access retained for framework callers. */
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
        return nonNegativeLong("explicitWaitSeconds", 10);
    }

    public static int retryCount() {
        int retries = Integer.parseInt(ConfigReader.getOptional("retryCount", "0"));
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

    private static long nonNegativeLong(String key, long defaultValue) {
        long value = Long.parseLong(ConfigReader.getOptional(key, Long.toString(defaultValue)));
        if (value < 0) throw new IllegalArgumentException(key + " must be zero or greater");
        return value;
    }
}
