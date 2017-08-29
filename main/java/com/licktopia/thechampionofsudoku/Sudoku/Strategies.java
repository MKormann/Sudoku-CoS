package com.licktopia.thechampionofsudoku.Sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Strategies class contains static methods to find squares on which moves can be made.
 */

public class Strategies {

    public static class Hint {
        public int[][] hintInfo;

        public Hint(int[][] hintInfo) {
            this.hintInfo = hintInfo;
        }

        public int getRemoveFrom() {
            return hintInfo[REMOVE_FROM][0];
        }

    }

    public static final int REMOVE_FROM = 0;
    public static final int SELECTED = 1;
    public static final int CONNECTED = 2;
    public static final int NOTEPAD = 3;
    
    public static final int NUM_CATEGORIES = 4;

    public static final int NAKED_SINGLE = 1;
    public static final int NAKED_DOUBLE = 2;
    public static final int HIDDEN_SINGLE = 3;
    public static final int HIDDEN_PAIR = 4;
    public static final int NAKED_TRIPLE = 5;
    public static final int LOCKED_CANDIDATE = 6;
    public static final int XWING = 7;
    public static final int YWING = 8;
    public static final int SINGLES_CHAIN = 9;
    public static final int NUM_STRATEGIES = 9;

    public static final int COLOR_ONE = 100;
    public static final int COLOR_TWO = 200;
    public static final int NO_COLOR = 0;

    /* Returns a single random element of type <T> from a list */
    public static <T> T getRandomElement(List<T> list) {
        int rand = ThreadLocalRandom.current().nextInt(0, list.size());
        return list.get(rand);
    }

    private static int[] getGroup(Board board, int idx1, int idx2) {
        int group[];
        if (board.isSameRow(idx1, idx2)) group = board.getRowIndicesByIndex(idx1);
        else if (board.isSameCol(idx1, idx2)) group = board.getColIndicesByIndex(idx1);
        else group = board.getClusterIndicesByIndex(idx1);
        return group;
    }

    /****** NAKED SINGLES *******/

    /* Return a list of square indices that have only one possible value */
    public static List<Hint> getNakedSingles(Board board) {

        ArrayList<Hint> nakedSquares = new ArrayList<>();
        for (int i = 0; i < board.totalBoardElements(); i++) {
            if (!board.isStartingValue(i) && board.getValueAt(i) == 0 && board.getTotalNotepadValues(i) == 1) {
                int[][] hintInfo = new int[NUM_CATEGORIES][];
                hintInfo[REMOVE_FROM] = new int[] {CONNECTED};
                hintInfo[SELECTED] = new int[] {i};
                hintInfo[CONNECTED] = new int[] {};
                int np = 1;
                for (int x = 1; x <= board.getSize(); x++) {
                    if (board.getNotepadAt(i, x)) np = x;
                }
                hintInfo[NOTEPAD] = new int[] {np};
                nakedSquares.add(new Hint(hintInfo));
            }
        }
        return nakedSquares;
    }

    /* Executes the strategy provided by the given Hint object */
    public static void executeStrategy(Board board, Hint hint) {
        // Get indices to remove notepad values from
        ArrayList<Integer> indicesToEdit = new ArrayList<>();
        if (hint.getRemoveFrom() == CONNECTED) {
            for (int idx : hint.hintInfo[CONNECTED])
                indicesToEdit.add(idx);
            for (int idx : hint.hintInfo[SELECTED])
                indicesToEdit.remove(Integer.valueOf(idx));
        }
        else
            for (int idx : hint.hintInfo[SELECTED])
                indicesToEdit.add(idx);

        for (int idx : indicesToEdit) {
            for (int np : hint.hintInfo[NOTEPAD])
                board.setNotepadAt(idx, np, false);
        }
    }


    /****** HIDDEN SINGLES *******/

