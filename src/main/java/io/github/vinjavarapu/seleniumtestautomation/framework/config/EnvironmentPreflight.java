package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import io.github.vinjavarapu.seleniumtestautomation.framework.utils.UrlRedactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Fail-fast HTTP reachability check before starting browser sessions. */
public final class EnvironmentPreflight {
    private static final Logger LOGGER = LogManager.getLogger(EnvironmentPreflight.class);

    private EnvironmentPreflight() {
    }

    public static void verifyReachable(String targetUrl, Duration timeout) {
        Objects.requireNonNull(targetUrl, "targetUrl must not be null");
        Objects.requireNonNull(timeout, "timeout must not be null");
        URI uri = URI.create(targetUrl.trim());
        String redactedUrl = UrlRedactor.redact(targetUrl);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        try {
            int status = sendProbe(client, uri, timeout, "HEAD");
            if (status == 405) {
                status = sendProbe(client, uri, timeout, "GET");
            }
            if (status >= 500) {
                throw new IllegalStateException(
                        "Environment preflight failed for " + redactedUrl + " with HTTP status " + status);
            }
            LOGGER.info("Environment preflight passed for {} (HTTP {})", redactedUrl, status);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Environment preflight could not reach " + redactedUrl + ": " + exception.getMessage(),
                    exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Environment preflight interrupted for " + redactedUrl, exception);
        }
    }

    private static int sendProbe(HttpClient client, URI uri, Duration timeout, String method)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode();
    }
}
