
package com.myapp.app101.game.command;

import com.myapp.app101.game.Cell;
import com.myapp.app101.game.CellCollection;
import com.myapp.app101.game.CellNote;

public class ClearAllNotesCommand extends AbstractMultiNoteCommand {

    public ClearAllNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                CellNote note = cell.getNote();
                if (!note.isEmpty()) {
                    mOldNotes.add(new NoteEntry(r, c, note));
                    cell.setNote(new CellNote());
                }
            }
        }
    }
}
