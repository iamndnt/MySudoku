package com.myapp.app101.game.command;

import com.myapp.app101.game.CellCollection;

public abstract class AbstractCellCommand extends AbstractCommand {

    private CellCollection mCells;

    protected CellCollection getCells() {
        return mCells;
    }

    protected void setCells(CellCollection mCells) {
        this.mCells = mCells;
    }

}