    /* Returns a list of Pairs (index - notepad num) of squares that have the sole occurrence of num in a group */
    public static List<Hint> getHiddenSingles(Board board) {

        ArrayList<Hint> hiddenSingles = new ArrayList<>();
        for (int[] group : board.getAllIndexGroups()) {
            for (int i = 1; i <= board.getSize(); i++) {
                // Find notepad values that only appear once in a group
                if (board.getTotalNotepadOccurrences(group, i) == 1) {
                    // Find square in group that has single appearance of value
                    for (int j = 0; j < group.length; j++) {
                        if (board.getNotepadAt(group[j], i)) {
                            int[][] hintInfo = new int[NUM_CATEGORIES][];
                            hintInfo[REMOVE_FROM] = new int[] {SELECTED};
                            hintInfo[SELECTED] = new int[] {group[j]};
                            hintInfo[CONNECTED] = group;
                            int[] notepad = new int[board.getSize() - 1];
                            for (int x = 0, y = 1; x < notepad.length; x++, y++) {
                                if (y == i) y++;
                                notepad[x] = y;
                            }
                            hintInfo[NOTEPAD] = notepad;
                            hiddenSingles.add(new Hint(hintInfo));
                        }
                    }
                }
            }
        }
        return hiddenSingles;
    }

    /****** NAKED PAIRS *******/

    /* Find all instances of naked pairs */
    public static List<Hint> getNakedPairs(Board board) {

        ArrayList<Hint> nakedPairs = new ArrayList<>();

        // Loop over every pair of squares
        for (int i = 0; i < board.totalBoardElements(); i++) {
            if (board.getValueAt(i) != 0 || board.getTotalNotepadValues(i) != 2) continue; // Skip non-empty squares
            for (int j = i + 1; j < board.totalBoardElements(); j++) {
                if (board.getValueAt(j) != 0) continue; // Skip non-empty squares
                // Only check pairs that are connected
                if ((board.isSameRow(i, j) || board.isSameCol(i, j) || board.isSameCluster(i, j)) && board.hasSameNotepad(i, j)) {
                    // Check to make sure there are values to be removed in other squares
                    List<Integer> values = board.getNotepadValues(i);
                    int[] group = getGroup(board, i, j);
                    if (board.getTotalNotepadOccurrences(group, values.get(0)) > 2 ||
                        board.getTotalNotepadOccurrences(group, values.get(1)) > 2) {

                        // Create hint object
                        int[][] hintInfo = new int[NUM_CATEGORIES][];
                        hintInfo[REMOVE_FROM] = new int[] {CONNECTED};
                        hintInfo[SELECTED] = new int[] {i, j};
                        hintInfo[CONNECTED] = group;
                        hintInfo[NOTEPAD] = new int[] {values.get(0), values.get(1)};
                        nakedPairs.add(new Hint(hintInfo));
                    }
                }
            }
        }

        return nakedPairs;
    }

    /****** HIDDEN PAIRS ********/

    // Hidden pairs are pairs of squares within a row/col/cluster that both contain the only two
    // appearances of a notepad number within that row/col/cluster
    public static List<Hint> getHiddenPairs(Board board) {

        ArrayList<Hint> hiddenPairs = new ArrayList<>();

        int numGroups = board.getSize() * 3;
        int[][] groups = board.getAllIndexGroups();
        for (int[] group: groups) {
            // Iterate over each group
            List<Integer> twoSquareNotepadNums = new ArrayList<>();
            // Find notepad numbers in group that appear in only two squares
            for (int j = 1; j <= board.getSize(); j++){
                if (board.getTotalNotepadOccurrences(group, j) == 2) {
                    // Check number against previously found numbers
                    for (int k = 0; k < twoSquareNotepadNums.size(); k++) {
                        if (board.hasTwoNumbersInSamePositions(group, twoSquareNotepadNums.get(k), j)) {
                            // Found hidden pair
                            int idx1 = -1;
                            int idx2 = -1;
                            for (int l = 0; l < board.getSize(); l++) {
                                if (board.getNotepadAt(group[l], j)) {
                                    if (idx1 == -1) idx1 = group[l];
                                    else idx2 = group[l];
                                }
                            }
                            // Check there are other notepad numbers to remove
                            if (board.getTotalNotepadValues(idx1) > 2 || board.getTotalNotepadValues(idx2) > 2) {

                                // Create hint object
                                int[][] hintInfo = new int[NUM_CATEGORIES][];
                                hintInfo[REMOVE_FROM] = new int[] {SELECTED};
                                hintInfo[SELECTED] = new int[] {idx1, idx2};
                                hintInfo[CONNECTED] = group;
                                int[] notepad = new int[board.getSize() - 2];
                                for (int x = 0, y = 1; x < notepad.length; x++, y++) {
                                    if (y == j || y == twoSquareNotepadNums.get(k)) y++;
                                    notepad[x] = y;
                                }
                                hintInfo[NOTEPAD] = notepad;
                                hiddenPairs.add(new Hint(hintInfo));
                            }
                        }
                    }
                    twoSquareNotepadNums.add(j);
                }
            }
        }

        return hiddenPairs;
    }

