

package com.myapp.app101.game;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class CellNote {

    public static final CellNote EMPTY = new CellNote();
    private final short mNotedNumbers;

    public CellNote() {
        mNotedNumbers = 0;
    }

    private CellNote(short notedNumbers) {
        mNotedNumbers = notedNumbers;
    }


    public static CellNote deserialize(String note) {
        return deserialize(note, CellCollection.DATA_VERSION);
    }

    public static CellNote deserialize(String note, int version) {

        int noteValue = 0;
        if (note != null && !note.equals("") && !note.equals("-")) {
            if (version == CellCollection.DATA_VERSION_1) {
                StringTokenizer tokenizer = new StringTokenizer(note, ",");
                while (tokenizer.hasMoreTokens()) {
                    String value = tokenizer.nextToken();
                    if (!value.equals("-")) {
                        int number = Integer.parseInt(value);
                        noteValue |= (1 << (number - 1));
                    }
                }
            } else {

                noteValue = Integer.parseInt(note);
            }
        }

        return new CellNote((short) noteValue);
    }



    public static CellNote fromIntArray(Integer[] notedNums) {
        int notedNumbers = 0;

        for (Integer n : notedNums) {
            notedNumbers = (short) (notedNumbers | (1 << (n - 1)));
        }

        return new CellNote((short) notedNumbers);
    }



    public void serialize(StringBuilder data) {
        data.append(mNotedNumbers);
        data.append("|");
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }


    public List<Integer> getNotedNumbers() {

        List<Integer> result = new ArrayList<>();
        int c = 1;
        for (int i = 0; i < 9; i++) {
            if ((mNotedNumbers & (short) c) != 0) {
                result.add(i + 1);
            }
            c = (c << 1);
        }

        return result;
    }


    public CellNote toggleNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers ^ (1 << (number - 1))));
    }


    public CellNote addNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers | (1 << (number - 1))));
    }


    public CellNote removeNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers & ~(1 << (number - 1))));
    }

    public boolean hasNumber(int number) {
        if (number < 1 || number > 9) {
            return false;
        }

        return (mNotedNumbers & (1 << (number - 1))) != 0;
    }

    public CellNote clear() {
        return new CellNote();
    }


    public boolean isEmpty() {
        return mNotedNumbers == 0;
    }

}
