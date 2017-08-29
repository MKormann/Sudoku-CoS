package com.licktopia.thechampionofsudoku;

import com.licktopia.thechampionofsudoku.Sudoku.Solver;
import com.licktopia.thechampionofsudoku.Sudoku.Square;
import com.licktopia.thechampionofsudoku.Sudoku.Strategies;

import java.util.Queue;
import java.util.Set;

/**
 * Created by John Konecny on 7/25/2017.
 */

public interface GameInterface {

    /*******************
     * Get the index image of image used in art mode
     * @return image number used for Sudoku art mode
     */
    public int getImageNumber();

    /****************************************
     * Set the index image of image used in art mode
     * @param imageNumber corresponding index of image used in art mode
     */
    public void setImageNumber(int imageNumber);



    /**************************************************
     * sets value of square given row and col
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @param value 0-9 int for square to be filled with
     * @return ids of problem squares in stack if failed. Empty stack if successful.
     */
    public abstract Set<Integer> setBoardValueAt(int row, int col, int value);


    /******************************************************
     * sets value of square given the index
     * @param idx index of square on Sudoku board
     * @param value 0-9 int for square to be filled with
     * @return ids of problem squares in stack if failed. Empty stack if successful.
     */
    public Set<Integer> setBoardValueAt(int idx, int value);

    //gets value of square given row and col

    /********************************************************
     * gets value of square given row and col
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @return value of square given row and col
     */
    public int getBoardValueAt(int row, int col);

    //gets board number

    /*******************************************
     * gets board number index for Sudoku Game
     * @return board number index for Sudoku Game
     */
    public long getBoardNumber();


    /************************************************
     * gets value of square given the index
     * @param idx index of square on Sudoku board
     * @return value of square given the index
     */
    public int getBoardValueAt(int idx);

    //gets number of 1's, 2's, etc

    /************************************************
     * gets number of 1's, 2's, etc
     * @return number of 1's, 2's, etc
     */
    public int[] getValueTotals();


    /************************************************
     * true if a square at the index is a starting value
     * @param idx index of square on Sudoku board
     * @return true if a square at the index is a starting value
     */
    public boolean isStartingValue(int idx);


    /************************************************
     * true if a square at the index is a starting value
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @return true if a square at the index is a starting value
     */
    public boolean isStartingValue(int row, int col);


    /**************************************************
     * set cell notepad value for given num
     * @param idx index of square on Sudoku board
     * @param num the notepad number which is being changed
     * @param value the value the board value will be set to
     */
    public void setBoardNotepadAt(int idx, int num, boolean value);

    //set cell notepad value for given num

    /*****************************************************
     * set cell notepad value for given num
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @param num the notepad number which is being changed
     * @param value the value the board value will be set to
     */
    public void setBoardNotepadAt(int row, int col, int num, boolean value);


    /******************************************************
     * toggles cell notepad value for given num
     * @param idx index of square on Sudoku board
     * @param num the notepad number which is being changed
     */
    public void toggleBoardNotepadAt(int idx, int num);

    //toggles cell notepad value for given num

    /******************************************************
     * toggles cell notepad value for given num
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @param num the notepad number which is being changed
     */
    public void toggleBoardNotepadAt(int row, int col, int num);



    /*******************************************************
     * gets the notepad value for boolean num
     * @param idx index of square on Sudoku board
     * @param num the notepad number which boolean is being returned
     * @return the notepad value for boolean num
     */
    public boolean getBoardNotepadAt(int idx, int num);


    /****************************************************
     * the notepad value for boolean num
     * @param row row index of sqaure on Sudoku board
     * @param col column index of square on Sudoku board
     * @param num the notepad number which boolean is being returned
     * @return the notepad value for boolean num
     */
    public boolean getBoardNotepadAt(int row, int col, int num);

    //returns index of a altered cell after an undo
    //returns -1 if no undo occurred

    /***************************************************
     * undo last move
     * @return index of a altered cell after an undo. -1 if no undo occurred
     */
    public int undo();


    /**********************************
     * true if a undo can occur
     * @return true if a undo can occur
     */
    public boolean canUndo();


    /**************************************************
     * redo last move
     * @return index of a altered cell after an redo. -1 if no redo occurred
     */
    public abstract int redo();

    /******************************************************
     * true if a redo can occur
     * @return true if a redo can occur
     */
    public boolean canRedo();


    /******************************************************
     * pauses game timer
     */
    public void pauseTimer();

    /******************************************************
     * unpauses game timer
     */
    public void unpauseTimer();

    //returns running time in seconds

    /****************************************************
     * total time game timer has been running (not including time being paused)
     * @return total time game timer has been running (not including time being paused)
     */
    public long getRunningTime();

    /**************************************************
     * gets the total score for the game
     * @return the total score for the game
     */
    public long getScore();

    /**************************************************
     * Bonus score for level difficulty
     * @return Bonus score for level difficulty
     */
    public long getLevelBonus();

