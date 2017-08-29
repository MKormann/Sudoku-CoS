package com.licktopia.thechampionofsudoku;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by do_de on 4/15/2017.
 */

public class SudokuFacts {

    private String[] funFacts = {
            "The name “Sudoku” stems from two Japanese words: “su”, which means “number,” and doku, which means “single.” Translated, it mostly means “single numbers only.”",
            "British Airways absolutely forbids its flight attendants from solving Sudoku puzzles during either takeoff or landing.",
            "There are 6,670,903,752,021,072,936,960 possible combinations for completing a 9-by-9 Sudoku grid, but only 5,472,730,538 of them really count for different solutions. Needlessly to say, you need a handful of lifetimes to solve all of them.",
            "Sudoku is a logic game and involves absolutely no math. Sometimes Sudoku puzzles even come with pictures, letters or symbols instead of numbers.",
            "The First World Sudoku Championship was held in Italy in 2006 and has been held annually since in different locations.", "" +
            "Playing Sudoku regularly can have benefits, like boosting your concentration and focus, preventing or easing depression and possibly even preventing dementia and Alzheimer’s disease, according to some studies.",
            "In June of the year 2008, a judge of one of the Australian courts stopped the trial, because 5 members of the jury were playing Sudoku instead of listening to the evidence presented.",
            "Actually, Sudoku isn’t a Japanese game at all. It’s American invented. Howard Garns created it as Number Place in 1979 but died in 1989 before Japanese publisher Nikoli got a hold of it. The game didn’t really take off until 2004 though, when Wayne Gould convinced The Times in London to publish it.",
            "Will Shortz, The New York Times crossword editor, told Fortune in October 2005 that “the craze, judging by history, will last four, five, six months, and then it will taper off.” He changed his mind by the May/June 2006 issue of Psychology Today and admitted that Sudoku had the staying power of crossword puzzles.",
              };

    private String[] mColors = {"#1E91D6","#ea3f6a","#7D1538","#7c8597","#E18335","#4A2772","#F2CA08","#329d53"};
    public String getFunFact(){
        int random = ThreadLocalRandom.current().nextInt(0, funFacts.length);
        return funFacts[random];
    }
    public String getColorHex(){
        int random = ThreadLocalRandom.current().nextInt(0, mColors.length);
        return mColors[random];
    }
}
