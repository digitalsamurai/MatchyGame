package com.digitalsamuraigames.matchygame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {
    GameBoard mBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mBoard = (GameBoard) findViewById(R.id.board_grid);
        mBoard.initializeBoardCells();
    }
}
