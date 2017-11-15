package com.digitalsamuraigames.matchygame;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

import java.util.Random;

public class Cell extends AppCompatImageView{
    private final int NUM_VALUES = 5;
    private int value;
    private Random r = new Random();

    private boolean isMatched = false;

    public Cell(Context context) {
        super(context);
        getNewRandomValue();
    }

    public Cell(Context context, int value) {
        super(context);
        this.value = value;
        setCellImage();
    }

    private void setCellImage() {
        switch (getValue()) {
            case 1: setImageResource(R.drawable.circle_black); break;
            case 2: setImageResource(R.drawable.circle_blue); break;
            case 3: setImageResource(R.drawable.circle_green); break;
            case 4: setImageResource(R.drawable.circle_red); break;
            case 5: setImageResource(R.drawable.circle_white); break;
        }
    }

    public void getNewRandomValue() {
        value = r.nextInt(NUM_VALUES - 1) + 1;
        setCellImage();
    }

    public int getValue() {
        return value;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }
}
