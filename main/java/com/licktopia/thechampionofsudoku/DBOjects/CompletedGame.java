package com.licktopia.thechampionofsudoku.DBOjects;

/**
 * Created by do_de on 6/29/2017.
 */
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;


@DynamoDBTable(tableName = "CompletedGame")
public class CompletedGame {
    private String gamerID;



    private String exactTime;
    private int realScore;
    private String time;
    private String date;
    private boolean color;
    private long puzzleNumber;
    private int level;
    private double accuracy;
    private int xp;
    private String gamerName;


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

    @DynamoDBAttribute(attributeName = "RealScore")
    public int getRealScore() {
        return realScore;
    }

    public void setRealScore(int realScore) {
        this.realScore = realScore;
    }

    @DynamoDBAttribute(attributeName = "Xp")
    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    @DynamoDBAttribute(attributeName = "Time")
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @DynamoDBAttribute(attributeName = "Date")
    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = "GamerName")
    public String getGamerName() {
        return gamerName;
    }


    public void setGamerName(String gamerName) {
        this.gamerName = gamerName;
    }

    @DynamoDBAttribute(attributeName = "Color")
    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "PuzzleNo-index", attributeName = "PuzzleNo")
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

    @DynamoDBAttribute(attributeName = "Accuracy")
    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }


}