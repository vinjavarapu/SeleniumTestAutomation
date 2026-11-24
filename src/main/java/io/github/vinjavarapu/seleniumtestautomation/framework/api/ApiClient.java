package io.github.vinjavarapu.seleniumtestautomation.framework.api;

import io.github.vinjavarapu.seleniumtestautomation.framework.utils.UrlRedactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/** Lightweight REST helper for API-backed test setup, auth seeding, and cleanup. */
public final class ApiClient {
    private static final Logger LOGGER = LogManager.getLogger(ApiClient.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private final URI baseUri;
    private final Duration timeout;
    private final HttpClient httpClient;

    private ApiClient(URI baseUri, Duration timeout) {
        this.baseUri = baseUri;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static ApiClient forBaseUrl(String baseUrl) {
        return forBaseUrl(baseUrl, DEFAULT_TIMEOUT);
    }

    public static ApiClient forBaseUrl(String baseUrl, Duration timeout) {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        String normalized = baseUrl.trim();
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        URI uri = URI.create(normalized);
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || uri.getHost() == null || scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("baseUrl must be an absolute HTTP(S) URL with a host");
        }
        return new ApiClient(uri, timeout);
    }

    public ApiResponse get(String path) {
        return get(path, Map.of());
    }

    public ApiResponse get(String path, Map<String, String> headers) {
        return send("GET", path, null, headers);
    }

    public ApiResponse postJson(String path, String jsonBody, Map<String, String> headers) {
        Objects.requireNonNull(jsonBody, "jsonBody must not be null");
        return send("POST", path, jsonBody, headers);
    }

    public ApiResponse putJson(String path, String jsonBody, Map<String, String> headers) {
        Objects.requireNonNull(jsonBody, "jsonBody must not be null");
        return send("PUT", path, jsonBody, headers);
    }

    public ApiResponse delete(String path, Map<String, String> headers) {
        return send("DELETE", path, null, headers);
    }

    private ApiResponse send(String method, String path, String jsonBody, Map<String, String> headers) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(headers, "headers must not be null");

        String relative = path.startsWith("/") ? path.substring(1) : path;
        URI targetUri = baseUri.resolve(relative);

        HttpRequest.Builder builder = HttpRequest.newBuilder(targetUri).timeout(timeout);
        if (jsonBody != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        headers.forEach(builder::header);

        String redactedTarget = UrlRedactor.redact(targetUri.toString());
        LOGGER.debug("Sending {} {}", method, redactedTarget);
        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(response.statusCode(), response.body(), response.headers());
        } catch (IOException exception) {
            throw new IllegalStateException("API request failed for " + method + " " + redactedTarget, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("API request interrupted for " + method + " " + redactedTarget, exception);
        }
    }

    /** Immutable HTTP response returned by {@link ApiClient}. */
    public record ApiResponse(int statusCode, String body, HttpHeaders headers) {
        public ApiResponse requireStatus(int expectedStatus) {
            if (statusCode != expectedStatus) {
                throw new IllegalStateException(
                        "Expected HTTP status " + expectedStatus + " but received " + statusCode);
            }
            return this;
        }

        public ApiResponse requireSuccess() {
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException("Expected 2xx HTTP status but received " + statusCode);
            }
            return this;
        }
    }
}
