package com.licktopia.thechampionofsudoku.Sudoku;

import android.content.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.licktopia.thechampionofsudoku.GameInterface;

/**
 * Created by John Konecny on 6/19/2017.
 */

//Runs a game of Sudoku
//UI uses only this class to interact with the game
public class Game implements GameInterface, java.io.Serializable
{

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    //imageNumber
    private int imageNumber;
    //time in seconds
    private long runningTime = 0;

    private ScoreWeights scoreWeights;

    //Time which timer was last started
    //time in seconds
    private long startTime = -1;

    //stores the total bonus given for the score
    private int level = -1;

    private int totalHintsUsed = -1;


    private Board solvedBoard;

    //context is need to load from assets folder
    public Game(int level, Context ctx)
    {
        this.level = level;
        curBoard = new Board(level, ctx, -1);
        solutionSolver = new Solver(curBoard);
        scoreWeights = ScoreCSV.getScoreWeights(level);
        totalHintsUsed = 0;
        solvedBoard = solutionSolver.solve();
        if(solvedBoard == null)
        {
            System.out.printf("Game cannot be solved!\n");
        }
    }

    public Game(long boardNumber, int level, int[] values)
    {
        this.level = level;
        curBoard = new Board(boardNumber, values);
        solutionSolver = new Solver(curBoard);
        scoreWeights = ScoreCSV.getScoreWeights(level);
        totalHintsUsed = 0;
        solvedBoard = solutionSolver.solve();
        if(solutionSolver.solve() == null)
        {
            System.out.printf("Game cannot be solved!\n");
        }
    }


    public Game(Game oldGame)
    {
        this.level = oldGame.level;
        this.curBoard = new Board(oldGame.curBoard);
        this.solutionSolver = new Solver(curBoard);
        this.solvedBoard = solutionSolver.solve();
        this.scoreWeights = oldGame.scoreWeights;
        this.totalHintsUsed = oldGame.totalHintsUsed;
    }

    private Board curBoard;

    //sets value of square given row and col
    //returns empty stack if successful
    //returns ids of problem squares in stack if failed
    public Set<Integer> setBoardValueAt(int row, int col, int value) {
        return this.curBoard.setValueAt(row, col, value);
    }

    //sets value of square given the index
    //returns empty stack if successful
    //returns ids of problem squares in stack if failed
    public Set<Integer> setBoardValueAt(int idx, int value) {
        return this.curBoard.setValueAt(idx, value);
    }

    //gets value of square given row and col
    public int getBoardValueAt(int row, int col) {
        return curBoard.getValueAt(row, col);
    }

    //gets board number
    public long getBoardNumber(){return curBoard.getBoardIdentifier();}

    //gets value of square given the index
    public int getBoardValueAt(int idx) {
        return curBoard.getValueAt(idx);
    }

    //gets number of 1's, 2's, etc
    public int[] getValueTotals(){
        int[] values = new int[9];
        Arrays.fill(values,0);
        for(int i = 0; i<Math.pow(curBoard.getSize(),2); i++){
            int val = getBoardValueAt(i);
            if(val!=0){
                values[val-1]++;
            }
        }
        return values;
    }

    //return if a square at the index is a starting value
    public boolean isStartingValue(int idx)
    {
        return curBoard.isStartingValue(idx);
    }


    //return if a square at row, col is a starting value
    public boolean isStartingValue(int row, int col)
    {
        return curBoard.isStartingValue(row, col);
    }

    //set cell notepad value for given num
    //for a square given its index for a single dimensional array of the board
    public void setBoardNotepadAt(int idx, int num, boolean value) {
        this.curBoard.setNotepadAt(idx, num, value);
    }

    //set cell notepad value for given num
    public void setBoardNotepadAt(int row, int col, int num, boolean value) {
        this.curBoard.setNotepadAt(row, col, num, value);
    }

    //toggles cell notepad value for given num
    //for a square given its index for a single dimensional array of the board
    public void toggleBoardNotepadAt(int idx, int num) {
        this.curBoard.setNotepadAt(idx, num, !this.getBoardNotepadAt(idx, num));
    }

    //toggles cell notepad value for given num
    public void toggleBoardNotepadAt(int row, int col, int num) {
        this.curBoard.setNotepadAt(row, col, num, !this.getBoardNotepadAt(row, col, num));
    }

