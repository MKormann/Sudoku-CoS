package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;


import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.Multiplayer.MultiGame;
import com.licktopia.thechampionofsudoku.PlaySounds;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.BADGETAGS;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.BOARD_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.GAMER_PROFILE_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.SAVED_GAME_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.USER_BOARD_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.clickedOut;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.getUsername;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.setId;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.setUsername;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.userSignIn;

public class MainMenuFragment extends Fragment {
    private static final String PREF_FILE = "com.licktopia.com.licktopia.championofsudoku.preferences";
    private static final int RC_SAVED_GAMES = 9009;
    private static final float RESUME_ALPHA = .3f;
    private final static String TAG = "MainMenuFragment";
    private int[] levelButtonTags = {R.id.beginnerButton,R.id.easyButton,R.id.mediumButton,R.id.hardButton,R.id.expertButton,R.id.masterButton};
    private int[] modeButtonTags = {R.id.topiaChoice,R.id.numberChoice};
    private ImageView userBadge;
    private LinearLayout mLevelSelect;
    private LinearLayout mModeSelect;
    private LinearLayout mSignInButton;
    private LinearLayout mBadge;
    private Button mSignOutButton;
    private LinearLayout mResumeButton;

    private int level = 3;
    private boolean colorMode = false;
    private Animation animShow, animHide, animHiSl, animShSlow;
    private PlaySounds mPlaySounds;


    private SharedPreferences.Editor ed;
    private boolean isGettingHighestScore = false;


    public static ProgressBar mProgressBar;
    private boolean multiplayer = false;
    private LinearLayout lickbar;
    private LinearLayout multiplayerBar;
    private float x1,x2;
    static final int MIN_DISTANCE = 25;
    private long startClickTime;
    static final int MAX_SWIPE_TIME = 200;
    private HorizontalScrollView scrollView;
    private LinearLayout mLick;
    private LinearLayout mBoardBrowswer;
    private LinearLayout innerScrollView;
    public static LinearLayout mainScreen;

    private Button hostButton;
    private Button quickButton;
    private LinearLayout screenWait;

