package com.digitalsamuraigames.matchygame;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.Random;

public class GameBoard extends ViewGroup {
    private final int NUM_COLUMNS = 6;
    private final int NUM_ROWS = 6;
    private final int NUM_VALUES = 5;

    private Cell[][] cells = new Cell[NUM_COLUMNS][NUM_ROWS];
    private Cell selectedCell = null;
    private int selectedCellRow;
    private int selectedCellCol;

    private ArrayList<ArrayList<Cell>> matchGroups = new ArrayList<ArrayList<Cell>>();

    private float touchDownX;
    private float touchDownY;

    private ScaleAnimation selectAnimation;

    private float maxSwipeDistance = 100;

    private Context mContext;

    private enum BoardState {
        INITIAL_SETUP,
        WAITING_FOR_INPUT,
        SWAPPING_CELLS,
        FILLING_BOARD
    }

    private BoardState mBoardState;

    public GameBoard(Context context) {
        super(context);
        mContext = context;
        mBoardState = BoardState.INITIAL_SETUP;
    }

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mBoardState = BoardState.INITIAL_SETUP;
    }

    public GameBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mBoardState = BoardState.INITIAL_SETUP;
    }

    public void createAnimations() {
        selectAnimation = new ScaleAnimation(1.0f, 1.25f, 1.0f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        selectAnimation.setInterpolator(new LinearInterpolator());
        selectAnimation.setDuration(250);
        selectAnimation.setZAdjustment(Animation.ZORDER_TOP);
        selectAnimation.setRepeatCount(Animation.INFINITE);
        selectAnimation.setRepeatMode(Animation.REVERSE);
        selectAnimation.setFillEnabled(false);
    }

    public void initializeBoardCells() {
        clearAllCells();

        int[][] boardValues;
        do {
            boardValues = createBoardValues(NUM_COLUMNS, NUM_ROWS);
        } while (!matchesAvailable(boardValues));

        addInitialCellsToBoard(boardValues);
    }

    private int[][] createBoardValues(int cols, int rows) {
        Random r = new Random();

        int[][] tempBoard = new int[cols][rows];

        for (int row = 0; row < tempBoard[0].length; row++) {
            for (int col = 0; col < tempBoard.length; col++) {
                boolean potentialVertMatch;
                boolean potentialHorMatch;
                int nextCellValue;
                if (row >= 2){
                    if (col >= 2) {
                        potentialHorMatch = tempBoard[col - 1][row] == tempBoard[col - 2][row];
                        potentialVertMatch = tempBoard[col][row - 1] == tempBoard[col][row - 2];
                        do {
                            nextCellValue = r.nextInt(NUM_VALUES) + 1;
                        } while ((potentialHorMatch && nextCellValue == tempBoard[col - 1][row]) ||
                                (potentialVertMatch && nextCellValue == tempBoard[col][row - 1]));
                    } else {
                        potentialVertMatch = tempBoard[col][row - 1] == tempBoard[col][row - 2];
                        do {
                            nextCellValue = r.nextInt(NUM_VALUES) + 1;
                        } while (potentialVertMatch && nextCellValue == tempBoard[col][row - 1]);
                    }
                } else {
                    if (col >= 2) {
                        potentialHorMatch = tempBoard[col - 1][row] == tempBoard[col - 2][row];
                        do {
                            nextCellValue = r.nextInt(NUM_VALUES) + 1;
                        } while (potentialHorMatch && nextCellValue == tempBoard[col - 1][row]);
                    } else {
                        nextCellValue = r.nextInt(NUM_VALUES) + 1;
                    }
                }
                tempBoard[col][row] = nextCellValue;
            }
        }

        return tempBoard;
    }

    private void addInitialCellsToBoard(int[][] aBoard) {
        for (int row = 0; row < aBoard[0].length; row++) {
            for (int col = 0; col < aBoard.length; col++) {
                Cell aCell = new Cell(mContext, aBoard[col][row]);
                aCell.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
                aCell.setPadding(10, 10, 10, 10);
                cells[col][row] = aCell;
                this.addView(cells[col][row]);
            }
        }
    }

    private void clearAllCells() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                cells[col][row] = null;
            }
        }
    }

    private void swapSelectedCell(int direction) {
        if (selectedCell != null) {
            selectedCell.clearAnimation();

            // Get selectedCell row and col to check for valid direction
            for (int i = 0; i < cells[0].length; i++) {
                for (int j = 0; j < cells.length; j++) {
                    if (cells[j][i] == selectedCell) {
                        selectedCellCol = j;
                        selectedCellRow = i;
                        j = cells.length;
                        i = cells[0].length;
                    }
                }
            }

            // Check for valid direction
            if ((direction == 0 && selectedCellRow < cells[0].length - 1) || (direction == 1 && selectedCellCol < cells.length - 1) ||
                    (direction == 2 && selectedCellRow > 0) || (direction == 3 && selectedCellCol > 0)) {

                final int swapDistance = selectedCell.getMeasuredHeight();

                final int translationX, translationY;
                switch (direction) {
                    case 0:
                        translationX = 0;
                        translationY = 1;
                        break;
                    case 1:
                        translationX = 1;
                        translationY = 0;
                        break;
                    case 2:
                        translationX = 0;
                        translationY = -1;
                        break;
                    case 3:
                        translationX = -1;
                        translationY = 0;
                        break;
                    default:
                        translationX = 0;
                        translationY = 0;
                        break;
                }

                final Cell swappedCell = cells[selectedCellCol + translationX][selectedCellRow + translationY];
                cells[selectedCellCol][selectedCellRow] = swappedCell;
                cells[selectedCellCol + translationX][selectedCellRow + translationY] = selectedCell;
                swappedCell.animate().xBy(swapDistance * -translationX).yBy(swapDistance * translationY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(250).start();
                selectedCell.animate().xBy(swapDistance * translationX).yBy(swapDistance * -translationY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(250).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!checkForMatches()) {
                            // Swap cells back
                            cells[selectedCellCol][selectedCellRow] = selectedCell;
                            cells[selectedCellCol + translationX][selectedCellRow + translationY] = swappedCell;
                            swappedCell.animate().xBy(swapDistance * translationX).yBy(swapDistance * -translationY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(250).start();
                            selectedCell.animate().xBy(swapDistance * -translationX).yBy(swapDistance * translationY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(250).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (mBoardState != BoardState.WAITING_FOR_INPUT) {
                                        mBoardState = BoardState.WAITING_FOR_INPUT;
                                    }
                                    selectedCell = null;
                                }
                            }).start();
                        } else {
                            // Process matched cells
                            processCellMatches();
                        }
                    }
                }).start();
            } else {
                if (mBoardState != BoardState.WAITING_FOR_INPUT) {
                    mBoardState = BoardState.WAITING_FOR_INPUT;
                }
                selectedCell = null;
            }
        }
    }

    private boolean checkForMatches() {
        // Create temporary Match Group
        ArrayList<Cell> matchGroup = new ArrayList<Cell>();

        // Get horizontal matches
        for (int row = 0; row < cells[0].length; row++) {
            // Check if matchGroup size >= 3 and add to matchGroups (to account for a matchGroup that ends at end of row
            if(matchGroup.size() >= 3) {
                matchGroups.add((ArrayList<Cell>)matchGroup.clone());
            }
            matchGroup.clear();

            for (int col = 0; col < cells.length; col++) {
                if (matchGroup.isEmpty()) {
                    matchGroup.add(cells[col][row]);
                } else {
                    // Check if current cell matches value of cells in matchGroup
                    if (cells[col][row].getValue() == matchGroup.get(0).getValue()) {
                        matchGroup.add(cells[col][row]);
                    } else {
                        if (matchGroup.size() >= 3) {
                            matchGroups.add((ArrayList<Cell>)matchGroup.clone());
                        }
                        matchGroup.clear();
                        matchGroup.add(cells[col][row]);
                    }
                }
            }
        }

        // Get vertical matches
        for (int col = 0; col < cells.length; col++) {
            // Check if matchGroup size >= 3 and add to matchGroups (to account for a matchGroup that ends at end of row
            if(matchGroup.size() >= 3) {
                matchGroups.add((ArrayList<Cell>)matchGroup.clone());
            }
            matchGroup.clear();

            for (int row = 0; row < cells[0].length; row++) {
                if (matchGroup.isEmpty()) {
                    matchGroup.add(cells[col][row]);
                } else {
                    // Check if current cell matches value of cells in matchGroup
                    if (cells[col][row].getValue() == matchGroup.get(0).getValue()) {
                        matchGroup.add(cells[col][row]);
                    } else {
                        if (matchGroup.size() >= 3) {
                            matchGroups.add((ArrayList<Cell>)matchGroup.clone());
                        }
                        matchGroup.clear();
                        matchGroup.add(cells[col][row]);
                    }
                }
            }
        }

        if (matchGroups.size() > 0) {
            setCellMatchStatuses(true);
            return true;
        }

        return false;
    }

    private void setCellMatchStatuses(boolean matchStatus) {
        for (int i = 0; i < matchGroups.size(); i++) {
            for (int j = 0; j < matchGroups.get(i).size(); j++) {
                matchGroups.get(i).get(j).setMatched(matchStatus);
            }
        }
    }

    private void processCellMatches() {
        combineMatchGroups();

        for (int i = 0; i < matchGroups.size(); i++) {
            for (int j = 0; j < matchGroups.get(i).size(); j++) {
                if (!(i == matchGroups.size() - 1 && j == matchGroups.get(i).size() - 1)) {
                    matchGroups.get(i).get(j).animate().alpha(0.0f).setInterpolator(new LinearInterpolator()).setDuration(250).start();
                } else {
                    matchGroups.get(i).get(j).animate().alpha(0.0f).setInterpolator(new LinearInterpolator()).setDuration(250).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            // Initialize variable to store cell with longest distance to determine which cell gets ending runnable
                            Cell cellWithLongestDistance = null;
                            int longestDistance = 0;

                            // Move matched cells, set new value, animate, etc.
                            for (int col = 0; col < cells.length; col++) {
                                int numMatches = 0;
                                for (int row = 0; row < cells[0].length - numMatches; row++) {
                                    if (cells[col][row].isMatched()) {
                                        numMatches++;

                                        Cell matchedCell = cells[col][row];

                                        // Move cells above matchedCell down and move matchedCell to top
                                        for (int i = row + 1; i < cells[0].length; i++) {
                                            cells[col][i - 1] = cells[col][i];
                                        }
                                        cells[col][cells[0].length - 1] = matchedCell;

                                        // Set new value for matchedCell and changed isMatched to false
                                        matchedCell.setMatched(false);
                                        matchedCell.getNewRandomValue();

                                        // Set layout position of matchedCell above board
                                        int width = matchedCell.getMeasuredWidth();
                                        int height = matchedCell.getMeasuredHeight();
                                        int left = col * width;
                                        int top = numMatches * height * -1;
                                        matchedCell.layout(left, top, left + width, top + height);
                                        matchedCell.setTranslationX(0.0f);
                                        matchedCell.setTranslationY(0.0f);

                                        // Reset alpha to 1.0f
                                        matchedCell.setAlpha(1.0f);
                                    }

                                    // Check if cell is at home position then animate as needed
                                    int height = cells[col][row].getMeasuredHeight();
                                    int homeTop = getMeasuredHeight() - (height * (row + 1));
                                    if (cells[col][row].getTop() != homeTop) {
                                        // Update farthest cell
                                        int distanceToHome = homeTop - cells[col][row].getTop();
                                        if (cellWithLongestDistance == null || distanceToHome > longestDistance) {
                                            cellWithLongestDistance = cells[col][row];
                                            longestDistance = distanceToHome;
                                        }

                                        // Set animation
                                        cells[col][row].animate().yBy(distanceToHome).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(250 * (distanceToHome / height));
                                    }
                                }
                            }

                            // Start animations, set callback for cell with longest distance
                            for (int col = 0; col < cells.length; col++) {
                                for (int row = 0; row < cells[0].length; row++) {
                                    if (cells[col][row] != cellWithLongestDistance) {
                                        cells[col][row].animate().start();
                                    } else {
                                        cells[col][row].animate().withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Set layouts for all Cells
                                                for (int col = 0; col < cells.length; col++) {
                                                    for (int row = 0; row < cells[0].length; row++) {
                                                        int width = cells[col][row].getMeasuredWidth();
                                                        int height = cells[col][row].getMeasuredHeight();
                                                        int left = col * width;
                                                        int top = getMeasuredHeight() - (height * (row + 1));
                                                        cells[col][row].layout(left, top, left + width, top + height);
                                                    }
                                                }

                                                // Clear old match groups
                                                matchGroups.clear();

                                                if (!checkForMatches()) {
                                                    mBoardState = BoardState.WAITING_FOR_INPUT;
                                                    selectedCell = null;
                                                } else {
                                                    processCellMatches();
                                                }
                                            }
                                        }).start();
                                    }
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    }

    private void combineMatchGroups() {
        if (matchGroups.size() > 1) {
            for (int i = 0; i < matchGroups.size() - 1; i++) {
                for (int j = i + 1; j < matchGroups.size(); j++) {
                    // Check if match groups' cells have same value
                    if (matchGroups.get(i).get(0).getValue() == matchGroups.get(j).get(0).getValue()) {
                        // Check if any cells match
                        boolean hasMatchingCells = false;
                        for (int k = 0; k < matchGroups.get(j).size(); k++) {
                            if(matchGroups.get(i).contains(matchGroups.get(j).get(k))) {
                                hasMatchingCells = true;
                                k = matchGroups.get(j).size();
                            }
                        }
                        if (hasMatchingCells) {
                            // Combine match groups
                            matchGroups.get(j).removeAll(matchGroups.get(i));
                            matchGroups.get(i).addAll(matchGroups.get(j));
                            matchGroups.remove(j);
                            j--;
                        }
                    }
                }
            }
        }
    }

    private boolean matchesAvailable(int[][] aBoard) {
        for (int row = 0; row < aBoard[0].length; row++) {
            for (int col = 0; col < aBoard.length; col++) {
                int[] neighbors = getNeighbors(aBoard, col, row);
                int[] neighborCount = new int[NUM_VALUES + 1];
                for (int direction = 0; direction < neighbors.length; direction++) {
                    neighborCount[neighbors[direction]] += 1;
                }
                for (int neighborValue = 1; neighborValue < neighborCount.length; neighborValue++) {
                    if (neighborCount[neighborValue] >= 3) {
                        return true;
                    } else if (neighborCount[neighborValue] == 2) {
                        for (int j = 0; j < neighbors.length; j++) {
                            if (neighbors[j] == neighborValue) {
                                switch (j) {
                                    case 0:
                                        if (row < aBoard[0].length - 2 && aBoard[col][row + 1] == aBoard[col][row + 2]) {
                                            return true;
                                        }
                                    case 1:
                                        if (col < aBoard.length - 2 && aBoard[col + 1][row] == aBoard[col + 2][row]) {
                                            return true;
                                        }
                                    case 2:
                                        if (row > 1 && aBoard[col][row - 1] == aBoard[col][row - 2]) {
                                            return true;
                                        }
                                    case 3:
                                        if (col > 1 && aBoard[col - 1][row] == aBoard[col - 2][row]) {
                                            return true;
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private int[] getNeighbors(int[][] aBoard, int col, int row) {
        int[] neighbors = new int[4];

        for (int i = 0; i < neighbors.length; i++) {
            switch (i) {
                case 0:
                    if (row < aBoard[0].length - 1) {
                        neighbors[i] = aBoard[col][row + 1];
                    } else {
                        neighbors[i] = 0;
                    }
                    break;
                case 1:
                    if (col < aBoard.length - 1) {
                        neighbors[i] = aBoard[col + 1][row];
                    } else {
                        neighbors[i] = 0;
                    }
                    break;
                case 2:
                    if (row > 0) {
                        neighbors[i] = aBoard[col][row - 1];
                    } else {
                        neighbors[i] = 0;
                    }
                    break;
                case 3:
                    if (col > 0) {
                        neighbors[i] = aBoard[col - 1][row];
                    } else {
                        neighbors[i] = 0;
                    }
                    break;
            }
        }

        return neighbors;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        switch (mBoardState) {
            case INITIAL_SETUP:
                super.onMeasure(widthSpec, widthSpec);
                int count = getChildCount();
                int boardWidth = this.getMeasuredWidth();
                int childWidth = (int)(boardWidth * 1.0f/cells[0].length);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
                int boardHeight = this.getMeasuredHeight();
                int childHeight = (int)(boardHeight * 1.0f/cells.length);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);

                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
                }

                createAnimations();

                maxSwipeDistance = this.getMeasuredWidth() / cells.length / 2.0f;

                break;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (mBoardState) {
            case INITIAL_SETUP:
                for (int row = 0; row < cells[0].length; row++) {
                    for (int col = 0; col < cells.length; col++) {
                        Cell aCell = cells[col][row];
                        int width = aCell.getMeasuredWidth();
                        int height = aCell.getMeasuredHeight();
                        int cellLeft = col * width;
                        int cellTop = (row + 1) * -height;

                        aCell.layout(cellLeft,cellTop,cellLeft + width,cellTop + height);

                        if (!(row == cells[0].length - 1 && col == cells.length - 1)) {
                            aCell.animate().yBy(this.getMeasuredHeight()).setDuration(1000).start();
                        } else {
                            aCell.animate().yBy(this.getMeasuredHeight()).setDuration(1000).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    layoutCellsInHomePositions();
                                    if (mBoardState != BoardState.WAITING_FOR_INPUT) {
                                        mBoardState = BoardState.WAITING_FOR_INPUT;
                                    }
                                }
                            }).start();
                        }
                    }
                }
                break;
            case WAITING_FOR_INPUT:
                layoutCellsInHomePositions();
                break;
        }
    }

    private void layoutCellsInHomePositions() {
        for (int col = 0; col < cells.length; col++) {
            for (int row = 0; row < cells[0].length; row++) {
                Cell aCell = cells[col][row];
                int width = aCell.getMeasuredWidth();
                int height = aCell.getMeasuredHeight();
                int left = col * width;
                int top = getMeasuredHeight() - ((row + 1) * height);
                aCell.layout(left, top, left + width, top + height);
                aCell.setTranslationY(0.0f);
                aCell.setTranslationX(0.0f);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch(action) {
            case (MotionEvent.ACTION_DOWN):
                if (mBoardState == BoardState.WAITING_FOR_INPUT) {
                    touchDownX = event.getX();
                    touchDownY = event.getY();
                    for (int i = 0; i < getChildCount(); i++) {
                        Cell aCell = (Cell) getChildAt(i);
                        if (touchDownX >= aCell.getLeft() && touchDownX < aCell.getLeft() + aCell.getMeasuredWidth() && touchDownY >= aCell.getTop() && touchDownY < aCell.getTop() + aCell.getMeasuredHeight()) {
                            selectedCell = aCell;
                            aCell.startAnimation(selectAnimation);
                        }
                    }
                }
                return true;
            case (MotionEvent.ACTION_MOVE):
                if (selectedCell != null && mBoardState == BoardState.WAITING_FOR_INPUT) {
                    // Check move distance
                    if (Math.hypot(event.getX() - touchDownX,event.getY() - touchDownY) >= maxSwipeDistance) {
                        mBoardState = BoardState.SWAPPING_CELLS;
                        // Get direction **NOTE: angles are swapped over x axis due to coordinates starting at 0,0 in top left of view
                        double angle = Math.toDegrees(Math.atan2(event.getY() - touchDownY, event.getX() - touchDownX));
                        if (angle >= 45 && angle < 135) {
                            swapSelectedCell(2);
                        } else if ((angle >= 135 && angle <= 180) || (angle >= -180 && angle < -135)) {
                            swapSelectedCell(3);
                        } else if (angle >= -135 && angle < -45) {
                            swapSelectedCell(0);
                        } else {
                            swapSelectedCell(1);
                        }
                    }
                }
                return true;
            case (MotionEvent.ACTION_UP):
                if (selectedCell != null && mBoardState == BoardState.WAITING_FOR_INPUT) {
                    selectedCell.clearAnimation();
                    selectedCell = null;
                }
                return true;
            case (MotionEvent.ACTION_CANCEL):
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
