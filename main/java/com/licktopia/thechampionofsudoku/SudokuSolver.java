package com.licktopia.thechampionofsudoku;

import android.content.Context;

import com.licktopia.thechampionofsudoku.Sudoku.Board;

import java.util.Arrays;
import java.util.Vector;

/**
 * Created by jeff on 12/15/16.
 */

public class SudokuSolver {



    /************************************
     * Get Functions return vectors with backtracking lists of moves
     * Numberlist: the number in the square
     * Xcoord: X position of square
     * Ycoord: Y position of square
     * MakeUnmake: If it was an add or delete
     *********************************/
    public Vector<Integer> getNumberList() {
        return NumberList;
    }
    public Vector<Integer> getXCoord() {
        return XCoord;
    }
    public Vector<Integer> getYCoord() {
        return YCoord;
    }
    public Vector<Boolean> getMakeUnmake() {
        return MakeUnmake;
    }

    /************************************
     * getSolution Returns solved board
     *********************************/
    public int[] getSolution(){return fin;}

    /************************************
     * getBoardIdentifier Returns board number
     *********************************/
    public int getBoardNumber(){return boardNumber;}

    /************************************
     * getBoardIdentifier Returns starting board
     *********************************/
    public int[] getStartBoard(){return startBoard;}




    private class LocationBoard {
        LocationBoard(int row, int col){
            x = row;
            y = col;
        }
        int x;
        int y;
    };

    private class Candidates{
        int nCandidates = 0;
        int[] candidates= new int[9];
    }
    private class Cluster {
        int rowBegin;
        int rowEnd;
        int colBegin;
        int colEnd;
    };

    private class BoardofClass {
        public BoardofClass() { openSquares = 0; }
        public int[][] contents = new int[9][9]; //this is sudoku puzzle we are solving
        private int openSquares; //num of open spots avail.
        private LocationBoard [] corrLoStor = new LocationBoard[81];//1D index of squares an array of x and y coordinates for a given position
    };
    private LocationBoard[] openSpots = new LocationBoard[81];
    private int correctValues[] = new int[81];
    private int currCorrVal;
    private BoardofClass sudokuBoardofClass = new BoardofClass();
    private boolean finished;
    private int NUM_OF_ELEMENTS;
    private int MAX_ROWS;
    private int MAX_COLS;
    private int MAX_POSSIBLE;
    private int[] fin;
    private int[] startBoard;
    private int boardNumber;
    private Context context;
    private Vector<Integer> NumberList;
    private Vector<Integer> XCoord;
    private Vector<Integer> YCoord;
    private Vector<Boolean> MakeUnmake;




    public SudokuSolver(Board curBoard)
    {
        while(fin == null) {
            NUM_OF_ELEMENTS = 81;
            MAX_ROWS = 9;
            MAX_COLS = 9;
            MAX_POSSIBLE = 10;
            currCorrVal = -1;
            int p = 0;
            XCoord = new Vector<>();
            YCoord = new Vector<>();
            MakeUnmake = new Vector<>();
            NumberList = new Vector<>();

            //open file and read it into the BoardofClass

            for (int row = 0; row < MAX_ROWS; row++) {
                for (int col = 0; col < MAX_COLS; col++) {
                    sudokuBoardofClass.contents[row][col] = curBoard.getValueAt(row,col);
                    if (sudokuBoardofClass.contents[row][col] == 0) {

                        openSpots[sudokuBoardofClass.openSquares] = new LocationBoard(row, col);
                        sudokuBoardofClass.openSquares++;

                    }
                    p++;
                }
            }
            finished = false;
            solvePuzzle();
        }
    }