    //gets the notepad value for boolean num
    // for a square given its index for a single dimensional array of the board
    public boolean getBoardNotepadAt(int idx, int num) {
        return curBoard.getNotepadAt(idx, num);
    }

    //get cell notepad value for given num
    public boolean getBoardNotepadAt(int row, int col, int num) {
        return curBoard.getNotepadAt(row, col, num);
    }

    //returns index of a altered cell after an undo
    //returns -1 if no undo occurred
    public int undo()
    {
        return curBoard.undo();
    }

    //returns true if a undo can occur
    public boolean canUndo()
    {
        return curBoard.canUndo();
    }

    //returns index of a altered cell after an redo
    //returns -1 if no redo occurred
    public int redo()
    {
        return curBoard.redo();
    }

    //returns true if a redo can occur
    public boolean canRedo()
    {
        return curBoard.canRedo();
    }

    //returns true if the save was successful
    public boolean saveGame()
    {
        return false;
    }

    private boolean isPaused = true;
    //pauses timer
    public void pauseTimer()
    {
        //don't do anything if already paused
        if(!isPaused)
        {
            isPaused = true;
            runningTime = this.getRunningTime();
            startTime = -1;
        }
    }

    public void unpauseTimer()
    {
        //don't do anything if already unpaused
        if(isPaused)
        {
            isPaused = false;
            startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }
    }

    //returns running time in seconds
    public long getRunningTime()
    {
        //clock has not be unpaused
        if(startTime < 0)
        {
            return runningTime;
        }
        long endTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        return runningTime + endTime - startTime;
    }

    //gets the total score for the game
    public long getScore()
    {
        if(!isGameFinished)
        {
            return getLevelBonus() +
                    getTotalOneTimeGuesses() * getOneTimeGuessSquareBonus() -
                    (getTimePenalty() * totalPenaltyTimeIntervals()) -
                    (long)(getIncorrectGuessesPenalty() * (double)totalIncorrectGuesses()) -
                    (getHintPenalty() * getTotalHintsUsed());
        }
        else
        {
            return getLevelBonus() +
                    getTotalOneTimeGuesses() * getOneTimeGuessSquareBonus() -
                    (getTimePenalty() * totalPenaltyTimeIntervals()) -
                    (long)(getIncorrectGuessesPenalty() * (double)totalIncorrectGuesses()) +
                    getEarlyFinishBonus() * (getEarlyFinishTime() - getRunningTime()) -
                    (getHintPenalty() * getTotalHintsUsed());
        }

    }

    public long getLevelBonus()
    {
        return scoreWeights.getBasePoints();
    }

    public long getTotalOneTimeGuesses()
    {
        long totalOneTimeGuesses = 0;
        for(int i = 0; i < curBoard.totalBoardElements(); i++)
        {
            if(curBoard.noMoreThanOneGuessAt(i) &&
                    !curBoard.isStartingValue(i) &&
                    curBoard.getValueAt(i) != 0)
            {
                totalOneTimeGuesses++;
            }
        }
        return totalOneTimeGuesses;
    }


    public long getOneTimeGuessSquareBonus()
    {
        return scoreWeights.getOneTimeGuessBonus();
    }

    //in points per seconds
    public long getTimePenalty() { return scoreWeights.getRunningTimePenalty(); }

    //in points per seconds
    public double getIncorrectGuessesPenalty()
    {
        return scoreWeights.getIncorrectGuessPenalty();
    }

    public long getRunningTimeRemovalInterval() {return scoreWeights.getRunningTimeRemovalInterval();}

    // Ensures that points are only removed when running time hit an interval set by
    // getRunningTimeRemovalInterval
    // Example: getRunningTimeRemovalInterval = 25 secs and getTimePenalty = 10 pts/secs
    // At 25 secs total penalty is 10 pts and this function would return 1 intervals
    // At 49 secs total penalty is 10 pts and this function would return 1 intervals
    // At 50 secs total penalty is 20 pts and this function would return 2 intervals
    public long totalPenaltyTimeIntervals()
    {
        long test = getRunningTime() / getRunningTimeRemovalInterval();

        return getRunningTime() / getRunningTimeRemovalInterval();
    }

    public long totalIncorrectGuesses()
    {
        return curBoard.totalIncorrectGuesses();
    }

