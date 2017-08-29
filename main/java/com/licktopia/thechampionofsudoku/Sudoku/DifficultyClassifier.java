package com.licktopia.thechampionofsudoku.Sudoku;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

/**
 * Created by Matt on 7/5/2017.
 */

public class DifficultyClassifier {

    public static final int[] LEVEL_DELIMS = {0, 37, 47, 83, 90, 109, Integer.MAX_VALUE};

    // Given a difficulty score of a board, returns its level
    public static int getLevelFromDifficulty(int difficultyScore) {
        int level = 0;
        for (int i = 0; i < LEVEL_DELIMS.length; i++) {
            if (difficultyScore >= LEVEL_DELIMS[i]) level = i;
        }

        return level;
    }

    public static void runBacktracking(Context context) {

        int unsolvedBoards = 0;
        for (int index = 1; index <= 60000; index++) {
            int level = index / 10000;
            Board board = new Board(level, context, index);
            Solver solutionSolver = new Solver(board);
            solutionSolver.solve();
            if (!solutionSolver.curBoard.isBoardFull()) unsolvedBoards++;
            Log.i("BoardResults", "BACK: Board #" + index);
            if (index % 10000 == 0) {
                Log.i("BoardResults", "Unsolved boards at level " + level + ": " + unsolvedBoards);
                unsolvedBoards = 0;
            }
        }
    }

    public static void runStrategies(Context context) {
        int[] scores = new int[60000];
        int unsolved = 0;
        for (int index = 1; index <= 60000; index++) {
            Board board = new Board((index - 1)/10000, context, index);
            Solver solutionSolver = new Solver(board);
            int score = solutionSolver.solveWithStrategies();
            if (solutionSolver.getExactSolution().size() != board.getOpenSquares()) unsolved++;
            if (index % 100 == 0) {
                Log.i("BoardResults", "STRAT: Finished board #" + index);
                Log.i("BoardResults", "STRAT: Unsolved at " + unsolved);
                Log.i("BoardResults", "Board 60000 " + score);
            }
        }
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("boardscoresAll.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("Board Scores:");
            for (int score : scores) {
                outputStreamWriter.write(String.valueOf(score));
                outputStreamWriter.write(",");
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
