package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Captures page source and supported browser console logs on test failure. */
public final class BrowserDiagnostics {
    private static final Logger LOGGER = LogManager.getLogger(BrowserDiagnostics.class);
    private static final Path DIAGNOSTICS_DIR = Path.of("target", "diagnostics");

    private BrowserDiagnostics() {
    }

    public static List<Path> captureFailureArtifacts(WebDriver driver, String artifactPrefix) {
        List<Path> artifacts = new ArrayList<>();
        if (driver == null) {
            return artifacts;
        }
        try {
            Files.createDirectories(DIAGNOSTICS_DIR);
            Path pageSource = capturePageSource(driver, artifactPrefix);
            if (pageSource != null) {
                artifacts.add(pageSource);
            }
            Path consoleLogs = captureConsoleLogs(driver, artifactPrefix);
            if (consoleLogs != null) {
                artifacts.add(consoleLogs);
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not prepare diagnostics directory", exception);
        }
        return artifacts;
    }

    private static Path capturePageSource(WebDriver driver, String artifactPrefix) {
        try {
            String source = driver.getPageSource();
            if (source == null || source.isBlank()) {
                return null;
            }
            Path target = DIAGNOSTICS_DIR.resolve(artifactPrefix + "-dom.html");
            Files.writeString(target, source, StandardCharsets.UTF_8);
            LOGGER.info("Saved failure DOM snapshot to {}", target.toAbsolutePath());
            return target;
        } catch (IOException | WebDriverException exception) {
            LOGGER.debug("Could not capture failure page source: {}", exception.getMessage());
            return null;
        }
    }

    private static Path captureConsoleLogs(WebDriver driver, String artifactPrefix) {
        try {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            List<String> lines = new ArrayList<>();
            for (LogEntry entry : entries) {
                lines.add(Instant.ofEpochMilli(entry.getTimestamp())
                        + " [" + entry.getLevel() + "] " + entry.getMessage());
            }
            if (lines.isEmpty()) {
                return null;
            }
            Path target = DIAGNOSTICS_DIR.resolve(artifactPrefix + "-console.log");
            Files.write(target, lines, StandardCharsets.UTF_8);
            LOGGER.info("Saved browser console logs to {}", target.toAbsolutePath());
            return target;
        } catch (IOException | WebDriverException exception) {
            LOGGER.debug("Browser console log capture unavailable for current driver: {}", exception.getMessage());
            return null;
        }
    }
}
