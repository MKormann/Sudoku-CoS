package com.licktopia.thechampionofsudoku.Multiplayer;

/**
 * Created by John Konecny on 7/24/2017.
 */

import android.content.Context;
import android.util.Log;

import com.google.example.games.basegameutils.GameHelper;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.GameInterface;
import com.licktopia.thechampionofsudoku.Sudoku.Game;
import com.licktopia.thechampionofsudoku.Sudoku.Solver;
import com.licktopia.thechampionofsudoku.Sudoku.Square;
import com.licktopia.thechampionofsudoku.Sudoku.Strategies;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;


import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;


import static java.lang.System.exit;

public class MultiGame implements GameInterface, java.io.Serializable {

    final static String TAG = "MultiGame";

    public static final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
    private Game mGame;

    //stores the bots and the threads they are playing on
    private Bot[] allBots;

    //stores the progress of human players
    private Opponent[] humanOpps;

    private GameMode mode;
    boolean isBotGame = false;

    //set to -100 to prevent an infinite loop of message sending
    final long DEFAULT_RANK = -100;
    final long MIN_RANK = 0;

    public enum GameMode {
        RACE(1000),
        BATTLE(2000),
        COOP(3000);

        private final int value;
        private GameMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static GameMode valueToGameMode(int num)
        {
            GameMode[] allValues = GameMode.values();

            for(int i = 0; i < allValues.length; i++)
            {
                if(allValues[i].getValue() == num)
                {
                    return allValues[i];
                }
            }
            return null;
        }
    }

    public enum ConnectType{
        BOT_GAME,
        HUMAN_GAME
    }

    public enum MessageType{
        GAME_OBJ,
        GAME_UPDATE,
        GAME_OVER,
        PLAYER_INFO,
        DEBUG
    }

    private class Opponent
    {
        private String mGamerId;
        private float mGameProgress;
        private boolean mGameFinished;
        private String mGamerName;
        private String mImageUri;
        private long mRank;

        protected Opponent(String gamerId, String gamerName, String imageUri)
        {
            mGameProgress = 0;
            mGamerName = gamerName;
            mRank = DEFAULT_RANK;
            mGamerId = gamerId;
            mImageUri = imageUri;
        }

        protected GameUpdate getGameUpdate() {
            return new GameUpdate(mGamerId, mGameProgress, mGameFinished);
        }

        protected float getGameProgress() {
            return mGameProgress;
        }

        protected long getRank() {return mRank;}

        protected void setRank(long rank) {
            if(rank < 0)
            {
                Log.d(TAG, "Bad Rank attempted to be sent!");
                mRank = MIN_RANK;
            }
            else
            {
                mRank = rank;
            }
        }

        protected void updateProgress(GameUpdate update)
        {
            //make sure data is good
            if(update.getGameProgress() < 0 || update.getGameProgress() > 1)
            {
                Log.d(TAG, "Progress was < 0.0 or > 1.0");
                return;
            }
            else if (update.getGameProgress() != 1 && update.isGameFinished())
            {
                Log.d(TAG, "Progress said game was over, but progress doesn't equal 1");
                return;
            }
            mGameProgress = update.getGameProgress();
            mGameFinished = update.isGameFinished();
        }

        protected String getGamerId() {
            return mGamerId.toString();
        }
        protected String getGamerName() {
            return mGamerName.toString();
        }

        protected boolean isGameFinished() { return mGameFinished; }

        protected String getImageUri() { return mImageUri;}
    }


    protected class GameUpdate implements java.io.Serializable
    {
        private String mGamerId;
        private float mGameProgress;
        private boolean mGameFinished;

        protected GameUpdate(String gamerId, float gameProgress, boolean gameFinished)
        {
            mGamerId = gamerId;
            mGameProgress = gameProgress;
            mGameFinished = gameFinished;
        }

        protected float getGameProgress() {
            return mGameProgress;
        }

        protected String getGamerId() {
            return mGamerId.toString();
        }

        protected boolean isGameFinished() { return mGameFinished; }

        private void writeObject(java.io.ObjectOutputStream stream)
                throws IOException {

            char[] charArr = mGamerId.toCharArray();
            stream.writeInt(charArr.length);
            for(int i = 0; i < charArr.length; i++)
            {
                stream.writeChar(charArr[i]);
            }

            stream.writeFloat(mGameProgress);
            stream.writeBoolean(mGameFinished);

        }

