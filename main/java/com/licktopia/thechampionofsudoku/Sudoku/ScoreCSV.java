package com.licktopia.thechampionofsudoku.Sudoku;

/**
 * This class replaces the old score.csv file
 * Created by John Konecny on 8/21/2017.
 */

public class ScoreCSV {

    /*
    ScoreWeights(long basePoints,
                           long oneTimeGuessBonus,
                           long runningTimePenalty,
                           long runningTimeRemovalInterval,
                           double incorrectGuessPenalty,
                           long earlyFinishBonus,
                           long earlyFinishTime,
                           long hintPenalty)
    */
    private static final ScoreWeights[] weights =
    {
            new ScoreWeights(0, 30, 0, 0, 0, 0, 0, 0),
            new ScoreWeights(20, 60, 1, 30, 1.0/30.0, 0, 0, 5),
            new ScoreWeights(100, 100, 10, 25, 1.0/30.0, 0, 0, 10),
            new ScoreWeights(200, 110, 10, 25, 1.0/25.0, 10, 10, 15),
            new ScoreWeights(300, 120, 10, 25, 1.0/25.0, 10, 11, 20),
            new ScoreWeights(400, 130, 10, 20, 1.0/25.0, 10, 12, 25)
    };


    public static ScoreWeights getScoreWeights(int level)
    {
        if(level < 0 || weights.length <= level)
        {
            return weights[0];
        }
        return weights[level];
    }
}