    private View view;
    private MainMenuFragmentListener callback;

    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_main_menu_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_main_menu, container, false);
        }
        else if(phoneSize ==3){
            view = inflater.inflate(R.layout.activity_main_menu_phablet, container, false);
        }
        else if(phoneSize == 4){
            view = inflater.inflate(R.layout.activity_main_menu_large, container, false);
        }

        scrollView = (HorizontalScrollView)view.findViewById(R.id.gestures);
        mLevelSelect = (LinearLayout)view.findViewById(R.id.level_select);
        screenWait = (LinearLayout)view.findViewById(R.id.screen_wait);
        mModeSelect = (LinearLayout)view.findViewById(R.id.mode_select);
        lickbar = (LinearLayout)view.findViewById(R.id.lickbar);
        multiplayerBar = (LinearLayout)view.findViewById(R.id.multiplayer_bar);
        innerScrollView = (LinearLayout)view.findViewById(R.id.innerscrollview);
        mProgressBar = (ProgressBar)view.findViewById(R.id.main_progress);
        mainScreen = (LinearLayout)view.findViewById(R.id.menu);
        mLick = (LinearLayout)view.findViewById(R.id.lick);
        mBoardBrowswer = (LinearLayout)view.findViewById(R.id.browser_button);
        WindowManager wm = getActivity().getWindowManager();
        Display d = wm.getDefaultDisplay();
        int view_width = 2*d.getWidth();
        ViewGroup.LayoutParams params=innerScrollView.getLayoutParams();
        params.width= view_width;
        innerScrollView.setLayoutParams(params);

        ViewGroup.LayoutParams params2=lickbar.getLayoutParams();
        params2.width = d.getWidth();
        lickbar.setLayoutParams(params2);

        ViewGroup.LayoutParams params3 =multiplayerBar.getLayoutParams();
        params3.width = d.getWidth();
        multiplayerBar.setLayoutParams(params3);

        mBadge = (LinearLayout)view.findViewById(R.id.badge);
        mPlaySounds = new PlaySounds();
        SharedPreferences mSharedPreferences = getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        ed = mSharedPreferences.edit();
        ed.apply();
        userBadge = (ImageView)view.findViewById(R.id.user_badge_level);
       // userBadge.setVisibility(GONE);
        mResumeButton = (LinearLayout)view.findViewById(R.id.resume);

        // Initialize the Amazon Cognito credentials provider

        signInInitialization();
        initAnimation();
        levelButtonInitialize();
        modeButtonInitialize();
        menuBackInitialize();
        initializeLick();
        initializeShareBar();
        initializeResumeButton(mResumeButton);

        hostButton = (Button)view.findViewById(R.id.host_button);

        ((BaseActivity)getActivity()).bindMainMenuButtons(
                (LinearLayout)view.findViewById(R.id.screen_wait),
                (LinearLayout)view.findViewById(R.id.level_select),
                (LinearLayout)view.findViewById(R.id.mode_select));

        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gameHelper.getApiClient().isConnected()) {
                    ((BaseActivity)getActivity()).hostGame(MultiGame.GameMode.RACE);
                }
                else{
                    Toast.makeText(getContext(), R.string.must_be_logged_in,Toast.LENGTH_SHORT).show();
                }
            }
        });



        ((Button)view.findViewById(R.id.join_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(gameHelper.getApiClient().isConnected()) {
                    ((BaseActivity)getActivity()).viewInvites();
                }
                else{
                    Toast.makeText(getContext(), R.string.must_be_logged_in,Toast.LENGTH_SHORT).show();
                }
            }
        });




        quickButton = (Button)view.findViewById(R.id.quick_button);
        quickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gameHelper.getApiClient().isConnected()) {
                    ((BaseActivity)getActivity()).startQuickGame(MultiGame.GameMode.RACE);
                }
                else{
                    Toast.makeText(getContext(), R.string.must_be_logged_in,Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (MainMenuFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement MainMenuFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void initializeResumeButton(LinearLayout mResumeButton) {
        mResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameHelper.getApiClient().isConnected()) {
                    callback.swapFragment(SAVED_GAME_FRAGMENT, new Bundle());
                }
                else{
                    Toast.makeText(getContext(), R.string.must_be_logged_in,Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    /***************************************
     * Creates mGoogleApi Client and sets listeners
     * for sign in and sign out
     ****************************************/
    private void signInInitialization() {


        mSignInButton = (LinearLayout)view.findViewById(R.id.signin_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternet()) {
                    signInClicked();
                }
                else{
                    Toast.makeText(getContext(), R.string.check_your_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSignOutButton = (Button)view.findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutClicked();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(!gameHelper.isConnecting() && gameHelper.isSignedIn() && !isGettingHighestScore) {
            getHighestScoreFromLeaderBoard();
        }
        else if((!gameHelper.isConnecting() && !gameHelper.isSignedIn() && !isGettingHighestScore)){
            mSignInButton.setVisibility(VISIBLE);
            mBadge.setVisibility(GONE);
            mProgressBar.setVisibility(GONE);
            mainScreen.setAlpha(1);
            setMessage();
        }
        userBadge.setImageDrawable(ContextCompat.getDrawable(getActivity(), BADGETAGS[BaseActivity.level]));


        screenWait.setVisibility(GONE);
        mLevelSelect.setVisibility(VISIBLE);
        mModeSelect.setVisibility(GONE);

    }

    private void loadPlayer(){
        if(gameHelper.getApiClient().isConnected()) {
            setUsername(Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getDisplayName());
            setId(Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getPlayerId());
            mBadgeButtonInitialize(getUsername(), BaseActivity.getId());

        }
       if(gameHelper.isSignedIn()&&!gameHelper.isConnecting()&&!isGettingHighestScore){
           mSignInButton.setVisibility(GONE);
           mBadge.setVisibility(VISIBLE);
           mProgressBar.setVisibility(GONE);
           mainScreen.setAlpha(1);
           setMessage();
       }
       else if(!gameHelper.isConnecting() && !gameHelper.isSignedIn() && !isGettingHighestScore){
            mSignInButton.setVisibility(VISIBLE);
            mBadge.setVisibility(GONE);
            mProgressBar.setVisibility(GONE);
            mainScreen.setAlpha(1);
           setMessage();
        }
    }

    private boolean isInternet(){
        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }


    /***************************************
    *Update UI when login or logout occurs
    ***************************************/
    public void updateUI(boolean loggedIn){
        if(loggedIn) {
            if (userSignIn) {
                mPlaySounds.playSounds(getContext(), 7);
                userSignIn = false;
            }

            //gameHelper.getApiClient().connect();

            if (!isGettingHighestScore) {
                getHighestScoreFromLeaderBoard();
            }
            else{
                loadPlayer();
            }
        }
        else{
            BaseActivity.setNullUser();
            if(clickedOut) {
                mPlaySounds.playSounds(getContext(),8);
            }

            clickedOut = false;

            userBadge.setImageDrawable(ContextCompat.getDrawable(getActivity(), callback.getBadgetag()));
            loadPlayer();

        }

        //userBadge.setVisibility(VISIBLE);

    }

    private void getHighestScoreFromLeaderBoard(){
        isGettingHighestScore = true;
        if(isInternet()) {
            PendingResult result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(),
                    getString(R.string.leaderboard_most_total_xp), LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC);

            result.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult result) {
                    // check if valid score
                    if (result != null
                            && GamesStatusCodes.STATUS_OK == result.getStatus().getStatusCode()
                            && result.getScore() != null) {
                        long highestScore = result.getScore().getRawScore();
                        if(highestScore>=BaseActivity.getXp()) {
                            BaseActivity.setXp(highestScore);


                            //userBadge.setImageDrawable(ContextCompat.getDrawable(getActivity(), callback.getBadgetag()));
                        }

                        //userBadge.setVisibility(VISIBLE);
                        isGettingHighestScore = false;
                        loadPlayer();

                    }
                    else{
                        isGettingHighestScore = false;
                        loadPlayer();

                    }


                }

            });

            PendingResult rankResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(),
                    "CgkIjrCfsNsFEAIQGA", LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC);

            rankResult.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult result) {
                    // check if valid score
                    if (result != null
                            && GamesStatusCodes.STATUS_OK == result.getStatus().getStatusCode()
                            && result.getScore() != null) {
                        long highestScore = result.getScore().getRawScore();
                        if(highestScore>=BaseActivity.getMultiplayerRank()) {
                            Log.d(TAG, "You have a rank of " + Long.toString(BaseActivity.getMultiplayerRank()));
                            BaseActivity.setMultiplayerRank(highestScore);
                        }
                    }

                }

            });


        }
        else{

            //userBadge.setVisibility(VISIBLE);
            isGettingHighestScore = false;
            loadPlayer();

        }

    }

    // Call when the sign-in button is clicked
    private void signInClicked() {
        BaseActivity.gameHelper.beginUserInitiatedSignIn();
        userSignIn = true;
    }

    // Call when the sign-out button is clicked
    private void signOutClicked() {
        gameHelper.signOut();
        clickedOut = true;
        updateUI(false);
    }

    private void initializeShareBar() {
        LinearLayout mShareBar = (LinearLayout)view.findViewById(R.id.share_bar);
        mShareBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    private void initializeLick() {
        WindowManager wm = getActivity().getWindowManager();
        final Display d = wm.getDefaultDisplay();
        final int view_width = d.getWidth();
        mLick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playLickSound();


            }
        });

        mBoardBrowswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                callback.swapFragment(USER_BOARD_FRAGMENT, args);
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        Log.i("ScrollInfo", "" + x1);
                        return true;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;

                        if (Math.abs(deltaX) > MIN_DISTANCE)
                        {
                            // Left to Right swipe action
                            if (x2 > x1) {

                                    scrollView.smoothScrollTo(0, 0);


                            }

                            // Right to left swipe action
                            else
                            {
                                scrollView.smoothScrollTo(view_width,0);
                            }

                        }
                        else
                        {
                           if(scrollView.getScrollX()!=0 || scrollView.getScrollX()!=view_width){
                               if(scrollView.getScrollX()<view_width/2) {
                                   scrollView.smoothScrollTo(0, 0);
                               }
                               else{
                                   scrollView.smoothScrollTo(view_width, 0);
                               }
                           }
                        }
                        return true;
                }
                return false;

            }
        });


    }


    private void menuBackInitialize() {
        ImageButton mMenuBack = (ImageButton)view.findViewById(R.id.menu_back);
        mMenuBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();

            }


        });
    }

    private void mBadgeButtonInitialize(final String gamername, final String gamerID){
        userBadge.setImageDrawable(ContextCompat.getDrawable(getActivity(), BADGETAGS[BaseActivity.level]));
        mBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("GAMERNAME", gamername);
                args.putString("GAMERID", gamerID);
                callback.swapFragment(GAMER_PROFILE_FRAGMENT, args);
            }
        });
    }

    private void modeButtonInitialize() {
        ImageButton[] modeButtons = new ImageButton[modeButtonTags.length];
        for(int i = 0; i< modeButtons.length; i++){
            modeButtons[i] =(ImageButton)view.findViewById(modeButtonTags[i]);
            final int finalI = i;
            modeButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(finalI ==0){
                        colorMode =true;
                    }
                    else{
                        colorMode=false;
                    }
                    boolean resume = false;
                    Bundle args = new Bundle();
                    args.putBoolean("RESUME", resume);
                    args.putInt("LEVEL", level);
                    args.putBoolean("COLOR_MODE", colorMode);
                    args.putBoolean("MULTIPLAYER", multiplayer);
                    callback.swapFragment(BOARD_FRAGMENT, args);
                }
            });
        }
    }

    private void levelButtonInitialize() {
        ImageButton[] levelButtons = new ImageButton[levelButtonTags.length];
        for(int i = 0; i< levelButtons.length; i++){
            levelButtons[i] = (ImageButton)view.findViewById(levelButtonTags[i]);
            final int finalI = i;
            levelButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    level = finalI;
                    setMessage();
                    chooseGameType();
                }
            });
        }
    }

    private void chooseGameType(){
        mPlaySounds.playSounds(getContext(),10);
        mLevelSelect.setVisibility(GONE);
        mLevelSelect.startAnimation(animHide);
        mModeSelect.setVisibility(VISIBLE);
        mModeSelect.startAnimation(animShow);

    }

    private void goBack() {
        mModeSelect.setVisibility(View.INVISIBLE);
        mModeSelect.startAnimation(animHide);
        mLevelSelect.setVisibility(VISIBLE);
        mLevelSelect.startAnimation(animShow);
        mPlaySounds.playSounds(getContext(),10);
    }

    private void playLickSound(){
        mPlaySounds.playSounds(getContext(), 5);
    }


    private void setMessage(){
        TextView mMessage = (TextView)view.findViewById(R.id.message);
        if(gameHelper.getApiClient().isConnected()){
            mMessage.setVisibility(View.INVISIBLE);
        }
        else{
            mMessage.setVisibility(VISIBLE);
        }
        if(gameHelper.getApiClient().isConnected()) {
            if (level < 2) {
                mMessage.setText(getString(R.string.harder_puzzle_message));
                mMessage.setVisibility(VISIBLE);
            } else {
                mMessage.setText("");
                mMessage.setVisibility(View.INVISIBLE);
            }
        }
        else{
            mMessage.setText(R.string.sign_in_message);
            mMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userSignIn = true;
                    callback.beginUserInitSignIn();
                }
            });
        }
    }
    private void initAnimation()
    {
        animShow = AnimationUtils.loadAnimation(getContext(), R.anim.view_show);
        animHide = AnimationUtils.loadAnimation(getContext(), R.anim.view_hide);
        animHiSl = AnimationUtils.loadAnimation(getContext(), R.anim.view_hideslow);
        animShSlow = AnimationUtils.loadAnimation(getContext(), R.anim.view_showslow);
    }

    private void share() {

        Intent i = new Intent(

                Intent.ACTION_SEND);

        i.setType("text/plain");
        i.putExtra(

                Intent.EXTRA_TEXT, this.getString(R.string.app_url));

        startActivity(Intent.createChooser(

                i,

                "Share via"));

        if (gameHelper.getApiClient() != null && gameHelper.getApiClient().isConnected()) {
            // Call a Play Games services API method, for example:
            Games.Achievements.unlock(gameHelper.getApiClient(), getString(R.string.achievement_joy_giver));
        }



    }

    //Interface to be implemented by containing activity.
    public interface MainMenuFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
        //void querySavedGameDb();
        int getBadgetag();
        void beginUserInitSignIn();

    }

}