    /**************************************************
     * total number of square which are user entered and a number has been inputted only
     * once on it
     * @return total number of square which are user entered and a number has been inputted only
     * once on it
     */
    public long getTotalOneTimeGuesses();


    /**********************************************************************
     * bonus score for each one time guess
     * @return bonus score for each one time guess
     */
    public abstract long getOneTimeGuessSquareBonus();


    /**********************************************************************
     * Returns the points lost for each second of game time
     * @return the points lost for each second of game time
     */
    public abstract long getTimePenalty();


    /***************************************************************************
     * penalty for each incorrect guess
     * @return penalty for each incorrect guess
     */
    public double getIncorrectGuessesPenalty();

    /***********************************************************************
     * The interval when points will be taken off when for time
     * related scoring penalties
     * @return The interval when points will be taken off when for time
     * related scoring penalties
     */
    public long getRunningTimeRemovalInterval();


    /***************************************************************************
     * Ensures that points are only removed when running time hit an interval set by
     * getRunningTimeRemovalInterval
     * Example: getRunningTimeRemovalInterval = 25 secs and getTimePenalty = 10 pts/secs
     * At 25 secs total penalty is 10 pts and this function would return 1 intervals
     * At 49 secs total penalty is 10 pts and this function would return 1 intervals
     * At 50 secs total penalty is 20 pts and this function would return 2 intervals
     * @return the number of penalty time intervals hit in game time
     */
    public long totalPenaltyTimeIntervals();

    /*****************************************************************************
     * Total incorrect guesses made by the user
     * @return Total incorrect guesses made by the user
     */
    public long totalIncorrectGuesses();


    /*****************************************************************************
     * the penalty for using a hint, in points per hint
     * @return
     */

    public long getHintPenalty();

    /**************************************************
     * The total number of hints used
     * @return
     */
    public int  getTotalHintsUsed();

    /****************************************************************************
     * The percentage, as a fraction, of one time guesses to total user input squares
     * @return The percentage, as a fraction, of one time guesses to total user input squares
     */
    public double getAccuracy();

    /**************************************************************************
     * Total score given to the player given at the start
     * @return Total score given to the player given at the start
     */
    public long getEarlyFinishBonus();

    /*************************************************************************
     * The time when the early finish bonus goes to zero
     * @return The time when the early finish bonus goes to zero
     */
    public long getEarlyFinishTime();

    /************************************************************************
     * Solver object solution to the game
     * @return Solver object solution to the game
     */
    public Solver getSolutionSolver();


    /******************************************************
     * passes current state of game to new solver
     */
    public void updateSolver();


    /*****************************************************
     * returns the steps required to solve from a blank board
     * @return the steps required to solve from a blank board
     */
    public Queue<Square> getSolutionSteps();

    /**********************************************************************************
     * Update all notepad values to where only valid numbers are set to true
     * @param removeOnly when true, update will only remove notepad values, not add any
     */
    public void updateAllNotepads(boolean removeOnly);

    /**************************************************************************
     * hint object which has hint for given hintType
     * @param hintType eg NAKED_DOUBLE or HIDDEN_SINGLE see Strategies for accepted int values
     * @return hint object which has hint for given hintType
     */
    public abstract Strategies.Hint getHint(int hintType);

    /*********************************************************************
     * Everyone knows what this does
     * @param hint the hint this will happen to
     */
    public void removeNakedDoubles(Strategies.Hint hint);


    /********************************************************************
     * returns the total number of square on the board
     * @return The total number of square on the board
     */
    public int totalBoardElements();

    //returns the total number of rows in the board

    /******************************************************************
     * returns the total number of rows on the board
     * @return the total number of rows on the board
     */
    public int totalBoardRows();


    /*****************************************************************
     * returns the total number of columns in the board
     * @return the total number of columns in the board
     */
    public abstract int totalBoardCols();

    /***************************************************************
     * returns the column/row size of a cluster
     * @return the column/row size of a cluster
     */
    public int getClusterSize();


    /***************************************************************
     * true if the board is complete
     * @return true if the board is complete
     */
    public boolean isGameFinished();

    /******************************************************************
     * reset the values of the game to what they were at the start
     */
    public void resetGame();

    /****************************************************************
     * returns the total number of open square remaining
     * @return the total number of open square remaining
     */
    public int getNumberOpenSquares();

    /*************************************************************
     * gets the total number of starting open squares
     * @return total number of starting open squares
     */
    public int getStartingOpenSquares();

    /*************************************************************
     * gets the total number of starting open squares
     * @return total number of starting open squares
     */
    public Set<Integer> checkCurrentProgress();

    /*************************************************************
     * backs up the current state of the board's notepads
     */
    public void backupAllNotepads();

    /*************************************************************
     * restores the current state of the board's notepads
     */
    public void restoreAllNotepads();
}
