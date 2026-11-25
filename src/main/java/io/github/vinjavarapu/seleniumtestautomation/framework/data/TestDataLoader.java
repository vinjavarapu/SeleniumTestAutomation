package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/** Loads test data from multiple file formats and converts rows to typed objects or data providers. */
public final class TestDataLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataLoader() {
    }

    /** Load data rows from a file, auto-detecting format by extension (csv, json, yaml, xlsx). */
    public static List<Map<String, String>> loadRows(String fileName) {
        TestDataSource source = selectSourceByExtension(fileName);
        return source.rows(fileName);
    }

    /** Load data rows and convert each to the specified class using Jackson mapping. */
    public static <T> List<T> load(String fileName, Class<T> targetClass) {
        List<Map<String, String>> rows = loadRows(fileName);
        return rows.stream()
                .map(row -> MAPPER.convertValue(row, targetClass))
                .toList();
    }

    /** Convert data rows to a TestNG data provider (Object[][]). */
    public static Object[][] toDataProvider(String fileName) {
        List<Map<String, String>> rows = loadRows(fileName);
        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            result[i][0] = rows.get(i);
        }
        return result;
    }

    /** Convert typed objects to a TestNG data provider (Object[][]). */
    public static <T> Object[][] toDataProvider(List<T> objects) {
        Object[][] result = new Object[objects.size()][1];
        for (int i = 0; i < objects.size(); i++) {
            result[i][0] = objects.get(i);
        }
        return result;
    }

    private static TestDataSource selectSourceByExtension(String fileName) {
        PathValidator.validateTestDataPath(fileName);
        String extension = getExtension(fileName).toLowerCase();
        return switch (extension) {
            case "csv" -> new CsvDataSource();
            case "json" -> new JsonDataSource();
            case "yaml", "yml" -> new YamlDataSource();
            case "xlsx", "xls" -> new ExcelDataSource();
            default -> throw new IllegalArgumentException("Unsupported file format: " + extension
                    + ". Supported formats: csv, json, yaml, xlsx");
        };
    }

    private static String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }
}
