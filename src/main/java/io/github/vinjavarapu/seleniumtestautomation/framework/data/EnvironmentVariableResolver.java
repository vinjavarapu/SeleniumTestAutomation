package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves ${VAR_NAME} environment variable references in test data. */
final class EnvironmentVariableResolver {
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([A-Z_]+)}");

    private EnvironmentVariableResolver() {
    }

    static String resolve(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String envValue = System.getenv(varName);
            if (envValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
