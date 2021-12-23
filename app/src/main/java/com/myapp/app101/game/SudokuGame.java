

package com.myapp.app101.game;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.myapp.app101.game.command.AbstractCommand;
import com.myapp.app101.game.command.ClearAllNotesCommand;
import com.myapp.app101.game.command.CommandStack;
import com.myapp.app101.game.command.EditCellNoteCommand;
import com.myapp.app101.game.command.FillInNotesCommand;
import com.myapp.app101.game.command.FillInNotesWithAllValuesCommand;
import com.myapp.app101.game.command.SetCellValueAndRemoveNotesCommand;
import com.myapp.app101.game.command.SetCellValueCommand;

import java.util.ArrayList;

public class SudokuGame {

    public static final int GAME_STATE_PLAYING = 0;
    public static final int GAME_STATE_NOT_STARTED = 1;
    public static final int GAME_STATE_COMPLETED = 2;

    private long mId;
    private long mCreated;
    private int mState;
    private long mTime;
    private long mLastPlayed;
    private String mNote;
    private CellCollection mCells;
    private SudokuSolver mSolver;
    private boolean mUsedSolver = false;
    private boolean mRemoveNotesOnEntry = false;

    private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
    private CommandStack mCommandStack;
    // Time when current activity has become active.
    private long mActiveFromTime = -1;

    public SudokuGame() {
        mTime = 0;
        mLastPlayed = 0;
        mCreated = 0;

        mState = GAME_STATE_NOT_STARTED;
    }

    public static SudokuGame createEmptyGame() {
        SudokuGame game = new SudokuGame();
        game.setCells(CellCollection.createEmpty());

        game.setCreated(System.currentTimeMillis());
        return game;
    }

    public void saveState(Bundle outState) {
        outState.putLong("id", mId);
        outState.putString("note", mNote);
        outState.putLong("created", mCreated);
        outState.putInt("state", mState);
        outState.putLong("time", mTime);
        outState.putLong("lastPlayed", mLastPlayed);
        outState.putString("cells", mCells.serialize());
        outState.putString("command_stack", mCommandStack.serialize());
    }

    public void restoreState(Bundle inState) {
        mId = inState.getLong("id");
        mNote = inState.getString("note");
        mCreated = inState.getLong("created");
        mState = inState.getInt("state");
        mTime = inState.getLong("time");
        mLastPlayed = inState.getLong("lastPlayed");
        mCells = CellCollection.deserialize(inState.getString("cells"));
        mCommandStack = CommandStack.deserialize(inState.getString("command_stack"), mCells);

        validate();
    }


    public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
        mOnPuzzleSolvedListener = l;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public long getCreated() {
        return mCreated;
    }

    public void setCreated(long created) {
        mCreated = created;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }


    public long getTime() {
        if (mActiveFromTime != -1) {
            return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
        } else {
            return mTime;
        }
    }


    public void setTime(long time) {
        mTime = time;
    }

    public long getLastPlayed() {
        return mLastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        mLastPlayed = lastPlayed;
    }

    public CellCollection getCells() {
        return mCells;
    }

    public void setCells(CellCollection cells) {
        mCells = cells;
        validate();
        mCommandStack = new CommandStack(mCells);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public CommandStack getCommandStack() {
        return mCommandStack;
    }

    public void setCommandStack(CommandStack commandStack) {
        mCommandStack = commandStack;
    }

    public void setRemoveNotesOnEntry(boolean removeNotesOnEntry) {
        mRemoveNotesOnEntry = removeNotesOnEntry;
    }

    public void setCellValue(Cell cell, int value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        if (cell.isEditable()) {
            if (mRemoveNotesOnEntry) {
                executeCommand(new SetCellValueAndRemoveNotesCommand(cell, value));
            } else {
                executeCommand(new SetCellValueCommand(cell, value));
            }

            validate();
            if (isCompleted()) {
                finish();
                if (mOnPuzzleSolvedListener != null) {
                    mOnPuzzleSolvedListener.onPuzzleSolved();
                }
            }
        }
    }


    public void setCellNote(Cell cell, CellNote note) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null.");
        }

        if (cell.isEditable()) {
            executeCommand(new EditCellNoteCommand(cell, note));
        }
    }

    private void executeCommand(AbstractCommand c) {
        mCommandStack.execute(c);
    }


    public void undo() {
        mCommandStack.undo();
    }

    public boolean hasSomethingToUndo() {
        return mCommandStack.hasSomethingToUndo();
    }

    public void setUndoCheckpoint() {
        mCommandStack.setCheckpoint();
    }

    public void undoToCheckpoint() {
        mCommandStack.undoToCheckpoint();
    }

    public boolean hasUndoCheckpoint() {
        return mCommandStack.hasCheckpoint();
    }

    public void undoToBeforeMistake() {
        mCommandStack.undoToSolvableState();
    }

    @Nullable
    public Cell getLastChangedCell() {
        return mCommandStack.getLastChangedCell();
    }


    public void start() {
        mState = GAME_STATE_PLAYING;
        resume();
    }

    public void resume() {

        mActiveFromTime = SystemClock.uptimeMillis();
    }


    public void pause() {

        mTime += SystemClock.uptimeMillis() - mActiveFromTime;
        mActiveFromTime = -1;

        setLastPlayed(System.currentTimeMillis());
    }


    public boolean isSolvable() {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        return !finalValues.isEmpty();
    }


    public void solve() {
        mUsedSolver = true;
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            Cell cell = mCells.getCell(row, col);
            this.setCellValue(cell, val);
        }
    }

    public boolean usedSolver() {
        return mUsedSolver;
    }


    public void solveCell(Cell cell) {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();

        int row = cell.getRowIndex();
        int col = cell.getColumnIndex();
        for (int[] rowColVal : finalValues) {
            if (rowColVal[0] == row && rowColVal[1] == col) {
                int val = rowColVal[2];
                this.setCellValue(cell, val);
            }
        }
    }


    private void finish() {
        pause();
        mState = GAME_STATE_COMPLETED;
    }


    public void reset() {
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = mCells.getCell(r, c);
                if (cell.isEditable()) {
                    cell.setValue(0);
                    cell.setNote(new CellNote());
                }
            }
        }
        mCommandStack = new CommandStack(mCells);
        validate();
        setTime(0);
        setLastPlayed(0);
        mState = GAME_STATE_NOT_STARTED;
        mUsedSolver = false;
    }


    public boolean isCompleted() {
        return mCells.isCompleted();
    }

    public void clearAllNotes() {
        executeCommand(new ClearAllNotesCommand());
    }


    public void fillInNotes() {
        executeCommand(new FillInNotesCommand());
    }


    public void fillInNotesWithAllValues() { executeCommand(new FillInNotesWithAllValuesCommand()); }

    public void validate() {
        mCells.validate();
    }

    public interface OnPuzzleSolvedListener {

        void onPuzzleSolved();
    }
}
