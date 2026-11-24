package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/** Removes common credential material before URLs are written to reports or logs. */
public final class UrlRedactor {
    private static final Pattern SENSITIVE_PARAMETER = Pattern.compile(
            ".*(password|secret|token|credential|authorization|api[_-]?key|access[_-]?key|session|signature).*",
            Pattern.CASE_INSENSITIVE);
    private static final String REDACTED = URLEncoder.encode("[REDACTED]", StandardCharsets.UTF_8);

    private UrlRedactor() {
    }

    public static String redact(String value) {
        if (value == null || value.isBlank()) return value;

        try {
            URI uri = URI.create(value.trim());
            String authority = uri.getRawAuthority();
            String scheme = uri.getScheme();
            if (authority == null || uri.getHost() == null || scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return "[invalid URL]";
            }

            int userInfoEnd = authority.lastIndexOf('@');
            if (userInfoEnd >= 0) authority = authority.substring(userInfoEnd + 1);

            StringBuilder result = new StringBuilder(scheme.toLowerCase(Locale.ROOT))
                    .append("://")
                    .append(authority)
                    .append(uri.getRawPath() == null ? "" : uri.getRawPath());

            String query = redactQuery(uri.getRawQuery());
            if (query != null) result.append('?').append(query);
            if (uri.getRawFragment() != null) result.append("#[REDACTED]");
            return result.toString();
        } catch (IllegalArgumentException exception) {
            return "[invalid URL]";
        }
    }

    private static String redactQuery(String query) {
        if (query == null) return null;

        String[] parameters = query.split("&", -1);
        for (int i = 0; i < parameters.length; i++) {
            int separator = parameters[i].indexOf('=');
            String key = separator < 0 ? parameters[i] : parameters[i].substring(0, separator);
            String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
            if (SENSITIVE_PARAMETER.matcher(decodedKey).matches()) {
                parameters[i] = key + "=" + REDACTED;
            }
        }
        return String.join("&", parameters);
    }
}
