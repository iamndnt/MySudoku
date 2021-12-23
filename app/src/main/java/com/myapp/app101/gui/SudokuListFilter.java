
package com.myapp.app101.gui;

import android.content.Context;

import com.myapp.app101.R;
import com.myapp.app101.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SudokuListFilter {

    public boolean showStateNotStarted = true;
    public boolean showStatePlaying = true;
    public boolean showStateCompleted = true;
    private Context mContext;

    public SudokuListFilter(Context context) {
        mContext = context;
    }

    @Override
    public String toString() {
        List<String> visibleStates = new ArrayList<>();
        if (showStateNotStarted) {
            visibleStates.add(mContext.getString(R.string.not_started));
        }
        if (showStatePlaying) {
            visibleStates.add(mContext.getString(R.string.playing));
        }
        if (showStateCompleted) {
            visibleStates.add(mContext.getString(R.string.solved));
        }
        return StringUtils.join(visibleStates, ",");
    }
}
