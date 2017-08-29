package com.licktopia.thechampionofsudoku.ListItems;



/**
 * Created by do_de on 3/30/2017.
 */

public class SavedGameListItem {


    private String date;
    private int level;
    private long puzzleNumber;
    private String time;
    private String exactTime;
    private String gamerID;
    private long stoppedTime;
    private byte[] savedGame;




    private boolean color;



    public SavedGameListItem(String date, String time, boolean colorMode, String exactTime, int level, long puzzleNumber, String gamerID, long stoppedTIme) {

        this.date = date;
        this.level = level;
        this.puzzleNumber = puzzleNumber;
        this.time = time;
        this.exactTime = exactTime;
        this.color = colorMode;
        this.gamerID = gamerID;
        this.stoppedTime = stoppedTIme;
        this.savedGame = savedGame;

    }

    public String getDate() {
        return date;
    }
    public String getGamerId() { return gamerID;
    }

    public int getLevel() {
        return level;
    }

    public long getPuzzleNumber() {
        return puzzleNumber;
    }


    public String getTime() {

        return time;
    }


    public boolean isColor() {
        return color;
    }

    public String getExactTime() {
        return exactTime;
    }

    public long getStoppedTime() {
        return stoppedTime;
    }

    public byte[] getSavedGame() {
        return savedGame;
    }

    public String getGamerID() {
        return gamerID;
    }






}
