package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class CsvTestDataReaderTest {
    @Test
    public void readsRowsFromRegressionDataFiles() {
        Assert.assertEquals(CsvTestDataReader.read("rejected-logins.csv",
                "username", "password", "expectedMessage").length, 6);
        Assert.assertEquals(CsvTestDataReader.read("sort-orders.csv",
                "sort", "names", "descending").length, 4);
        Assert.assertEquals(CsvTestDataReader.read("checkout-validation.csv",
                "firstName", "lastName", "postalCode", "expectedMessage").length, 4);
    }

    @Test
    public void parsesQuotedCommasAndEscapedQuotes() {
        Object[][] rows = CsvTestDataReader.read("csv-reader-fixture.csv", "name", "description");

        Assert.assertEquals(rows[0][0], "sample");
        Assert.assertEquals(rows[0][1], "comma, and \"quoted\" text");
    }

    @Test
    public void rejectsUnexpectedHeaders() {
        Assert.expectThrows(IllegalStateException.class,
                () -> CsvTestDataReader.read("sort-orders.csv", "unexpected", "names", "descending"));
    }

    @Test
    public void rejectsFixturePathTraversal() {
        Assert.expectThrows(IllegalArgumentException.class,
                () -> CsvTestDataReader.read("../secrets.csv", "value"));
    }

    @Test
    public void resolvesEnvironmentReferencesWithoutExposingMissingValues() {
        String resolved = CsvTestDataReader.resolveEnvironmentReferences(
                "${CLIENT_TEST_PASSWORD}", Map.of("CLIENT_TEST_PASSWORD", "private-value")::get, "fixture row");
        Assert.assertEquals(resolved, "private-value");

        IllegalStateException exception = Assert.expectThrows(IllegalStateException.class,
                () -> CsvTestDataReader.resolveEnvironmentReferences(
                        "${MISSING_TEST_SECRET}", ignored -> null, "fixture row"));
        Assert.assertTrue(exception.getMessage().contains("MISSING_TEST_SECRET"));
        Assert.assertFalse(exception.getMessage().contains("private-value"));
    }
}
