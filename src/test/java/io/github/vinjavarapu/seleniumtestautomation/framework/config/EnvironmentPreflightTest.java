package io.github.vinjavarapu.seleniumtestautomation.framework.config;

import com.sun.net.httpserver.HttpServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

public class EnvironmentPreflightTest {
    private HttpServer server;
    private String baseUrl;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ok", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/get-only", exchange -> {
            int status = "HEAD".equalsIgnoreCase(exchange.getRequestMethod()) ? 405 : 200;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.createContext("/down", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void passesWhenTargetRespondsOk() {
        EnvironmentPreflight.verifyReachable(baseUrl + "/ok", Duration.ofSeconds(5));
    }

    @Test
    public void fallsBackToGetWhenHeadReturnsMethodNotAllowed() {
        EnvironmentPreflight.verifyReachable(baseUrl + "/get-only", Duration.ofSeconds(5));
    }

    @Test
    public void failsFastOnServerErrorStatus() {
        Assert.expectThrows(IllegalStateException.class,
                () -> EnvironmentPreflight.verifyReachable(baseUrl + "/down", Duration.ofSeconds(5)));
    }
}
