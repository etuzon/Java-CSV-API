package io.github.etuzon.java.csv.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.github.etuzon.java.csv.enums.CellsSplitterEnum;
import io.github.etuzon.projects.core.expections.InvalidValueException;
import io.github.etuzon.projects.core.utils.StringUtil;

/*************************************************
 * Parse CSV file.
 *
 * @author Eyal Tuzon
 * 
 */
public class CsvReaderApi extends CsvApiBase {
    public static final boolean CSV_CONTAIN_HEADERS = true;

    private String cell = "";
    private List<String> cellsInLine = new ArrayList<String>();
    private boolean isCellInInvertedComma = false;

    /*********************************
     * Constructor.
     *
     * @param filePath CSV file path.
     * @throws IOException in case fail read CSV file.
     */
    public CsvReaderApi(String filePath) throws IOException {
        this(filePath, CELLS_DEFAULT_SPLITTER);
    }

    /********************************
     * Constructor.
     *
     * @param filePath CSV file path.
     * @param cellsSplitter CSV cells splitter enum.
     * @throws IOException in case fail read CSV file.
     */
    public CsvReaderApi(String filePath, CellsSplitterEnum cellsSplitter) throws IOException {
        this(filePath, cellsSplitter.getChar());
    }

    /********************************
     * Constructor.
     * 
     * @param filePath CSV file path.
     * @param isCsvContainHeaders true in case CSV file contain headers, else the value is false.
     * @throws IOException in case fail read CSV file.
     */
    public CsvReaderApi(String filePath, boolean isCsvContainHeaders) throws IOException {
        this(filePath, CELLS_DEFAULT_SPLITTER, isCsvContainHeaders);
    }
    
    /********************************
     * Constructor.
     * 
     * @param filePath CSV file path.
     * @param cellsSplitter Cells splitter char.
     * @throws IOException in case fail read CSV file.
     */
    public CsvReaderApi(String filePath, char cellsSplitter) throws IOException {
        this(filePath, cellsSplitter, CSV_CONTAIN_HEADERS);
    }

    /*******************************
     * Constructor.
     *
     * @param filePath CSV file path.
     * @param cellsSplitter CSV cells splitter char.
     * @param isCsvContainHeaders true in case CSV file contain headers, else the value is false.
     * @throws IOException in case fail read CSV file.
     */
    public CsvReaderApi(String filePath, char cellsSplitter, final boolean isCsvContainHeaders) throws IOException {
        super(filePath, cellsSplitter);

        parseCsv(filePath);

        if (isCsvContainHeaders) {
            setHeaderList();
        }
    }

    /******************************
     * Get CSV rows amount. Not include headers line.
     *
     * @return CSV rows amount.
     */
    public int getRowsAmount() {
        if (csvRows == null) {
            return -1;
        }

        return csvRows.size();
    }

    /******************************
     * Return true if CSV not contain rows, Ignore headers line if exists.
     *
     * @return true in case the CSV file not contain any row.
     */
    public boolean isCsvEmpty() {
        return getRowsAmount() < 1;
    }

    /******************************
     * Get header list.
     *
     * @return String list of CSV headers.
     */
    public List<String> getHeaderList() {
        return headerList;
    }

    /******************************
     * Get column index that it's header is 'headerName'. First index is 0.
     *
     * @param headerName Header name.
     * @return Column index. Return -1 in case header not exists.
     */
    public int getColumnIndex(String headerName) {
        for (int i = 0; i < headerList.size(); i++) {
            if (headerList.get(i).equals(headerName)) {
                return i;
            }
        }

        return -1;
    }

