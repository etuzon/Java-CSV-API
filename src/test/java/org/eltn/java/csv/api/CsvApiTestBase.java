package org.eltn.java.csv.api;

import java.io.IOException;
import java.util.List;

import org.eltn.java.csv.enums.CellsSplitterEnum;
import org.eltn.projects.core.expections.InvalidValueException;
import org.eltn.projects.core.tests.asserts.SoftAssert;
import org.eltn.projects.core.tests.base.BaseTest;
import org.eltn.projects.core.tests.exceptions.AutomationTestException;
import org.eltn.projects.core.utils.ListUtil;
import org.eltn.projects.core.utils.StringUtil;

public abstract class CsvApiTestBase extends BaseTest {
    protected enum GetRowEnum {
        GET_ROW, GET_ROWS
    }
   
    public static final String CELL_LOCATION_STR_TEMPLATE_BASE = "ell in row [" + StringUtil.REPLACE_STR + "], column ["
            + StringUtil.REPLACE_STR + "] in CSV file [" + StringUtil.REPLACE_STR + "]";
    public static final String CELL_LOCATION_START_WITH_CAPITAL_STR_TEMPLATE = "C" + CELL_LOCATION_STR_TEMPLATE_BASE;
    public static final String CELL_LOCATION_STR_TEMPLATE = "c" + CELL_LOCATION_STR_TEMPLATE_BASE;
    
    public CsvApiTestBase() {
    }
    
    protected void verifyCsv(String csvPath, String[] expectedCsvHeaders, String[][] expectedCsvBody,
            CellsSplitterEnum cellSplitter, GetRowEnum getRowEnum) throws AutomationTestException {
        try {
            CsvReaderApi csv = readCsvFile(csvPath, cellSplitter);
            verifyCsvHeaders(csv, expectedCsvHeaders);
            verifyCsvBody(csv, expectedCsvBody, getRowEnum);
        } finally {
            SoftAssert.assertAll();
        }
    }
   
    protected void verifyCsv(String csvPath, String[] expectedCsvHeaders, String[][] expectedCsvBody,
            GetRowEnum getRowEnum) throws AutomationTestException {
        try {
            CsvReaderApi csv = readCsvFile(csvPath);
            verifyCsvHeaders(csv, expectedCsvHeaders);
            verifyCsvBody(csv, expectedCsvBody, getRowEnum);
        } finally {
            SoftAssert.assertAll();
        }
    }

    private void verifyCsvBody(CsvReaderApi csv, String[][] expectedBody, GetRowEnum getRowEnum)
            throws AutomationTestException {
        try {
            for (int i = 0; i < expectedBody.length; i++) {
                List<String> currentRow = getRow(i, getRowEnum, csv);

                if (SoftAssert.assertTrue(expectedBody[i].length == currentRow.size(),
                        "Row number [" + (i + 1) + "] size in csv file [" + csv.getPath() + "] is [" + currentRow.size()
                                + "] and should be [" + expectedBody[i].length + "]")) {
                    verifyCsvLine(currentRow, ListUtil.asList(expectedBody[i]), i, csv.getPath());
                }
            }
        } catch (Exception e) {
            throw new AutomationTestException(e);
        }
    }
    
    protected void verifyCsvHeaders(CsvReaderApi csv, String[] expectedHeaders) {
        final int EXPECTED_HEADER_AMOUNT = expectedHeaders.length;
        SoftAssert.assertTrueNow(csv.getHeaderList().size() == EXPECTED_HEADER_AMOUNT,
                "Headers amount is [" + csv.getHeaderList().size() + "] and should be [" + EXPECTED_HEADER_AMOUNT
                        + "]. Headers [" + ListUtil.getMultilineStringFromList(csv.getHeaderList()) + "]",
                "Verify header amount is [" + EXPECTED_HEADER_AMOUNT + "]");

        for (int i = 0; i < expectedHeaders.length; i++) {
            SoftAssert.assertTrueNow(csv.getHeaderList().get(i).equals(expectedHeaders[i]),
                    "Header in index [" + i + "] is [" + csv.getHeaderList().get(i) + "] and should be ["
                            + expectedHeaders[i] + "]",
                    "Verify header in index [" + i + "] is [" + expectedHeaders[i] + "]");
        }

        SoftAssert.assertAll();
    }
    
    protected CsvReaderApi readCsvFile(String csvPath) throws AutomationTestException {
        CsvReaderApi csv = null;

        try {
            csv = new CsvReaderApi(csvPath);
        } catch (IOException e) {
            throw new AutomationTestException(e);
        }

        return csv;
    }
    
    protected CsvReaderApi readCsvFile(String csvPath, CellsSplitterEnum cellSpliter) throws AutomationTestException {
        CsvReaderApi csv = null;

        try {
            csv = new CsvReaderApi(csvPath, cellSpliter);
        } catch (IOException e) {
            throw new AutomationTestException(e);
        }

        return csv;
    }
    
    private void verifyCsvLine(List<String> currentRow, List<String> expectedRow, int rowIndex, String csvPath)
            throws Exception {
        int rowNumber = rowIndex + 2;
        if (SoftAssert.assertTrue(expectedRow.size() == currentRow.size(),
                "Row number [" + rowNumber + "] size in csv file [" + csvPath + "] is [" + currentRow.size()
                        + "] and should be [" + expectedRow.size() + "]")) {

            for (int i = 0; i < currentRow.size(); i++) {
                String currentCell = currentRow.get(i);
                String expectedCell = expectedRow.get(i);
                int columnNumber = i + 1;

                String cellLocationCapitalStr = StringUtil.replace(CELL_LOCATION_START_WITH_CAPITAL_STR_TEMPLATE,
                        String.valueOf(rowNumber), String.valueOf(columnNumber), csvPath);
                String cellLocationStr = StringUtil.replace(CELL_LOCATION_STR_TEMPLATE, String.valueOf(rowNumber),
                        String.valueOf(columnNumber), csvPath);

                if (SoftAssert.assertTrue(currentCell != null, cellLocationCapitalStr + " should not be null")) {
                    SoftAssert.assertTrue(expectedCell.equals(currentCell),
                            cellLocationStr + " value is [" + currentCell + "] and should be [" + expectedCell + "]",
                            "Verify that " + cellLocationStr + " value is [" + expectedCell + "]");
                }
            }
        }
    }
    
    private List<String> getRow(int index, GetRowEnum getRowEnum, CsvReaderApi csv)
            throws InvalidValueException, IndexOutOfBoundsException {
        if (getRowEnum == GetRowEnum.GET_ROW) {
            return csv.getRow(index);
        }

        return csv.getRows().get(index);
    }
}