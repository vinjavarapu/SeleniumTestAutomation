package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads validated CSV fixtures for TestNG data providers. */
public final class CsvTestDataReader {
    private static final Pattern ENVIRONMENT_REFERENCE = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");

    private CsvTestDataReader() {
    }

    public static Object[][] read(String fileName, String... expectedHeaders) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..")
                || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("CSV fixture must be a file name under /testdata/: " + fileName);
        }
        if (expectedHeaders == null || expectedHeaders.length == 0) {
            throw new IllegalArgumentException("At least one expected CSV header is required");
        }

        String resource = "/testdata/" + fileName;
        try (InputStream input = CsvTestDataReader.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("CSV test data resource not found: " + resource);
            }
            List<List<String>> records = parse(new String(
                    input.readAllBytes(), StandardCharsets.UTF_8), resource);
            validateHeaders(records.get(0), expectedHeaders, resource);

            List<List<String>> data = records.subList(1, records.size());
            if (data.isEmpty()) {
                throw new IllegalStateException("CSV test data contains no rows: " + resource);
            }

            Object[][] result = new Object[data.size()][expectedHeaders.length];
            for (int row = 0; row < data.size(); row++) {
                List<String> values = data.get(row);
                if (values.size() != expectedHeaders.length) {
                    throw new IllegalStateException("CSV row " + (row + 2) + " in " + resource
                            + " has " + values.size() + " columns; expected " + expectedHeaders.length);
                }
                for (int column = 0; column < values.size(); column++) {
                    result[row][column] = resolveEnvironmentReferences(values.get(column), System::getenv,
                            resource + " row " + (row + 2) + " column " + expectedHeaders[column]);
                }
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read CSV test data: " + resource, exception);
        }
    }

    private static void validateHeaders(List<String> actual, String[] expected, String resource) {
        List<String> normalized = new ArrayList<>(actual);
        if (!normalized.isEmpty() && normalized.get(0).startsWith("\uFEFF")) {
            normalized.set(0, normalized.get(0).substring(1));
        }
        normalized.replaceAll(String::trim);
        if (!normalized.equals(Arrays.asList(expected))) {
            throw new IllegalStateException("Unexpected CSV headers in " + resource + ": " + normalized
                    + "; expected " + Arrays.asList(expected));
        }
    }

    static String resolveEnvironmentReferences(String value, Function<String, String> environmentLookup,
                                               String location) {
        Matcher matcher = ENVIRONMENT_REFERENCE.matcher(value);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String environmentName = matcher.group(1);
            String environmentValue = environmentLookup.apply(environmentName);
            if (environmentValue == null || environmentValue.isBlank()) {
                throw new IllegalStateException("Missing environment variable " + environmentName
                        + " referenced by " + location);
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(environmentValue));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private static List<List<String>> parse(String csv, String resource) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        int line = 1;

        for (int i = 0; i < csv.length(); i++) {
            char current = csv.charAt(i);
            if (quoted) {
                if (current == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(current);
                    if (current == '\n') line++;
                }
                continue;
            }

            if (closedQuote && current != ',' && current != '\r' && current != '\n') {
                throw malformed(resource, line, "unexpected character after a quoted field");
            }
            if (current == '"' && field.isEmpty() && !closedQuote) {
                quoted = true;
            } else if (current == '"') {
                throw malformed(resource, line, "quote inside an unquoted field");
            } else if (current == ',') {
                record.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (current == '\r' || current == '\n') {
                record.add(field.toString());
                addRecord(records, record);
                record = new ArrayList<>();
                field.setLength(0);
                closedQuote = false;
                if (current == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') i++;
                line++;
            } else {
                field.append(current);
            }
        }

        if (quoted) throw malformed(resource, line, "unterminated quoted field");
        if (!record.isEmpty() || !field.isEmpty() || closedQuote) {
            record.add(field.toString());
            addRecord(records, record);
        }
        if (records.isEmpty()) throw new IllegalStateException("CSV test data is empty: " + resource);
        return records;
    }

    private static void addRecord(List<List<String>> records, List<String> record) {
        if (record.size() == 1 && record.get(0).isBlank()) return;
        records.add(List.copyOf(record));
    }

    private static IllegalStateException malformed(String resource, int line, String reason) {
        return new IllegalStateException("Malformed CSV test data in " + resource + " at line " + line + ": "
                + reason);
    }
}
