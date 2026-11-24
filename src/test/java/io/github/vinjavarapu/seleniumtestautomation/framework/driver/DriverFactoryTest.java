package io.github.vinjavarapu.seleniumtestautomation.framework.driver;

import org.openqa.selenium.edge.EdgeOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class DriverFactoryTest {
    @Test
    public void addsLinuxContainerArgumentsForHeadlessEdge() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");

            EdgeOptions options = DriverFactory.createEdgeOptions(true);
            List<?> arguments = edgeArguments(options);

            Assert.assertTrue(arguments.contains("--no-sandbox"));
            Assert.assertTrue(arguments.contains("--disable-dev-shm-usage"));
            Assert.assertTrue(arguments.contains("--headless=new"));
        } finally {
            if (originalOsName == null) {
                System.clearProperty("os.name");
            } else {
                System.setProperty("os.name", originalOsName);
            }
        }
    }

    @Test
    public void doesNotAddLinuxContainerArgumentsForHeadedEdge() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");

            EdgeOptions options = DriverFactory.createEdgeOptions(false);
            List<?> arguments = edgeArguments(options);

            Assert.assertFalse(arguments.contains("--no-sandbox"));
            Assert.assertFalse(arguments.contains("--disable-dev-shm-usage"));
        } finally {
            if (originalOsName == null) {
                System.clearProperty("os.name");
            } else {
                System.setProperty("os.name", originalOsName);
            }
        }
    }

    private List<?> edgeArguments(EdgeOptions options) {
        Object capability = options.getCapability("ms:edgeOptions");
        if (!(capability instanceof Map<?, ?> edgeOptions)) {
            throw new AssertionError("Edge options capability is missing");
        }
        Object arguments = edgeOptions.get("args");
        if (!(arguments instanceof List<?> argumentList)) {
            throw new AssertionError("Edge arguments are missing");
        }
        return argumentList;
    }
}
