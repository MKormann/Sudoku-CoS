package com.licktopia.thechampionofsudoku.ListItems;



/**
 * Created by do_de on 3/30/2017.
 */

public class UserGameListItem {


    private String gamerID;
    private String boardIdentifier;
    private String gamerName;
    private int[] board;
    private int difficulty;
    private String date;
    private int level;


    public UserGameListItem(String gamerID, String boardIdentifier, String gamerName, int[] board, int difficulty, String date, int level) {
        this.gamerID = gamerID;
        this.boardIdentifier = boardIdentifier;
        this.gamerName = gamerName;
        this.board = board;
        this.difficulty = difficulty;
        this.date = date;
        this.level = level;
    }

    public String getGamerID() {
        return gamerID;
    }

    public void setGamerID(String gamerID) {
        this.gamerID = gamerID;
    }

    public String getBoardIdentifier() {
        return boardIdentifier;
    }

    public void setBoardIdentifier(String boardIdentifier) {
        this.boardIdentifier = boardIdentifier;
    }
    public Long getBoardIdentifierAsLong() {
        return Long.valueOf(boardIdentifier);
    }
    public String getGamerName() {
        return gamerName;
    }

    public void setGamerName(String gamerName) {
        this.gamerName = gamerName;
    }

    public int[] getBoard() {
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
