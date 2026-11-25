package io.github.vinjavarapu.seleniumtestautomation.framework.data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads Excel test data files (XLSX) and returns rows as string maps from the first sheet. */
class ExcelDataSource implements TestDataSource {
    @Override
    public List<Map<String, String>> rows(String fileName) {
        PathValidator.validateTestDataPath(fileName);
        String resource = "/testdata/" + fileName;
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Excel test data resource not found: " + resource);
            }
            Workbook workbook = WorkbookFactory.create(input);
            Sheet sheet = workbook.getSheetAt(0);
            List<Map<String, String>> result = new ArrayList<>();

            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalStateException("Excel file is empty: " + fileName);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalStateException("Excel file has no header row: " + fileName);
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                headers.add(cell != null ? getCellValueAsString(cell) : "");
            }

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String value = cell != null ? getCellValueAsString(cell) : "";
                    rowMap.put(headers.get(j), EnvironmentVariableResolver.resolve(value));
                }
                result.add(rowMap);
            }
            workbook.close();
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read Excel file: " + fileName, exception);
        }
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> cell.toString();
        };
    }
}