    /****** NAKED TRIPLE ********/

    public static List<Hint> getNakedTriples(Board board) {

        ArrayList<Hint> nakedTriples = new ArrayList<>();

        // Mark all squares that have 2 or 3 notepad values
        int size = board.getSize();
        boolean[] qualified = new boolean[size * size];
        for (int i = 0; i < board.totalBoardElements(); i++) {
            int npVals =  board.getTotalNotepadValues(i);
            qualified[i] = npVals == 2 || npVals == 3;
        }

        int[][] groups = board.getAllIndexGroups();
        for (int[] group : groups) {

            // Get all indices within group that qualify
            ArrayList<Integer> candidates = new ArrayList<>();
            for (int cnt = 0; cnt < group.length; cnt++) {
                int index = group[cnt];
                if (qualified[index]) candidates.add(index);
            }

            // Not enough possible candidates in group
            if (candidates.size() < 3) continue;

            // Compare sets of triplets for a naked triple
            int idx1, idx2, idx3;
            for (int x = 0; x < candidates.size() - 2; x++) {
                idx1 = candidates.get(x);
                for (int y = x + 1; y < candidates.size() - 1; y++) {
                    idx2 = candidates.get(y);
                    for (int z = y + 1; z < candidates.size(); z++) {
                        idx3 = candidates.get(z);
                        int count = 0;
                        int[] nums = new int[3];
                        for (int num = 1; num <= size; num++) {
                            if (board.isNotepadInTriple(num, idx1, idx2, idx3)) {
                                if (count < 3) nums[count++] = num;
                                else count++;
                            }
                        }

                        // Found a set of three squares that contain only 3 notepad numbers total
                        if (count == 3) {
                            // Check if triple is valid (will remove numbers from other squares)
                            boolean valid = false;
                            for (int index : group) {
                                if (index == idx1 || index == idx2 || index == idx3) continue;
                                else if (board.getNotepadAt(index, nums[0]) ||
                                        board.getNotepadAt(index, nums[1]) ||
                                        board.getNotepadAt(index, nums[2])) {
                                    valid = true;
                                    break;
                                }
                            }
                            if (valid) {
                                // Create hint object
                                int[][] hintInfo = new int[NUM_CATEGORIES][];
                                hintInfo[REMOVE_FROM] = new int[]{CONNECTED};
                                hintInfo[SELECTED] = new int[]{idx1, idx2, idx3};
                                hintInfo[CONNECTED] = group;
                                hintInfo[NOTEPAD] = nums;
                                nakedTriples.add(new Hint(hintInfo));
                            }
                        }
                    }
                }
            }

        }

        return nakedTriples;
    }


    /****** LOCKED CANDIDATE *******/

