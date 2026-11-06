package org.example.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.example.framework.config.FrameworkConfig;

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
    private static ExtentReports extent;
    private static Path reportPath;

    private ExtentReportManager() {
    }

    private static synchronized ExtentReports getExtent() {
        if (extent == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            reportPath = Path.of("target", "extent-reports", "Test-Report-" + timestamp + ".html");
            try {
                Files.createDirectories(reportPath.getParent());
            } catch (Exception exception) {
                throw new IllegalStateException("Could not create the Extent report directory", exception);
            }
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath.toString());
            spark.config().setDocumentTitle("Test Automation Results");
            spark.config().setReportName(FrameworkConfig.reportName());
            spark.config().setTheme(Theme.DARK);
            spark.config().setJs(REPORT_SUMMARY_SCRIPT);
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework", "Selenium + Java + TestNG");
            extent.setSystemInfo("Environment", FrameworkConfig.environment());
            extent.setSystemInfo("Browser", FrameworkConfig.browser());
            extent.setSystemInfo("Headless", String.valueOf(FrameworkConfig.headless()));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    public static void startTest(String name, String description) {
        ExtentTest test = getExtent().createTest(name, description == null ? "" : description);
        CURRENT_TEST.set(test);
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

    public static void attachScreenshot(byte[] screenshot, String title) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null || screenshot == null) return;
        try {
            test.addScreenCaptureFromBase64String(
                    java.util.Base64.getEncoder().encodeToString(screenshot), title);
        } catch (Exception exception) {
            test.warning("Screenshot could not be attached: " + exception.getMessage());
        }
    }

    public static void skipped(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) test.skip(message);
    }

    public static synchronized void flush() {
        if (extent != null) extent.flush();
    }

    public static Path reportPath() {
        if (reportPath == null) {
            getExtent();
        }
        return reportPath;
    }

    public static void clearCurrentTest() {
        CURRENT_TEST.remove();
    }
}