    public double getAccuracy() {
        return (double)(getTotalOneTimeGuesses())
                / (double)curBoard.getStartingOpenSquares();
    }

    public long getEarlyFinishBonus() {return  scoreWeights.getEarlyFinishBonus();}

    public long getEarlyFinishTime() {return  scoreWeights.getEarlyFinishTime();}

    public long getHintPenalty() {return  scoreWeights.getHintPenalty();}

    public int  getTotalHintsUsed() {return totalHintsUsed;}

    //stores solution to game
    private Solver solutionSolver;

    public Solver getSolutionSolver(){ return solutionSolver;}

    //passes current state of game to new solver
    public void updateSolver() {
        solutionSolver = new Solver(curBoard);
    }

    //returns the steps required to solve from a blank board
    public Queue<Square> getSolutionSteps()
    {
        updateSolver();
        solutionSolver.solveWithStrategies();
        return solutionSolver.getExactSolution();
    }

    // Returns a set of the squares in the current board that are wrong
    public Set<Integer> checkCurrentProgress() {
        Set<Integer> incorrectSquares = new HashSet<>();

        for (int i = 0; i < totalBoardElements(); i++) {
            if (curBoard.getValueAt(i) != 0 && curBoard.getValueAt(i) != solvedBoard.getValueAt(i)) {
                incorrectSquares.add(i);
            }
        }

        return incorrectSquares;
    }

    public void updateAllNotepads(boolean removeOnly) {
        Strategies.updateAllNotepads(curBoard, removeOnly);
    }

    public void backupAllNotepads() {
        curBoard.backupAllNotepads();
    }

    public void restoreAllNotepads() {
        curBoard.restoreAllNotepads();
    }

    public Strategies.Hint getHint(int hintType) {
        List<Strategies.Hint> hints;
        totalHintsUsed++;
        switch(hintType) {
            case Strategies.NAKED_SINGLE: hints = Strategies.getNakedSingles(curBoard); break;
            case Strategies.SINGLES_CHAIN: hints = Strategies.getSinglesChains(curBoard); break;
            case Strategies.YWING: hints = Strategies.getYWings(curBoard); break;
            case Strategies.NAKED_TRIPLE: hints = Strategies.getNakedTriples(curBoard); break;
            case Strategies.NAKED_DOUBLE: hints = Strategies.getNakedPairs(curBoard); break;
            case Strategies.HIDDEN_SINGLE: hints = Strategies.getHiddenSingles(curBoard); break;
            case Strategies.HIDDEN_PAIR: hints = Strategies.getHiddenPairs(curBoard); break;
            case Strategies.LOCKED_CANDIDATE: hints = Strategies.getLockedCandidates(curBoard); break;
            case Strategies.XWING: hints = Strategies.getXWings(curBoard); break;
            default: hints = Strategies.getHiddenSingles(curBoard); break;
        }
        if (hints.isEmpty()) return null;
        Strategies.Hint hint = Strategies.getRandomElement(hints);
        return hint;
    }

    public void removeNakedDoubles(Strategies.Hint hint) {
        //Strategies.removeNakedPairValues(curBoard, hint);
    }

    //returns the total number of elements in the board
    public  int totalBoardElements() {return curBoard.totalBoardElements(); }

    //returns the total number of rows in the board
    public  int totalBoardRows() {return curBoard.totalBoardRows(); }

    //returns the total number of columns in the board
    public  int totalBoardCols() {return curBoard.totalBoardCols(); }

    //returns the column/row size of a cluster
    public  int getClusterSize() {return curBoard.getClusterSize(); }

    private boolean isGameFinished = false;
    //true if the board is complete
    public boolean isGameFinished()
    {
        if(curBoard.isBoardFull())
        {
            isGameFinished = true;
        }
        else
        {
            isGameFinished = false;
        }
        return isGameFinished;
    }

    public void resetGame()
    {
        curBoard.resetToStart();
    }
    public int getNumberOpenSquares(){
        int val = 0;
        val = curBoard.getOpenSquares();
        return val;
    }

    /*
    returns a copy of the game's internal board
     */
    private Board getBoardCopy(){return new Board(this.curBoard);}


    public int getStartingOpenSquares() { return this.curBoard.getStartingOpenSquares(); }
}