        private void readObject(java.io.ObjectInputStream stream)
                throws IOException, ClassNotFoundException {

            int strLen = stream.readInt();

            char[] charArr = new char[strLen];

            for(int i = 0; i < strLen; i++)
            {
                charArr[i] = stream.readChar();
            }

            mGamerId = new String(charArr);
            mGameProgress = stream.readFloat();
            mGameFinished = stream.readBoolean();
        }

    }

    protected class PlayerInfo implements java.io.Serializable
    {
        private String mGamerId;
        private long mRank;

        protected PlayerInfo(String gamerId, long rank)
        {
            mGamerId = gamerId;
            mRank = rank;
        }

        protected long getRank() {
            return mRank;
        }

        protected String getGamerId() {
            return mGamerId.toString();
        }

        private void writeObject(java.io.ObjectOutputStream stream)
                throws IOException {

            char[] charArr = mGamerId.toCharArray();
            stream.writeInt(charArr.length);
            for(int i = 0; i < charArr.length; i++)
            {
                stream.writeChar(charArr[i]);
            }

            stream.writeLong(mRank);

        }

        private void readObject(java.io.ObjectInputStream stream)
                throws IOException, ClassNotFoundException {

            int strLen = stream.readInt();

            char[] charArr = new char[strLen];

            for(int i = 0; i < strLen; i++)
            {
                charArr[i] = stream.readChar();
            }

            mGamerId = new String(charArr);
            mRank = stream.readLong();
        }

    }


    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;
    // My participant ID in the currently active game
    private String mMyId = null;
    // Client used to interact with Google APIs.
    private GameHelper gameHelper = null;
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    /****************************************
     * start functions already implemented
     **************************************/

    /****
     * Constructor
     * @param mode what type of multiplayer game the user wants
     * @param cType how user wants to find people to play against
     * @param level difficulty of board
     * @param ctx
     */
    public MultiGame(GameMode mode,
                     ConnectType cType,
                     int level,
                     Context ctx,
                     ArrayList<Participant> mParticipants,
                     String mMyId,
                     GameHelper gameHelper,
                     String mRoomId)
    {
        this.mParticipants = mParticipants;
        this.mMyId = mMyId;
        this.gameHelper = gameHelper;
        this.mRoomId = mRoomId;
        initializePlayerArray();
        this.mode = mode;
        this.mGame = new Game(level, ctx);
        switch (cType)
        {
            case BOT_GAME:
                isBotGame = true;
                sendGameState();
                break;
            case HUMAN_GAME:
                isBotGame = false;
                sendGameState();
                sendMyInfo();
                break;
            default:
                Log.d(TAG, "Invalid ConnectionType Used");
                exit(1);
                break;
        }


    }


    public MultiGame(GameMode mode,
                     ConnectType cType,
                     int level,
                     int boardNumber,
                     int[] values,
                     ArrayList<Participant> mParticipants,
                     String mMyId,
                     GameHelper gameHelper,
                     String mRoomId)
    {
        this.mParticipants = mParticipants;
        this.mMyId = mMyId;
        this.gameHelper = gameHelper;
        this.mRoomId = mRoomId;
        initializePlayerArray();
        this.mode = mode;
        this.mGame = new Game(boardNumber, level, values);
        switch (cType)
        {
            case BOT_GAME:
                isBotGame = true;
                sendGameState();
                break;
            case HUMAN_GAME:
                isBotGame = false;
                sendGameState();
                sendMyInfo();
                break;
            default:
                Log.d(TAG, "Invalid ConnectionType Used");
                exit(1);
                break;
        }


    }




    private MultiGame(Game mGame,
                      ArrayList<Participant> mParticipants,
                      String mMyId,
                      GameHelper gameHelper,
                      String mRoomId)
    {
        this.mParticipants = mParticipants;
        this.mMyId = mMyId;
        this.gameHelper = gameHelper;
        this.mRoomId = mRoomId;
        initializePlayerArray();
        this.mGame = mGame;
        isBotGame = false;

        sendMyInfo();
    }


