package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads CSV test data files and returns rows as string maps. */
class CsvDataSource implements TestDataSource {
    @Override
    public List<Map<String, String>> rows(String fileName) {
        PathValidator.validateTestDataPath(fileName);
        String resource = "/testdata/" + fileName;
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("CSV test data resource not found: " + resource);
            }
            String csv = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return parseCsv(csv, fileName);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV file: " + fileName, exception);
        }
    }

    private List<Map<String, String>> parseCsv(String csv, String fileName) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < csv.length(); i++) {
            char current = csv.charAt(i);
            if (current == '"') {
                quoted = !quoted;
            } else if (current == ',' && !quoted) {
                record.add(field.toString().trim());
                field = new StringBuilder();
            } else if ((current == '\n' || current == '\r') && !quoted) {
                record.add(field.toString().trim());
                if (!record.isEmpty() && !record.stream().allMatch(String::isEmpty)) {
                    records.add(new ArrayList<>(record));
                }
                record.clear();
                field = new StringBuilder();
                if (current == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                field.append(current);
            }
        }
        if (!field.isEmpty() || !record.isEmpty()) {
            record.add(field.toString().trim());
            if (!record.stream().allMatch(String::isEmpty)) {
                records.add(new ArrayList<>(record));
            }
        }

        if (records.isEmpty()) {
            throw new IllegalStateException("CSV file is empty: " + fileName);
        }

        List<String> headers = records.get(0);
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> row = records.get(i);
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                String value = j < row.size() ? row.get(j) : "";
                rowMap.put(headers.get(j), EnvironmentVariableResolver.resolve(value));
            }
            result.add(rowMap);
        }
        return result;
    }
}