    // Search CLUSTERS for notepad candidates that are restricted to single row or column
    // Returns a Pair object containing the cluster number and the locked notepad number
    public static List<Hint> getLockedCandidates(Board board) {

        ArrayList<Hint> lockedCandidates = new ArrayList<>();

        // Iterate over each cluster to find possible restricted candidates
        for (int i = 0; i < board.getSize(); i++) {
            // Get cluster indices
            int[] indices = board.getClusterIndicesByIndex(i);
            // Iterate through each notepad number
            for (int np = 1; np <= board.getSize(); np++) {
                // Check if a number appears only two or three times in a cluster
                int occurrences = board.getTotalNotepadOccurrences(indices, np);
                if (occurrences == 2 || occurrences == 3) {
                    int first, second, third;
                    first = second = third = -1;
                    // Get indices of occurrences
                    for (int index : indices) {
                        if (board.getNotepadAt(index, np)) {
                            if (first == -1) first = index;
                            else if (second == - 1) second = index;
                            else third = index;
                        }
                    }
                    // See if occurrences are all in same row or col
                    if (board.isSameRow(first, second) && (third == -1 || board.isSameRow(first, third))) {
                        // If there are values in the row to be removed, add to list
                        int[] rowIndices = board.getRowIndicesByIndex(first);
                        int rowOccurrences = board.getTotalNotepadOccurrences(rowIndices, np);
                        if (rowOccurrences > occurrences) {

                            // Create hint object
                            int[][] hintInfo = new int[NUM_CATEGORIES][];
                            hintInfo[REMOVE_FROM] = new int[] {CONNECTED};
                            int[] selected;
                            if (board.isSameCluster(first, first - 1)) selected = new int[] {first - 1, first, second};
                            else if  (board.isSameCluster(second, second + 1)) selected = new int[] {first, second, second + 1};
                            else selected = new int[] {first, first + 1, second};
                            hintInfo[SELECTED] = selected;
                            hintInfo[CONNECTED] = board.getRowIndicesByIndex(first);
                            hintInfo[NOTEPAD] = new int[] {np};
                            lockedCandidates.add(new Hint(hintInfo));
                        }
                    }
                    else if (board.isSameCol(first, second) && (third == -1 || board.isSameCol(first, third))) {
                        // If there are values in the column to be removed, add to list
                        int[] colIndices = board.getColIndicesByIndex(first);
                        int colOccurrences = board.getTotalNotepadOccurrences(colIndices, np);
                        if (colOccurrences > occurrences) {

                            // Create hint object
                            int[][] hintInfo = new int[NUM_CATEGORIES][];
                            hintInfo[REMOVE_FROM] = new int[] {CONNECTED};
                            int[] selected;
                            int size = board.getSize();
                            if (board.isSameCluster(first, first - board.getSize()) && first >= size)
                                selected = new int[] {first - board.getSize(), first, second};
                            else if (board.isSameCluster(second, second + board.getSize()) && second < (size * size) - size)
                                selected = new int[] {first, second, second + board.getSize()};
                            else selected = new int[] {first, first + board.getSize(), second};
                            hintInfo[SELECTED] = selected;
                            hintInfo[CONNECTED] = board.getColIndicesByIndex(first);
                            hintInfo[NOTEPAD] = new int[] {np};
                            lockedCandidates.add(new Hint(hintInfo));
                        }
                    }
                }
            }
        }

        return lockedCandidates;
    }

    /****** X WING *******/
    //  Two rows (or two columns) each contain only two squares with a given candidate
    //  Same candidate in both rows (or columns) and share the same two columns (or rows)
    //  Eliminate that candidate from the other squares in that column (or row) since it must appear
    //  in one of the two spots in that column
    public static List<Hint> getXWings(Board board) {

        ArrayList<Hint> xWings = new ArrayList<>();

        // Get all index groups of rows and columns
        int[][] rows = board.getEveryRowIndices();
        int[][] cols = board.getEveryColIndices();
        int[][] groups = new int[rows.length + cols.length][];
        for (int i = 0; i < groups.length; i++) {
            if (i < rows.length) groups[i] = rows[i];
            else groups[i] = cols[i % cols.length];
        }
        // Iterate through every notepad number
        for (int num = 1; num <= board.getSize(); num++) {
            // Iterate through each group
            for (int i = 0; i < groups.length; i++) {
                // If group has exactly two occurrences of current notepad num
                if (board.getTotalNotepadOccurrences(groups[i], num) == 2) {
                    // Iterate through remaining rows if row or remaining columns if column
                    int iterateTo = i < rows.length ?  rows.length : groups.length;
                    for (int j = i + 1; j < iterateTo; j++) {
                        // If second group has matching position as first group
                        if (board.hasNumberInSamePositions(groups[i], groups[j], num)) {
                            int idx1 = board.getFirstOccurrence(groups[i], num);
                            int idx2 = board.getLastOccurrence(groups[i], num);
                            // Create hint object
                            int[][] hintInfo = new int[NUM_CATEGORIES][];
                            hintInfo[REMOVE_FROM] = new int[] {CONNECTED};
                            hintInfo[SELECTED] = new int[] {groups[i][idx1], groups[i][idx2], groups[j][idx1], groups[j][idx2]};
                            int[] conn1, conn2;
                            if (i < rows.length) {
                                // If instance found in two rows, get corresponding columns
                                conn1 = board.getColIndices(idx1);
                                conn2 = board.getColIndices(idx2);
                            }
                            else { // Get corresponding rows
                                conn1 = board.getRowIndices(idx1);
                                conn2 = board.getRowIndices(idx2);
                            }
                            // Check if xWing instance would remove any notepad numbers
                            if (board.getTotalNotepadOccurrences(conn1, num) > 2 || board.getTotalNotepadOccurrences(conn2, num) > 2) {
                                // Merge groups and add to Hint
                                int[] connected = new int[conn1.length + conn2.length];
                                for (int k = 0; k < connected.length; k++) {
                                    if (k < conn1.length) connected[k] = conn1[k];
                                    else connected[k] = conn2[k % conn2.length];
                                }
                                hintInfo[CONNECTED] = connected;
                                hintInfo[NOTEPAD] = new int[]{num};
                                xWings.add(new Hint(hintInfo));
                            }
                        }
                    }
                }
            }
        }

        return xWings;
    }

