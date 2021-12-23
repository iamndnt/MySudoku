
package com.myapp.app101.game;

import java.util.StringTokenizer;


public class Cell {
    private final Object mCellCollectionLock = new Object();

    private CellCollection mCellCollection;
    private int mRowIndex = -1;
    private int mColumnIndex = -1;
    private CellGroup mSector;
    private CellGroup mRow;
    private CellGroup mColumn;

    private int mValue;
    private CellNote mNote;
    private boolean mEditable;
    private boolean mValid;


    public Cell() {
        this(0, new CellNote(), true, true);
    }


    public Cell(int value) {
        this(value, new CellNote(), true, true);
    }

    private Cell(int value, CellNote note, boolean editable, boolean valid) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        mValue = value;
        mNote = note;
        mEditable = editable;
        mValid = valid;
    }


    public static Cell deserialize(StringTokenizer data, int version) {
        Cell cell = new Cell();
        cell.setValue(Integer.parseInt(data.nextToken()));
        cell.setNote(CellNote.deserialize(data.nextToken(), version));
        cell.setEditable(data.nextToken().equals("1"));

        return cell;
    }


    public static Cell deserialize(String cellData) {
        StringTokenizer data = new StringTokenizer(cellData, "|");
        return deserialize(data, CellCollection.DATA_VERSION);
    }


    public int getRowIndex() {
        return mRowIndex;
    }


    public int getColumnIndex() {
        return mColumnIndex;
    }


    protected void initCollection(CellCollection cellCollection, int rowIndex, int colIndex,
                                  CellGroup sector, CellGroup row, CellGroup column) {
        synchronized (mCellCollectionLock) {
            mCellCollection = cellCollection;
        }

        mRowIndex = rowIndex;
        mColumnIndex = colIndex;
        mSector = sector;
        mRow = row;
        mColumn = column;

        sector.addCell(this);
        row.addCell(this);
        column.addCell(this);
    }


    public CellGroup getSector() {
        return mSector;
    }


    public CellGroup getRow() {
        return mRow;
    }


    public CellGroup getColumn() {
        return mColumn;
    }


    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }
        mValue = value;
        onChange();
    }


    public CellNote getNote() {
        return mNote;
    }

    public void setNote(CellNote note) {
        mNote = note;
        onChange();
    }


    public boolean isEditable() {
        return mEditable;
    }


    public void setEditable(Boolean editable) {
        mEditable = editable;
        onChange();
    }


    public boolean isValid() {
        return mValid;
    }


    public void setValid(Boolean valid) {
        mValid = valid;
        onChange();
    }


    public void serialize(StringBuilder data, int dataVersion) {
        if (dataVersion == CellCollection.DATA_VERSION_PLAIN) {
            data.append(mValue);
        } else {
            data.append(mValue).append("|");
            if (mNote == null || mNote.isEmpty()) {
                data.append("0").append("|");
            } else {
                mNote.serialize(data);
            }
            data.append(mEditable ? "1" : "0").append("|");
        }
    }


    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb, CellCollection.DATA_VERSION);
        return sb.toString();
    }


    public String serialize(int dataVersion) {
        StringBuilder sb = new StringBuilder();
        serialize(sb, dataVersion);
        return sb.toString();
    }


    private void onChange() {
        synchronized (mCellCollectionLock) {
            if (mCellCollection != null) {
                mCellCollection.onChange();
            }

        }
    }
}