    private  void solvePuzzle(){
        Candidates mCandidate;
        int[] candidates;
        int nCandidates;

        if(isSolution())
        {
            int count =0;
            int num[] = new int[81];
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    num[count] = sudokuBoardofClass.contents[i][j];
                    count++;
                }
            }
            fin = num;
        }

        else
        {


            currCorrVal++;

            mCandidate = findCandidates();

            candidates = mCandidate.candidates;
            nCandidates = mCandidate.nCandidates;
            for(int i = 0; i < nCandidates; i++)
            {
                correctValues[currCorrVal] = candidates[i];
                makeMove();
                solvePuzzle();
                if (finished) { return; } //we are done! Solution has been found
                else
                {
                    unmakeMove();
                    if(currCorrVal > 0) {
                        currCorrVal--;
                    }
                }
            }

        }
        return;

    }
    private boolean isSolution(){
        if (sudokuBoardofClass.openSquares != 0) { return false; }//if the board isn't full
        else//if the board is full
        {
            for (int i = 0; i < 9; i++)//for each row, column and cluster - outer of nested loop
            {

                int rowbank[] = { 0,0,0,0,0,0,0,0,0 };//create new empty row to fill
                int colbank[] = { 0,0,0,0,0,0,0,0,0 };//create new empty column to fill
                int clusterbank[] = { 0,0,0,0,0,0,0,0,0 };//create new empty cluster to fill

                //SECTION CONVERTS CLUSTER INTO A FLAT ARRAY

                int edgerow = 0;//Setting Edge parameters
                int edgecol = 0;
                edgecol = 3*((i + 3) % 3);

                if (i == 3 || i == 4 || i ==5) { edgerow += 3; }
                if (i == 6 || i == 7 || i == 8) { edgerow += 6; }

                int val = 0;
                int clusterArray[] = { 0,0,0,0,0,0,0,0,0 };//array that will hold cluster

                for (int m = 0; m < 3; m++)
                {
                    for (int p = 0; p < 3; p++)
                    {
                        //copying current cluster into flat cluster Array
                        clusterArray[val] = sudokuBoardofClass.contents[m + edgerow][p+edgecol];
                        val++;
                    }
                }

                for (int j = 0; j < 9; j++)//puts number 1-9 into bank slot of number
                {
                    if(sudokuBoardofClass.contents[i][j]!=0) {
                        rowbank[sudokuBoardofClass.contents[i][j] - 1] = sudokuBoardofClass.contents[i][j];
                    }
                    if(sudokuBoardofClass.contents[j][i]!=0) {
                        colbank[sudokuBoardofClass.contents[j][i] - 1] = sudokuBoardofClass.contents[j][i];
                    }
                    //put value found in clustArrray[j] into empty cluster vector
                    if(clusterArray[j]!=0) {
                        clusterbank[clusterArray[j] - 1] = clusterArray[j];
                    }
                }

                for (int z = 0; z < 9; z++)//loop through bank vectors
                {
                    if (rowbank[z] != z + 1 || colbank[z] != z + 1 || clusterbank[z] != z + 1)
                    {//if rowbank slot j doesn't = j, then false
                        return false;//then return false
                    }
                }
            }
        }
       finished = true;
        return true;

    }
    private Candidates findCandidates()
    {
        Candidates mCandidate = new Candidates();
        int row, col; // Will store position of next move
        LocationBoard locationBoard;

        boolean possible[] = new boolean[10]; //what is possible for the current square
        Arrays.fill(possible, true);
        //memset(possible, true, MAX_POSSIBLE); //set all values to true for later

        //which square should we fill next?
        locationBoard = findNextSquare();
        row = locationBoard.x;
        col = locationBoard.y;

        //store the current row and column into the correct location storage
        sudokuBoardofClass.corrLoStor[currCorrVal] = new LocationBoard(row, col);



        int nCandidates = 0;


        if(row < 0 && col < 0) { return null; } //no moves possible, abort

        //What are the possible values for this square?
        possible = findPossibilites(row, col);


        //update nCandidates and Candidate array
        for(int i = 1; i <= 9; i++)
        {
            if(possible[i] == true)

            {
                mCandidate.nCandidates++;
                mCandidate.candidates[nCandidates] = i;
                nCandidates++;
            }
        }
        return mCandidate;
    }

    private boolean[] findPossibilites (int row, int col){
        //check current row
        boolean possible[] = new boolean[10];
        Arrays.fill(possible, true);
        for(int i = 0; i < 9; i++)
        {
            if(sudokuBoardofClass.contents[row][i] != 0)
            {
                possible[sudokuBoardofClass.contents[row][i]] = false;
            }
        }

        //check current column
        for(int i = 0; i < 9; i++ )
        {
            if(sudokuBoardofClass.contents[i][col] != 0)
            {
                possible[sudokuBoardofClass.contents[i][col]] = false;
            }
        }

        //check current cluster
        Cluster cluster = new Cluster();
        int rowEnd, rowBegin, colEnd, colBegin;;
        cluster = getCluster(row, col);
        rowBegin = cluster.rowBegin;
        rowEnd = cluster.rowEnd;
        colBegin = cluster.colBegin;
        colEnd = cluster.colEnd;


        for(int i = rowBegin; i < rowEnd; i++)
        {
            for(int j = colBegin; j < colEnd; j++)
            {
                if(sudokuBoardofClass.contents[i][j] != 0)
                {
                    possible[sudokuBoardofClass.contents[i][j]] = false;

                }
            }
        }
        return possible;
    }
    private Cluster getCluster(int row, int col){
        Cluster cluster = new Cluster();
        if(row <= 2)
        {
            cluster.rowEnd = 3;
            cluster.rowBegin = 0;
            if(col <=2)
            {
                cluster.colBegin = 0;
                cluster.colEnd = 3;
            }
            else if(col >=3 && col <= 5)
            {
                cluster.colBegin = 3;
                cluster.colEnd = 6;
            }
            else if(col >= 6 && col <= 8)
            {
                cluster.colBegin = 6;
                cluster.colEnd = 9;
            }
        }

        else if(row >= 3 && row <= 5)
        {
            cluster.rowEnd = 6;
            cluster.rowBegin = 3;
            if(col <= 2)
            {
                cluster.colBegin = 0;
                cluster.colEnd = 2;
            }
            else if(col >= 3 && col <= 5)
            {
                cluster.colBegin = 3;
                cluster.colEnd = 6;
            }
            else if(col >= 6 && col <= 8)
            {
                cluster.colBegin = 6;
                cluster.colEnd = 9;
            }
        }

        else if(row >= 6 && row <= 8)
        {
            cluster.rowBegin = 6;
            cluster.rowEnd = 9;
            if(col <= 2)
            {
                cluster.colBegin = 0;
                cluster.colEnd = 3;
            }
            else if(col >= 3 && col <= 5)
            {
                cluster.colBegin = 3;
                cluster.colEnd = 6;
            }
            else if(col >= 6 && col <= 8)
            {
                cluster.colBegin = 6;
                cluster.colEnd = 9;
            }
        }
        return cluster;

    }

    private void makeMove(){
        int correctRow = sudokuBoardofClass.corrLoStor[currCorrVal].x;
        int correctCol = sudokuBoardofClass.corrLoStor[currCorrVal].y;
        sudokuBoardofClass.contents[correctRow][correctCol] = correctValues[currCorrVal];
        sudokuBoardofClass.openSquares--;
        XCoord.add(correctRow);
        YCoord.add(correctCol);
        NumberList.add(correctValues[currCorrVal]);
        MakeUnmake.add(true);

    }
    private void unmakeMove(){
        int correctRow = sudokuBoardofClass.corrLoStor[currCorrVal].x;
        int correctCol = sudokuBoardofClass.corrLoStor[currCorrVal].y;
        sudokuBoardofClass.contents[correctRow][correctCol] = 0;
        sudokuBoardofClass.openSquares++;
        XCoord.add(correctRow);
        YCoord.add(correctCol);
        NumberList.add(0);
        MakeUnmake.add(false);
    }
    private LocationBoard findNextSquare(){
        int rowzeros[] = new int[9];
        int colzeros[] = new int[9];
        int clusterzeros[] = new int[9];

        //SECTION GETS AND STORES TOTAL ZEROS FOR EACH ROW, COLUMN AND CLUSTER
        for (int i = 0; i < 9; i++)//for each row, column and cluster - outer of nested loop
        {
            //SECTION CONVERTS CLUSTER INTO A FLAT ARRAY
            int edgerow = 0;
            int edgecol = 0;
            edgecol = 3 * ((i + 3) % 3);

            if (i == 3 || i == 4 || i == 5)
            { edgerow += 3; }
            if (i == 6 || i == 7 || i == 8)
            { edgerow += 6; }

            int val = 0;
            int clusterArray[] = { 0,0,0,0,0,0,0,0,0 };

            for (int m = 0; m < 3; m++)
            {
                for (int p = 0; p < 3; p++)
                {
                    clusterArray[val] = sudokuBoardofClass.contents[m + edgerow][p + edgecol];
                    val++;
                }
            }
            //END CONVERSION

            int rowcount = 0;//counts zeros in row
            int colcount = 0;
            int cluscount = 0;

            for (int j = 0; j < 9; j++)//process through row, column and cluster  - inner of nest loop
            {
                //if spot in row is 0, add to 0 counter for row
                if (sudokuBoardofClass.contents[i][j] == 0) { rowcount++; }
                if (sudokuBoardofClass.contents[j][i] == 0) { colcount++; }
                if (clusterArray[j] == 0) { cluscount++; }
            }

            rowzeros[i] = rowcount;//i.e. set row [1] to rowcount of row 1
            colzeros[i] = colcount;
            clusterzeros[i] = cluscount;

        }
        //END GET AND STORE TOTAL ZEROS FOR ROW, COLUMN, AND CLUSTER
        //BEGIN FIND 0 SQUARE WITH LEAST ZERO VALUE
        int minzeros = 81;//initialize min zeros
        LocationBoard bestSquare = new LocationBoard(0,0);//initialize best square
        int cluster = 0;//cluster tracker

        for (int k = 0; k < 9; k++)//outer loop, loop through board rows
        {
            //edge tracking for cluster
            if ((k != 0) && ((k % 3) == 0)) { cluster += 3; }
            //loop through board columns
            for (int h = 0; h < 9; h++)
            {
                //edge tracking for cluster
                if (h != 0 && h % 3 == 0)	{	cluster++; }

                int totalzeros = 0;//initialize total zeros counter
                int samezeros = 0;//initialze same zeros counter (to not double count 0s from the same cluster, if in the same row/col)
                if (sudokuBoardofClass.contents[k][h] == 0)//if the spot on the board is a zero
                {
                    if ((h + 4) % 3 == 1)//get same zeros
                    {
                        if (sudokuBoardofClass.contents[k][h+1] == 0) {	samezeros++;}
                        if (sudokuBoardofClass.contents[k][h+2] == 0) {	samezeros++;}
                    }
                    else if ((h + 4) % 3 == 2)//get same zeros
                    {
                        if (sudokuBoardofClass.contents[k][h + 1] == 0) { samezeros++;}
                        if (sudokuBoardofClass.contents[k][h - 1] == 0) { samezeros++;}
                    }
                    else //get same zeros
                    {
                        if (sudokuBoardofClass.contents[k][h - 1] == 0) { samezeros++;}
                        if (sudokuBoardofClass.contents[k][h - 2] == 0) {	samezeros++;}
                    }
                    if ((k + 4) % 3 == 1)//get same zeros
                    {
                        if (sudokuBoardofClass.contents[k + 1][h] == 0) {samezeros++;}
                        if (sudokuBoardofClass.contents[k + 2][h] == 0) {samezeros++;}
                    }
                    else if ((k + 4) % 3 == 2)//get same zeros
                    {
                        if (sudokuBoardofClass.contents[k + 1][h] == 0) {samezeros++;}
                        if (sudokuBoardofClass.contents[k - 1][h] == 0) {samezeros++;}
                    }
                    else//get same zeros
                    {
                        if (sudokuBoardofClass.contents[k - 1][h] == 0) {samezeros++;}
                        if (sudokuBoardofClass.contents[k - 2][h] == 0) {samezeros++;}
                    }
                    totalzeros = rowzeros[k] + colzeros[h] + clusterzeros[cluster] - samezeros -3;//calculates how many other 0s are in the same row, column, and cluster,
                    if (totalzeros < minzeros)//if minimum zeros found so far is less than total zeros													  //not counting itself or other 0s in the same row and column of the cluster
                    {
                        minzeros = totalzeros;//set minzeros to total zeros
                        bestSquare.x = k;//set bestSquare to the current location
                        bestSquare.y = h;
                    }
                }
            }

            cluster = cluster - 2;
        }

        return bestSquare;

    }




};

