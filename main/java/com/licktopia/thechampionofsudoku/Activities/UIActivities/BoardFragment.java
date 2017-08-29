package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.BackgroundImages;
import com.licktopia.thechampionofsudoku.CircleTransform;
import com.licktopia.thechampionofsudoku.DBOjects.SavedGameData;
import com.licktopia.thechampionofsudoku.Multiplayer.MultiGame;
import com.licktopia.thechampionofsudoku.PlaySounds;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.Settings.SudokuSettings;
import com.licktopia.thechampionofsudoku.SquareView;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;

import com.licktopia.thechampionofsudoku.Sudoku.Game;
import com.licktopia.thechampionofsudoku.DBOjects.SavedGame;
import com.licktopia.thechampionofsudoku.Sudoku.Square;
import com.licktopia.thechampionofsudoku.Sudoku.Strategies;
import com.licktopia.thechampionofsudoku.GameInterface;
import com.licktopia.thechampionofsudoku.Tags;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;

import main.java.com.maximeroussy.invitrode.RandomWord;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.getXp;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;


public class BoardFragment extends Fragment {

    private static final float UNDOREDOALPHA = .45f;
    private String mCurrentSaveName = "snapshotTemp";
    private PlaySounds mPlaySounds;
    private Chronometer mChronometer;
    private SudokuSettings settings;
    private Handler startAnimationHandler;
    private SquareView[] squareViews;
    private int selectedSquare = -1;
    private boolean colorGame = false;
    private boolean rowColHighlights = true;
    private boolean keyboardState = true;
    private boolean solvedState = false;
    private boolean animationFinished = false;
    private boolean solveClicked = false;
    private TextView scoreBoard;

    private boolean smallNumPadState = false;
    private Button[] numberButtons;
    private TextView[] lilNums;
    private Button clearButton;
    private Button solveButton;
    private Button checkProgressButton;
    private Button nakedSingleButton;
    private Button nakedDoubleButton;
    private Button hiddenSingleButton;
    private Button hiddenPairButton;
    private Button lockedCandidateButton;
    private Button xWingButton;
    private Button nakedTripleButton;
    private Button yWingButton;
    private Button[] hintButtonArray;
    private boolean hintPressed = false;
    private ImageButton mUndoButton;
    private ImageButton mRedoButton;
    private ImageButton notepadIcon;
    private Button hintButton;
    private int size;
    private long timeWhenStopped;
    private Handler highlightHandler;

    private LinearLayout mHintsLayout;
    private LinearLayout mEndGameLayout;
    private ImageView userBadge;
    private TextView userText;
    private String gamerName;
    private String gamerID;
    private int level;
    private boolean resume;
    private boolean fromResume;
    private boolean mAutoSave;
    private boolean multiplayer;
    private RelativeLayout botBar;
    private String exactTime;
    private boolean alreadySaved = false;
    private String savedName;
    private String savedDate;
    private String fileSaveName;
    private WindowManager wm;
    private Display d;
    private ImageView[] Opponents;
    private ImageView playerOpponent;
    private int numPlayers;
    private ShareDialog shareDialog;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private CognitoSyncManager syncClient;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;
    private GameInterface mGame;
    private View view;
    private BoardFragmentListener callback;
    private long sTime =0;
    private long mpStartTime;
    public Toolbar toolbar;
    private int[] userBoard = null;
    private long boardIdentifier;
    private byte[] myResumedGame;