    /******************************
     * Get row index thats it's column header is 'headerName', and cell in that column is 'value'.
     *
     * @param headerName Header name.
     * @param value Field value.
     * @return Row index. Return -1 in case column or cell not exist.
     * @throws IndexOutOfBoundsException In case column index exceed row.
     */
    public int getRowIndex(String headerName, String value) throws IndexOutOfBoundsException {
        int columnIndex = getColumnIndex(headerName);

        if (columnIndex == -1) {
            return -1;
        }

        for (int i = 0; i < csvRows.size(); i++) {
            List<String> row = csvRows.get(i);

            if (row.size() <= columnIndex) {
                throw new IndexOutOfBoundsException("Column index [" + columnIndex + "] in CSV file [" + getPath()
                        + "] exceed of row size [" + row.size() + "]");
            }

            if (row.get(columnIndex).equals(value)) {
                return i;
            }
        }

        return -1;
    }

    /******************************
     * Get column cells.
     *
     * @param headerName Header name.
     * @return List of column cells. Return null in case column not exists.
     */
    public List<String> getColumn(String headerName) {
        List<String> columnValues = new ArrayList<String>();

        int index = getColumnIndex(headerName);

        if (index == -1) {
            return null;
        }

        for (List<String> row : csvRows) {
            columnValues.add(row.get(index));
        }

        return columnValues;
    }

    /******************************
     * Get cell value.
     *
     * @param headerName Header name.
     * @param rowIndex Row index.
     * @return Field value in column that it's header is 'headerName', and the cell exists 
     *         in 'rowIndex'. Return null in case 'headerName' column not exists.
     * @throws InvalidValueException Row index is negative number.
     * @throws IndexOutOfBoundsException Row index exceed of column size.
     */
    public String getFieldValue(String headerName, int rowIndex)
            throws InvalidValueException, IndexOutOfBoundsException {
        if (rowIndex < 0) {
            throw new InvalidValueException("Index value [" + rowIndex + "] should not be negative");
        }

        List<String> columnFields = getColumn(headerName);

        if (columnFields == null) {
            return null;
        }

        if (rowIndex >= columnFields.size()) {
            throw new IndexOutOfBoundsException("Index [" + rowIndex + "] is out of bound. CSV [" + getPath()
                    + "] cells amount for header [" + columnFields + "] is [" + columnFields.size() + "]");
        }

        return columnFields.get(rowIndex);
    }

    /******************************
     * Get rows not include headers row.
     *
     * @return CSV cells not include headers line.
     */
    public List<List<String>> getRows() {
        return csvRows;
    }

    /******************************
     * Get row in index. Index 0 start after headers row.
     *
     * @param index Row index not include headers row.
     * @return Row cells.
     * @throws InvalidValueException Index is negative number.
     * @throws IndexOutOfBoundsException Index exceed of rows amount.
     */
    public List<String> getRow(int index) throws InvalidValueException, IndexOutOfBoundsException {
        if (index < 0) {
            throw new InvalidValueException("Index value [" + index + "] should not be negative");
        }

        if (index >= getRowsAmount()) {
            throw new IndexOutOfBoundsException("Index [" + index + "] is out of bound. CSV [" + getPath()
                    + "] rows amount [" + getRowsAmount() + "]");
        }

        return csvRows.get(index);
    }

    /******************************
     * Get CSV file path.
     *
     * @return CSV file path.
     */
    public String getPath() {
        return filePath;
    }

