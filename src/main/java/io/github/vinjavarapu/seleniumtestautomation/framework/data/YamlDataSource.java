package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads YAML test data files and returns rows as string maps. */
class YamlDataSource implements TestDataSource {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Override
    public List<Map<String, String>> rows(String fileName) {
        PathValidator.validateTestDataPath(fileName);
        String resource = "/testdata/" + fileName;
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("YAML test data resource not found: " + resource);
            }
            List<Map<String, Object>> parsed = MAPPER.readValue(input,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class));
            List<Map<String, String>> result = new ArrayList<>();
            for (Map<String, Object> row : parsed) {
                Map<String, String> stringRow = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    Object value = entry.getValue();
                    String stringValue = value == null ? "" : value.toString();
                    stringRow.put(entry.getKey(), EnvironmentVariableResolver.resolve(stringValue));
                }
                result.add(stringRow);
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read YAML file: " + fileName, exception);
        }
    }
}
