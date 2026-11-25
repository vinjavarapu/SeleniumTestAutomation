package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TestDataLoaderTest {
    @Test(description = "Load CSV test data as row maps")
    public void loadCsvAsRowMaps() {
        List<Map<String, String>> rows = TestDataLoader.loadRows("rejected-logins.csv");
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() > 0);
        Map<String, String> firstRow = rows.get(0);
        Assert.assertTrue(firstRow.containsKey("username"));
        Assert.assertTrue(firstRow.containsKey("password"));
        Assert.assertTrue(firstRow.containsKey("expectedMessage"));
    }

    @Test(description = "Load JSON test data as row maps")
    public void loadJsonAsRowMaps() {
        List<Map<String, String>> rows = TestDataLoader.loadRows("rejected-logins.json");
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() > 0);
        Map<String, String> firstRow = rows.get(0);
        Assert.assertTrue(firstRow.containsKey("username"));
        Assert.assertEquals(firstRow.get("username"), "unknown-user");
    }

    @Test(description = "Load YAML test data as row maps")
    public void loadYamlAsRowMaps() {
        List<Map<String, String>> rows = TestDataLoader.loadRows("sort-orders.yaml");
        Assert.assertNotNull(rows);
        Assert.assertTrue(rows.size() > 0);
        Map<String, String> firstRow = rows.get(0);
        Assert.assertTrue(firstRow.containsKey("sort"));
        Assert.assertTrue(firstRow.containsKey("names"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, description = "Reject path traversal attempts")
    public void rejectPathTraversal() {
        TestDataLoader.loadRows("../../../etc/passwd");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, description = "Reject unsupported file extensions")
    public void rejectUnsupportedExtension() {
        TestDataLoader.loadRows("data.txt");
    }

    @Test(description = "Convert rows to TestNG data provider format")
    public void convertToDataProvider() {
        Object[][] provider = TestDataLoader.toDataProvider("rejected-logins.csv");
        Assert.assertNotNull(provider);
        Assert.assertTrue(provider.length > 0);
        Assert.assertEquals(provider[0].length, 1);
        Assert.assertTrue(provider[0][0] instanceof Map);
    }
}
