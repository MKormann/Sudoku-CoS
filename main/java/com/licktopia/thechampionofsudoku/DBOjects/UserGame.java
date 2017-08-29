package com.licktopia.thechampionofsudoku.DBOjects;

/**
 * Created by do_de on 6/29/2017.
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;


@DynamoDBTable(tableName = "UserGame")
public class UserGame {
    private String gamerID;
    private String exactTime;
    private String gamerName;
    private int[] board;
    private int difficulty;
    private String date;
    private int level;

    @DynamoDBRangeKey(attributeName = "ExactTime")
    public String getExactTime() {
        return exactTime;
    }
    public void setExactTime(String exactTime) {
        this.exactTime = exactTime;
    }

    @DynamoDBHashKey(attributeName = "GamerID")
    public String getGamerID() {
        return gamerID;
    }
    public void setGamerID(String gamerID) {
        this.gamerID = gamerID;
    }

    @DynamoDBAttribute(attributeName = "gamerName")
    public String getGamerName() {
        return gamerName;
    }
    public void setGamerName(String gamerName) {
        this.gamerName = gamerName;
    }
    @DynamoDBAttribute(attributeName = "board")
    public int[] getBoard() {
        return board;
    }
    public void setBoard(int[] board) {
        this.board = board;
    }
    @DynamoDBAttribute(attributeName = "difficulty")
    public int getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    @DynamoDBAttribute(attributeName = "date")
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    @DynamoDBAttribute(attributeName = "level")
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
}