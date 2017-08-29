package com.licktopia.thechampionofsudoku.Sudoku;

import android.content.Context;
import android.util.Log;

import com.licktopia.thechampionofsudoku.Database.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Set;

/**
 * BoardofClass class represents a single instance of a Sudoku puzzle
 */

public class Board implements java.io.Serializable {

    private static int SIZE = 9;         // BoardofClass size
    private static int CLUSTER_SIZE = 3; // Size of N x N cluster
    private Square[][] contents;           // Holds values of the sudoku puzzle
    private int startingOpenSquares;     // Tracks the starting amount of open squares
    private int openSquares;            // Tracks how many squares are empty
    private int incorrectGuesses = 0; //total number of incorrect guesses



    private long boardIdentifier;

     /* Constructor*/
    protected Board(int level, Context ctx, long index) {

        int[] values;
        if (index == -1) {
            values = DbIO.loadBoard(level, ctx, SIZE * SIZE);
            boardIdentifier = DatabaseAccess.getRandomBoardIndex();
            Log.i("BoardDB", "BoardID: " + boardIdentifier);
        }
        else {
            values = DbIO.loadSpecificBoard(level, ctx, SIZE * SIZE, index);
            boardIdentifier = index;
        }

        // Check parameter has correct number of values
        if (values.length != SIZE * SIZE) {
            System.out.printf("The loaded game contains %d elements but needs to have %d elements\n", values.length, SIZE * SIZE);
        }

        contents = new Square[SIZE][SIZE];
        openSquares = 0;

        // Copy parameter values into contents array
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {   // Non-empty square
                contents[i / SIZE][i % SIZE] = new Square(values[i], true, SIZE, i);
            }
            else                    // Empty square
            {
                contents[i / SIZE][i % SIZE] = new Square(values[i], false, SIZE, i);
                openSquares++;
            }
            startingOpenSquares = openSquares;

        }
    }

    /* Constructor*/
    protected Board(long boardIdentifier, int[] values) {

        this.boardIdentifier = boardIdentifier;
        // Check parameter has correct number of values
        if (values.length != SIZE * SIZE) {
            System.out.printf("The loaded game contains %d elements but needs to have %d elements\n", values.length, SIZE * SIZE);
        }

        contents = new Square[SIZE][SIZE];
        openSquares = 0;

        // Copy parameter values into contents array
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {   // Non-empty square
                contents[i / SIZE][i % SIZE] = new Square(values[i], true, SIZE, i);
            }
            else                    // Empty square
            {
                contents[i / SIZE][i % SIZE] = new Square(values[i], false, SIZE, i);
                openSquares++;
            }
            startingOpenSquares = openSquares;

        }
    }





    //creates exact copy of passed board
    protected Board(Board oldBoard)
    {


        contents = new Square[SIZE][SIZE];
        openSquares = oldBoard.openSquares;
        startingOpenSquares = oldBoard.startingOpenSquares;

        // copy all data from oldBoard into this
        for (int i = 0; i < this.totalBoardRows(); i++) {
            for (int j = 0; j < this.totalBoardCols(); j++) {
                contents[i][j] = new Square(oldBoard.contents[i][j]);
            }
        }
    }


    //stores the ids of squares which prevent setValueAt from making a move
    private Set<Integer> problemSqrs;
    /* Inserts parameter value at the board location specified by row and col.
    *  Returns empty stack if value inserted successfully, not empty if not */
    public Set<Integer> setValueAt(int row, int col, int value) {
        //return this set
        problemSqrs = new HashSet<Integer>();

        if (value < 0 || value > SIZE)      // Check value input
            return problemSqrs;
        if (contents[row][col].isStartingValue())        // Check not trying to change a starting value
            return problemSqrs;

        boolean isValid = false;
        //stores the current state of square in case the move is invalid
        int tempVal = contents[row][col].getValue();
        contents[row][col].setValue(value);

        if (value == 0) {                   // Removing value
            openSquares++;                  // Increment number of open squares
            isValid = true;
        }
        else {
            int clusterNum = ((row / CLUSTER_SIZE) * CLUSTER_SIZE) + (col / CLUSTER_SIZE);  // Get cluster number

            //ensure that all of these operations are performed since problemSqrs gets update in them
            boolean isRowValid = isRowValid(row);
            boolean isColumnValid = isColumnValid(col);
            boolean isClusterValid = isClusterValid(clusterNum);
            problemSqrs.remove(row * SIZE + col);
            //isRowValid, isColumnValid and isClusterValid all add to this.problemSqrs if they are false
            isValid = (isRowValid && isColumnValid && isClusterValid);
        }

        if(isValid)
        {
            if(tempVal == 0)
            {
                openSquares--; // Decrement number of open squares
            }

            //clear redo
            redoStk = new Stack();
            undoStk.push(new Square(contents[row][col]));
            undoStk.peek().setValue(tempVal);

        }
        else
        {
            incorrectGuesses++;
            contents[row][col].setValue(tempVal);     // restore old value
        }

        return problemSqrs;
    }

    //sets value of square given the index
    //returns empty stack if successful
    //returns ids of problem squares in stack if failed
    public Set<Integer> setValueAt(int idx, int value) {
        return this.setValueAt(idxToRow(idx), idxToCol(idx), value);
    }

    public long getBoardIdentifier() {
        return boardIdentifier;
    }

    // sets square at idx to it's only possible value
    protected Set<Integer> setOnlyPossibleValueAt(int idx) {
        if (getTotalNotepadValues(idx) != 1)
            return null;
        int value = 1;
        for (int i = 1; i <= SIZE; i++) {
            if (getNotepadAt(idx, i))
                value = i;
        }
        return setValueAt(idx, value);
    }


    /* Get value at given row/column */
    public int getValueAt(int row, int col) {
        return contents[row][col].getValue();
    }

    //gets value of square given the index
    protected int getValueAt(int idx) {
        return this.getValueAt(idxToRow(idx), idxToCol(idx));
    }

    protected void setNotepadAt(int row, int col, int num, boolean value) {
        contents[row][col].setNotepad(num, value);
    }

    //set cell notepad value for given num
    //for a square given its index for a single dimensional array of the board
    protected void setNotepadAt(int idx, int num, boolean value) {
        this.setNotepadAt(idxToRow(idx), idxToCol(idx), num, value);
    }

    //sets notepad value num of square idx to true, all others to false
    protected void setOnlyNotepadValue(int idx, int num) {
        for (int i = 1; i <= SIZE; i++) {
            if (i == num) setNotepadAt(idx, i, true);
            else setNotepadAt(idx, i, false);
        }
    }

    protected boolean getNotepadAt(int row, int col, int num) {
        return contents[row][col].getNotepad(num);
    }

    //get cell notepad value for given num
    protected boolean getNotepadAt(int idx, int num) {
        return this.getNotepadAt(idxToRow(idx), idxToCol(idx), num);
    }

    //returns a list of all the values in the notepad of idx
    protected List<Integer> getNotepadValues(int idx) {
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) {
            if (getNotepadAt(idx, i)) values.add(i);
        }
        return values;
    }

    //return if a square at row, col is a starting value
    protected boolean isStartingValue(int row, int col)
    {
        return contents[row][col].isStartingValue();
    }

    //return if a square at the index is a starting value
    protected boolean isStartingValue(int idx)
    {
        return this.isStartingValue(idxToRow(idx), idxToCol(idx));
    }


    //return if a square at row, col is a starting value
    protected boolean noMoreThanOneGuessAt(int row, int col)
    {
        return contents[row][col].noMoreThanOneGuess();
    }

    //return if a square at the index is a starting value
    protected boolean noMoreThanOneGuessAt(int idx)
    {
        return this.noMoreThanOneGuessAt(idxToRow(idx), idxToCol(idx));
    }


    /* Returns whether or not every space has been filled */
    protected boolean isBoardFull() {
        return (openSquares == 0);
    }


    /* Checks if current board is valid */
    //adds id of problem square to problemSqrs
    protected boolean isBoardValid() {
        //must be reset before using isRowValid isColumnValid and !isClusterValid
        problemSqrs = new HashSet<Integer>();
        for (int i = 0; i < SIZE; i++) {
            if (!isRowValid(i) || !isColumnValid(i) || !isClusterValid(i)){}
                return false; // BoardofClass not correct
        }
        return true;
    }

    /* Checks if board is complete and correct */
    protected boolean isSolution() {
        return isBoardFull() && isBoardValid(); // BoardofClass is complete and correct
    }


    /* Check that row provided does not contain conflicts */
    // adds to this.problemSqr if false
    private boolean isRowValid(int row) {

        for (int i = 0; i < SIZE - 1; i++) {
            for (int j = i + 1; j < SIZE; j++) {
                if (contents[row][i].getValue() != 0 && contents[row][i].getValue() == contents[row][j].getValue())
                {
                    problemSqrs.add(row * SIZE + i);
                    problemSqrs.add(row * SIZE + j);
                    return false;  // Found repeat, row invalid
                }

            }
        }
        return true; // Row is valid
    }


    /* Check that column provided does not contain conflicts */
    // adds to this.problemSqr if false
     private boolean isColumnValid(int col) {
        for (int i = 0; i < SIZE - 1; i++) {
            for (int j = i + 1; j < SIZE; j++) {
                if (contents[i][col].getValue() != 0 && contents[i][col].getValue() == contents[j][col].getValue())
                {
                    problemSqrs.add(i * SIZE + col);
                    problemSqrs.add(j * SIZE + col);

                    return false; // Found repeat, column invalid
                }
            }
        }
        return true; // Column is valid
    }


    /* Check that cluster provided does not contain conflicts */
    // adds to this.problemSqr if false
    private boolean isClusterValid(int cluster) {

        // Get cluster start indices based on board size
        int rowStart = (cluster / CLUSTER_SIZE) * CLUSTER_SIZE;
        int colStart = (cluster % CLUSTER_SIZE) * CLUSTER_SIZE;

        // Add clusterNums to array
        int[] clusterNums = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            clusterNums[i] = contents[(i / CLUSTER_SIZE) + rowStart][(i % CLUSTER_SIZE) + colStart].getValue();
        }

        //check for no repeats
        for (int i = 0; i < SIZE - 1; i++) {
            for(int j = i + 1; j < SIZE ; j++)
            {
                if (clusterNums[i] != 0 && clusterNums[i] == clusterNums[j])
                {
                    problemSqrs.add(((i / CLUSTER_SIZE) + rowStart) * SIZE + ((i % CLUSTER_SIZE) + colStart));
                    problemSqrs.add(((j / CLUSTER_SIZE) + rowStart) * SIZE + ((j % CLUSTER_SIZE) + colStart));

                    return false; // Found repeat, cluster invalid
                }
            }

        }
        return true; // Cluster is valid
    }


    /* Clear board to original state */
    protected void resetToStart() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                contents[i][j].reset();
            }
        }
    }


    //returns current state of a altered cell after an undo
    //returns null if no undo occurred
    private Stack<Square> undoStk = new Stack<>();
    protected int undo()
    {
        if(undoStk.isEmpty()){
            return -1;
        }
        Square temp = undoStk.pop();

        if(temp.getValue() == 0 && contents[temp.getRow()][temp.getCol()].getValue() != 0)
        {
            openSquares++;
        }
        else if(temp.getValue() != 0 && contents[temp.getRow()][temp.getCol()].getValue() == 0)
        {
            openSquares--;
        }

        redoStk.push(new Square(contents[temp.getRow()][temp.getCol()]));

        contents[temp.getRow()][temp.getCol()] = temp;
        return temp.getIdx();
    }

    //returns true if a undo can occur
    protected boolean canUndo()
    {
        return !undoStk.isEmpty();
    }

    //returns current state of a altered cell after an redo
    //returns null if no redo occurred
    private Stack<Square> redoStk = new Stack<>();
    protected int redo()
    {
        if(redoStk.isEmpty()){
            return -1;
        }

        Square temp = redoStk.pop();

        if(temp.getValue() == 0 && contents[temp.getRow()][temp.getCol()].getValue() != 0)
        {
            openSquares++;
        }
        else if(temp.getValue() != 0 && contents[temp.getRow()][temp.getCol()].getValue() == 0)
        {
            openSquares--;
        }


        undoStk.push(new Square(contents[temp.getRow()][temp.getCol()]));

        contents[temp.getRow()][temp.getCol()] = temp;
        return temp.getIdx();
    }

    //returns true if a redo can occur
    protected boolean canRedo()
    {
        return !redoStk.isEmpty();
    }


    //gets the length for both the rows and columns of the game
    protected int getSize()
    {
            return  SIZE;
    }

    //gets the length for both the rows and columns of a cluster
    protected int getClusterSize() {
        return CLUSTER_SIZE;
    }

    // Returns number of values set to true in notepad
    protected int getTotalNotepadValues(int row, int col) {
        int total = 0;
        for (int i = 1; i <= SIZE; i++) {
            if (getNotepadAt(row, col, i)) total++;
        }
        return total;
    }

    // Returns number of values set to true in notepad
    protected int getTotalNotepadValues(int idx) {
        return getTotalNotepadValues(idxToRow(idx), idxToCol(idx));
    }

    // Returns number of times a notepad number appears in a given group of indices
    protected int getTotalNotepadOccurrences(int[] indices, int num) {
        int total = 0;
        for (int i : indices)
            if (getValueAt(i) == 0 && getNotepadAt(i, num)) total++;
        return total;
    }

    // Compare notepad of one square to another
    protected boolean hasSameNotepad(int idx1, int idx2) {
        for (int i = 1; i <= SIZE; i++) {
            if (getNotepadAt(idx1, i) != getNotepadAt(idx2, i))
                return false;
        }

        return true;
    }

    // Compares a list of squares to see if two notepad numbers appear in the same positions
    protected boolean hasTwoNumbersInSamePositions(int[] indices, int num1, int num2) {
        for (int i : indices) {
            if (getNotepadAt(i, num1) != getNotepadAt(i, num2))
                return false;
        }
        return true;
    }

    // Compare two groups of indices to see if they have the same positions of a notepad number
    protected boolean hasNumberInSamePositions(int[] indices1, int[] indices2, int num) {
        for (int i = 0; i < indices1.length; i++) {
            if (getNotepadAt(indices1[i], num) != getNotepadAt(indices2[i], num))
                return false;
        }
        return true;
    }

    // Get index of first occurrence of a notepad number in a group
    protected int getFirstOccurrence(int[] indices, int num) {
        for (int i = 0; i < indices.length; i++) {
            if (getNotepadAt(indices[i], num)) return i;
        }
        return -1;
    }

    // Get index of last occurrence of a notepad number in a group
    protected int getLastOccurrence(int[] indices, int num) {
        for (int i = indices.length - 1; i >= 0; i--) {
            if (getNotepadAt(indices[i], num)) return i;
        }
        return -1;
    }

    // Returns if two indices are in the same row
    protected boolean isSameRow(int idx1, int idx2) { return idxToRow(idx1) == idxToRow(idx2); }

    // Returns if two indices are in the same column
    protected boolean isSameCol(int idx1, int idx2) { return idxToCol(idx1) == idxToCol(idx2); }

    // Returns if two indices are in the same cluster
    protected boolean isSameCluster(int idx1, int idx2) {
        int cluster1 = ((idxToRow(idx1) / CLUSTER_SIZE) * CLUSTER_SIZE) + (idxToCol(idx1) / CLUSTER_SIZE);
        int cluster2 = ((idxToRow(idx2) / CLUSTER_SIZE) * CLUSTER_SIZE) + (idxToCol(idx2) / CLUSTER_SIZE);
        return cluster1 == cluster2;
    }

    // Returns if two indices are connected: 1 if same row, 2 if same column, 3 if same cluster, -1 if not connected
    protected int isConnected(int idx1, int idx2) {
        if (isSameRow(idx1, idx2))
            return 1;
        if (isSameCol(idx1, idx2))
            return 2;
        if (isSameCluster(idx1, idx2))
            return 3;
        else return -1;
    }

    protected boolean isNotepadInTriple(int notepad, int idx1, int idx2, int idx3) {
        return getNotepadAt(idx1, notepad) || getNotepadAt(idx2, notepad) || getNotepadAt(idx3, notepad);
    }

    // Get first instance of both indices having same notepad number
    protected int getMatchingNotepadNum(int idx1, int idx2) {
        for (int i = 1; i <= 9; i++) {
            if (getNotepadAt(idx1, i) && getNotepadAt(idx2, i))
                return i;
        }
        return -1;
    }

    // Get first instance of notepad number present in idx2 but not idx1
    protected int getMismatchingNotepadNum(int idx1, int idx2) {
        for (int i = 1; i <= 9; i++) {
            if (!getNotepadAt(idx1, i) && getNotepadAt(idx2, i))
                return i;
        }
        return -1;
    }

    // Get set of square indices that are connected to both idx1 and idx2
    protected ArrayList<Integer> getSharedConnections(int idx1, int idx2) {
        ArrayList<Integer> connections = new ArrayList<>();
        for (int i = 0; i < totalBoardElements(); i++) {
            if (i != idx1 && i != idx2 && isConnected(idx1, i) > 0 && isConnected(idx2, i) > 0) {
                connections.add(i);
            }
        }
        return connections;
    }

    protected int getSinglesChainInRow(int idx, int np) {
        return getSinglesChain(idx, np, getRowIndicesByIndex(idx));
    }

    protected int getSinglesChainInCol(int idx, int np) {
        return getSinglesChain(idx, np, getColIndicesByIndex(idx));
    }

    protected int getSinglesChainInCluster(int idx, int np) {
        return getSinglesChain(idx, np, getClusterIndicesByIndex(idx));
    }

    // Returns index in group that forms a singles chain with idx and notepad provided
    protected int getSinglesChain(int idx, int np, int[] group) {
        if (getTotalNotepadOccurrences(group, np) == 2) {
            for (int chain : group) {
                if (chain != idx && getNotepadAt(chain, np))
                    return chain;
            }
        }
        return -1;
    }

    // Returns an array of indices of all squares in same row as idx
    protected int[] getRowIndicesByIndex(int idx) {
        return getRowIndices(idxToRow(idx));
    }

    // Returns an array of indices of all squares in row
    protected int[] getRowIndices(int row) {
        int[] indices = new int[SIZE];
        int rowStart = row * SIZE;
        for (int i = 0; i < SIZE; i++) {
            indices[i] = rowStart + i;
        }
        return indices;
    }

    // Returns a 2d array of indices in each row
    protected int[][] getEveryRowIndices() {
        int[][] rows = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            rows[i] = getRowIndices(i);
        return rows;
    }

    // Returns an array of indices of all squares in same column as idx
    protected int[] getColIndicesByIndex(int idx) {
        return getColIndices(idxToCol(idx));
    }

    // Returns an array of indices of all squares in column
    protected int[] getColIndices(int col) {
        int[] indices = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            indices[i] = col + (SIZE * i);
        }
        return indices;
    }

    // Returns a 2d array of indices in each col
    protected int[][] getEveryColIndices() {
        int[][] cols = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            cols[i] = getColIndices(i);
        return cols;
    }

    // Returns an array of indices of all squares in same cluster as idx
    protected int[] getClusterIndicesByIndex(int idx) {

        int cluster = ((idxToRow(idx) / CLUSTER_SIZE) * CLUSTER_SIZE) + (idxToCol(idx) / CLUSTER_SIZE);
        return getClusterIndices(cluster);
    }

    // Returns an array of indices of all squares in cluster provided
    protected int[] getClusterIndices(int cluster) {

        // Get cluster start indices based on board size
        int rowStart = (cluster / CLUSTER_SIZE) * CLUSTER_SIZE;
        int colStart = (cluster % CLUSTER_SIZE) * CLUSTER_SIZE;
        int indexStart = rowStart * SIZE + colStart;

        // Add indices to array
        int[] indices = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            indices[i] = indexStart + (i % CLUSTER_SIZE) + ((i / CLUSTER_SIZE) * SIZE);
        }

        return indices;
    }

    // Returns a 2d array of indices in each cluster
    protected int[][] getEveryClusterIndices() {
        int[][] clusters = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            clusters[i] = getClusterIndices(i);
        return clusters;
    }

    // Returns a 2d array where each outer array is one of the rows/cols/clusters of the board
    protected int[][] getAllIndexGroups() {
        int[][] groups = new int[SIZE * 3][SIZE];
        for (int i = 0; i < SIZE; i++) {
            groups[i] = getRowIndices(i);
            groups[i + SIZE] = getColIndices(i);
            groups[i + SIZE + SIZE] = getClusterIndices(i);
        }

        return groups;
    }

    //returns the number of open squares in board
    protected  int getOpenSquares() { return openSquares; }

    protected int getStartingOpenSquares() { return startingOpenSquares; }

    //returns the total number of elements in the board
    protected  int totalBoardElements() {return totalBoardRows() * totalBoardCols(); }

    //returns the total number of rows in the board
    protected  int totalBoardRows() {return contents.length; }

    //returns the total number of columns in the board
    protected  int totalBoardCols() {return contents[0].length; }

    //retuns the current state of a square at row, col
    protected  Square getSquare(int row, int col) {return new Square(contents[row][col]);}

    //retuns the current state of a square at idx
    protected  Square getSquare(int idx) {return new Square(contents[idxToRow(idx)][idxToCol(idx)]);}

    protected int totalIncorrectGuesses(){ return incorrectGuesses; }

    protected void backupAllNotepads() {
        for (Square[] row : contents) {
            for (Square square : row) {
                square.backupNotepad();
            }
        }
    }

    protected void restoreAllNotepads() {
        for (Square[] row : contents) {
            for (Square square : row) {
                square.restoreNotepad();
            }
        }
    }

    //given an index for a single dimensional array of the board
    //converts it into the correct row of a 2d board array
    private int idxToRow(int idx)
    {
        return idx / this.getSize();
    }

    //given an index for a single dimensional array of the board
    //converts it into the correct column of a 2d board array
    private int idxToCol(int idx)
    {
        return idx % this.getSize();
    }
}