    /*******************************
     * Parse CSV file.
     *
     * @param path CSV file path.
     * @throws IOException In case fail read CSV file.
     */
    private void parseCsv(String path) throws IOException {
        BufferedReader buff = null;

        try {
            buff = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(path)), StandardCharsets.UTF_8));

            String row = "";

            while (row != null) {
                row = buff.readLine();

                if (row != null) {
                    parsePreFormattedRow(row);

                    if (isCellInInvertedComma == false) {
                        csvRows.add(cellsInLine);
                        cellsInLine = new ArrayList<String>();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("CSV file [" + path + "] was not found");
        } finally {
            if (buff != null) {
                try {
                    buff.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void parsePreFormattedRow(String row) {
        List<String> preFormattedCellList = StringUtil.split(row, cellsSplitter);

        for (int i = 0; i < preFormattedCellList.size(); i++) {
            String preFormattedCell = preFormattedCellList.get(i);

            if (isCellInInvertedComma) {
                parsePreCellWhenItIsInInvertedComma(i, preFormattedCell);
            } else {
                parsePreCellWhenItIsNotInInvertedComma(preFormattedCell);
            }
        }
    }

    private void parsePreCellWhenItIsInInvertedComma(int preFormattedCellIndex, String preFormattedCellInRow) {
        // In case pre parsed cell already in inverted comma and this is first pre
        // parsed cell in row
        if (preFormattedCellIndex == 0) {
            cell += "\n" + preFormattedCellInRow;
        } else {
            cell += cellsSplitter + preFormattedCellInRow;
        }

        boolean isOddInvertedCommas = isOddInvertedCommasFromEnd(preFormattedCellInRow);

        if (isOddInvertedCommas) {
            isCellInInvertedComma = false;
            cell = removeCsvInvertedCommasInCell(cell);
            cellsInLine.add(cell);
            cell = "";
        }
    }

    private void parsePreCellWhenItIsNotInInvertedComma(String preFormattedCell) {
        if (preFormattedCell.isEmpty()) {
            cellsInLine.add("");
        } else {
            if (preFormattedCell.startsWith("\"")) {
                parseCellStartWithInvertedComma(preFormattedCell);
            } else {
                cellsInLine.add(preFormattedCell);
            }
        }
    }

    private void parseCellStartWithInvertedComma(String preFormattedCell) {
        boolean isOddInvertedCommas = isOddInvertedCommasFromBegin(preFormattedCell);

        if (isOddInvertedCommas) {
            if ((isOddInvertedCommasFromEnd(preFormattedCell)) && (preFormattedCell.length() > 1)) {
                cellsInLine.add(removeCsvInvertedCommasInCell(preFormattedCell));
            } else {
                isCellInInvertedComma = true;
                cell = preFormattedCell;
            }
        } else {
            cellsInLine.add(removeCsvInvertedCommasInCell(preFormattedCell));
        }
    }

    private boolean isOddInvertedCommasFromBegin(String cell) {
        if (cell == null) {
            return false;
        }

        if (cell.startsWith("\"") == false) {
            return false;
        }

        int count = 0;

        for (int i = 0; i < cell.length(); i++) {
            char c = cell.charAt(i);
            if (c == '"') {
                count++;
            } else {
                break;
            }
        }

        return Math.abs(count) % 2 == 1;
    }

    /*********************************************************
     * Check if there is odd number of sequence inverted commas in raw from its end.
     * 
     * @param raw A raw.
     * @return true if there is odd number of sequence inverted commas in raw from its end.
     *********************************************************/
    private boolean isOddInvertedCommasFromEnd(String raw) {
        if (raw == null) {
            return false;
        }

        if (raw.endsWith("\"") == false) {
            return false;
        }

        int count = 0;

        for (int i = raw.length() - 1; i >= 0; i--) {
            char c = raw.charAt(i);
            if (c == '"') {
                count++;
            } else {
                break;
            }
        }

        return Math.abs(count) % 2 == 1;
    }

    /********************************************************************
     * Remove the additional inverted commas that * the csv add to the cells
     * 
     * @param cell
     * @return
     ********************************************************************/
    private String removeCsvInvertedCommasInCell(String cell) {
        if (cell == null) {
            return null;
        }

        if (cell.isEmpty()) {
            return "";
        }

        // remove the first and last "
        cell = cell.substring(1, cell.length() - 1);
        cell = cell.replaceAll("\"\"", "\"");

        return cell;
    }

    private void setHeaderList() {
        if (csvRows != null) {
            if (csvRows.size() > 0) {
                headerList = csvRows.get(0);
                csvRows.remove(0);
            }
        }
    }
}