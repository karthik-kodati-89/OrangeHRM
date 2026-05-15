package com.orangehrm.utils;

import com.orangehrm.exceptions.FrameworkException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads .xlsx test data using Apache POI.
 * Powers DATA-DRIVEN and HYBRID frameworks (TestNG @DataProvider feeds).
 */
public final class ExcelReader {

    private ExcelReader() {}

    public static Object[][] getSheetData(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new FrameworkException("Sheet not found: " + sheetName);
            }

            int lastRow = sheet.getLastRowNum();          // 0-based; data rows = lastRow
            int cols = sheet.getRow(0).getLastCellNum();  // header determines width

            Object[][] data = new Object[lastRow][cols];
            DataFormatter df = new DataFormatter();

            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                for (int c = 0; c < cols; c++) {
                    data[r - 1][c] = (row == null) ? "" : df.formatCellValue(row.getCell(c));
                }
            }
            return data;

        } catch (IOException e) {
            throw new FrameworkException("Failed reading Excel: " + filePath, e);
        }
    }

    /** Returns rows as List<String[]> - convenient for keyword-driven engines. */
    public static List<String[]> getSheetAsList(String filePath, String sheetName) {
        Object[][] raw = getSheetData(filePath, sheetName);
        List<String[]> list = new ArrayList<>();
        for (Object[] row : raw) {
            String[] strRow = new String[row.length];
            for (int i = 0; i < row.length; i++) strRow[i] = String.valueOf(row[i]);
            list.add(strRow);
        }
        return list;
    }
}
