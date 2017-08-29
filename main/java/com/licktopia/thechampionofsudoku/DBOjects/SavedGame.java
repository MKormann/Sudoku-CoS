package com.licktopia.thechampionofsudoku.DBOjects;

/**
 * Created by do_de on 6/29/2017.
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.File;


@DynamoDBTable(tableName = "SavedGame")
public class SavedGame {
    private String gamerID;
    private String exactTime;
    private boolean colorMode;
    private String date;
    private String time;
    private int level;
    private long puzzleNumber;
    private long stoppedTime;

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

    //must be a binary file type
    @DynamoDBAttribute(attributeName = "colorMode")
    public boolean getColorMode() {
        return colorMode;
    }
    public void setColorMode(boolean colorMode) {
        this.colorMode = colorMode;
    }

    @DynamoDBAttribute(attributeName = "Date")
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = "Time")
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    @DynamoDBAttribute(attributeName = "PuzzleNo")
    public long getPuzzleNumber() {
        return puzzleNumber;
    }
    public void setPuzzleNumber(long puzzleNumber) {
        this.puzzleNumber = puzzleNumber;
    }

    @DynamoDBAttribute(attributeName = "Level")
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    @DynamoDBAttribute(attributeName = "StoppedTime")
    public long getStoppedTime() {
        return stoppedTime;
    }
    public void setStoppedTime(long stoppedTime) {
        this.stoppedTime = stoppedTime;
    }



}