    /****** Y WING *******/
    //  Square #1 with only candidates AB, connected to two other squares, #2 and #3, (in different groups) with
    //  candidates AC and BC respectively.  Any other square that is connected to both #2 and #3
    //  is able to remove candidate C as one of those two squares must contain it.

    public static List<Hint> getYWings(Board board) {

        ArrayList<Hint> yWings = new ArrayList<>();
        int size = board.getSize() * board.getSize();

        // Get list of squares whose notepads have two values only
        ArrayList<Integer> twoNotepadSquares = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (board.getTotalNotepadValues(i) == 2)
                twoNotepadSquares.add(i);
        }

        // Iterate through all qualifying squares
        for (int i = 0; i < twoNotepadSquares.size(); i++) {
            int idx1 = twoNotepadSquares.get(i);
            for (int j = 0; j < twoNotepadSquares.size(); j++) {
                if (j == i) continue;
                int idx2 = twoNotepadSquares.get(j);
                int connectionOne =  board.isConnected(idx1, idx2);           // Check if connected
                if (connectionOne < 0) continue;
                int notepadA = board.getMatchingNotepadNum(idx1, idx2);       // Get candidate "A"
                if (notepadA < 0) continue;
                int notepadC = board.getMismatchingNotepadNum(idx1, idx2);    // Get candidate "C"
                if (notepadC < 0) continue;
                int notepadB = board.getMismatchingNotepadNum(idx2, idx1);    // Get candidate "B"
                for (int k = 0; k < twoNotepadSquares.size(); k++) {
                    if (k == i || k == j) continue;
                    int idx3 = twoNotepadSquares.get(k);
                    int connectionTwo = board.isConnected(idx1, idx3);                  // Check if connected
                    if (connectionTwo < 0 || connectionOne == connectionTwo) continue;  // Skip if same connection as one
                    // Check if third square has numbers "BC"
                    if (board.getNotepadAt(idx3, notepadB) && board.getNotepadAt(idx3, notepadC)) {
                        // Found eligible YWing
                        ArrayList<Integer> connectedSquares = board.getSharedConnections(idx2, idx3);
                        connectedSquares.remove(new Integer(idx1)); // Remove original square AB from connected list

                        // Check if strategy will remove a number
                        boolean valid = false;
                        for (int index : connectedSquares) {
                            if (board.getNotepadAt(index, notepadC)) {
                                valid = true;
                                break;
                            }
                        }

                        // Create hint object
                        if (valid) {
                            int[][] hintInfo = new int[NUM_CATEGORIES][];
                            hintInfo[REMOVE_FROM] = new int[]{CONNECTED};
                            hintInfo[SELECTED] = new int[] {idx1, idx2, idx3};
                            int[] connected = new int[connectedSquares.size()];
                            for (int x = 0; x < connected.length; x++) {
                                connected[x] = connectedSquares.get(x);
                            }
                            hintInfo[CONNECTED] = connected;
                            hintInfo[NOTEPAD] = new int[]{notepadC};
                            yWings.add(new Hint(hintInfo));
                        }
                    }
                }
            }
        }

