package com.licktopia.thechampionofsudoku.Sudoku;

import java.util.Arrays;

/**
 * Created by John Konecny on 6/19/2017.
 */


//Represents a single cell on a Sudoku board
public class Square implements java.io.Serializable
{
    private int row = -1;
    private int col = -1;
    private int boardSize = -1;
    private boolean isStartingValue;
    private boolean[] notepad;
    private boolean[] notepadBackup;
    private int value = -1;
    //true if the user has changed the square number
    private boolean hasBeenChange;
    //true if the square has been change no more than once
    private boolean noMoreThanOneGuess;
    protected Square(int entry, boolean isStartingValue, int boardSize, int row, int col)
    {
        buildSquare(entry, isStartingValue, boardSize, row, col);
    }

    public Square(int entry, boolean isStartingValue, int boardSize, int idx)
    {
        buildSquare(entry, isStartingValue, boardSize, idx / boardSize, idx % boardSize);
    }

    private void buildSquare(int entry, boolean isStartingValue, int boardSize, int row, int col)
    {
        this.row = row;
        this.col = col;
        this.boardSize = boardSize;
        notepad = new boolean[boardSize];
        notepadBackup = new boolean[boardSize];
        this.value = entry;
        this.isStartingValue = isStartingValue;
        hasBeenChange = false;
        noMoreThanOneGuess = true;
    }

    /*
    * Makes exact copy of the passed square
    * */
    protected Square(Square oldSqr)
    {
        this.row = oldSqr.row;
        this.col = oldSqr.col;
        this.boardSize = oldSqr.boardSize;
        this.notepad = new boolean[boardSize];
        for(int i = 0; i < boardSize; i++)
        {
            this.notepad[i] = oldSqr.notepad[i];
        }
        this.value = oldSqr.value;
        this.isStartingValue = oldSqr.isStartingValue;
        this.hasBeenChange = oldSqr.hasBeenChange;
        this.noMoreThanOneGuess = oldSqr.noMoreThanOneGuess;
    }

    //num is the value which is the current state of the notepad
    //sets the boolean for num to the passed value
    protected void setNotepad(int num, boolean value) {

        notepad[num - 1] = value;
    }


    protected void setValue(int entry) {
        if(!hasBeenChange)
        {
            hasBeenChange = true;
        }
        else
        {
            noMoreThanOneGuess = false;
        }
        this.value = entry;
    }



    //saves state of cell given the row and column
    //returns null if the save state failed
    protected Square saveState()
    {
        Square sav = new Square(this);
        return sav;
    }

    //resets cell to default values
    protected void reset()
    {

        hasBeenChange = false;
        noMoreThanOneGuess = true;
        this.clear();

    }

    //clears user input values
    protected void clear()
    {
        for(int i = 0; i < notepad.length; i++)
        {
            notepad[i] = false;
        }

        if(!this.isStartingValue)
        {
            value = 0;
        }
    }


    protected boolean isStartingValue()
    {
        return this.isStartingValue;
    }


    //num is the value which is the current state of the notepad
    //returns the boolean for the given num
    public boolean getNotepad(int num) {

        return notepad[num - 1];
    }

    protected void backupNotepad() {
        notepadBackup = Arrays.copyOf(notepad, notepad.length);
    }

    protected void restoreNotepad() {
        notepad = Arrays.copyOf(notepadBackup, notepadBackup.length);
    }

    protected boolean noMoreThanOneGuess(){return this.noMoreThanOneGuess;}

    public int getValue() {
        return value;
    }
    public int getCol()
    {
        return col;
    }
    public int getRow()
    {
        return row;
    }
    public int getIdx() { return col + boardSize * row;}

}
