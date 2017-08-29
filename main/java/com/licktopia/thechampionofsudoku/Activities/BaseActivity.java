package com.licktopia.thechampionofsudoku.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.AboutFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.BoardFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.CompletedGameFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.GamerProfileFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.InstructionsFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.InviteDialogFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.MainMenuFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.SavedGameFragment;
import com.licktopia.thechampionofsudoku.Activities.UIActivities.UserBoardFragment;
import com.licktopia.thechampionofsudoku.Adapters.UserGameListAdapter;
import com.licktopia.thechampionofsudoku.DBOjects.SavedGameData;
import com.licktopia.thechampionofsudoku.Multiplayer.MultiGame;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.Adapters.SavedGameListAdapter;
import com.licktopia.thechampionofsudoku.Settings.SudokuSettingsFragment;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;
import com.licktopia.thechampionofsudoku.DBOjects.SavedGame;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.licktopia.thechampionofsudoku.Activities.UIActivities.MainMenuFragment.mProgressBar;
import static com.licktopia.thechampionofsudoku.Activities.UIActivities.MainMenuFragment.mainScreen;

/**
 * Created by Matt on 6/22/2017.
 */

public class BaseActivity extends BaseGameActivity
        implements MainMenuFragment.MainMenuFragmentListener,
        UserBoardFragment.UserBoardFragmentListener,
        CompletedGameFragment.CompletedGameFragmentListener,
        SavedGameFragment.SavedGameFragmentListener,
        GamerProfileFragment.GamerProfileFragmentListener,
        BoardFragment.BoardFragmentListener,
        RealTimeMessageReceivedListener, SavedGameListAdapter.SavedGameLAListener, UserGameListAdapter.UserGameLAListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {

    public static final String MAIN_MENU_FRAGMENT = "MAIN_MENU_FRAGMENT";
    public static final String ABOUT_FRAGMENT = "ABOUT_FRAGMENT";
    public static final String BOARD_FRAGMENT = "BOARD_FRAGMENT";
    public static final String COMPLETED_GAME_FRAGMENT = "COMPLETED_GAME_FRAGMENT";
    public static final String GAMER_PROFILE_FRAGMENT = "GAMER_PROFILE_FRAGMENT";
    public static final String INSTRUCTIONS_FRAGMENT = "INSTRUCTIONS_FRAGMENT";
    public static final String SAVED_GAME_FRAGMENT = "SAVED_GAME_FRAGMENT";
    public static final String INVITE_DIALOG_FRAGMENT_SHOW = "INVITE_DIALOG_FRAGMENT_SHOW";
    public static final String INVITE_DIALOG_FRAGMENT_HIDE = "INVITE_DIALOG_FRAGMENT_HIDE";
    public static final String SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    public static final String USER_BOARD_FRAGMENT = "USER_BOARD_FRAGMENT";
    public static GameHelper gameHelper;
    public static boolean clickedOut = false;
    public static boolean userSignIn=false;
    public static int phoneSize = 2;

    //will store the multiplayer game information
    private MultiGame mMultiGame = null;
    private boolean switchToBoard = false;


    public static int[] CUTOFF = {0,250,5000,11000,18000,30000,42000,55000,70000,90000,115000,140000,167000,195000,225000,262000,305000,355000,413000,480000,560000,650000,
            755000, 880000, 1025000, 1190000, 1375000, 1600000, 1850000, 2145000, 2485000, 2875000, 3320000, 3835000, 4425000, 5107000, 5890000,6785000,
            7815000, 9000000, 10345000,11890000,13665000,15695000,18020000,20675000,25625000,35705000,46000000,61625000,77500000,99000000};
    public static double[] MULTIPLIER = {1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,1.9,2.0,2.1,2.2,2.3,2.4,2.5,2.6,2.7,2.8,2.9,3.0,3.1,3.2,3.3,3.4,3.5,3.6,3.7,3.8,3.9,
                                         4.0,4.1,4.2,4.3,4.4,4.5,4.6,4.7,4.8,4.9,5.0,5.1,5.2,5.3,5.4,5.5,5.6,5.7,5.8,5.9,6.0,6.1};
    public static  int[] BADGETAGS = {R.drawable.rank0,R.drawable.rank1,R.drawable.rank2,R.drawable.rank3,R.drawable.rank4,R.drawable.rank5,R.drawable.rank6,R.drawable.rank7,
            R.drawable.rank8,R.drawable.rank9,R.drawable.rank10,R.drawable.rank11,R.drawable.rank12,R.drawable.rank13,R.drawable.rank14,R.drawable.rank15,
            R.drawable.rank16,R.drawable.rank17,R.drawable.rank18,R.drawable.rank19,R.drawable.rank20,R.drawable.rank21,R.drawable.rank22,R.drawable.rank23,
            R.drawable.rank24,R.drawable.rank25,R.drawable.rank26,R.drawable.rank27,R.drawable.rank28,R.drawable.rank29,R.drawable.rank30,R.drawable.rank31,
            R.drawable.rank32,R.drawable.rank33,R.drawable.rank34,R.drawable.rank35,R.drawable.rank36,R.drawable.rank37,R.drawable.rank38,R.drawable.rank39,
            R.drawable.rank40,R.drawable.rank41,R.drawable.rank42,R.drawable.rank43,R.drawable.rank44,R.drawable.rank45,R.drawable.rank46,R.drawable.rank47,R.drawable.rank48,R.drawable.rank49,R.drawable.rank50,R.drawable.rank51};
    public static String[] RANKNAMES = {"Elementary","Freshman","Sophomore","Junior","Senior","Graduate","Bronze I","Bronze II","Bronze III","Bronze IV","Bronze V","Bronze Eagle","Silver I","Silver II","Silver III","Silver IV","Silver V",
            "Silver Eagle","Gold I","Gold II","Gold III","Gold IV","Gold V","Golden Eagle", "Sapphire I", "Sapphire II", "Sapphire III", "Sapphire IV", "Sapphire Elite",
            "Diamond I", "Diamond II", "Diamond III", "Diamond IV", "Diamond Elite","Master I","Master II","Master III","Master IV","Master V","Grand Master","Legend I","Legend II","Legend III","Legend IV","Legend V",
            "Super Legend","Ace","Genius","Wizard","Champion","Super Champion","Grand Champion"};

    public static  String username;
    public static String id = "0";
    public static int level=0;
    public static long xp;
    public static long multiplayerRank = 0;
    //public static ArrayList<CompletedGame> completedGames;
    //public static ArrayList<SavedGame> savedGames;
    public static CognitoCachingCredentialsProvider credentialsProvider;
    public static AmazonDynamoDBClient ddbClient;
    public static DynamoDBMapper mapper;

    DialogFragment inviteDialog = null;
    final static String TAG = "BaseActivity";

    public void swapFragment(String fragmentName, Bundle args) {

        Fragment fragment = null;

        switch(fragmentName) {
            case ABOUT_FRAGMENT:
                fragment = AboutFragment.newInstance();
                break;
            case BOARD_FRAGMENT:
                fragment = BoardFragment.newInstance();
                break;
            case COMPLETED_GAME_FRAGMENT:
                fragment = CompletedGameFragment.newInstance();
                break;
            case GAMER_PROFILE_FRAGMENT:
                fragment = GamerProfileFragment.newInstance();
                break;
            case INSTRUCTIONS_FRAGMENT:
                fragment = InstructionsFragment.newInstance();
                break;
            case SAVED_GAME_FRAGMENT:
                fragment = SavedGameFragment.newInstance();
                break;
            case USER_BOARD_FRAGMENT:
                fragment = UserBoardFragment.newInstance();
                break;
            case SETTINGS_FRAGMENT:
                fragment = SudokuSettingsFragment.newInstance();
                break;
            case INVITE_DIALOG_FRAGMENT_SHOW:
                inviteDialog.setArguments(args);
                inviteDialog.show(getSupportFragmentManager(), INVITE_DIALOG_FRAGMENT_SHOW);
                return;
            case INVITE_DIALOG_FRAGMENT_HIDE:
                inviteDialog.dismiss();
                return;
            case MAIN_MENU_FRAGMENT:
            default:
                fragment = MainMenuFragment.newInstance();
                break;
        }

        if (fragment != null)
            fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }



    /*public static byte[] getThisSavedGame(String exactTime){
        for(int i = 0; i < savedGames.size(); i ++){
            if(exactTime.compareTo(savedGames.get(i).getExactTime()) == 0){
                return savedGames.get(i).getSaveFile();
            }
        }
        return null;
    }*/







    public static boolean addExperience(long xp) {
        BaseActivity.xp += xp;
        return setLevel(xp);

    }


    /* Check if a level up has occurred */
    public static boolean setLevel(long totalXP) {
        // Upgrade level as much as necessary, stopping at last level
        int oldLevel = level;
        int counter = 0;
        boolean flag=false;
        while (counter<CUTOFF.length && level < CUTOFF.length &&flag==false) {
            if (xp >= CUTOFF[level]) {
                    level++;
            }
            else{
                flag=true;
            }

        }
        level--;

        if(level>oldLevel){
            return true;
        }
        else{
            return false;
        }
    }

    /**** Basic getters & setters ****/

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        BaseActivity.username = username;
    }

    public static int getLevel() {
        return level;
    }



    public static long getXp() {
        return xp;
    }

    public static void setXp(long xp) {

        BaseActivity.xp = xp;
        setLevel(xp);
    }


    public static long getMultiplayerRank() {
        return multiplayerRank;
    }

    public static void setMultiplayerRank(long rank) {

        BaseActivity.multiplayerRank = rank;
    }


    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        BaseActivity.id = id;
    }

    public static void setNullUser(){
        BaseActivity.username = null;
        BaseActivity.id = null;
        BaseActivity.xp = 0;
        BaseActivity.level = 0;
        BaseActivity.multiplayerRank = 0;
    }

    public static Long formatExactTime(String time) {
        Long timeNumber;
        StringBuilder str = new StringBuilder();
        for(char c : time.toCharArray()){
            if(c != ','){
                str.append(c);
            }

        }
        timeNumber = Long.valueOf(str.toString());
        return timeNumber;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inviteDialog = InviteDialogFragment.newInstance();

        setContentView(R.layout.activity_base);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        boolean isNormalPhone = getResources().getBoolean(R.bool.is_normal_phone);
        boolean isPhablet = getResources().getBoolean(R.bool.is_phablet);

        if(isTablet){
            phoneSize = 4;
        }
        else if(isPhablet){
            phoneSize = 3;
        }
        else if(isNormalPhone){
            phoneSize = 2;
        }
        else {
            phoneSize =1;
        }

        //Check if there is a fragment container
        if (findViewById(R.id.fragment_container) != null) {

            //Check if there is already a fragment displayed
            if (savedInstanceState != null) {
                return;
            }

            MainMenuFragment menuFragment = MainMenuFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, menuFragment, MAIN_MENU_FRAGMENT).commit();
        }

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-2:40802ef0-7537-4910-8442-31e2b30c5a7d", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );
       /* CognitoSyncManager client = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_2,
                credentialsProvider);
                */
        mMultiGame = null;

        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_2));
        gameHelper = getGameHelper();
        //completedGames = new ArrayList<>();
        //savedGames = new ArrayList<>();





    }

    @Override
    protected void onStop() {
        super.onStop();
        stopKeepingScreenOn();
    }

    /* Implemented from MainMenuFragmentListener */
    public int getBadgetag() {
        return BADGETAGS[level];
    }
    public void beginUserInitSignIn() {
        beginUserInitiatedSignIn();
    }

    /* Implemented from GamerProfileFragmentListener*/
    public String getRank() { return RANKNAMES[level]; }
    public int getCutoff() { return CUTOFF[level]; }
    public int getNextCutoff() { return CUTOFF[level + 1]; }

    /* Implemented from BoardFragmentListener */
    public double getMultiplier() { return MULTIPLIER[level]; }




    @Override
    public void onSignInFailed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof MainMenuFragment) {
            MainMenuFragment mmf = (MainMenuFragment)f;
            mmf.updateUI(false);
            return;
        }
        else if (f instanceof BoardFragment) {
            BoardFragment ba = (BoardFragment)f;
            ba.updateUI(false);
            return;
        }
        else if (f instanceof GamerProfileFragment) {
            GamerProfileFragment gpf = (GamerProfileFragment)f;
            gpf.signInFailed();
            return;
        }
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "In onSignInSucceeded");
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        Games.Invitations.registerInvitationListener(gameHelper.getApiClient(), this);
        if(gameHelper.hasInvitation())
        {
            acceptInviteToRoom(null);
            return;
        }
        if (f instanceof MainMenuFragment) {
            MainMenuFragment mmf = (MainMenuFragment)f;
            mmf.updateUI(true);
            return;
        }
        else if (f instanceof BoardFragment) {
            BoardFragment ba = (BoardFragment)f;
            ba.updateUI(true);
            return;
        }
        else if (f instanceof GamerProfileFragment) {
            GamerProfileFragment gpf = (GamerProfileFragment)f;
            gpf.signInSucceeded();
            return;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "In onActivityResult");
        super.onActivityResult(requestCode,resultCode,data);
        gameHelper.onActivityResult(requestCode, resultCode, data);


        /************************************************************************
         * From:
         * https://github.com/playgameservices/android-basic-samples/blob/master/BasicSamples/ButtonClicker/src/main/java/com/google/example/games/bc/MainActivity.java
         */
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(resultCode, data);
                break;
            case RC_INVITATION_INBOX:
                handleInvitationInboxResult(resultCode, data);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (resultCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startMultiGame();
                } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
        }



    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof BoardFragment) {
            BoardFragment ba = (BoardFragment) f;
            if (!ba.getSolvedState()) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.are_you_sure))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            } else {
                swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
            }
        } else if (f instanceof SudokuSettingsFragment) {
            getSupportFragmentManager().popBackStack();
        } else if (f instanceof MainMenuFragment) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.sure_exit)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            moveTaskToBack(true);
                            System.exit(0);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
        else{
            super.onBackPressed();
        }
    }

    //calls this in oncreate in the main menu fragment
    public void bindMainMenuButtons(LinearLayout screenWait, LinearLayout levelSelect, LinearLayout modeSelect)
    {
        this.screenWait = screenWait;
        mLevelSelect = levelSelect;
        mModeSelect = modeSelect;
    }


    /********************************************************************************************
     * Multiplayer Stuff
     ********************************************************************************************/

    private String mIncomingInvitationId;
    Button mInvitePlayers;
    Button mQuickGame;
    Button mSeeInvites;
    private LinearLayout mLevelSelect;
    private LinearLayout mModeSelect;
    private LinearLayout screenWait;

    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;
    // My participant ID in the currently active game
    private String mMyId = null;
    private MultiGame.GameMode mGameMode;

    String mRoomId = null;

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;



    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        Log.d(TAG, "We got a message!");

        if(mMultiGame != null)
        {
            Log.d(TAG, "mMultigame is not null");
            mMultiGame.readMessage(buf);
        }
        else
        {
            Log.d(TAG, "Creating game object...");
            mMultiGame = MultiGame.byteToObj(buf, mParticipants,
                    mMyId, gameHelper, mRoomId);
            if(mMultiGame != null)
            {
                //Looks like post resume gets called before we get to this statement
                //switchToBoard = true;
                switchToBoardFrag();
            }
            else
            {
                Log.d(TAG, "mMultigame is still null");
            }
        }
    }


    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        Log.d(TAG, "we got and invite!");
        mIncomingInvitationId = invitation.getInvitationId();

        swapFragment(BaseActivity.INVITE_DIALOG_FRAGMENT_SHOW, new Bundle());

    }

    @Override
    public void onInvitationRemoved(String invitationId) {

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.invitation_removed))
                .show();
        if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            swapFragment(BaseActivity.INVITE_DIALOG_FRAGMENT_HIDE, new Bundle());
        }


    }


    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(gameHelper.getApiClient()));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mRoomId==null)
            mRoomId = room.getRoomId();

        mMultiGame = null;
        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        //switchToWaitScreen();
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        mMultiGame = null;
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.disconnectedFromRoom))
                .show();
        swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
        Log.d(TAG, "Disconnected from room");
    }

    //TODO showGameError
    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        /*
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
        */
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.match_canceled))
                .show();
        switchToMainMenuFrag();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);

    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }


    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        mMultiGame = null;
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onJoinedRoom, status " + statusCode);
            showGameError();
            return;
        }
        showWaitingRoom(room);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("INSTANCE", "Called onSaveInstanceState");
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }



    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        Log.d(TAG, "Peer has left the room!");
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);


    }

    private void startMultiGame()
    {
        Log.d(TAG, "Starting the game");

        for(int i = 0; i < mParticipants.size(); i++)
        {
            if(mParticipants.get(i).isConnectedToRoom())
                if(mParticipants.get(i).getParticipantId().equals(mMyId))

                {
                    Log.d(TAG, "You are the host");
                    Log.d(TAG, "Creating the game");
                    mMultiGame = new MultiGame(mGameMode,
                            MultiGame.ConnectType.HUMAN_GAME, 0, this,
                            mParticipants,
                            mMyId,
                            gameHelper,
                            mRoomId);

                    switchToBoard = true;
                    break;
                }
                else
                {
                    Log.d(TAG, "You are the guest");
                    break;
                }
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "In post resume");
        if (switchToBoard) {
            switchToBoard = false;
            switchToBoardFrag();
            acceptInviteToRoom(null);
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }


    void updateRoom(Room room) {

        if (room != null) {
            mParticipants = room.getParticipants();
        }
    }


    public MultiGame getMultiGame()
    {
        return mMultiGame;
    }

    public void startQuickGame(MultiGame.GameMode gameMode ) {
        mGameMode = gameMode;
        clearMenu();

        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MultiGame.MIN_OPPONENTS,
                MultiGame.MAX_OPPONENTS , 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        rtmConfigBuilder.setVariant(mGameMode.getValue());

        keepScreenOn();
        Games.RealTimeMultiplayer.create(gameHelper.getApiClient(), rtmConfigBuilder.build());
    }

    public void hostGame(MultiGame.GameMode gameMode) {
        mGameMode = gameMode;
        keepScreenOn();
        Intent i = Games.RealTimeMultiplayer.getSelectOpponentsIntent(gameHelper.getApiClient(),
                MultiGame.MIN_OPPONENTS, MultiGame.MAX_OPPONENTS);

        clearMenu();

        // show waiting room UI
        startActivityForResult(i, RC_SELECT_PLAYERS);
    }

    public void viewInvites()
    {
        Intent i = Games.Invitations.getInvitationInboxIntent(gameHelper.getApiClient());
        clearMenu();
        startActivityForResult(i, RC_INVITATION_INBOX);
    }


    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void switchToBoardFrag()
    {
        Log.d(TAG, "Switching to board fragment");
        Bundle args = new Bundle();
        args.putBoolean("MULTIPLAYER", true);
        swapFragment(BOARD_FRAGMENT, args);
    }

    void switchToMainMenuFrag()
    {
        Bundle args = new Bundle();
        swapFragment(MAIN_MENU_FRAGMENT, args);
    }

    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        mMultiGame = null;
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(this.getApiClient(), this, mRoomId);
            mRoomId = null;
            clearMenu();
        } else {
            switchToMainMenuFrag();
        }
    }


    /***
     * Shows the loading screen instead of the
     */
    void clearMenu() {
        mLevelSelect.setVisibility(GONE);
        mModeSelect.setVisibility(GONE);
        screenWait.setVisibility(VISIBLE);
    }


    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mGameMode = MultiGame.GameMode.valueToGameMode(room.getVariant());
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(gameHelper.getApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    /**
     * from https://github.com/playgameservices/android-basic-samples/blob/master/BasicSamples/ButtonClicker/src/main/java/com/google/example/games/bc/MainActivity.java
     * @param response
     * @param data
     */
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            mLevelSelect.setVisibility(VISIBLE);
            mModeSelect.setVisibility(VISIBLE);
            screenWait.setVisibility(GONE);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setVariant(mGameMode.getValue());
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        clearMenu();
        keepScreenOn();
        Games.RealTimeMultiplayer.create(gameHelper.getApiClient(), rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    /**
     * from https://github.com/playgameservices/android-basic-samples/blob/master/BasicSamples/ButtonClicker/src/main/java/com/google/example/games/bc/MainActivity.java
     * @param response
     * @param data
     */
    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.

    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            mLevelSelect.setVisibility(VISIBLE);
            mModeSelect.setVisibility(VISIBLE);
            screenWait.setVisibility(GONE);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    /**
     * from https://github.com/playgameservices/android-basic-samples/blob/master/BasicSamples/ButtonClicker/src/main/java/com/google/example/games/bc/MainActivity.java
     * @param invId
     */
    // Accept the given invitation.
    public void acceptInviteToRoom(String invId) {
        if(invId == null)
        {
            if(gameHelper.hasInvitation() && mIncomingInvitationId != null)
            {
                if(mIncomingInvitationId.equals(gameHelper.getInvitationId()))
                {
                    Log.d(TAG, "Invite already sent!");
                    return;
                }
            }

            if(gameHelper.getInvitationId() != null)
            {
                mIncomingInvitationId = gameHelper.getInvitationId();
                invId = mIncomingInvitationId;
            }
            else if (mIncomingInvitationId != null)
            {
                invId = mIncomingInvitationId;
            }
            else
            {

                Log.d(TAG, "No invite found!");
                return;
            }
        }
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        clearMenu();
        keepScreenOn();
        Games.RealTimeMultiplayer.join(gameHelper.getApiClient(), roomConfigBuilder.build());
    }

}
