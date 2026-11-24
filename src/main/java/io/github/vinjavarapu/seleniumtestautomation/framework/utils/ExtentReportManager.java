package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import io.github.vinjavarapu.seleniumtestautomation.framework.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Creates the HTML report and stores the current test for each TestNG thread. */
public final class ExtentReportManager {
    private static final String REPORT_SUMMARY_SCRIPT = """
            (function () {
              function addRunSummary() {
                var header = document.querySelector('.header .nav-right');
                var tests = document.querySelectorAll('.test-list-item .test-item');
                if (!header || tests.length === 0) return false;

                var passed = document.querySelectorAll('.test-item[status="pass"]').length;
                var failed = document.querySelectorAll('.test-item[status="fail"]').length;
                var skipped = document.querySelectorAll('.test-item[status="skip"]').length;
                var summary = document.getElementById('run-summary');
                if (!summary) {
                  summary = document.createElement('li');
                  summary.id = 'run-summary';
                  summary.className = 'm-r-10';
                  var badge = document.createElement('span');
                  badge.className = 'badge badge-primary';
                  badge.style.cssText = 'font-size:12px;padding:7px 10px;background:#263238;color:#fff';
                  summary.appendChild(badge);
                  header.insertBefore(summary, header.firstChild);
                }
                summary.firstChild.textContent = 'TOTAL: ' + tests.length +
                  '  |  PASS: ' + passed + '  |  FAIL: ' + failed + '  |  SKIP: ' + skipped;
                return true;
              }

              function start() {
                if (addRunSummary()) return;
                var attempts = 0;
                var timer = window.setInterval(function () {
                  attempts++;
                  if (addRunSummary() || attempts >= 20) window.clearInterval(timer);
                }, 100);
              }

              if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', start);
              } else {
                start();
              }
            })();
            """;
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final Object LOCK = new Object();
    private static ExtentReports extent;
    private static Path reportPath;

    private ExtentReportManager() {
    }

    private static ExtentReports getExtent() {
        synchronized (LOCK) {
            if (extent == null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String runId = System.getProperty("runId", "local").replaceAll("[^a-zA-Z0-9._-]", "_");
                String environment = FrameworkConfig.environment().replaceAll("[^a-zA-Z0-9._-]", "_");
                String browser = FrameworkConfig.browser().replaceAll("[^a-zA-Z0-9._-]", "_");
                reportPath = Path.of("target", "extent-reports",
                        "Test-Report-" + environment + "-" + browser + "-" + runId + "-" + timestamp + ".html");
                try {
                    Path parent = reportPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException("Could not create the Extent report directory", exception);
                }
                ExtentSparkReporter spark = new ExtentSparkReporter(reportPath.toString());
                spark.config().setDocumentTitle("Test Automation Results");
                spark.config().setReportName(FrameworkConfig.reportName());
                spark.config().setTheme(Theme.DARK);
                spark.config().setJs(REPORT_SUMMARY_SCRIPT);
                spark.config().setTimelineEnabled(true);
                extent = new ExtentReports();
                extent.attachReporter(spark);
                extent.setSystemInfo("Framework", "Selenium + Java + TestNG");
                extent.setSystemInfo("Environment", FrameworkConfig.environment());
                extent.setSystemInfo("Application URL", UrlRedactor.redact(FrameworkConfig.baseUrl()));
                extent.setSystemInfo("Browser", FrameworkConfig.browser());
                extent.setSystemInfo("Browser Version", browserVersion());
                extent.setSystemInfo("Headless", String.valueOf(FrameworkConfig.headless()));
                extent.setSystemInfo("OS Name", System.getProperty("os.name", "Unknown"));
                extent.setSystemInfo("OS Version", System.getProperty("os.version", "Unknown"));
                extent.setSystemInfo("Java Version", System.getProperty("java.version", "Unknown"));
                extent.setSystemInfo("Executed By", System.getProperty("user.name", "Unknown"));
            }
            return extent;
        }
    }

    public static void startTest(String name, String description, String... categories) {
        synchronized (LOCK) {
            ExtentTest test = getExtent().createTest(name, description == null ? "" : description);
            if (categories != null && categories.length > 0) test.assignCategory(categories);
            CURRENT_TEST.set(test);
        }
    }

    public static void step(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) test.info(message);
    }

    public static void pass(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) test.pass(message);
    }

    public static void fail(Throwable failure) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) return;
        test.fail(failure);
    }

    public static void attachScreenshot(String screenshot, String title) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null || screenshot == null || screenshot.isBlank()) return;
        test.info(MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot, title).build());
    }

    public static void skipped(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) test.skip(message);
    }

    public static void flush() {
        synchronized (LOCK) {
            if (extent != null) extent.flush();
        }
    }

    public static Path reportPath() {
        synchronized (LOCK) {
            if (reportPath == null) {
                getExtent();
            }
            return reportPath;
        }
    }

    public static void clearCurrentTest() {
        CURRENT_TEST.remove();
    }

    private static String browserVersion() {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver instanceof RemoteWebDriver remoteDriver) {
                return remoteDriver.getCapabilities().getBrowserVersion();
            }
        } catch (IllegalStateException exception) {
            return "Not available";
        }
        return "Not available";
    }
}
