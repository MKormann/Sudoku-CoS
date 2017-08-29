package com.licktopia.thechampionofsudoku.Sudoku;

import android.support.v4.util.Pair;
import android.util.Log;

import com.licktopia.thechampionofsudoku.SudokuSolver;


import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;


/**
 * Created by John Konecny on 6/19/2017.
 */

public class Solver implements java.io.Serializable {

    Board curBoard;
    Queue<Square> solvingSteps;
    Queue<Square> solvingStepsStrategies;
    int initialOpenSquares;

    public Solver(Board curBoard)
    {
        this.curBoard = new Board(curBoard);
        initialOpenSquares = curBoard.getOpenSquares();
        solvingStepsStrategies = new LinkedList<>();
    }

    //solves board using backtracking method and returns solved board
    //stores the steps used to solve along the way
    //ASSUMES ALL EMPTY SQUARES ARE EQUAL TO 0!!!
    //returns a null board if it cannot be solved
    protected  Board solve()
    {
        SudokuSolver sudokuSolver = new SudokuSolver(curBoard);
        int [] solvedBoard = sudokuSolver.getSolution();
        for(int i = 0; i < Math.pow(curBoard.getSize(),2); i++){
            curBoard.setValueAt(i,solvedBoard[i]);
        }

        return curBoard;
    }

    // Returns an int representing a difficulty score, calculated by how many strategies were used
    public int solveWithStrategies() {
        int strategiesUsed = 0;
        // Number of steps
        int squaresToComplete = curBoard.getOpenSquares();
        boolean cannotSolve = false;
        // Level of strategy to try
        int strategyNumber = 1;
        Strategies.updateAllNotepads(curBoard, false);
        // Iterate while there are still squares to fill or we can't solve any more
        while (squaresToComplete > 0 && !cannotSolve) {
            int beforeStrategiesUsed = strategiesUsed;
            switch(strategyNumber) {
                case Strategies.SINGLES_CHAIN:
                    strategiesUsed += Strategies.removeRandomSinglesChainValues(curBoard);
                case Strategies.YWING:
                    strategiesUsed += Strategies.removeRandomYWingValues(curBoard);
                case Strategies.XWING:
                    strategiesUsed += Strategies.removeRandomXWingValues(curBoard);
                case Strategies.LOCKED_CANDIDATE:
                    strategiesUsed += Strategies.removeRandomLockedCandidateValues(curBoard);
                case Strategies.NAKED_TRIPLE:
                    strategiesUsed += Strategies.removeRandomNakedTripleValues(curBoard);
                case Strategies.HIDDEN_PAIR:
                    strategiesUsed += Strategies.removeRandomHiddenPairValues(curBoard);
                case Strategies.HIDDEN_SINGLE:
                    strategiesUsed += Strategies.removeRandomNakedPairValues(curBoard);
                case Strategies.NAKED_DOUBLE:
                    strategiesUsed += Strategies.removeRandomHiddenSingle(curBoard);
                case Strategies.NAKED_SINGLE:
                    int index = Strategies.removeRandomNakedSingle(curBoard);
                    // If no squares are found
                    if (index == -1) {
                        // Not a single change was made from any strategy, we cannot solve at this point (YET)
                        if (strategyNumber == Strategies.NUM_STRATEGIES && (strategiesUsed - beforeStrategiesUsed == 0))
                            cannotSolve = true;
                        else if (strategyNumber != Strategies.NUM_STRATEGIES)// Try next strategy
                            strategyNumber++;
                    }
                    // Add step to queue, and repeat
                    else {
                        strategiesUsed++;
                        solvingStepsStrategies.add(curBoard.getSquare(index));
                        squaresToComplete--;
                        Strategies.updateAllNotepads(curBoard, true);
                    }
                    break;
            }
        }

        if (cannotSolve)
            return -1;
        else
            return strategiesUsed;
    }

    //updates all squares' notepads so that only valid moves are true
    private void updateAllNotepads(boolean removeOnly)
    {
        for(int i = 0; i < curBoard.totalBoardRows(); i++)
        {
            for(int j = 0; j < curBoard.totalBoardCols(); j++)
            {
                updateNotepad(i, j, removeOnly);
            }
        }
    }

    // updates notepad for square at row, col so that only valid moves are true
    // if removeOnly is set, update will only remove notepad values, not add any
    private void updateNotepad(int row, int col, boolean removeOnly)
    {
        if(!curBoard.isStartingValue(row, col))
        {
            for(int i = 1; i < curBoard.getSize(); i++)
            {


                Set<Integer> testStk = curBoard.setValueAt(row, col, i);
                if(testStk.isEmpty() && !removeOnly)
                {
                    curBoard.setNotepadAt(row, col, i, true);

                }
                else
                {
                    curBoard.setNotepadAt(row, col, i, false);
                }
                curBoard.setValueAt(row, col, 0);

            }
            //add the change to the state to the cell as a solving step
            solvingSteps.add(curBoard.getSquare(row, col));
        }

    }

    protected Queue<Square> getSolvingSteps()
    {
        return  solvingSteps;
    }

    public Queue<Square> getExactSolution() {
        return solvingStepsStrategies;
    }

    public int getNumberOfSquaresUnfilled() {
        return initialOpenSquares - solvingStepsStrategies.size();
    }

}