    public static BoardFragment newInstance() {
        return new BoardFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        colorGame = bundle.getBoolean("COLOR_MODE");
        level = bundle.getInt("LEVEL");
        resume = bundle.getBoolean("RESUME");
        sTime = bundle.getLong("STOPPED_TIME");
        multiplayer = bundle.getBoolean("MULTIPLAYER");
        //multiplayer = true;
        exactTime = bundle.getString("EXACT_TIME");
        savedName = bundle.getString("SAVED_NAME");
        savedDate = bundle.getString("SAVED_DATE");
        userBoard =bundle.getIntArray("USER_BOARD");
        boardIdentifier = bundle.getLong("BOARD_IDENTIFIER");
        myResumedGame = bundle.getByteArray("SAVE_FILE");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_board_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_board, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_board_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_board_large, container, false);
        }



        //pass in mode and level
        wm = getActivity().getWindowManager();
        d = wm.getDefaultDisplay();



        //initialize action bar
        if(phoneSize==1) {
            toolbar = (Toolbar) view.findViewById(R.id.toolbar_small);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }
        else if(phoneSize == 2){
            toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }
        else if(phoneSize == 3){
            toolbar = (Toolbar) view.findViewById(R.id.toolbar_phablet);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }
        else{
            toolbar = (Toolbar) view.findViewById(R.id.toolbar_large);
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        }

        shareDialog = new ShareDialog(BoardFragment.this);

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(),
                "us-east-2:40802ef0-7537-4910-8442-31e2b30c5a7d", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );

        // Initialize the Cognito Sync client
        syncClient = new CognitoSyncManager(
                getContext(),
                Regions.US_EAST_2, // Region
                credentialsProvider);


        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_2));


        //bindings
        mChronometer = (Chronometer)view.findViewById(com.licktopia.thechampionofsudoku.R.id.chronometer);
        mUndoButton = (ImageButton)view.findViewById(R.id.backButton);
        mRedoButton = (ImageButton)view.findViewById(R.id.forwardButton);
        notepadIcon = (ImageButton) view.findViewById(R.id.scratch);
        scoreBoard = (TextView)view.findViewById(R.id.score);
        mHintsLayout = (LinearLayout) view.findViewById(R.id.hints_layout);
        mEndGameLayout = (LinearLayout)view.findViewById(R.id.end_game_layout);
        userBadge = (ImageView)view.findViewById(R.id.user_image);
        userText = (TextView)view.findViewById(R.id.user_text);
        botBar = (RelativeLayout)view.findViewById(R.id.botbar);
        highlightHandler = new Handler();
        Opponents = new ImageView[8];

        mPlaySounds = new PlaySounds();
        ////if we are coming back from back press
        boolean comeBackFlag = false;
        if(mGame!=null){
            comeBackFlag = true;
            sTime = timeWhenStopped;
        }
        //////then mGame isn't null, set comeback flag
        //Game instance
        //LOAD GAME STUFF IS HERE
        if(resume && mGame == null)
        {
           // byte[] temp = getIntent().getExtras().getByteArray("SAVE_FILE");

            try(ByteArrayInputStream b = new ByteArrayInputStream(myResumedGame)){


                ObjectInputStream o = new ObjectInputStream(b);
                mGame = (Game)o.readObject();
            }
            catch(IOException e)
            {
                Log.d("IOException", "Failed to load the game!");
                mGame = new Game(0, getContext());
            }
            catch(ClassNotFoundException e)
            {
                Log.d("ClassNotFoundException", "What class are you talking about?");
            }
        }
        else if(multiplayer)
        {
            scoreBoard.setVisibility(GONE);
            mGame = ((BaseActivity)getActivity()).getMultiGame();
            if(mGame == null)
            {
                mGame = new MultiGame(MultiGame.GameMode.RACE,
                        MultiGame.ConnectType.BOT_GAME,
                        level, getContext(), null, null, null, null);
            }
            initializePlayerTokens();
            if(!comeBackFlag){
                mpStartTime = SystemClock.elapsedRealtime();
            }
        }
        //START NEW GAME HERE
        else
        {
            if(mGame == null) {
                if(userBoard==null) {
                    mGame = new Game(level, getContext());
                    boardIdentifier = mGame.getBoardNumber();
                }
                else{
                    mGame = new Game(boardIdentifier, level, userBoard);
                }
            }
        }

        size = mGame.totalBoardRows();
        startAnimationHandler = new Handler();
        squareViews = new SquareView[size * size];//array of 81 squareViews
        numberButtons = new Button[size];//keypad 1-9
        lilNums = new TextView[size];



        updateUndoRedoButtons();
        initializeButtons();
        if(resume || comeBackFlag){
            initializeBoardFromResume(sTime);
            alreadySaved = true;
        }
        else {
            initializeBoard();//get starting board
        }
        if(comeBackFlag){
            customizeLayout(true);
        }
        else {
            customizeLayout(resume);
        }
        listeners(toolbar);//set all listeners for BoardFragment layout
        updateScore();
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                updateScore();
                upDatePositions();
            }
        });
        setHasOptionsMenu(true);

        return view;
    }

    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (BoardFragment.BoardFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement BoardFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void initializePlayerTokens(){
        if(multiplayer) {
            CircleTransform cf = new CircleTransform();
            numPlayers = ((MultiGame) mGame).getTotalOpponents();
            for (int i = 0; i < ((MultiGame) mGame).getAllImgUri().length; i++) {

                Opponents[i] = (ImageView) view.findViewById(Tags.PlayerTags[i]);
                try {
                    String uri = ((MultiGame) mGame).getAllImgUri()[i];
                    Picasso.with(getActivity()).load(uri).transform(cf).into(Opponents[i]);
                } catch (Exception e) {

                    Opponents[i].setImageDrawable(getResources().getDrawable(Tags.colorButtonTags[i],null));
                }

                if (i < numPlayers) {
                    Opponents[i].setVisibility(View.VISIBLE);
                    //Opponents[i].setX(0);
                } else {
                    Opponents[i].setVisibility(GONE);
                }
            }
            playerOpponent = (ImageView)view.findViewById(Tags.PlayerTags[7]);
            playerOpponent.setVisibility(View.VISIBLE);


            ViewGroup.LayoutParams paramsBar = botBar.getLayoutParams();

            //int view_height = d.getHeight();

            ViewGroup.LayoutParams params2=Opponents[0].getLayoutParams();

            int playerTokenHeight = params2.height;

            paramsBar.height = (numPlayers+1)*playerTokenHeight;

            botBar.setLayoutParams(paramsBar);


            ViewGroup.LayoutParams params=botBar.getLayoutParams();

            int botBarHeight = params.height;





            // WAS: float offset = (botBarHeight-playerTokenHeight)/(numPlayers - 1)
            // if numPlayers = 1 we'll divide by zero
            float offset = (botBarHeight-playerTokenHeight)/(numPlayers);
            for(int i = 0; i < numPlayers; i++){
                Opponents[i].setY(i*offset);
            }
            playerOpponent.setY(numPlayers*offset);
        }
    }

    private void upDatePositions(){
        if(multiplayer) {
            int thisViewWidth = d.getWidth();
            ObjectAnimator animX;
            ObjectAnimator fadeOut;
            ObjectAnimator fadeIn;
            AnimatorSet animatorSet;
            for (int i = 0; i < numPlayers; i++) {
                animatorSet = new AnimatorSet();
                ViewGroup.LayoutParams params = Opponents[i].getLayoutParams();
                float oldPos = Opponents[i].getX();
                float newPos = ((MultiGame) mGame).getOpponentProgress(i) * (thisViewWidth - params.width);
                if (newPos != oldPos) {
                    animX = ObjectAnimator.ofFloat(Opponents[i], "x", newPos);
                    animX.setDuration(800);
                    fadeOut = ObjectAnimator.ofFloat(Opponents[i], "alpha", .5f);
                    fadeOut.setDuration(100);
                    fadeIn = ObjectAnimator.ofFloat(Opponents[i], "alpha", 1);
                    fadeIn.setDuration(100);
                    animatorSet.playSequentially(fadeOut, animX, fadeIn);
                    animatorSet.start();
                }
                //animX.start();
                //Opponents[i].setX(newPos);
            }
            animatorSet = new AnimatorSet();
            ViewGroup.LayoutParams params = playerOpponent.getLayoutParams();
            float oldPos = playerOpponent.getX();
            float newPos = ((MultiGame) mGame).getMyProgress() * (thisViewWidth - params.width);
            if (newPos != oldPos) {
                animX = ObjectAnimator.ofFloat(playerOpponent, "x", newPos);
                animX.setDuration(800);
                fadeOut = ObjectAnimator.ofFloat(playerOpponent, "alpha", .5f);
                fadeOut.setDuration(100);
                fadeIn = ObjectAnimator.ofFloat(playerOpponent, "alpha", 1);
                fadeIn.setDuration(100);
                animatorSet.playSequentially(fadeOut, animX, fadeIn);
                animatorSet.start();
            }

        }
    }

    private boolean isInternet(){
        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    private void uploadSavedGame(boolean autosave){
        // fileSaveName wican be null if autosave checkbox is checked mid way through the game
        if(fileSaveName == null && savedName == null)
        {
            alreadySaved = false;
        }


        if(isInternet() && gameHelper.getApiClient().isConnected() && !multiplayer) {
            if(!alreadySaved){
                try {
                    ThreadLocalRandom _Rand = ThreadLocalRandom.current();
                    int randomNum= _Rand.nextInt(4, 11);
                    fileSaveName = RandomWord.getNewWord(randomNum);
                    savedName = fileSaveName;
                } catch(Exception e) {
                    //handle exception
                    //the method getNewWord can throw a WordLengthException when the length chosen is outside of the current limits (between 3 and 15 characters)
                }
            }
            if(resume){
                fileSaveName = savedName;
            }
            long stoppedTime = mChronometer.getBase() - SystemClock.elapsedRealtime();
            Date dt = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d hh:mm aaa");
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aaa");
            String today = dateFormat.format(dt);
            String now = sdf.format(dt);
            final SavedGame obj = new SavedGame();
            final SavedGameData data = new SavedGameData();
            obj.setGamerID(BaseActivity.getId());
            data.setGamerID(BaseActivity.getId());

            if(!alreadySaved) {
                Long exactoTime = System.currentTimeMillis();
                exactTime = String.format("%,d", exactoTime);
                obj.setExactTime(String.format("%,d", exactoTime));
                data.setExactTime(String.format("%,d", exactoTime));
                obj.setDate(today);
                savedDate = today;
                obj.setTime(fileSaveName);
            }
            else{
                obj.setExactTime(exactTime);
                data.setExactTime(exactTime);
                obj.setDate(savedDate);
                obj.setTime(savedName);
            }
            obj.setColorMode(colorGame);
            obj.setPuzzleNumber(mGame.getBoardNumber());
            obj.setLevel(level);
            obj.setStoppedTime(stoppedTime);
            //game object to byte array
            try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
                ObjectOutputStream o = new ObjectOutputStream(b);
                o.writeObject(mGame);
                data.setSaveFile(b.toByteArray());
            }
            catch(IOException e)
            {
                Log.d("IOException", "Failed to save the game!");
            }

            // id field is null at this point
            Runnable runnable = new Runnable() {
                public void run() {
                    //DynamoDB calls go here
                    try {
                        mapper.save(obj);
                        mapper.save(data);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //execute code on main thread
                                Toast.makeText(getActivity(), new StringBuilder().append(getString(R.string.gamesavedwithalias)).append(fileSaveName).toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    catch(final Exception e){
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //execute code on main thread
                                Toast.makeText(getActivity(),  R.string.game_not_saved,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };

            Thread mythread = new Thread(runnable);
            mythread.start();
            alreadySaved = true;




        }
        else{
            if(!multiplayer) {
                Toast.makeText(getActivity(), R.string.not_upload, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteSavedGame(){
        if(isInternet() && resume) {
            final SavedGame sg = new SavedGame();
            sg.setGamerID(BaseActivity.getId());
            sg.setExactTime(exactTime);



            // id field is null at this point
            Runnable runnable = new Runnable() {
                public void run() {
                    //DynamoDB calls go here
                    mapper.delete(sg);

                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
    }

    private void uploadCompletedGame(int xp){
        if(isInternet()&&gameHelper.getApiClient().isConnected()) {
            Date dt = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            String today = dateFormat.format(dt);
            final CompletedGame obj = new CompletedGame();
            obj.setGamerID(BaseActivity.getId());
            obj.setRealScore((int) mGame.getScore());
            obj.setXp(xp);
            obj.setTime(String.format("%,d",(int) mGame.getRunningTime()));
            obj.setDate(today);
            obj.setColor(colorGame);
            obj.setPuzzleNumber(mGame.getBoardNumber());
            obj.setLevel(level);
            obj.setAccuracy(mGame.getAccuracy());
            obj.setExactTime(String.format("%,d", System.currentTimeMillis()));
            obj.setGamerName(BaseActivity.getUsername());





            // id field is null at this point
            Runnable runnable = new Runnable() {
                public void run() {
                    //DynamoDB calls go here
                    mapper.save(obj);
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
        else{
            Toast.makeText(getActivity(), R.string.not_upload,Toast.LENGTH_SHORT).show();
        }
    }

    public void updateUI(boolean isLoggedIn){
        if(isLoggedIn){
            loadPlayer();
        }
        else{
            userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
            userText.setText(R.string.me);
            ImageView homeMenu = (ImageView) toolbar.findViewById(R.id.home_menu_button);
            homeMenu.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            homeMenu.setPadding(0,0,0,0);
        }
    }

    private void loadPlayer(){
        gameHelper.getApiClient().connect();
        if(gameHelper.getApiClient().isConnected()) {
            gamerName = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getDisplayName();
            gamerID = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getPlayerId();
            userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
            userText.setText(BaseActivity.getUsername());
            View homeMenu = toolbar.findViewById(R.id.home_menu_button);
            CircleTransform cf = new CircleTransform();
            try {
                String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                Picasso.with(getActivity()).load(me).transform(cf).into((ImageView) homeMenu);
            }catch (Exception e){

            }
        }
    }

    /************************************
     * Handles the end of the game
     *********************************/
    private void endOfGame(boolean wasSolvedButtonClicked){
        gameHelper.getApiClient().connect();
        solvedState = true;
        mGame.pauseTimer();
        mChronometer.stop();
        // If notepad selected, unselect
        if (smallNumPadState)
            notepadIcon.performClick();
        //make board unclickable
        allClickable(false);
        userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
        userText.setText(BaseActivity.getUsername());
        if(wasSolvedButtonClicked){
            solveClicked = true;
            solvePuzzle();
            scoreBoard.setText("0");
            mHintsLayout.setVisibility(GONE);
            mEndGameLayout.setVisibility(View.VISIBLE);

        }
        else if(multiplayer)
        {
            if(gameHelper.getApiClient().isConnected()) {
                try {
                    Games.Leaderboards.submitScore(gameHelper.getApiClient(), getString(R.string.leaderboard_opponents_vanquished_multiplayer), numPlayers, "1337");
                }catch(Exception e){
                    Log.d(TAG, "endOfGame: Failed to upload to db");
                }
                try {
                    Games.Leaderboards.submitScore(gameHelper.getApiClient(), getString(R.string.leaderboard_rank_multiplayer), ((MultiGame)mGame).updateMyRank(mGame.isGameFinished()), "1337");
                }catch(Exception e){
                    Log.d(TAG, "endOfGame: Failed to upload to db");
                }
            }
            mHintsLayout.setVisibility(GONE);
            mEndGameLayout.setVisibility(View.VISIBLE);
            final RelativeLayout mStatsBox = (RelativeLayout) view.findViewById(R.id.game_stats_box);
            setStatsBox(0);
            mStatsBox.setVisibility(View.VISIBLE);
            closeKeyboard();
        }
        else{
            final RelativeLayout mStatsBox = (RelativeLayout) view.findViewById(R.id.game_stats_box);
            final LinearLayout mLevelUpBox = (LinearLayout) view.findViewById(R.id.level_up_view);
            final Button mCloseLevelUp = (Button) view.findViewById(R.id.close_levelup);
            Button mCloseStats = (Button) view.findViewById(R.id.close_stats);
            userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
            userText.setText(BaseActivity.getUsername());
            int sound;
            updateScore();
            long gameXP = (long) (mGame.getScore()* callback.getMultiplier());
            boolean levelUp = BaseActivity.addExperience(gameXP);
            if(!solveClicked) {
                uploadCompletedGame((int) gameXP);
                deleteSavedGame();
                sendToLeaderBoard(gameXP);
                handleAchievements(gameXP);
                setStatsBox(gameXP);
                mStatsBox.setVisibility(View.VISIBLE);
                closeKeyboard();
            }

            mCloseStats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mStatsBox.setVisibility(GONE);
                }
            });

            if(!levelUp){//Not level up
                sound = 3;
            }
            else {//Level Up
                sound = 4;
                ImageView levelUpBadge = (ImageView)view.findViewById(R.id.level_up_badge);
                ImageButton shareLevelUp = (ImageButton) view.findViewById(R.id.share_levelup);
                TextView newMultiplier = (TextView) view.findViewById(R.id.new_multiplier);
                levelUpBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
                userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
                userText.setText(BaseActivity.getUsername());
                shareLevelUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        facebookShare();
                    }
                });
                newMultiplier.setText("Multiplier " + String.format("%,2.1f",callback.getMultiplier())+"x");
                mLevelUpBox.setVisibility(View.VISIBLE);
                mCloseLevelUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLevelUpBox.setVisibility(GONE);
                    }
                });

            }
            if(!solveClicked) {
                mPlaySounds.playSounds(getContext(), sound);
            }

        }

    }


    /********************************
    * Updates Little numbers on keyboard that track number of values on board
     * ***************************************/
    private void updateLilNums(){

        int[] array = mGame.getValueTotals();
        for(int i=0; i< array.length;i++){
             lilNums[i]=(TextView)view.findViewById(Tags.lilNumTags[i]);
             lilNums[i].setText(String.valueOf(array[i]));
        }

    }

    /************************************
     * Send to leaderboard at end of game
     *********************************/
    private void sendToLeaderBoard(long gameXP){
        gameHelper.getApiClient().connect();
        if(gameHelper.getApiClient().isConnected()) {
            if(mGame.getAccuracy()>=.8) {
                Games.Leaderboards.submitScore(gameHelper.getApiClient(),getString(R.string.leaderboard_highest_single_game_score), (int)gameXP,"1337");
                Games.Leaderboards.submitScore(gameHelper.getApiClient(), getString(Tags.leaderboards[level]), (int)mGame.getRunningTime() * 1000,"1337");
            }
            Games.Leaderboards.submitScore(gameHelper.getApiClient(), getString(R.string.leaderboard_most_total_xp), (int)BaseActivity.getXp(),"1337");
            Toast.makeText(getActivity(), R.string.scoretime_submitted, Toast.LENGTH_SHORT).show();
        }


    }

    /************************************
     * Unlock achievements at end of game
     *********************************/
    private void handleAchievements(long gameXP){
        gameHelper.getApiClient().connect();
        if(gameHelper.getApiClient().isConnected()){
            unlockAchievements(R.string.achievement_up_and_comer);
            if(level>=2&&mGame.getAccuracy()>=1){
                unlockAchievements(R.string.achievement_sharp_shooter);
            }
            if(level>=2&&mGame.getRunningTime()<12*60){
                unlockAchievements(R.string.achievement_speed_demon);
            }
            if(gameXP>=10000){
                unlockAchievements(R.string.achievement_big_time_scorer);
            }
            if(BaseActivity.getXp()>=100000){
                unlockAchievements(R.string.achievement_up_and_comer_ii);
            }
            if(level>=3&&mGame.getAccuracy()>=1&&mGame.getRunningTime()<15*60){
                unlockAchievements(R.string.achievement_sharp_shooter_ii);
            }
            if(level>=3&&mGame.getRunningTime()<10*60){
                unlockAchievements(R.string.achievement_speed_demon_ii);
            }
            if(gameXP>=15000){
                unlockAchievements(R.string.achievement_big_time_scorer_ii);
            }
            if(BaseActivity.getXp()>=1000000){
                unlockAchievements(R.string.achievement_up_and_comer_iii);
            }
            if(level>=4&&mGame.getAccuracy()>=1&&mGame.getRunningTime()<10*60){
                unlockAchievements(R.string.achievement_sharp_shooter_iii);
            }
            if(level>=5&&mGame.getRunningTime()<8*60){
                unlockAchievements(R.string.achievement_speed_demon_iii);
            }
            if(gameXP>=20000){
                unlockAchievements(R.string.achievement_big_time_scorer_iii);
            }
        }
    }

    /************************************
     * Set stats box at end of game
     *********************************/
    private void setStatsBox(long gameXP){
        TextView mTitleStats = (TextView)view.findViewById(R.id.title_stats);
        ImageView userIcon = (ImageView) view.findViewById(R.id.user_icon);
        TextView mTotalXpStats = (TextView)view.findViewById(R.id.totalxp_stats);
        TextView mRpStats = (TextView)view.findViewById(R.id.regular_points);
        TextView mLevelBonusStats = (TextView)view.findViewById(R.id.levelbonus_stats);
        TextView mTimeBonusStats = (TextView)view.findViewById(R.id.timebonus_stats);
        TextView mGoodSquaresStats = (TextView)view.findViewById(R.id.goodsquares_stats);
        TextView mAccuracyStats = (TextView)view.findViewById(R.id.accurracy_stats);
        TextView mPenalty = (TextView)view.findViewById(R.id.penalty_stats);
        if(multiplayer)
        {
            if(mGame.isGameFinished())
            {
                mTitleStats.setText(R.string.multiplayer_win);
            }
            else
            {
                mTitleStats.setText(R.string.multiplayer_loss);
            }
            if(gameHelper.isSignedIn()) {
                    CircleTransform cf = new CircleTransform();
                    String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                    try {
                        Picasso.with(getActivity()).load(me).transform(cf).into(userIcon);
                    }catch(Exception e){

                    }
            }
            else{
                userIcon.setVisibility(GONE);
            }
            mTotalXpStats.getLayoutParams().height = ActionBar.LayoutParams.WRAP_CONTENT;
            // my rank text
            mTotalXpStats.setText("Your Old Rank: " +
                    String.format("%,d",((MultiGame)mGame).getMyOldRank()) +
                    "\nYour New Rank: " + String.format("%,d",BaseActivity.multiplayerRank));


            String oppRanks = "";

            for(int i = 0; i < ((MultiGame)mGame).getTotalOpponents(); i++)
            {
                oppRanks += ((MultiGame)mGame).getOpponentName(i) + "'s Rank: " +
                        String.format("%,d",((MultiGame)mGame).getOpponentRank(i)) + "\n";
            }

            mRpStats.getLayoutParams().height = ActionBar.LayoutParams.WRAP_CONTENT;

            mRpStats.setText(oppRanks);
            mLevelBonusStats.setText("");
            mLevelBonusStats.setVisibility(View.INVISIBLE);
            mTimeBonusStats.setText("");
            mTimeBonusStats.setVisibility(View.INVISIBLE);
            mGoodSquaresStats.setText("");
            mGoodSquaresStats.setVisibility(View.INVISIBLE);
            mAccuracyStats.setText("");
            mAccuracyStats.setVisibility(View.INVISIBLE);
            mPenalty.setText("");
            mPenalty.setVisibility(View.INVISIBLE);
        }
        else
        {
            mTitleStats.setText(BaseActivity.getUsername());
            if(gameHelper.isSignedIn()) {
                CircleTransform cf = new CircleTransform();
                String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                try {
                    Picasso.with(getActivity()).load(me).transform(cf).into(userIcon);
                }catch(Exception e){

                }
            }
            else{
                userIcon.setVisibility(GONE);
            }
            mTotalXpStats.setText(String.format("%,d",(int) gameXP)+ " XP");
            mRpStats.setText("Multiplier: " + String.format("%,d",(int) mGame.getScore())+ " x " + String.format("%2.1f", callback.getMultiplier()));
            mLevelBonusStats.setText("Level Bonus: " + String.format("%,d",(int) mGame.getLevelBonus()));
            if((mGame.getEarlyFinishTime() - mGame.getRunningTime()) >= 0)
            {
                mTimeBonusStats.setText("Time Bonus: " + String.format("%,d",(int) (mGame.getEarlyFinishTime() - mGame.getRunningTime())) + " x " + String.format("%,d",(int) mGame.getEarlyFinishBonus()) );
            }
            else
            {
                mTimeBonusStats.setText("Time Bonus: 0 x " + String.format("%,d",(int) mGame.getEarlyFinishBonus()) );
            }

            mGoodSquaresStats.setText("Square Points: " + String.format("%,d",(int) mGame.getTotalOneTimeGuesses())+ " x " + String.format("%,d",(int) mGame.getOneTimeGuessSquareBonus()));
            mAccuracyStats.setText("Accuracy: " + String.format("%,d",(int)Math.round(mGame.getAccuracy()*100),2)+"%");
            mPenalty.setText("Penalty: " + String.format("%,d",(int) mGame.totalPenaltyTimeIntervals()) + " x " + String.format("%,d",(int) mGame.getTimePenalty()));

        }





    }


    /************************************
     * Initialize undo redo buttons and sets
     *********************************/

    private void updateScore(){
        if(!solveClicked) {
            scoreBoard.setText("" + mGame.getScore());
        }
        if(multiplayer)
        {
            for(int i = 0; i < ((MultiGame)mGame).getTotalOpponents(); i++)
            {
                if(((MultiGame)mGame).isOpponentGameFinished(i))
                {
                    endOfGame(false);
                }
            }
        }
    }
    private void undoRedoListener() {

        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGame.canUndo()) {
                    int square =  mGame.undo();
                    int value = mGame.getBoardValueAt(square);
                    if(value!=0) {
                        if (!colorGame) {
                            squareViews[square].setBigNumber(0, false);
                        } else {
                            squareViews[square].setSquareColor(0, false);
                        }
                    }
                    else{
                        if(!colorGame) {
                            squareViews[square].setBigNumber(value, false);
                        }
                        else{
                            squareViews[square].setSquareColor(value, false);
                        }
                    }
                }
                updateUndoRedoButtons();

            }
        });
        mRedoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGame.canRedo()) {
                    int square = mGame.redo();
                    int value = mGame.getBoardValueAt(square);
                    if (!colorGame) {
                        squareViews[square].setBigNumber(value, false);
                    }
                    else{
                        squareViews[square].setSquareColor(value, false);
                    }

                }
                updateUndoRedoButtons();
            }
        });
    }

    /************************************
     * Dims or brightens undo redo buttons
     * if undo or redo is not possible or possible
     *********************************/
    private void updateUndoRedoButtons() {
        if(mGame.canUndo()){
            mUndoButton.setAlpha(1f);
        }
        else{
            mUndoButton.setAlpha(UNDOREDOALPHA);
        }
        if(mGame.canRedo()){
            mRedoButton.setAlpha(1f);
        }
        else{
            mRedoButton.setAlpha(UNDOREDOALPHA);
        }
    }

    /************************************
     * Starts Chronometer for the first time
     *********************************/
    private void startTimer() {
        mGame.unpauseTimer();
        mGame.getRunningTime();
        mChronometer.start();
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    public void onResume() {
        settings = new SudokuSettings(getContext());
        rowColHighlights= settings.getHighlightPreference();
        mPlaySounds.setMute(settings.getSoundPreference());
        mAutoSave = settings.getAutoSavePreference();

        for (int i=0;i<lilNums.length;i++) {
            lilNums[i]=(TextView)view.findViewById(Tags.lilNumTags[i]);
            if (settings.getLilNumPreference()) {
                lilNums[i].setVisibility(View.VISIBLE);
            } else {
                lilNums[i].setVisibility(View.INVISIBLE);
            }
        }

        if(animationFinished && !multiplayer) {
            if(!fromResume) {
                mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            }
            if(!solvedState) {
                mChronometer.start();
                mGame.unpauseTimer();
            }
            mGame.getRunningTime();
        }
        fromResume = false;
        upDatePositions();
        super.onResume();
    }

    @Override
    public void onPause() {
        removeAllHighlights();
        selectedSquare = -1;
        settings = new SudokuSettings(getContext());
        rowColHighlights= settings.getHighlightPreference();
        mPlaySounds.setMute(settings.getSoundPreference());
        if(!multiplayer) {
            mGame.pauseTimer();
            timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        }
        super.onPause();
    }

    /************************************
     * Initialize all keypad buttons
     *********************************/
    private void initializeButtons() {
        for (int i = 0; i < Tags.numberButtonTags.length; i++) {//getButtons
            numberButtons[i] = (Button) view.findViewById(Tags.numberButtonTags[i]);
        }
        clearButton = (Button) view.findViewById(R.id.clear);
        solveButton = (Button) view.findViewById(R.id.solveButton);
        checkProgressButton = (Button) view.findViewById(R.id.progressButton);
        nakedSingleButton = (Button) view.findViewById(R.id.naked_single);
        nakedDoubleButton = (Button) view.findViewById(R.id.naked_double);
        hiddenSingleButton = (Button) view.findViewById(R.id.hidden_single);
        hiddenPairButton = (Button) view.findViewById(R.id.hidden_pair);
        lockedCandidateButton = (Button) view.findViewById(R.id.locked_candidate);
        xWingButton = (Button) view.findViewById(R.id.x_wing);
        nakedTripleButton = (Button) view.findViewById(R.id.naked_triple);
        yWingButton = (Button) view.findViewById(R.id.y_wing);
        hintButton = (Button) view.findViewById(R.id.hints);
        hintButtonArray = new Button[] {nakedSingleButton, nakedDoubleButton, hiddenSingleButton, hiddenPairButton, nakedTripleButton,
                lockedCandidateButton, xWingButton, yWingButton};
    }

    /************************************
     * Function binds 81 squareViews and sets
     * number/color to starting value with animation
     * Then sets starts the clock
     *********************************/
    private void initializeBoard() {
        fromResume = false;
        for (int i = 0; i < squareViews.length; i++) {//set up starting board
            RelativeLayout rl = (RelativeLayout) view.findViewById(Tags.squareTags[i]); // get Relative layout
            squareViews[i] = new SquareView(getContext(), rl,colorGame);
            final int value = mGame.getBoardValueAt(i);

            int timer;
            if(value == 0){
                timer = 0;
            }else {
                timer = value * 250 + 200;
            }

            final int finalI = i;
            startAnimationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    if(colorGame){
                        squareViews[finalI].setSquareColorAnimated(value);
                    }
                    else {
                        squareViews[finalI].setBigNumberAnimated(value);
                    }
                }
            }, timer);

        }
        Handler startClockHandler = new Handler();
        startClockHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                startTimer();
                animationFinished = true;
                ///Make board clickable
                allClickable(true);
                updateLilNums();


            }
        },2950);
    }
    private void initializeBoardFromResume(Long stoppedTime) {
        fromResume = true;
        if(gameHelper.isSignedIn()){
            userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
            userText.setText(BaseActivity.getUsername());
        }
        else{
            userBadge.setImageDrawable(getResources().getDrawable(callback.getBadgetag(), null));
            userText.setText("Me");
        }
        for (int i = 0; i < squareViews.length; i++){
            RelativeLayout rl = (RelativeLayout) view.findViewById(Tags.squareTags[i]); // get Relative layout
            squareViews[i] = new SquareView(getContext(), rl,colorGame);
            for (int j = 0; j < 9; j++){
                squareViews[i].showNotepadNumber(j+1,mGame.getBoardNotepadAt(i,j+1));

            }
        }
        for (int i = 0; i < squareViews.length; i++) {//set up starting board

            int value = mGame.getBoardValueAt(i);
            if(colorGame){
                squareViews[i].setSquareColor(value, mGame.isStartingValue(i));
            }
            else {
                squareViews[i].setBigNumber(value, mGame.isStartingValue(i));
            }
        }

       if(!multiplayer) {
           mGame.unpauseTimer();
           mChronometer.setBase(SystemClock.elapsedRealtime() + stoppedTime);
           if (!solvedState) {
               mChronometer.start();
           }
       }
       else{
           mChronometer.setBase(SystemClock.elapsedRealtime()- (SystemClock.elapsedRealtime()-mpStartTime));
           mChronometer.start();
       }
        if(solvedState){//if back buttoned into after game is completed
            allClickable(false);
            LinearLayout mBottomLayout = (LinearLayout)view.findViewById(R.id.bottomLayout);
            RelativeLayout mKeypad = (RelativeLayout) view.findViewById(R.id.keypad);
            mBottomLayout.setScaleY(1);
            mBottomLayout.setVisibility(View.VISIBLE);
            mKeypad.setVisibility(GONE);
            mHintsLayout.setVisibility(GONE);
            mEndGameLayout.setVisibility(View.VISIBLE);

        }

        updateScore();
        animationFinished = true;

        ///Make board clickable
        allClickable(true);
        updateLilNums();
    }



    /************************************
     * Calls appropriate listener functions
     *********************************/
    public void listeners(Toolbar toolbar) {
        squareListeners();
        hintButtonListener();
        toolbarButtonsListener(toolbar);
        numberListeners();
        smallNumPadListener();
        clearButtonListener();
        checkProgressButtonListener();
        for (int i = 0; i < hintButtonArray.length; i++) {
            setHintButtonListener(hintButtonArray[i], i + 1);
        }
        solveButtonListener();
        undoRedoListener();
        continueListener();
        userBadgeListener();
        leaderBoardsListener();
        facebookListener();
        //onScrollHintsListener();
    }

    private void continueListener(){
        LinearLayout continueButton =(LinearLayout)view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainMenu();
            }
        });
    }
    private void userBadgeListener(){
        LinearLayout userBadgeButton =(LinearLayout)view.findViewById(R.id.user_button);
        userBadgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameHelper.getApiClient().connect();
                if(gameHelper.getApiClient().isConnected()) {
                    Bundle args = new Bundle();
                    args.putString("GAMERNAME", gamerName);
                    args.putString("GAMERID", gamerID);
                    callback.swapFragment(BaseActivity.GAMER_PROFILE_FRAGMENT, args);
                }
                else{
                    Toast.makeText(getActivity(),getString(R.string.login_toview_profile),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void leaderBoardsListener(){
        gameHelper.getApiClient().connect();
        LinearLayout leaderBoardButton =(LinearLayout)view.findViewById(R.id.leader_button);
        leaderBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameHelper.getApiClient() != null && gameHelper.getApiClient().isConnected()) {
                    // Call a Play Games services API method, for example:
                    startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(gameHelper.getApiClient()), 5);;
                } else {
                    // Alternative implementation (or warn user that they must
                    // sign in to use this feature)
                    Toast.makeText(getActivity(), R.string.login_tosee_leaderboards , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void facebookListener(){
        LinearLayout facebookButton =(LinearLayout)view.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                facebookShare();
            }
        });
    }

    private void facebookShare() {
        gameHelper.getApiClient().connect();
        String imageTag;
        if(gameHelper.getApiClient().isConnected()) {
            if(!colorGame){
                imageTag = Tags.postScoreImagesNumber[BaseActivity.getLevel()];
            }
            else{
               imageTag = Tags.postScoreImagesColor[BaseActivity.getLevel()];
            }

        }
        else{
            if(!colorGame){
                imageTag = Tags.postScoreNum;
            }
            else{
                imageTag = Tags.postScoreCol;
            }
        }
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(imageTag))
                .build();
        shareDialog.show(content);
    }

    /************************************
     * Listener for toolbar icon
     *********************************/
    private void toolbarButtonsListener(Toolbar toolbar) {
        View homeMenu = toolbar.findViewById(R.id.home_menu_button);
        if(gameHelper.isSignedIn()) {
            try{
                CircleTransform cf = new CircleTransform();
                String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                Picasso.with(getActivity()).load(me).transform(cf).into((ImageView) homeMenu);
                homeMenu.setPadding(7,7,7,7);

            }
            catch (Exception e){
                Log.d(TAG, "toolbarButtonsListener: Couldn't get image");
            }
        }
        homeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeClick();

            }
        });

    }

    /************************************
     * Listeners for 81 squareViews
     *********************************/
    private void squareListeners() {
        for (int i = 0; i < (size * size); i++) {
            final int finalI = i;
            final int finalI1 = i;
            RelativeLayout rl = (RelativeLayout) view.findViewById(Tags.squareTags[i]);
            squareViews[i].getSquareHolder().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    squareCommands(finalI);
                }
            });
        }
    }

    /************************************
     * Listeners for keypad 1-9
     *********************************/
    private void numberListeners() {
        for (int i = 0; i < size; i++) {
            final int finalI = i + 1;
            numberButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numberCommands(finalI);
                }
            });
        }
    }

    /************************************
     * Listeners numberPadButtonBackground
     *********************************/
    private void clearButtonListener() {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllHighlights();
                numberCommands(0);
            }
        });
    }

    /************************************
     * Listener numberPadButtonBackground
     *********************************/
    private void smallNumPadListener() {
        notepadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSquare > -1) {
                    RelativeLayout notepadBackground = (RelativeLayout) view.findViewById(R.id.scratch_bg);
                    if (smallNumPadState) {
                        smallNumPadState = false;
                        notepadBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorKeypad));

                    } else {
                        smallNumPadState = true;
                        if (!colorGame) {
                            notepadBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.notepadNumberHighlight));
                        } else {
                            notepadBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.notepadColorHighlight));
                        }
                    }
                }
            }
        });

    }

    /************************************
     * Listener keboardDownButton
     *********************************/
    private void hintButtonListener() {
        if(!multiplayer) {
            hintButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!multiplayer) {
                        selectedSquare = -1;
                        closeKeyboard();
                        highlightHandler.removeCallbacksAndMessages(null);
                        mGame.updateSolver();
                        removeAllHighlights();
                    }
                }
            });
        }

    }

    /************************************
     * Listeners solveButton
     *********************************/
    private void solveButtonListener() {
        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endOfGame(true);
            }
        });
    }

    /************************************
     * Listeners checkProgressButton
     *********************************/
    private void checkProgressButtonListener() {
        checkProgressButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Set<Integer> conflicts = mGame.checkCurrentProgress();  // Check for errors
                    if (conflicts.isEmpty()) {
                        if(!solveClicked&&!mGame.isGameFinished()) {
                            mPlaySounds.playSounds(getContext(), 1);
                            Toast.makeText(getContext(), "Board is currently correct!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        if(!solveClicked) {
                            mPlaySounds.playSounds(getContext(), 2);
                        }
                        for (int conflict : conflicts) {
                            squareViews[conflict].setErrorHighlight(colorGame);     // Display error highlight

                        }
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    removeAllHighlights();
                    if (selectedSquare != -1) {
                        squareViews[selectedSquare].setSelectedHighlight();
                        //highlightConnected();
                    }
                }
                return true;
            }
        });
    }

    private void setHintButtonListener(Button button, final int strategyNum) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Set<Integer> conflicts = mGame.checkCurrentProgress();  // Check for errors
                    if (conflicts.isEmpty()) {
                        v.setBackground(getResources().getDrawable(R.drawable.hint_button_pressed));
                        showHint(strategyNum);
                    }
                    else {
                        if(!solveClicked) {
                            mPlaySounds.playSounds(getContext(), 2);
                        }
                        for (int conflict : conflicts) {
                            squareViews[conflict].setErrorHighlight(colorGame);     // Display error highlight
                        }
                        Toast.makeText(getContext(), "Hints require a correct board. Check these errors first.", Toast.LENGTH_LONG).show();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackground(getResources().getDrawable(R.drawable.hint_rounded_button));
                    mGame.restoreAllNotepads();
                    updateBoard();
                    removeAllHighlights();
                }
                return true;
            }
        });
    }

    /************************************
     * Listeners onScrollHints
     *********************************/
   /* private void onScrollHintsListener() {
        mHintsLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

            }
        });
    }
*/
    /************************************
     * Update board notepad display
     *********************************/
    private void updateBoard() {
        for (int idx = 0; idx < mGame.totalBoardElements(); idx++) {
            if (mGame.isStartingValue(idx) || mGame.getBoardValueAt(idx) != 0) continue;
            for (int num = 1; num <= mGame.totalBoardRows(); num++) {
                squareViews[idx].showNotepadNumber(num, mGame.getBoardNotepadAt(idx, num));
            }
        }
    }

    /************************************
     * Show one instance of a given hint
     ***********************************/
    private void showHint(int hintType) {
        Set<Integer> conflicts = mGame.checkCurrentProgress();  // Set board value to number
        if (!conflicts.isEmpty()) {

        }
        mGame.backupAllNotepads();
        mGame.updateAllNotepads(false);
        updateBoard();
        Strategies.Hint hint = mGame.getHint(hintType);
        if (hint != null) {
            hintDisplay(hint);
        }
        else {
            mPlaySounds.playSounds(getContext(), 2);
        }
    }

    /************************************
     * Handles highlighting for hint display
     *********************************/
    private void hintDisplay(Strategies.Hint hint) {
        removeAllHighlights();
        for (int i : hint.hintInfo[Strategies.CONNECTED]) {
            squareViews[i].setHighlighted(true, !colorGame);
        }
        for (int i : hint.hintInfo[Strategies.SELECTED]) {
            squareViews[i].setDarkHighlight();
        }
        if (hint.getRemoveFrom() == Strategies.SELECTED) {
            for (int i : hint.hintInfo[Strategies.SELECTED]) {
                boolean[] colors = new boolean[9];
                for (int num : hint.hintInfo[Strategies.NOTEPAD])
                    colors[num - 1] = true;
                for (int j = 1; j <= colors.length; j++) {
                    if (colors[j - 1])
                        squareViews[i].setNotepadHighlightRed(j);
                    else
                        squareViews[i].setNotepadHighlightGreen(j);
                }
            }
        }
        else {
            for (int i : hint.hintInfo[Strategies.CONNECTED]) {
                for (int num : hint.hintInfo[Strategies.NOTEPAD])
                    squareViews[i].setNotepadHighlightRed(num);
            }
            for (int i : hint.hintInfo[Strategies.SELECTED]) {
                for (int num : hint.hintInfo[Strategies.NOTEPAD])
                    squareViews[i].setNotepadHighlightGreen(num);
            }
        }
    }

    /************************************
     * Clears highlights from squares
     *********************************/
    private void removeAllHighlights() {
        for (int i = 0; i < squareViews.length; i++) {
            if(!colorGame) {
                squareViews[i].setHighlighted(false, true);
                squareViews[i].clearNotepadHighlights();
            }
            else{
                squareViews[i].setHighlighted(false, false);
                squareViews[i].clearNotepadHighlights();
            }
            squareViews[i].setFullAlpha();
        }
    }


    /************************************
     * Highlight connected squares
     *********************************/
    private void highlightConnected() {
        // Get location information for selected square
        int row = selectedSquare / size;
        int col = selectedSquare % size;
        int clusterSize = mGame.getClusterSize();
        int cluster = ((row / clusterSize) * clusterSize) + (col / clusterSize);

        // Set highlights for every square based on selection
        for (int i = 0; i < squareViews.length; i++) {
            // Get location info for each
            int iRow = i / size;
            int iCol = i % size;
            int iCluster = ((iRow / clusterSize) * clusterSize) + (iCol / clusterSize);
            if(i==selectedSquare){
                if(!colorGame){
                    squareViews[i].setSelectedHighlight();
                }
                else{

                }
            }
            else if (iRow == row || iCol == col || iCluster == cluster)
                if(!colorGame) {
                    squareViews[i].setHighlighted(true, true); // Set true if current square is connected to selected
                }
                else{
                    squareViews[i].setHighlighted(true, false); // Set true if current square is connected to selected
                }
            else {
                if(!colorGame) {
                    squareViews[i].setHighlighted(false, true);
                }
                else{
                    squareViews[i].setHighlighted(false, false);
                }
            }
        }
    }

    /************************************
     * Logic for 81 squares
     *********************************/
    public void squareCommands(int squareNumber) {

        if(!keyboardState && !solveClicked){
            openKeyboard();

        }

        selectedSquare = squareNumber;
        highlightHandler.removeCallbacksAndMessages(null);
        removeAllHighlights();

        if (mGame.isStartingValue(squareNumber)) { // Starting square clicked
            int value = mGame.getBoardValueAt(squareNumber);
            for (int i = 0; i < mGame.totalBoardElements(); i++) {
                if (mGame.getBoardValueAt(i) == value) {
                    if(!colorGame) {
                        squareViews[i].setSameNumberHighlight();
                    }
                }
                else{
                    if(colorGame){
                        squareViews[i].setSameColorHighlight();
                        //squareViews[i].setSameColorHighlightNotepad();
                    }
                }
            }
        }

        else { // Input square clicked

            //Set highlights
            squareViews[squareNumber].setSelectedHighlight();

            // Bold similar numbers
            if (mGame.getBoardValueAt(selectedSquare) != 0) {
                int value = mGame.getBoardValueAt(squareNumber);
                for (int i = 0; i < mGame.totalBoardElements(); i++) {
                    if (mGame.getBoardValueAt(i) == value) {
                        if(!colorGame) {
                            squareViews[i].setSameNumberHighlight();
                        }
                    }
                    else{
                        if(colorGame){
                            squareViews[i].setSameColorHighlight();
                        }
                    }
                }
            }
            else if(rowColHighlights)
                highlightConnected();
        }
    }

    /************************************
     * Logic for 1-9 keypadbuttons, always
     * updates undo-redo, score, and checks for
     * end of game
     *********************************/
    private void numberCommands(int keypadNumber) {
        gameHelper.getApiClient().connect();

        // Cancel any delayed highlighting
        highlightHandler.removeCallbacksAndMessages(null);

        // If nothing selected or square is a starting square, do nothing
        if (selectedSquare == -1 || mGame.isStartingValue(selectedSquare))
            return;

        SquareView currSquare = squareViews[selectedSquare];

        // If notepad entry is active
        if (smallNumPadState) {
            // If square has value, remove it
            if (mGame.getBoardValueAt(selectedSquare) != 0) {
                mGame.setBoardValueAt(selectedSquare, 0);
                if (!colorGame)
                    currSquare.setBigNumber(0,false);
                else
                    currSquare.setSquareColor(0,false);
            }
            // Clearing notepad of all values
            if (keypadNumber == 0) {
                for (int i = 1; i <= mGame.totalBoardRows(); i++) {
                    mGame.setBoardNotepadAt(selectedSquare, i, false);
                    currSquare.showNotepadNumber(i, false);
                }
            }
            else {
                // Toggle notepad number and display notepad
                mGame.setBoardNotepadAt(selectedSquare, keypadNumber, !(mGame.getBoardNotepadAt(selectedSquare, keypadNumber)));
                currSquare.toggleNotepadNumber(keypadNumber);
            }
        }

        // Enter number into square, if valid, and display
        else {

            // Clearing on empty square, removes any notepad entries
            if (mGame.getBoardValueAt(selectedSquare) == 0 && keypadNumber == 0) {
                for (int i = 1; i <= mGame.totalBoardRows(); i++) {
                    mGame.setBoardNotepadAt(selectedSquare, i, false);
                    currSquare.showNotepadNumber(i, false);
                }
            }

            // If number clicked matches value already in square, remove number
            if (mGame.getBoardValueAt(selectedSquare) == keypadNumber) {
                keypadNumber = 0;
                removeAllHighlights();
            }

            Set<Integer> conflicts = mGame.setBoardValueAt(selectedSquare, keypadNumber);  // Set board value to number
            if (conflicts.isEmpty()) {
                if(!solveClicked&&!mGame.isGameFinished()&&mGame.getBoardValueAt(selectedSquare)!=0) {
                    mPlaySounds.playSounds(getContext(), 1);
                }
                if (!colorGame)
                    currSquare.setBigNumber(keypadNumber,false);   // Display number
                else
                    currSquare.setSquareColor(keypadNumber,false); // Display color
            }
            else {
                if(!solveClicked) {
                    mPlaySounds.playSounds(getContext(), 2);
                }
                for (int conflict : conflicts) {
                    squareViews[conflict].setErrorHighlight(colorGame);     // Display error highlight
                    highlightHandler.postDelayed(new Runnable() {  // Wait then remove error color
                        @Override
                        public void run() {
                            // Reset to regular selection highlights
                            removeAllHighlights();
                            squareViews[selectedSquare].setSelectedHighlight();
                            if (rowColHighlights)
                                highlightConnected();
                        }
                    }, 2000);
                }
            }
        }
        updateScore();
        updateUndoRedoButtons();
        updateLilNums();
        if(mGame.isGameFinished()&&!solveClicked){
                endOfGame(false);
        }
    }

    /* Uses the BoardFragment UI to solve a puzzle given the solution steps */
    public void solvePuzzle() {

        Handler solutionHandler = new Handler();

        // Get solution steps

        Set<Integer> incorrect = mGame.checkCurrentProgress();
        for (Integer i : incorrect) {
            mGame.setBoardValueAt(i, 0);
        }
        removeAllHighlights();
        mGame.updateAllNotepads(false);
        final ArrayList<Square> newSteps = new ArrayList<>(mGame.getSolutionSteps());

        if (newSteps.isEmpty()) {
            return;
        }


        /*
        double regression = 1340100 * Math.pow(.88316, 81-mGame.getNumberOpenSquares());
        int speedControl = (int) regression;
        int speed = speedControl / newSteps.size();
        */
        int speed = 400;
        Log.i("Speed", "Speed is " + speed);

        for(int i = 0; i < newSteps.size();i++) {


            final int finalI = i;
            solutionHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Square square = newSteps.get(finalI);
                    selectedSquare = square.getIdx();
                    numberCommands(square.getValue());
                    mGame.updateAllNotepads(false);
                    updateBoard();

                }
            }, speed * i);
        }
    }

    /************************************
     * Number vs Color initial setup
     *********************************/
    private void customizeLayout(boolean resume) {

// Changes the height and width to the specified *pixels*

        if(!multiplayer){
            ViewGroup.LayoutParams params = botBar.getLayoutParams();
            int view_height = d.getHeight();
            params.height = 0;
            botBar.setLayoutParams(params);
        }

        /////make board unclickable, made clickable after start animation
        allClickable(false);
        if(multiplayer){
            hintButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            mHintsLayout.setClickable(false);
        }
        /////set keypad colors
        for (int i = 0; i < size; i++) {
            if (!colorGame) {
                numberButtons[i].setBackgroundColor(getResources().getColor(R.color.colorKeypad));
                numberButtons[i].setText(String.format("%d", i + 1));
            } else {
                numberButtons[i].setBackgroundColor(getResources().getColor(Tags.colors[i]));
                numberButtons[i].setText("");
            }
        }
        /////reset all keypad buttons to original color
        RelativeLayout notepadBackground = (RelativeLayout) view.findViewById(R.id.scratch_bg);
        notepadBackground.setBackgroundColor(getResources().getColor(R.color.colorKeypad));
        clearButton.setBackgroundColor(getResources().getColor(R.color.colorKeypad));
        hintButton.setBackgroundColor(getResources().getColor(R.color.colorKeypad));

        /////////set notepadbutton with correct image, color or number

        ImageView artHolder = (ImageView)view.findViewById(R.id.artHolder);
        if (!colorGame) {
            notepadIcon.setImageDrawable(getResources().getDrawable(R.drawable.numbers));
            artHolder.setImageDrawable(null);

        } else {
            notepadIcon.setImageDrawable(getResources().getDrawable(R.drawable.colors));
            BackgroundImages mBackgroundImages = new BackgroundImages();

            if(resume){
                artHolder.setImageDrawable(getResources().getDrawable(mBackgroundImages.getImage(mGame.getImageNumber())));
            }
            else {
                artHolder.setImageDrawable(getResources().getDrawable(mBackgroundImages.getImage()));
                mGame.setImageNumber(mBackgroundImages.getPictureNumber());
            }
            //int imageNumber = mBackgroundImages.getPictureNumber();
        }
    }

    private void allClickable(boolean clickable){
        mUndoButton.setClickable(clickable);
        mRedoButton.setClickable(clickable);

        for (SquareView squareView : squareViews) {
            squareView.getSquareHolder().setClickable(clickable);
        }
        for (Button numberButton : numberButtons) {
            numberButton.setClickable(clickable);
        }
        clearButton.setClickable(clickable);
        hintButton.setClickable(clickable);
        notepadIcon.setClickable(clickable);


    }

    /************************************
     * Close keyboard animation
     *********************************/
    private void closeKeyboard() {
        if(solvedState&&!solveClicked) {
            mHintsLayout.setVisibility(GONE);
            mEndGameLayout.setVisibility(View.VISIBLE);
        }
        keyboardState=false;
        removeAllHighlights();
        LinearLayout mBottomLayout = (LinearLayout)view.findViewById(R.id.bottomLayout);
        RelativeLayout mKeypad = (RelativeLayout) view.findViewById(R.id.keypad);
        mBottomLayout.setVisibility(View.VISIBLE);

        ObjectAnimator scalex = ObjectAnimator.ofFloat(mBottomLayout, "scaleY", 0, 1);
        scalex.setDuration(50);


        ObjectAnimator scaley = ObjectAnimator.ofFloat(mKeypad, "scaleY", 1, 0);
        scaley.setDuration(50);
        AnimatorSet set2 = new AnimatorSet();
        set2.playSequentially(scaley, scalex);
        set2.start();
        mKeypad.setVisibility(GONE);


    }
    /************************************
     * Open keyboard animation
     *********************************/
    private void openKeyboard() {
            keyboardState = true;
            LinearLayout mBottomLayout = (LinearLayout)view.findViewById(R.id.bottomLayout);
            RelativeLayout mKeypad = (RelativeLayout) view.findViewById(R.id.keypad);


            mKeypad.setVisibility(View.VISIBLE);
            ObjectAnimator scalex = ObjectAnimator.ofFloat(mBottomLayout, "scaleY", 1, 0);
            scalex.setDuration(50);

            ObjectAnimator scaley = ObjectAnimator.ofFloat(mKeypad, "scaleY", 0, 1);
            scaley.setDuration(50);
            AnimatorSet set2 = new AnimatorSet();
            set2.playSequentially(scalex, scaley);
            set2.start();
            mBottomLayout.setVisibility(View.GONE);



    }
    /************************************
     * Overflow menu functions
     *********************************/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(com.licktopia.thechampionofsudoku.R.menu.menu_main, menu);


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        /*
        if(solvedState) {
            SpannableString s = new SpannableString("Save game");
            s.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), 0);
            menu.getItem(5).setTitle(s);
        }
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.settingsMenu) {
            callback.swapFragment(BaseActivity.SETTINGS_FRAGMENT, new Bundle());
        }

        if (id == R.id.action_instructions) {
            callback.swapFragment(BaseActivity.INSTRUCTIONS_FRAGMENT, new Bundle());
        }

        if (id == R.id.action_about) {
            callback.swapFragment(BaseActivity.ABOUT_FRAGMENT, new Bundle());
        }

        if (id == R.id.save_and_exit) {
            uploadSavedGame(false);

        }
        if (id == R.id.main_menu) {
            goToMainMenu();
        }

        if (id == R.id.action_share) {
            share();
        }
        return true;

    }


    private void homeClick() {
        goToMainMenu();
    }

    private void goToMainMenu() {
        if(!solvedState) {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.quit_to_menu))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            callback.swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
        else{
            callback.swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
        }
    }

    public boolean getSolvedState() { return solvedState; }

    private void share() {

        Intent i = new Intent(

                Intent.ACTION_SEND);

        i.setType("text/plain");
        i.putExtra(

                Intent.EXTRA_TEXT, this.getString(R.string.app_url));

        startActivity(Intent.createChooser(

                i,

                "Share via"));
        gameHelper.getApiClient().connect();
        unlockAchievements(R.string.achievement_joy_giver);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAutoSave && isInternet() && gameHelper.getApiClient().isConnected()&&!solvedState){
            uploadSavedGame(true);
        }

    }





    private void unlockAchievements(int achievement){
        if(gameHelper.isSignedIn()) {
            Games.Achievements.unlock(gameHelper.getApiClient(), getString(achievement));
        }
    }



    //Interface to be implemented by containing activity.
    public interface BoardFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
        int getBadgetag();
        double getMultiplier();
    }
}


