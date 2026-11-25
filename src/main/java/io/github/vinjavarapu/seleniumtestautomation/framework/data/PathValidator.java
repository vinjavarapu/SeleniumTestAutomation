package io.github.vinjavarapu.seleniumtestautomation.framework.data;

/** Validates test data file paths for safety. */
final class PathValidator {
    private PathValidator() {
    }

    static void validateTestDataPath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        if (filePath.contains("..") || filePath.contains("/") || filePath.contains("\\")) {
            throw new IllegalArgumentException(
                    "Invalid path traversal detected in test data file: " + filePath);
        }
    }
}