        return yWings;
    }

    /****** SINGLES CHAIN *******/
    //  Connect an eligible list of squares containing a single notepad number and assign them
    //  one of two colors, alternating the color from the previous square. These chains can be checked
    //  for different criteria to see if numbers can be removed from the notepad

    public static List<Hint> getSinglesChains(Board board) {

        ArrayList<Hint> singlesChains = new ArrayList<>();
        int boardSize = board.totalBoardElements();

        for (int np = 1; np <= 9; np++) {

            // Arrays to track visited squares
            boolean[] visited = new boolean[boardSize];
            boolean[] hasNumber = new boolean[boardSize];

            // Get all squares with np number
            for (int i = 0; i < boardSize; i++) {
                if (board.getNotepadAt(i, np))
                    hasNumber[i] = true;
            }

            // Iterate through all squares, forming a queue for each separate chain
            for (int idx = 0; idx < boardSize; idx++) {
                if (hasNumber[idx] && !visited[idx]) {
                    // Create new chain and add current square
                    Queue<Integer> currentChain = new LinkedList<>();
                    currentChain.add(idx);
                    visited[idx] = true;
                    // Create lists to track colors
                    ArrayList<Integer> colorOne = new ArrayList<>();
                    ArrayList<Integer> colorTwo = new ArrayList<>();
                    colorOne.add(idx);
                    while (!currentChain.isEmpty()) {

                        // Check current number for unmarked chains
                        int current = currentChain.poll();
                        ArrayList nextColor = colorOne.contains(current) ? colorTwo : colorOne;
                        int rowChain = board.getSinglesChainInRow(current, np);
                        int colChain = board.getSinglesChainInCol(current, np);
                        int clusterChain = board.getSinglesChainInCluster(current, np);

                        // If new chains, add to currentChain
                        if (rowChain >= 0 && !visited[rowChain]) {
                            currentChain.add(rowChain);
                            visited[rowChain] = true;
                            nextColor.add(rowChain);
                        }
                        if (colChain >= 0 && !visited[colChain]) {
                            currentChain.add(colChain);
                            visited[colChain] = true;
                            nextColor.add(colChain);
                        }
                        if (clusterChain >= 0 && !visited[clusterChain]) {
                            currentChain.add(clusterChain);
                            visited[clusterChain] = true;
                            nextColor.add(clusterChain);
                        }
                    }
                    // Flags to track if we find a correct color
                    boolean oneCorrect = false;
                    boolean twoCorrect = false;
                    outer: for (int i = 0; i < colorOne.size(); i++) {
                        for (int j = i + 1; j < colorOne.size(); j++) {
                            if (board.isConnected(colorOne.get(i), colorOne.get(j)) > 0) {
                                twoCorrect = true;
                                break outer;
                            }
                        }
                    }
                    if (!twoCorrect) {
                    outer: for (int i = 0; i < colorTwo.size(); i++) {
                            for (int j = i + 1; j < colorTwo.size(); j++) {
                                if (board.isConnected(colorTwo.get(i), colorTwo.get(j)) > 0) {
                                    oneCorrect = true;
                                    break outer;
                                }
                            }
                        }
                    }
                    // Found a valid instance of single chain
                    if (oneCorrect || twoCorrect) {
                        int[] colorOneArray = new int[colorOne.size()];
                        int[] colorTwoArray = new int[colorTwo.size()];
                        for (int i = 0; i < colorOne.size(); i++) {
                            colorOneArray[i] = colorOne.get(i);
                        }
                        for (int i = 0; i < colorTwo.size(); i++) {
                            colorTwoArray[i] = colorTwo.get(i);
                        }
                        int[][] hintInfo = new int[NUM_CATEGORIES][];
                        hintInfo[REMOVE_FROM] = new int[]{CONNECTED};
                        hintInfo[SELECTED] = oneCorrect ? colorOneArray : colorTwoArray;
                        hintInfo[CONNECTED] = oneCorrect ? colorTwoArray : colorOneArray;
                        hintInfo[NOTEPAD] = new int[]{np};
                        singlesChains.add(new Hint(hintInfo));
                    }
                }
            }

/*
                    // TODO CHANGE THIS, NEED TO CHECK CONNECTING SQUARES FOR COLORS TOO BEFORE ASSIGNING
                    // If both uncolored, assign either color
                    if (squareColoring[idx1] == NO_COLOR && squareColoring[idx2] == NO_COLOR) {
                        squareColoring[idx1] = COLOR_ONE;
                        squareColoring[idx2] = COLOR_TWO;
                    }
                    // If only square two uncolored, assign opposite of first
                    else if (squareColoring[idx2] == NO_COLOR)
                        squareColoring[idx2] = squareColoring[idx1] == COLOR_ONE ? COLOR_TWO : COLOR_ONE;
                    // If only square one uncolored, assign opposite of second
                    else if (squareColoring[idx1] == NO_COLOR)
                        squareColoring[idx1] = squareColoring[idx2] == COLOR_ONE ? COLOR_TWO : COLOR_ONE;
                    // Else both are already colored, nothing to do
                }
            }*/
        }
        return singlesChains;
    }

    public static int executeStrategyFromList(Board board, List<Hint> hints) {
        if (!hints.isEmpty()) {
            Hint hint = getRandomElement(hints);
            executeStrategy(board, hint);
            return 1;   // Signify a move was made
        }
        else return 0;  // No move made
    }

    public static int executeSinglesStrategyFromList(Board board, List<Hint> hints) {
        if (!hints.isEmpty()) {
            Hint hint = getRandomElement(hints);
            int index = hint.hintInfo[SELECTED][0];
            board.setOnlyPossibleValueAt(index);
            return index;
        }
        else return -1; // Returns -1 for no move made since 0 is a valid index
    }

    /* Finds, selects, and executes naked pair strategy on one random existing hidden single */
    public static int removeRandomNakedSingle(Board board) {
        return executeSinglesStrategyFromList(board, getNakedSingles(board));
    }

    /* Finds, selects, and executes naked pair strategy on one random existing hidden single */
    public static int removeRandomHiddenSingle(Board board) {
        return executeStrategyFromList(board, getHiddenSingles(board));
    }
    /* Finds, selects, and executes naked pair strategy on one random existing naked pair */
    public static int removeRandomNakedPairValues(Board board) {
        return executeStrategyFromList(board, getNakedPairs(board));
    }

    /* Finds, selects, and executes hidden pair strategy on one random existing hidden pair */
    public static int removeRandomHiddenPairValues(Board board) {
        return executeStrategyFromList(board, getHiddenPairs(board));
    }

    /* Finds, selects, and executes naked triple strategy on one random existing instance */
    public static int removeRandomNakedTripleValues(Board board) {
        return executeStrategyFromList(board, getNakedTriples(board));
    }

    public static int removeRandomLockedCandidateValues(Board board) {
        return executeStrategyFromList(board, getLockedCandidates(board));
    }

    public static int removeRandomXWingValues(Board board) {
        return executeStrategyFromList(board, getXWings(board));
    }

    public static int removeRandomYWingValues(Board board) {
        return executeStrategyFromList(board, getYWings(board));
    }

    public static int removeRandomSinglesChainValues(Board board) {
        return executeStrategyFromList(board, getSinglesChains(board));
    }

    //updates all squares' notepads so that only valid moves are true
    public static void updateAllNotepads(Board board, boolean removeOnly)
    {
        for(int i = 0; i < board.totalBoardRows(); i++)
        {
            for(int j = 0; j < board.totalBoardCols(); j++)
            {
                updateNotepad(board, i, j, removeOnly);
            }
        }
    }

    // updates notepad for square at row, col so that only valid moves are true
    // if removeOnly is set, update will only remove notepad values, not add any
    public static void updateNotepad(Board board, int row, int col, boolean removeOnly)
    {
        if(!board.isStartingValue(row, col) && board.getValueAt(row, col) == 0)
        {
            //used to prevent the score from being impacted
            Board tempBoard = new Board(board);
            for(int i = 1; i <= board.getSize(); i++)
            {
                Set<Integer> testStk = tempBoard.setValueAt(row, col, i);
                if(testStk.isEmpty())
                {
                    if (!removeOnly) board.setNotepadAt(row, col, i, true);
                }
                else
                {
                    board.setNotepadAt(row, col, i, false);
                }
                //commented out because tempBoard was created
                //board.setValueAt(row, col, 0);
            }
        }
    }
}
