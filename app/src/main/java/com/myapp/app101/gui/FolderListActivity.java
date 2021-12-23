

package com.myapp.app101.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.myapp.app101.R;
import com.myapp.app101.db.FolderColumns;
import com.myapp.app101.db.SudokuDatabase;


public class FolderListActivity extends ThemedActivity {

    public static final int MENU_ITEM_ADD = Menu.FIRST;
    public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
    public static final int MENU_ITEM_EXPORT = Menu.FIRST + 4;
    public static final int MENU_ITEM_EXPORT_ALL = Menu.FIRST + 5;
    public static final int MENU_ITEM_IMPORT = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 7;

    private static final int OPEN_FILE = 1;

    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_ADD_FOLDER = 1;
    private static final int DIALOG_RENAME_FOLDER = 2;
    private static final int DIALOG_DELETE_FOLDER = 3;
    private static final String TAG = "FolderListActivity";
    private int STORAGE_PERMISSION_CODE = 1;
    private Cursor mCursor;
    private SudokuDatabase mDatabase;
    private FolderListViewBinder mFolderListBinder;
    private ListView mListView;
    private Menu mMenu;


    private long mRenameFolderID;
    private long mDeleteFolderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.folder_list);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);



        mDatabase = new SudokuDatabase(getApplicationContext());
        mCursor = mDatabase.getFolderList();
        startManagingCursor(mCursor);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item,
                mCursor, new String[]{FolderColumns.NAME, FolderColumns._ID},
                new int[]{R.id.name, R.id.detail});
        mFolderListBinder = new FolderListViewBinder(this);
        adapter.setViewBinder(mFolderListBinder);

        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(getApplicationContext(), SudokuListActivity.class);
            i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
            startActivity(i);
        });
        registerForContextMenu(mListView);

    }

    @Override
    protected void onStart() {
        super.onStart();

        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
        mFolderListBinder.destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("mRenameFolderID", mRenameFolderID);
        outState.putLong("mDeleteFolderID", mDeleteFolderID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        mRenameFolderID = state.getLong("mRenameFolderID");
        mDeleteFolderID = state.getLong("mDeleteFolderID");
    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onOptionsItemSelected(mMenu.findItem(MENU_ITEM_IMPORT));
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateList() {
        mCursor.requery();
    }

    private static class FolderListViewBinder implements ViewBinder {
        private Context mContext;
        private FolderDetailLoader mDetailLoader;


        public FolderListViewBinder(Context context) {
            mContext = context;
            mDetailLoader = new FolderDetailLoader(context);
        }

        @Override
        public boolean setViewValue(View view, Cursor c, int columnIndex) {

            switch (view.getId()) {
                case R.id.name:
                    ((TextView) view).setText(c.getString(columnIndex));
                    break;
                case R.id.detail:
                    final long folderID = c.getLong(columnIndex);
                    final TextView detailView = (TextView) view;
                    detailView.setText(mContext.getString(R.string.loading));
                    mDetailLoader.loadDetailAsync(folderID, folderInfo -> {
                        if (folderInfo != null)
                            detailView.setText(folderInfo.getDetail(mContext));
                    });
            }
            return true;
        }

        public void destroy() {
            mDetailLoader.destroy();
        }
    }
}