    public static MultiGame byteToObj(byte[] msgBuffer,
                                      ArrayList<Participant> mParticipants,
                                      String mMyId,
                                      GameHelper gameHelper,
                                      String mRoomId)
    {
        Game curGame;

        //done to check and see if the byte array is working
        try(ByteArrayInputStream b = new ByteArrayInputStream(msgBuffer)){

            ObjectInputStream o = new ObjectInputStream(b);
            MessageType temp =(MessageType)o.readObject();

            // This message is not a game object so prevent an error from happening
            if(temp != MessageType.GAME_OBJ)
                return null;

            curGame = (Game)o.readObject();


        }
        catch(IOException e)
        {
            Log.d(TAG, "Failed to load the game from byte array!");
            return null;
        }
        catch(ClassNotFoundException e)
        {
            Log.d(TAG, "What class are you talking about?");
            return null;
        }

        return new MultiGame(curGame,
                mParticipants,
                mMyId,
                gameHelper,
                mRoomId);

    }

    void initializePlayerArray()
    {
        if(isBotGame)
        {
            allBots = new Bot[MAX_OPPONENTS];
        }
        else
        {
            // don't include yourself
            humanOpps = new Opponent[mParticipants.size() - 1];
            int i = 0;
            for (Participant p : mParticipants) {

                if (p.getParticipantId().equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                humanOpps[i] = new Opponent(p.getParticipantId(), p.getDisplayName(), p.getIconImageUrl());
                i++;
            }

        }
    }

    void sendMyInfo()
    {
        Log.d(TAG, "Sending my info");
        if(isBotGame)
        {
            //do nothing
        }
        else
        {
            sendMessage(MessageType.PLAYER_INFO, new PlayerInfo(mMyId, BaseActivity.multiplayerRank));
        }

    }



    //send byte array with game state in buffer
    //mgame cannot be null!
    private void sendGameState()
    {

        //debug multiplayer UI using bots
        if(isBotGame)
        {
            //done to check and see if the byte array is working

            for(int i = 0; i < allBots.length; i++)
            {
                allBots[i] = new Bot(new Game(mGame), "SudokuBot" + Integer.toString(i) + "000", 5);
            }
        }
        else
        {
            byte[] msgBuffer = null;

            Log.d(TAG, "Sending the game object");
            try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
                ObjectOutputStream o = new ObjectOutputStream(b);


                o.writeObject(mGame);

                msgBuffer = b.toByteArray();
                o.close();
            }
            catch(IOException e)
            {
                Log.d(TAG, "Failed to make mgame a byte array");
            }

            //send game state away to other clients
            Log.d(TAG, "Sending Game...");
            sendMessage(MessageType.GAME_OBJ, mGame);
        }
    }


    /************************************************************
     * Puts together a byte array and sends it to all opponents
     * ATTACH ONLY THE MESSAGE BEING SENT
     * EXAMPLE: For progress send the floating point number 0.5 as a byte array
     * do not add anything else to it. This method will add all necessary bytes
     * for readMessage to be able to understand what was sent.
     * @param msgType
     * @param msgObj object being sent
     */
    private void sendMessage(MessageType msgType, Object msgObj)
    {
        boolean sendReliably = true;
        byte[] msgBuffer;

        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            ObjectOutputStream o = new ObjectOutputStream(b);

            //write message type as the header
            o.writeObject(msgType);

            //create message
            switch (msgType)
            {
                case GAME_OBJ:
                    o.writeObject((Game)msgObj);
                    sendReliably = true;
                    break;
                case GAME_UPDATE:
                    o.writeObject((GameUpdate)msgObj);
                    // this message does not always need to reach the other players
                    sendReliably = false;
                    break;
                case PLAYER_INFO:
                    o.writeObject((PlayerInfo)msgObj);
                    sendReliably = true;
                    break;
                case DEBUG:
                    o.writeObject((String)msgObj);
                    sendReliably = true;
                    break;
                case GAME_OVER:
                    o.writeObject((GameUpdate)msgObj);
                    sendReliably = true;
                    break;
                default:
                    Log.d(TAG, "Invalid MessageType Used");
                    return;
            }



            msgBuffer = b.toByteArray();
            o.close();
        }
        catch(IOException e)
        {
            Log.d(TAG, "Failed to create the message!");
            return;
        }




