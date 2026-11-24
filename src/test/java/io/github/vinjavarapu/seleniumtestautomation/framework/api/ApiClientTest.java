package io.github.vinjavarapu.seleniumtestautomation.framework.api;

import com.sun.net.httpserver.HttpServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClientTest {
    private HttpServer server;
    private ApiClient client;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/items", exchange -> {
            byte[] body = exchange.getRequestBody().readAllBytes();
            String method = exchange.getRequestMethod();
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            String responseBody = method + ":" + new String(body, StandardCharsets.UTF_8)
                    + (token == null ? "" : "|auth=" + token);
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            int status = "POST".equals(method) ? 201 : 200;
            exchange.sendResponseHeaders(status, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();
        client = ApiClient.forBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void sendsGetPostPutAndDeleteRequests() {
        ApiClient.ApiResponse getResponse = client.get("/items", Map.of("Authorization", "Bearer test-token"))
                .requireStatus(200);
        Assert.assertEquals(getResponse.body(), "GET:|auth=Bearer test-token");

        ApiClient.ApiResponse postResponse = client.postJson("/items", "{\"name\":\"widget\"}", Map.of())
                .requireStatus(201)
                .requireSuccess();
        Assert.assertEquals(postResponse.body(), "POST:{\"name\":\"widget\"}");

        ApiClient.ApiResponse putResponse = client.putJson("/items", "{\"name\":\"updated\"}", Map.of())
                .requireSuccess();
        Assert.assertEquals(putResponse.body(), "PUT:{\"name\":\"updated\"}");

        ApiClient.ApiResponse deleteResponse = client.delete("/items", Map.of()).requireStatus(200);
        Assert.assertEquals(deleteResponse.body(), "DELETE:");
    }

    @Test
    public void requireSuccessRejectsServerErrors() {
        ApiClient.ApiResponse response = client.get("/error");
        Assert.expectThrows(IllegalStateException.class, response::requireSuccess);
    }

    @Test
    public void rejectsInvalidBaseUrl() {
        Assert.expectThrows(IllegalArgumentException.class, () -> ApiClient.forBaseUrl("ftp://example.test"));
    }
}
