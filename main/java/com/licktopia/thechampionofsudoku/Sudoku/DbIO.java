package com.licktopia.thechampionofsudoku.Sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.MainMenuFragment;
import com.licktopia.thechampionofsudoku.Database.DatabaseAccess;

/**
 * Created by John Konecny on 6/19/2017.
 */

public class DbIO {

    //loads a random board from the given level
    protected static int[] loadBoard(int level, Context ctx, int totalBoardElements) {

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ctx);
        databaseAccess.open();
        try {
            int[] values = databaseAccess.getRandomBoard(level);
            return values;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    //loads a board from row 'boardIndex' from the given level
    public static int[] loadSpecificBoard(int level, Context ctx, int totalBoardElements, long boardIndex) {

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ctx);
        databaseAccess.open();
        try {
            int[] values = databaseAccess.getSpecificBoard(boardIndex);
            return values;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    //loads all static weights for
    protected static ScoreWeights loadScoreWeights(int level, Context ctx) {
        String filename = "score.csv";
        String cvsSplitBy = ",";

        BufferedReader reader = null;
        //stores which column the level's score weight are
        int levelCol = -1;
        long basePoints = -1,
                oneTimeGuessBonus = -1,
                runningTimePenalty = -1,
                //Time, in seconds, interval when time is penalty removes points
                runningTimeRemovalInterval = -1,
                earlyFinishBonus = -1,
                //The user gets a bonus if they finished before this time, in seconds
                earlyFinishTime = -1;
        double incorrectGuessPenalty = -1;


        try {
            reader = new BufferedReader(new InputStreamReader(
                    ctx.getAssets().open(filename)));
            String line;

            //get column of the level
            if((line = reader.readLine())!= null)
            {
                String[] lineArr = line.split(cvsSplitBy);
                for(int i = 0; i < lineArr.length; i++)
                {
                    try
                    {
                        if(level == Integer.valueOf(lineArr[i]))
                        {
                            levelCol = i;
                            break;
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        //do nothing
                    }

                }

                //if the level's column is found then continue
                if(levelCol >= 0)
                {
                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    basePoints = Long.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    oneTimeGuessBonus = Long.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    runningTimePenalty = Long.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    runningTimeRemovalInterval = Long.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    incorrectGuessPenalty = Double.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    earlyFinishBonus = Long.valueOf(lineArr[levelCol]);

                    line = reader.readLine();
                    lineArr = line.split(cvsSplitBy);
                    earlyFinishTime = TimeUnit.MINUTES.toSeconds(Long.valueOf(lineArr[levelCol]));
                }
            }


            reader.close();
        } catch (IOException e) {
            System.out.printf("Failed to open %s\n", filename);
            e.printStackTrace();
        }
        return new ScoreWeights(basePoints,
                oneTimeGuessBonus,
                runningTimePenalty,
                runningTimeRemovalInterval,
                incorrectGuessPenalty,
                earlyFinishBonus,
                earlyFinishTime, 0);

    }
}
