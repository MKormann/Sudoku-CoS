package com.licktopia.thechampionofsudoku.ListItems;



/**
 * Created by do_de on 3/30/2017.
 */

public class CompletedGameListItem {
    private double accuracy;

    private String date;
    private int level;
    private long puzzleNumber;
    private int realScore;
    private String time;
    private int xp;
    private String GamerId;



    private String exactTime;



    private boolean color;

    public CompletedGameListItem(double accuracy, String date, int level, long puzzleNumber, int realScore, String time, int xp, String gamerId, boolean color, String exactTime) {
        this.accuracy = accuracy;
        this.date = date;
        this.level = level;
        this.puzzleNumber = puzzleNumber;
        this.realScore = realScore;
        this.time = time;
        this.xp = xp;
        this.GamerId = gamerId;
        this.exactTime = exactTime;
        this.color = color;

    }



    public double getAccuracy() {
        return accuracy;
    }



    public String getDate() {
        return date;
    }

    public int getLevel() {
        return level;
    }

    public long getPuzzleNumber() {
        return puzzleNumber;
    }

    public int getRealScore() {
        return realScore;
    }

    public String getTime() {

        return formatTime(time);
    }

    public int getXp() {
        return xp;
    }

    public String getGamerId() {
        return GamerId;
    }

    public boolean isColor() {
        return color;
    }

    public String getExactTime() {
        return exactTime;
    }

    private String formatTime(String seconds){
        seconds = seconds.replace(",","");
        int intSeconds = Integer.valueOf(seconds);
        String formattedTime;
        int minutes = intSeconds/60;
        int secs = intSeconds%60;
        String minutos = String.format("%d",minutes);
        String segundos = String.format("%d",secs);
        if(segundos.length()==1){
            segundos = "0" + segundos;
        }
        formattedTime = minutos +  ":" +segundos;

        return formattedTime;
    }




}
