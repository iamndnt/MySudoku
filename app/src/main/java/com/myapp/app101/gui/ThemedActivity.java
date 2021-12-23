package com.myapp.app101.gui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.myapp.app101.utils.ThemeUtils;

public class ThemedActivity extends AppCompatActivity {
    private int mThemeId = 0;
    private long mTimestampWhenApplyingTheme = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        mTimestampWhenApplyingTheme = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    }
}
