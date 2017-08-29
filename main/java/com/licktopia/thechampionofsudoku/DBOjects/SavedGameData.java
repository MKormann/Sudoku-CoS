package com.licktopia.thechampionofsudoku.DBOjects;

/**
 * Created by do_de on 6/29/2017.
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;


@DynamoDBTable(tableName = "SavedGameData")
public class SavedGameData {
    private String gamerID;
    private String exactTime;
    private byte[] saveFile;

    @DynamoDBRangeKey(attributeName = "ExactTime")
    public String getExactTime() {
        return exactTime;
    }
    public void setExactTime(String exactTime) {
        this.exactTime = exactTime;
    }

    @DynamoDBHashKey(attributeName = "GamerId")
    public String getGamerID() {
        return gamerID;
    }
    public void setGamerID(String gamerID) {
        this.gamerID = gamerID;
    }

    //must be a binary file type
    @DynamoDBAttribute(attributeName = "saveFile")
    public byte[] getSaveFile() {
        return saveFile;
    }
    public void setSaveFile(byte[] saveFile) {
        this.saveFile = saveFile;
    }
}