        //send message
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (sendReliably) {
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(gameHelper.getApiClient(), null, msgBuffer,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(gameHelper.getApiClient(), msgBuffer, mRoomId,
                        p.getParticipantId());
            }
        }

    }


    /********************************
     * Read messages from other users
     * ALWAYS ASSUME THE DATA COULD BE COMPRISED
     * @param msgBuffer
     */
    public void readMessage(byte[] msgBuffer)
    {


        try(ByteArrayInputStream b = new ByteArrayInputStream(msgBuffer)){

            ObjectInputStream o = new ObjectInputStream(b);
            //get the header
            MessageType msgType = (MessageType)o.readObject();

            switch (msgType)
            {
                case GAME_OBJ:
                    Log.d(TAG, "I received a game object!");
                    //this is handled in the baseActivity.java class
                    break;
                case GAME_UPDATE:
                    Log.d(TAG, "I received a game update!");
                    GameUpdate update = (GameUpdate)o.readObject();
                    for (int i = 0; i < humanOpps.length; i++) {
                        if(humanOpps[i].getGamerId().equals(update.getGamerId()))
                        {
                            humanOpps[i].updateProgress(update);
                            break;
                        }
                    }
                    break;
                case GAME_OVER:
                    Log.d(TAG, "An opponent has finished their game!");
                    GameUpdate endGameUpdate = (GameUpdate)o.readObject();
                    for (int i = 0; i < humanOpps.length; i++) {
                        if(humanOpps[i].getGamerId().equals(endGameUpdate.getGamerId()))
                        {
                            humanOpps[i].updateProgress(endGameUpdate);
                            break;
                        }
                    }
                    break;
                case PLAYER_INFO:
                    Log.d(TAG, "An opponent has sent their XP to us!");
                    PlayerInfo playerInfo = (PlayerInfo)o.readObject();
                    for (int i = 0; i < humanOpps.length; i++) {
                        if(humanOpps[i].getGamerId().equals(playerInfo.getGamerId()))
                        {
                            //they probably don't have my info
                            if(humanOpps[i].getRank() == DEFAULT_RANK)
                            {
                                sendMyInfo();
                            }
                            // prevent an infinite loop of calling sendMyInfo
                            if(playerInfo.getRank() == DEFAULT_RANK)
                            {
                                humanOpps[i].setRank(MIN_RANK);
                            }
                            else
                            {
                                humanOpps[i].setRank(playerInfo.getRank());
                            }

                            break;
                        }
                    }
                    break;
                case DEBUG:
                    Log.d(TAG, "I received a debug message!");
                    String debugMessage = (String)o.readObject();
                    Log.d(TAG, "Debug Message: " + debugMessage);
                    break;
                default:
                    Log.d(TAG, "Invalid MessageType Used");
                    return;
            }


        }
        catch(IOException e)
        {
            Log.d(TAG, "Failed to load the game from byte array!");
            return;
        }
        catch(ClassNotFoundException e)
        {
            Log.d(TAG, "What class are you talking about?");
            return;
        }
    }


    /*********************************************************************
     *
     * @return An array of the image uri's of all the opponents
     */
    public String[] getAllImgUri()
    {
        String[] uris;

        if(isBotGame)
        {
            uris = new String[allBots.length];
            for(int i = 0; i < allBots.length; i++)
            {
                uris[i] = null;
            }
        }
        else
        {
            uris = new String[humanOpps.length];
            for(int i = 0; i < humanOpps.length; i++)
            {
                uris[i] = humanOpps[i].getImageUri();
            }
        }

        return uris;

    }


    /*****
     *
     * @return total number of bots
     */
    public int getTotalOpponents()
    {
        if(isBotGame)
        {
            return allBots.length;
        }
        else
        {
            return humanOpps.length;
        }
    }

    /******
     * get the opponent score
     * @param i index of the opponent
     * @return the opponent score
     */
    public long getOpponentScore(int i)
    {
        if(isBotGame)
        {
            return this.allBots[i].getGameObj().getScore();
        }
        else
        {
            return -100;
        }
    }

    public long getOpponentRank(int i)
    {
        if(isBotGame)
        {
            return this.allBots[i].getRank();
        }
        else
        {
            return humanOpps[i].getRank();
        }
    }

    public int getOpponentFilledSquares(int i)
    {
        if(isBotGame)
        {
            return  this.allBots[i].getGameObj().totalBoardElements()
                    - this.allBots[i].getGameObj().getNumberOpenSquares();
        }
        else
        {
            return -100;
        }
    }

    public boolean isOpponentGameFinished(int i)
    {
        if(isBotGame)
        {
            return this.allBots[i].getGameObj().isGameFinished();
        }
        else
        {
            return humanOpps[i] != null && humanOpps[i].isGameFinished();

        }

    }

    /***********************************************
     * Total board elements opponent's board has
     * @param i index of opponent
     * @return total board elements opponent's board has
     */
    public int getOpponentTotalBoardElements(int i)
    {
        if(isBotGame)
        {
            return  this.allBots[i].getGameObj().totalBoardElements();
        }
        else
        {
            return -100;
        }
    }

    /************************************************
     * Return the name of the opponent
     * @param i index of opponent
     * @return name of opponent
     */
    public String getOpponentName(int i)
    {
        if(isBotGame)
        {
            return this.allBots[i].getName();
        }
        else
        {
            return humanOpps[i].getGamerName();
        }
    }

    /************************************************
     * Returns a number between 0.0 and 1.0
     * 1 means opponent is completely finished
     * 0 means opponent hasn't made any progress
     * @param i index of opponent
     * @return the progress of the opponent as a fraction
     */
    public float getOpponentProgress(int i)
    {
        if(isBotGame)
        {
            return (float)1.0 - ((float)allBots[i].getGameObj().getNumberOpenSquares()
                    / (float)allBots[i].getGameObj().getStartingOpenSquares());
        }
        else if(humanOpps[i] != null)
        {
            return humanOpps[i].getGameProgress();
        }
        else
        {
            return 0;
        }

    }

    public float getMyProgress()
    {
        return (float)1.0 - ((float)getNumberOpenSquares()
                / (float)this.getStartingOpenSquares());
    }

    /****
     *
     * @param i total number of open squares for bot i
     * @return total number of filled in squares
     */
    public long getOpponentNumberFilledSquares(int i)
    {
        return this.allBots[i].getGameObj().totalBoardElements() - this.allBots[i].getGameObj().getNumberOpenSquares();
    }

    /*******
     * get total number of squares filled in by the user
     * @return total number of squares filled in by the user
     */
    public long getNumberFilledSquares()
    {
        return this.mGame.totalBoardElements() - this.mGame.getNumberOpenSquares();
    }

    private long myOldRank = MIN_RANK;
    /*************************************
     * gets my rank before the call to updateMyRank
     * @return
     */
    public long getMyOldRank()
    {
        return myOldRank;
    }
    /******************************************************
     * Called at the end of the game and returns the new rank of a player
     * UPDATES THE RANK IN THE BASEACTIVITY CLASS!!!!!
     * @return
     */
    public long updateMyRank(boolean isWinner)
    {
        // Are you really a champion of Sudoku if you can only beat bots?
        if(isBotGame)
        {
            return 0;
        }

        myOldRank = BaseActivity.multiplayerRank;
        double winProbability = 0;
        // ranks are divided by this number
        final double rankDenominator = 1e6d;

        // maximum amount a rank can change in a single game
        final double K = 1000d / rankDenominator;


        // we are going to assume that the rank will be a number between
        // 0 and 5,000,000
        // We cannot have the number be too large on the code side since
        // the exponential function grows so quickly

        for(int i = 0; i < humanOpps.length; i++)
        {
            // prevent overflow from calculating the exponential function
            // will now be a number between 0.0 and 5.0
            double temp = (double)humanOpps[i].getRank() / rankDenominator;

            // won't be actual win probability when for loop is complete
            winProbability += Math.exp(temp);
        }

        double myFactoredRank = ((double)BaseActivity.multiplayerRank / rankDenominator);

        // this is our real win probability
        // should be a number between 0.0 and 1.0
        winProbability =
                Math.exp(myFactoredRank) /
                (Math.exp(myFactoredRank) + winProbability);


        if(isWinner)
        {
            BaseActivity.multiplayerRank =
                    (long)((myFactoredRank + K * (1 - winProbability)) * rankDenominator);
        }
        else
        {
            BaseActivity.multiplayerRank =
                    (long)((myFactoredRank - K * winProbability) * rankDenominator);

            // don't allow negative ranks
            if(BaseActivity.multiplayerRank < 0)
            {
                BaseActivity.multiplayerRank = 0;
            }
        }

        return BaseActivity.multiplayerRank;
    }

    /****************************************
     * end of functions already implemented
     **************************************/



    /****************************************************
     * Game Class Object Functions
     ****************************************************/

    public int getImageNumber() {
        return mGame.getImageNumber();
    }

    public void setImageNumber(int imageNumber) {
        mGame.setImageNumber(imageNumber);
    }

    //imageNumber
    private int imageNumber;
    //time in seconds
    private long runningTime = 0;


    //sets value of square given row and col
    //returns empty stack if successful
    //returns ids of problem squares in stack if failed
    public Set<Integer> setBoardValueAt(int row, int col, int value) {
        Set<Integer> tempSet = mGame.setBoardValueAt(row, col, value);
        sendGameUpdate();
        return tempSet;
    }

    //sets value of square given the index
    //returns empty stack if successful
    //returns ids of problem squares in stack if failed
    public Set<Integer> setBoardValueAt(int idx, int value) {
        Set<Integer> tempSet = mGame.setBoardValueAt(idx, value);
        sendGameUpdate();
        return tempSet;
    }

    private void sendGameUpdate()
    {
        if(this.isGameFinished())
        {
            sendMessage(MessageType.GAME_OVER, new GameUpdate(mMyId, this.getMyProgress(), this.isGameFinished()));
        }
        else
        {
            sendMessage(MessageType.GAME_UPDATE, new GameUpdate(mMyId, this.getMyProgress(), this.isGameFinished()));
        }

    }

    // Returns a set of the squares in the current board that are wrong
    public Set<Integer> checkCurrentProgress() {
        Set<Integer> incorrectSquares = mGame.checkCurrentProgress();
        return incorrectSquares;
    }

    //gets value of square given row and col
    public int getBoardValueAt(int row, int col) {
        return mGame.getBoardValueAt(row, col);
    }

    //gets board number
    public long getBoardNumber(){return mGame.getBoardNumber();}

    //gets value of square given the index
    public int getBoardValueAt(int idx) {
        return mGame.getBoardValueAt(idx);
    }

    //gets number of 1's, 2's, etc
    public int[] getValueTotals(){
        return mGame.getValueTotals();
    }

    //return if a square at the index is a starting value
    public boolean isStartingValue(int idx)
    {
        return mGame.isStartingValue(idx);
    }


    //return if a square at row, col is a starting value
    public boolean isStartingValue(int row, int col)
    {
        return mGame.isStartingValue(row, col);
    }

    //set cell notepad value for given num
    //for a square given its index for a single dimensional array of the board
    public void setBoardNotepadAt(int idx, int num, boolean value) {
        mGame.setBoardNotepadAt(idx, num, value);
    }

    //set cell notepad value for given num
    public void setBoardNotepadAt(int row, int col, int num, boolean value) {
        mGame.setBoardNotepadAt(row, col, num, value);
    }

    //toggles cell notepad value for given num
    //for a square given its index for a single dimensional array of the board
    public void toggleBoardNotepadAt(int idx, int num) {
        mGame.toggleBoardNotepadAt(idx, num);
    }

    //toggles cell notepad value for given num
    public void toggleBoardNotepadAt(int row, int col, int num) {
        mGame.toggleBoardNotepadAt(row, col, num);
    }

    //gets the notepad value for boolean num
    // for a square given its index for a single dimensional array of the board
    public boolean getBoardNotepadAt(int idx, int num) {
        return mGame.getBoardNotepadAt(idx, num);
    }

    //get cell notepad value for given num
    public boolean getBoardNotepadAt(int row, int col, int num) {
        return mGame.getBoardNotepadAt(row, col, num);
    }

    //returns index of a altered cell after an undo
    //returns -1 if no undo occurred
    public int undo()
    {
        int temp = mGame.undo();
        sendGameUpdate();
        return temp;
    }

    //returns true if a undo can occur
    public boolean canUndo()
    {
        return mGame.canUndo();
    }

    //returns index of a altered cell after an redo
    //returns -1 if no redo occurred
    public int redo()
    {
        int temp = mGame.redo();
        sendGameUpdate();
        return temp;
    }

    //returns true if a redo can occur
    public boolean canRedo()
    {
        return mGame.canRedo();
    }

    //pauses timer
    public void pauseTimer()
    {
        mGame.pauseTimer();

    }

    public void unpauseTimer()
    {
        mGame.unpauseTimer();

        if(isBotGame)
        {
            //let the games begin
            for(int i = 0; i < allBots.length; i++)
            {
                allBots[i].startPlayingGame();
            }
        }

    }

    //returns running time in seconds
    public long getRunningTime()
    {
        return mGame.getRunningTime();
    }

    //gets the total score for the game
    public long getScore()
    {
        return mGame.getScore();
    }

    public long getLevelBonus()
    {
        return mGame.getLevelBonus();
    }

    public long getTotalOneTimeGuesses()
    {
        return mGame.getTotalOneTimeGuesses();
    }

    public long getOneTimeGuessSquareBonus()
    {
        return mGame.getOneTimeGuessSquareBonus();
    }

    //in points per seconds
    public long getTimePenalty() { return mGame.getTimePenalty(); }

    //in points per seconds
    public double getIncorrectGuessesPenalty()
    {
        return mGame.getIncorrectGuessesPenalty();
    }

    public long getRunningTimeRemovalInterval() {return mGame.getRunningTimeRemovalInterval();}

    // Ensures that points are only removed when running time hit an interval set by
    // getRunningTimeRemovalInterval
    // Example: getRunningTimeRemovalInterval = 25 secs and getTimePenalty = 10 pts/secs
    // At 25 secs total penalty is 10 pts and this function would return 1 intervals
    // At 49 secs total penalty is 10 pts and this function would return 1 intervals
    // At 50 secs total penalty is 20 pts and this function would return 2 intervals
    public long totalPenaltyTimeIntervals()
    {
        return mGame.totalPenaltyTimeIntervals();
    }

    public long totalIncorrectGuesses()
    {
        return mGame.totalIncorrectGuesses();
    }

    public double getAccuracy() {
        return mGame.getAccuracy();
    }

    public long getEarlyFinishBonus() {return  mGame.getEarlyFinishBonus();}

    public long getEarlyFinishTime() {return  mGame.getEarlyFinishTime();}

    public int getTotalHintsUsed() {return mGame.getTotalHintsUsed();}
    public long getHintPenalty() {return mGame.getHintPenalty();}

    //passes current state of game to new solver
    public void updateSolver() {
        mGame.updateSolver();
    }

    //returns the steps required to solve from a blank board
    public Queue<Square> getSolutionSteps()
    {
        return mGame.getSolutionSteps();
    }

    public void updateAllNotepads(boolean removeOnly) {
        mGame.updateAllNotepads(removeOnly);
    }

    public Strategies.Hint getHint(int hintType) {
        return mGame.getHint(hintType);
    }

    public void removeNakedDoubles(Strategies.Hint hint) {
        mGame.removeNakedDoubles(hint);
    }

    //returns the total number of elements in the board

    public  int totalBoardElements() {return mGame.totalBoardElements(); }

    //returns the total number of rows in the board
    public  int totalBoardRows() {return mGame.totalBoardRows(); }

    //returns the total number of columns in the board
    public  int totalBoardCols() {return mGame.totalBoardCols(); }

    //returns the column/row size of a cluster
    public  int getClusterSize() {return mGame.getClusterSize(); }

    //true if the board is complete
    public boolean isGameFinished()
    {
        return mGame.isGameFinished();
    }

    public void resetGame()
    {
        mGame.resetGame();
    }

    public int getNumberOpenSquares(){
        return mGame.getNumberOpenSquares();
    }

    /************************************************************************
     * Solver object solution to the game
     * @return Solver object solution to the game
     */
    public Solver getSolutionSolver() {return mGame.getSolutionSolver();}


    public int getStartingOpenSquares() { return mGame.getStartingOpenSquares(); }

    public void backupAllNotepads() { mGame.backupAllNotepads(); }

    public void restoreAllNotepads() { mGame.restoreAllNotepads(); }
}
