

package com.myapp.app101.game;

import android.content.Context;

import com.myapp.app101.R;


public class FolderInfo {


    public long id;


    public String name;


    public int puzzleCount;


    public int solvedCount;


    public int playingCount;

    public FolderInfo() {

    }

    public FolderInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getDetail(Context c) {
        StringBuilder sb = new StringBuilder();

        if (puzzleCount == 0) {
            sb.append(c.getString(R.string.no_puzzles));
        } else {
            sb.append(puzzleCount == 1 ? c.getString(R.string.one_puzzle) : c.getString(R.string.n_puzzles, puzzleCount));

            int unsolvedCount = puzzleCount - solvedCount;

            if (playingCount != 0 || unsolvedCount != 0) {
                sb.append(" (");

                if (playingCount != 0) {
                    sb.append(c.getString(R.string.n_playing, playingCount));
                    if (unsolvedCount != 0) {
                        sb.append(", ");
                    }
                }

                if (unsolvedCount != 0) {
                    sb.append(c.getString(R.string.n_unsolved, unsolvedCount));
                }

                sb.append(")");
            }

            if (unsolvedCount == 0 && puzzleCount != 0) {
                sb.append(" (").append(c.getString(R.string.all_solved)).append(")");
            }

        }

        return sb.toString();

    }

}
