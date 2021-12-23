package com.myapp.app101.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.preference.PreferenceManager;

import com.myapp.app101.R;
import com.myapp.app101.db.SudokuDatabase;
import com.myapp.app101.game.SudokuGame;

public class TitleScreenActivity extends ThemedActivity {

    private final int MENU_ITEM_SETTINGS = 0;
    private final int MENU_ITEM_ABOUT = 1;
    private final int DIALOG_ABOUT = 0;
    private Button mResumeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        mResumeButton = findViewById(R.id.resume_button);
        Button mSudokuListButton = findViewById(R.id.sudoku_lists_button);
        Button mSettingsButton = findViewById(R.id.settings_button);

        setupResumeButton();

        mSudokuListButton.setOnClickListener((view) ->
                startActivity(new Intent(this, FolderListActivity.class)));

        mSettingsButton.setOnClickListener((view) ->
                startActivity(new Intent(this, GameSettingsActivity.class)));

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showSudokuFolderListOnStartup = gameSettings.getBoolean("show_sudoku_lists_on_startup", false);
        if (showSudokuFolderListOnStartup) {
            startActivity(new Intent(this, FolderListActivity.class));
        }
    }

    private boolean canResume(long mSudokuGameID) {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        SudokuGame mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        if (mSudokuGame != null) {
            return mSudokuGame.getState() != SudokuGame.GAME_STATE_COMPLETED;
        }
        return false;
    }

    private void setupResumeButton() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long mSudokuGameID = gameSettings.getLong("most_recently_played_sudoku_id", 0);
        if (canResume(mSudokuGameID)) {
            mResumeButton.setVisibility(View.VISIBLE);
            mResumeButton.setOnClickListener((view) -> {
                Intent intentToPlay = new Intent(TitleScreenActivity.this, SudokuPlayActivity.class);
                intentToPlay.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, mSudokuGameID);
                startActivity(intentToPlay);
            });
        } else {
            mResumeButton.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_SETTINGS:
                startActivity(new Intent(this, GameSettingsActivity.class));
                return true;

            case MENU_ITEM_ABOUT:
                showDialog(DIALOG_ABOUT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onResume() {
        super.onResume();

        setupResumeButton();
    }
}
