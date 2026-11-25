package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import java.util.List;
import java.util.Map;

/** Abstraction for reading test data from various file formats. */
public interface TestDataSource {
    /** Read all data rows from the specified file name. Each row is a string map of column names to values. */
    List<Map<String, String>> rows(String fileName);
}
