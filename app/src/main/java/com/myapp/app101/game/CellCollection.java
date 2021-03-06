
package com.myapp.app101.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class CellCollection {

    public static final int SUDOKU_SIZE = 9;


    public static int DATA_VERSION_PLAIN = 0;


    public static int DATA_VERSION_1 = 1;


    public static int DATA_VERSION_2 = 2;


    public static int DATA_VERSION_3 = 3;

    public static int DATA_VERSION = DATA_VERSION_3;
    private static Pattern DATA_PATTERN_VERSION_PLAIN = Pattern.compile("^\\d{81}$");
    private static Pattern DATA_PATTERN_VERSION_1 = Pattern.compile("^version: 1\\n((?#value)\\d\\|(?#note)((\\d,)+|-)\\|(?#editable)[01]\\|){0,81}$");
    private static Pattern DATA_PATTERN_VERSION_2 = Pattern.compile("^version: 2\\n((?#value)\\d\\|(?#note)(\\d){1,3}\\|{1,2}(?#editable)[01]\\|){0,81}$");
    private static Pattern DATA_PATTERN_VERSION_3 = Pattern.compile("^version: 3\\n((?#value)\\d\\|(?#note)(\\d){1,3}\\|(?#editable)[01]\\|){0,81}$");
    private final List<OnChangeListener> mChangeListeners = new ArrayList<>();


    private Cell[][] mCells;


    private CellGroup[] mSectors;
    private CellGroup[] mRows;
    private CellGroup[] mColumns;
    private boolean mOnChangeEnabled = true;


    private CellCollection(Cell[][] cells) {

        mCells = cells;
        initCollection();
    }


    public static CellCollection createEmpty() {
        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        for (int r = 0; r < SUDOKU_SIZE; r++) {

            for (int c = 0; c < SUDOKU_SIZE; c++) {
                cells[r][c] = new Cell();
            }
        }

        return new CellCollection(cells);
    }


    public static CellCollection createDebugGame() {
        CellCollection debugGame = new CellCollection(new Cell[][]{
                {new Cell(), new Cell(), new Cell(), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9),},
                {new Cell(), new Cell(), new Cell(), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3),},
                {new Cell(), new Cell(), new Cell(), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6),},
                {new Cell(2), new Cell(3), new Cell(4), new Cell(), new Cell(), new Cell(), new Cell(8), new Cell(9), new Cell(1),},
                {new Cell(5), new Cell(6), new Cell(7), new Cell(), new Cell(), new Cell(), new Cell(2), new Cell(3), new Cell(4),},
                {new Cell(8), new Cell(9), new Cell(1), new Cell(), new Cell(), new Cell(), new Cell(5), new Cell(6), new Cell(7),},
                {new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2),},
                {new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5),},
                {new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8),},
        });
        debugGame.markFilledCellsAsNotEditable();
        return debugGame;
    }

    public static CellCollection deserialize(StringTokenizer data, int version) {
        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        int r = 0, c = 0;
        while (data.hasMoreTokens() && r < 9) {
            cells[r][c] = Cell.deserialize(data, version);
            c++;

            if (c == 9) {
                r++;
                c = 0;
            }
        }

        return new CellCollection(cells);
    }


    public static CellCollection deserialize(String data) {
        // TODO: use DATA_PATTERN_VERSION_1 to validate and extract puzzle data
        String[] lines = data.split("\n");
        if (lines.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize Sudoku, data corrupted.");
        }

        String line = lines[0];
        if (line.startsWith("version:")) {
            String[] kv = line.split(":");
            int version = Integer.parseInt(kv[1].trim());
            StringTokenizer st = new StringTokenizer(lines[1], "|");
            return deserialize(st, version);
        } else {
            return fromString(data);
        }
    }

    public static CellCollection fromString(String data) {


        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        int pos = 0;
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                int value = 0;
                while (pos < data.length()) {
                    pos++;
                    if (data.charAt(pos - 1) >= '0'
                            && data.charAt(pos - 1) <= '9') {
                        value = data.charAt(pos - 1) - '0';
                        break;
                    }
                }
                Cell cell = new Cell();
                cell.setValue(value);
                cell.setEditable(value == 0);
                cells[r][c] = cell;
            }
        }

        return new CellCollection(cells);
    }


    public static boolean isValid(String data, int dataVersion) {
        if (dataVersion == DATA_VERSION_PLAIN) {
            return DATA_PATTERN_VERSION_PLAIN.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_1) {
            return DATA_PATTERN_VERSION_1.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_2) {
            return DATA_PATTERN_VERSION_2.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_3) {
            return DATA_PATTERN_VERSION_3.matcher(data).matches();
        } else {
            throw new IllegalArgumentException("Unknown version: " + dataVersion);
        }
    }


    public static boolean isValid(String data) {
        return (DATA_PATTERN_VERSION_PLAIN.matcher(data).matches() ||
                DATA_PATTERN_VERSION_1.matcher(data).matches() ||
                DATA_PATTERN_VERSION_2.matcher(data).matches() ||
                DATA_PATTERN_VERSION_3.matcher(data).matches()
        );
    }


    public boolean isEmpty() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() != 0)
                    return false;
            }
        }
        return true;
    }

    public Cell[][] getCells() {
        return mCells;
    }


    public Cell getCell(int rowIndex, int colIndex) {
        return mCells[rowIndex][colIndex];
    }

    public Cell findFirstCell(int val) {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() == val)
                    return cell;
            }
        }
        return null;
    }

    public void markAllCellsAsValid() {
        mOnChangeEnabled = false;
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                mCells[r][c].setValid(true);
            }
        }
        mOnChangeEnabled = true;
        onChange();
    }


    public boolean validate() {

        boolean valid = true;

        markAllCellsAsValid();

        mOnChangeEnabled = false;
        for (CellGroup row : mRows) {
            if (!row.validate()) {
                valid = false;
            }
        }
        for (CellGroup column : mColumns) {
            if (!column.validate()) {
                valid = false;
            }
        }
        for (CellGroup sector : mSectors) {
            if (!sector.validate()) {
                valid = false;
            }
        }

        mOnChangeEnabled = true;
        onChange();

        return valid;
    }

    public boolean isCompleted() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() == 0 || !cell.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }


    public void markAllCellsAsEditable() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.setEditable(true);
            }
        }
    }


    public void markFilledCellsAsNotEditable() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.setEditable(cell.getValue() == 0);
            }
        }
    }


    public void fillInNotes() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = getCell(r, c);
                cell.setNote(new CellNote());

                CellGroup row = cell.getRow();
                CellGroup column = cell.getColumn();
                CellGroup sector = cell.getSector();
                for (int i = 1; i <= SUDOKU_SIZE; i++) {
                    if (row.DoesntContain(i) && column.DoesntContain(i) && sector.DoesntContain(i)) {
                        cell.setNote(cell.getNote().addNumber(i));
                    }
                }
            }
        }
    }


    public void fillInNotesWithAllValues() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = getCell(r, c);
                cell.setNote(new CellNote());
                for (int i = 1; i <= SUDOKU_SIZE; i++) {
                    cell.setNote(cell.getNote().addNumber(i));
                }
            }
        }
    }

    public void removeNotesForChangedCell(Cell cell, int number) {
        if (number < 1 || number > 9) {
            return;
        }

        CellGroup row = cell.getRow();
        CellGroup column = cell.getColumn();
        CellGroup sector = cell.getSector();
        for (int i = 0; i < SUDOKU_SIZE; i++) {
            row.getCells()[i].setNote(row.getCells()[i].getNote().removeNumber(number));
            column.getCells()[i].setNote(column.getCells()[i].getNote().removeNumber(number));
            sector.getCells()[i].setNote(sector.getCells()[i].getNote().removeNumber(number));
        }
    }


    public Map<Integer, Integer> getValuesUseCount() {
        Map<Integer, Integer> valuesUseCount = new HashMap<>();
        for (int value = 1; value <= CellCollection.SUDOKU_SIZE; value++) {
            valuesUseCount.put(value, 0);
        }

        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                int value = getCell(r, c).getValue();
                if (value != 0) {
                    valuesUseCount.put(value, valuesUseCount.get(value) + 1);
                }
            }
        }

        return valuesUseCount;
    }


    private void initCollection() {
        mRows = new CellGroup[SUDOKU_SIZE];
        mColumns = new CellGroup[SUDOKU_SIZE];
        mSectors = new CellGroup[SUDOKU_SIZE];

        for (int i = 0; i < SUDOKU_SIZE; i++) {
            mRows[i] = new CellGroup();
            mColumns[i] = new CellGroup();
            mSectors[i] = new CellGroup();
        }

        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];

                cell.initCollection(this, r, c,
                        mSectors[((c / 3) * 3) + (r / 3)],
                        mRows[c],
                        mColumns[r]
                );
            }
        }
    }


    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb, DATA_VERSION);
        return sb.toString();
    }


    public String serialize(int dataVersion) {
        StringBuilder sb = new StringBuilder();
        serialize(sb, dataVersion);
        return sb.toString();
    }


    public void serialize(StringBuilder data) {
        serialize(data, DATA_VERSION);
    }

    public void serialize(StringBuilder data, int dataVersion) {
        if (dataVersion > DATA_VERSION_PLAIN) {
            data.append("version: ");
            data.append(dataVersion);
            data.append("\n");
        }
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.serialize(data, dataVersion);
            }
        }
    }

    public void addOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + "is already registered.");
            }
            mChangeListeners.add(listener);
        }
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (!mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            mChangeListeners.remove(listener);
        }
    }

    public boolean isOnChangeEnabled() {
        return mOnChangeEnabled;
    }


    protected void onChange() {
        if (mOnChangeEnabled) {
            synchronized (mChangeListeners) {
                for (OnChangeListener l : mChangeListeners) {
                    l.onChange();
                }
            }
        }
    }

    public interface OnChangeListener {

        void onChange();
    }
}
