package com.licktopia.thechampionofsudoku.Database;

/**
 * Created by do_de on 8/19/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.licktopia.thechampionofsudoku.ListItems.UserGameListItem;
import com.licktopia.thechampionofsudoku.Sudoku.DifficultyClassifier;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;
    private static long randomBoardIndex;
    private static ThreadLocalRandom _Rand = ThreadLocalRandom.current();


    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Read all quotes from the database.
     *
     * @return a List of quotes
     */
    public ArrayList<UserGameListItem> getStockGamesFromLevel(int level) {
        ArrayList <UserGameListItem> list = new ArrayList<>();
        int minDifficulty = DifficultyClassifier.LEVEL_DELIMS[level];
        int maxDifficulty = DifficultyClassifier.LEVEL_DELIMS[level + 1];
        String rowName = "Difficulty";

        Cursor cursor = database.rawQuery("SELECT * FROM Boards WHERE " + rowName + ">=? AND " + rowName + "<?",
                new String[] {String.valueOf(minDifficulty), String.valueOf(maxDifficulty)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserGameListItem userGameListItem = new UserGameListItem(
                    cursor.getString(2),
                    cursor.getString(0),
                    cursor.getString(3),
                    convert(cursor.getString(1).toCharArray()),
                    Integer.parseInt(cursor.getString(5)),
                    cursor.getString(4),
                    DifficultyClassifier.getLevelFromDifficulty(Integer.parseInt(cursor.getString(5)))
            );
            list.add(userGameListItem);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * Get IDs of all the boards within a difficulty range.
     *
     * @return a List of all board identifiers within a given difficulty level
     */
    public ArrayList<Integer> getIdentifiersFromLevel(int level) {
        ArrayList <Integer> list = new ArrayList<>();
        int minDifficulty = DifficultyClassifier.LEVEL_DELIMS[level];
        int maxDifficulty = DifficultyClassifier.LEVEL_DELIMS[level + 1];
        String rowName = "Difficulty";

        Cursor cursor = database.rawQuery("SELECT Identifier FROM Boards WHERE " + rowName + ">=? AND " + rowName + "<?",
                new String[] {String.valueOf(minDifficulty), String.valueOf(maxDifficulty)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(Integer.parseInt(cursor.getString(0)));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    // Gets random board from given difficulty level
    public int[] getRandomBoard(int level) {

        int minDifficulty = DifficultyClassifier.LEVEL_DELIMS[level];
        int maxDifficulty = DifficultyClassifier.LEVEL_DELIMS[level + 1];
        String rowName = "Difficulty";

        Cursor cursor = database.rawQuery("SELECT * FROM Boards WHERE " + rowName + ">=? AND " + rowName + "<?",
                new String[] {String.valueOf(minDifficulty), String.valueOf(maxDifficulty)});
        cursor.moveToFirst();
        int numBoards = cursor.getCount();
        randomBoardIndex = _Rand.nextLong(0, numBoards);
        cursor.moveToPosition((int)randomBoardIndex);
        int[] board = convert(cursor.getString(1).toCharArray());
        cursor.close();
        return board;
    }

    // Gets specific board from given board ID
    public int[] getSpecificBoard(long boardIndex){
        int levelNum = (int) ((boardIndex - 1)/10000);

        String identifier = Long.toString(boardIndex);
        String rowName = "Identifier";

        Cursor cursor = database.rawQuery("SELECT * FROM Boards WHERE " + rowName + "=?", new String[] { identifier });
        cursor.moveToFirst();
        int[] board = convert(cursor.getString(1).toCharArray());
        cursor.close();
        return board;
    }

    public UserGameListItem getSpecificBoardGame(long boardIndex){

        String identifier = Long.toString(boardIndex);
        String rowName = "Identifier";

        Cursor cursor = database.rawQuery("SELECT * FROM Boards WHERE " + rowName + "=?", new String[] { identifier });
        cursor.moveToFirst();
        UserGameListItem userGameListItem = new UserGameListItem(
                cursor.getString(2),
                cursor.getString(0),
                cursor.getString(3),
                convert(cursor.getString(1).toCharArray()),
                Integer.parseInt(cursor.getString(5)),
                cursor.getString(4),
                DifficultyClassifier.getLevelFromDifficulty(Integer.parseInt(cursor.getString(5)))
        );
        cursor.close();
        return userGameListItem;
    }


    private int[] convert(char[] board){
        int newBoard[] = new int[81];
        for(int i =0; i < 81; i++){
            newBoard[i] = Character.getNumericValue(board[i]);
        }
        return newBoard;
    }

    public static long getRandomBoardIndex() {
        return randomBoardIndex;
    }
}

