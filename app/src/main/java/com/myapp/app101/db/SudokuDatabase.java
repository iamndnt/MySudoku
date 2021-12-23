

package com.myapp.app101.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import com.myapp.app101.game.CellCollection;
import com.myapp.app101.game.FolderInfo;
import com.myapp.app101.game.SudokuGame;
import com.myapp.app101.game.command.CommandStack;
import com.myapp.app101.gui.SudokuListFilter;
import com.myapp.app101.gui.SudokuListSorter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class SudokuDatabase {
    public static final String DATABASE_NAME = "opensudoku";
    public static final String SUDOKU_TABLE_NAME = "sudoku";
    public static final String FOLDER_TABLE_NAME = "folder";


    private static final String INBOX_FOLDER_NAME = "Inbox";
    private DatabaseHelper mOpenHelper;
    private SQLiteStatement mInsertSudokuStatement;

    public SudokuDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }


    public Cursor getFolderList() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return qb.query(db, null, null, null, null, null, "created ASC");
    }


    public FolderInfo getFolderInfo(long folderID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);
        qb.appendWhere(FolderColumns._ID + "=" + folderID);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        try (Cursor c = qb.query(db, null, null,
                null, null, null, null)) {

            if (c.moveToFirst()) {
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

                FolderInfo folderInfo = new FolderInfo();
                folderInfo.id = id;
                folderInfo.name = name;

                return folderInfo;
            } else {
                return null;
            }
        }
    }

    public FolderInfo getFolderInfoFull(long folderID) {
        FolderInfo folder = null;

        SQLiteDatabase db;
        String q = "select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id where folder._id = " + folderID + " group by sudoku.state";
        db = mOpenHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(q, null)) {


            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));
                int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
                int count = c.getInt(c.getColumnIndex("count"));

                if (folder == null) {
                    folder = new FolderInfo(id, name);
                }

                folder.puzzleCount += count;
                if (state == SudokuGame.GAME_STATE_COMPLETED) {
                    folder.solvedCount += count;
                }
                if (state == SudokuGame.GAME_STATE_PLAYING) {
                    folder.playingCount += count;
                }
            }
        }

        return folder;
    }


    public FolderInfo getInboxFolder() {
        FolderInfo inbox = findFolder(INBOX_FOLDER_NAME);
        if (inbox != null) {
            inbox = insertFolder(INBOX_FOLDER_NAME, System.currentTimeMillis());
        }
        return inbox;
    }


    public FolderInfo findFolder(String folderName) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);
        qb.appendWhere(FolderColumns.NAME + " = ?");

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        try (Cursor c = qb.query(db, null, null,
                new String[]{folderName}, null, null, null)) {

            if (c.moveToFirst()) {
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

                FolderInfo folderInfo = new FolderInfo();
                folderInfo.id = id;
                folderInfo.name = name;

                return folderInfo;
            } else {
                return null;
            }
        }
    }


    public FolderInfo insertFolder(String name, Long created) {
        ContentValues values = new ContentValues();
        values.put(FolderColumns.CREATED, created);
        values.put(FolderColumns.NAME, name);

        long rowId;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        rowId = db.insert(FOLDER_TABLE_NAME, FolderColumns._ID, values);

        if (rowId > 0) {
            FolderInfo fi = new FolderInfo();
            fi.id = rowId;
            fi.name = name;
            return fi;
        }

        throw new SQLException(String.format("Failed to insert folder '%s'.", name));
    }

    public void updateFolder(long folderID, String name) {
        ContentValues values = new ContentValues();
        values.put(FolderColumns.NAME, name);

        SQLiteDatabase db;
        db = mOpenHelper.getWritableDatabase();
        db.update(FOLDER_TABLE_NAME, values, FolderColumns._ID + "=" + folderID, null);
    }


    public void deleteFolder(long folderID) {

        // TODO: should run in transaction
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // delete all puzzles in folder we are going to delete
        db.delete(SUDOKU_TABLE_NAME, SudokuColumns.FOLDER_ID + "=" + folderID, null);
        // delete the folder
        db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
    }


    public Cursor getSudokuList(long folderID, SudokuListFilter filter, SudokuListSorter sorter) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUDOKU_TABLE_NAME);
        //qb.setProjectionMap(sPlacesProjectionMap);
        qb.appendWhere(SudokuColumns.FOLDER_ID + "=" + folderID);

        if (filter != null) {
            if (!filter.showStateCompleted) {
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_COMPLETED);
            }
            if (!filter.showStateNotStarted) {
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_NOT_STARTED);
            }
            if (!filter.showStatePlaying) {
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_PLAYING);
            }
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        return qb.query(db, null, null, null,
                null, null, sorter.getSortOrder());
    }


    public List<SudokuGame> getAllSudokuByFolder(long folderID, SudokuListSorter sorter) {
        Cursor cursor = getSudokuList(folderID, null, sorter);
        if (cursor.moveToFirst())
        {
            List<SudokuGame> sudokuList = new LinkedList<>();
            while (!cursor.isAfterLast()) {
                sudokuList.add(extractSudokuGameFromCursorRow(cursor));
                cursor.moveToNext();
            }
            return sudokuList;
        }
        return Collections.emptyList();
    }


    public SudokuGame getSudoku(long sudokuID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUDOKU_TABLE_NAME);
        qb.appendWhere(SudokuColumns._ID + "=" + sudokuID);



        SQLiteDatabase db;
        SudokuGame s = null;
        db = mOpenHelper.getReadableDatabase();
        try (Cursor c = qb.query(db, null, null,
                null, null, null, null)) {

            if (c.moveToFirst()) {
                s = extractSudokuGameFromCursorRow(c);
            }
        }

        return s;
    }

    private SudokuGame extractSudokuGameFromCursorRow(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(SudokuColumns._ID));
        long created = cursor.getLong(cursor.getColumnIndex(SudokuColumns.CREATED));
        String data = cursor.getString(cursor.getColumnIndex(SudokuColumns.DATA));
        long lastPlayed = cursor.getLong(cursor.getColumnIndex(SudokuColumns.LAST_PLAYED));
        int state = cursor.getInt(cursor.getColumnIndex(SudokuColumns.STATE));
        long time = cursor.getLong(cursor.getColumnIndex(SudokuColumns.TIME));
        String note = cursor.getString(cursor.getColumnIndex(SudokuColumns.PUZZLE_NOTE));

        SudokuGame sudoku = new SudokuGame();
        sudoku.setId(id);
        sudoku.setCreated(created);
        sudoku.setCells(CellCollection.deserialize(data));
        sudoku.setLastPlayed(lastPlayed);
        sudoku.setState(state);
        sudoku.setTime(time);
        sudoku.setNote(note);

        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            String command_stack = cursor.getString(cursor.getColumnIndex(SudokuColumns.COMMAND_STACK));
            if (command_stack != null && !command_stack.equals("")) {
                sudoku.setCommandStack(CommandStack.deserialize(command_stack, sudoku.getCells()));
            }
        }
        return sudoku;
    }


    public long insertSudoku(long folderID, SudokuGame sudoku) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.CREATED, sudoku.getCreated());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
        values.put(SudokuColumns.FOLDER_ID, folderID);
        String command_stack = "";
        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            command_stack = sudoku.getCommandStack().serialize();
        }
        values.put(SudokuColumns.COMMAND_STACK, command_stack);

        long rowId = db.insert(SUDOKU_TABLE_NAME, FolderColumns.NAME, values);
        if (rowId > 0) {
            return rowId;
        }

        throw new SQLException("Failed to insert sudoku.");
    }

    public long importSudoku(long folderID, SudokuImportParams pars) throws SudokuInvalidFormatException {
        if (pars.data == null) {
            throw new SudokuInvalidFormatException(null);
        }

        if (!CellCollection.isValid(pars.data)) {
            throw new SudokuInvalidFormatException(pars.data);
        }

        if (mInsertSudokuStatement == null) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            mInsertSudokuStatement = db.compileStatement(
                    "insert into sudoku (folder_id, created, state, time, last_played, data, puzzle_note, command_stack) values (?, ?, ?, ?, ?, ?, ?, ?)"
            );
        }

        mInsertSudokuStatement.bindLong(1, folderID);
        mInsertSudokuStatement.bindLong(2, pars.created);
        mInsertSudokuStatement.bindLong(3, pars.state);
        mInsertSudokuStatement.bindLong(4, pars.time);
        mInsertSudokuStatement.bindLong(5, pars.lastPlayed);
        mInsertSudokuStatement.bindString(6, pars.data);
        if (pars.note == null) {
            mInsertSudokuStatement.bindNull(7);
        } else {
            mInsertSudokuStatement.bindString(7, pars.note);
        }
        if (pars.command_stack == null) {
            mInsertSudokuStatement.bindNull(8);
        } else {
            mInsertSudokuStatement.bindString(8, pars.command_stack);
        }


        long rowId = mInsertSudokuStatement.executeInsert();
        if (rowId > 0) {
            return rowId;
        }

        throw new SQLException("Failed to insert sudoku.");
    }


    public Cursor exportFolder(long folderID) {
        String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note, s.command_stack from folder f left outer join sudoku s on f._id = s.folder_id";
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (folderID != -1) {
            query += " where f._id = ?";
        }
        return db.rawQuery(query, folderID != -1 ? new String[]{String.valueOf(folderID)} : null);
    }


    public Cursor exportSudoku(long sudokuID) {
        String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note, s.command_stack from sudoku s inner join folder f on s.folder_id = f._id where s._id = ?";
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery(query, new String[]{String.valueOf(sudokuID)});
    }

    public void updateSudoku(SudokuGame sudoku) {
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
        String command_stack = null;
        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            command_stack = sudoku.getCommandStack().serialize();
        }
        values.put(SudokuColumns.COMMAND_STACK, command_stack);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.update(SUDOKU_TABLE_NAME, values, SudokuColumns._ID + "=" + sudoku.getId(), null);
    }



    public void deleteSudoku(long sudokuID) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(SUDOKU_TABLE_NAME, SudokuColumns._ID + "=" + sudokuID, null);
    }

    public void close() {
        if (mInsertSudokuStatement != null) {
            mInsertSudokuStatement.close();
        }

        mOpenHelper.close();
    }

    public void beginTransaction() {
        mOpenHelper.getWritableDatabase().beginTransaction();
    }

    public void setTransactionSuccessful() {
        mOpenHelper.getWritableDatabase().setTransactionSuccessful();
    }

    public void endTransaction() {
        mOpenHelper.getWritableDatabase().endTransaction();
    }
}
