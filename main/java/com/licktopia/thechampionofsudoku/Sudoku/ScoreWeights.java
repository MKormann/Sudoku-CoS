package com.licktopia.thechampionofsudoku.Sudoku;

/**
 * Created by John Konecny on 6/23/2017.
 */

class ScoreWeights implements java.io.Serializable {
    private long basePoints = -1,
            oneTimeGuessBonus = -1,
            runningTimePenalty = -1,
    //Time, in seconds, interval when time is penalty removes points
            runningTimeRemovalInterval = -1,
            earlyFinishBonus = -1,
    //The user gets a bonus if they finished before this time, in seconds
            earlyFinishTime = -1;
    private double incorrectGuessPenalty = -1;

    // points removed for each hint used, in points per hint
    private long hintPenalty = -1;

    //all time must be in seconds
    protected ScoreWeights(long basePoints,
                           long oneTimeGuessBonus,
                           long runningTimePenalty,
                           long runningTimeRemovalInterval,
                           double incorrectGuessPenalty,
                           long earlyFinishBonus,
                           long earlyFinishTime,
                           long hintPenalty)
    {
        this.basePoints = basePoints;
        this.oneTimeGuessBonus = oneTimeGuessBonus;
        this.runningTimePenalty = runningTimePenalty;

        if(runningTimeRemovalInterval < 1)
        {
            this.runningTimeRemovalInterval = 1;
        }
        else
        {
            this.runningTimeRemovalInterval = runningTimeRemovalInterval;
        }

        this.hintPenalty = hintPenalty;
        this.earlyFinishBonus = earlyFinishBonus;
        this.earlyFinishTime = earlyFinishTime;
        this.incorrectGuessPenalty = incorrectGuessPenalty;
    }

    protected long getBasePoints() {
        return basePoints;
    }

    protected long getOneTimeGuessBonus() {
        return oneTimeGuessBonus;
    }

    protected long getRunningTimePenalty() {
        return runningTimePenalty;
    }

    protected long getRunningTimeRemovalInterval() {return runningTimeRemovalInterval;}

    protected long getEarlyFinishBonus() {return  earlyFinishBonus;}

    protected long getEarlyFinishTime() {return  earlyFinishTime;}

    protected double getIncorrectGuessPenalty() {
        return incorrectGuessPenalty;
    }
    protected long getHintPenalty() {
        return hintPenalty;
    }
}
