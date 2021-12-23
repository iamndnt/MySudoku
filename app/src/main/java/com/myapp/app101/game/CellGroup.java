

package com.myapp.app101.game;

import java.util.HashMap;
import java.util.Map;



public class CellGroup {
    private Cell[] mCells = new Cell[CellCollection.SUDOKU_SIZE];
    private int mPos = 0;

    public void addCell(Cell cell) {
        mCells[mPos] = cell;
        mPos++;
    }



    protected boolean validate() {
        boolean valid = true;

        Map<Integer, Cell> cellsByValue = new HashMap<>();
        for (Cell cell : mCells) {
            int value = cell.getValue();
            if (cellsByValue.get(value) != null) {
                cell.setValid(false);
                cellsByValue.get(value).setValid(false);
                valid = false;
            } else {
                cellsByValue.put(value, cell);

            }
        }

        return valid;
    }

    public boolean DoesntContain(int value) {
        for (Cell mCell : mCells) {
            if (mCell.getValue() == value) {
                return false;
            }
        }
        return true;
    }

    public Cell[] getCells() {
        return mCells;
    }
}
