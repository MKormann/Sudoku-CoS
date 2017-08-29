package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.google.android.gms.games.Games;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.CircleTransform;
import com.licktopia.thechampionofsudoku.PlaySounds;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.SudokuFacts;
import com.licktopia.thechampionofsudoku.Tags;
import com.squareup.picasso.Picasso;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.COMPLETED_GAME_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.GAMER_PROFILE_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.MAIN_MENU_FRAGMENT;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.setNullUser;

public class GamerProfileFragment extends Fragment {

    private SudokuFacts sudokuFacts;
    private PlaySounds mPlaySounds;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private CognitoSyncManager syncClient;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;
    private Button mHistoryButton;

    private View view;
    private GamerProfileFragmentListener callback;

    public static GamerProfileFragment newInstance() {
        return new GamerProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_gamer_profile_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_gamer_profile, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_gamer_profile_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_gamer_profile_large, container, false);
        }

        mHistoryButton = (Button) view.findViewById(R.id.history_button);
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(),
                "us-east-2:40802ef0-7537-4910-8442-31e2b30c5a7d", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);

        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_2));

        initializeSignOut();
        updateUI();
        listeners();

        return view;
    }


    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (GamerProfileFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement GamerProfileFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageView iconView = (ImageView)view.findViewById(R.id.icon_gamer);
        if(gameHelper.isSignedIn()){
            CircleTransform cf = new CircleTransform();
            try {
                String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                Picasso.with(getActivity()).load(me).transform(cf).into(iconView);
            }catch (Exception e){

            }
        }
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
            }
        });
        updateUI();


    }

    private void initializeSignOut() {
        mPlaySounds = new PlaySounds();
        Button mSignOut = (Button)view.findViewById(R.id.sign_out_new);
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameHelper.signOut();
                setNullUser();
                playDisconnect();
                callback.swapFragment(MAIN_MENU_FRAGMENT, new Bundle());

            }
        });
    }

    private void playDisconnect() {
        mPlaySounds.playSounds(getContext(), 8);
    }
    private void listeners() {
        Button mLeaderButton = (Button)view.findViewById(R.id.leader);
        mLeaderButton.setOnClickListener(new View.OnClickListener() {
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
        Button mAchievementButton = (Button)view.findViewById(R.id.achievements);
        mAchievementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameHelper.getApiClient() != null && gameHelper.getApiClient().isConnected()) {
                    // Call a Play Games services API method, for example:

                    startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()),
                            5434);
                } else {
                    // Alternative implementation (or warn user that they must
                    // sign in to use this feature)
                    Toast.makeText(getActivity(), R.string.mustbe_loggedin_viewachievements, Toast.LENGTH_SHORT).show();
                }

            }
        });

        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.swapFragment(COMPLETED_GAME_FRAGMENT, new Bundle());
            }
        });
    }

    private void updateUI() {
        TextView mTitleName = (TextView) view.findViewById(R.id.title_name);
        TextView mFactText = (TextView) view.findViewById(R.id.fact_text);
        TextView mDidYouKnow = (TextView) view.findViewById(R.id.did_you_know);
        TextView xpDisplay = (TextView) view.findViewById(R.id.xp_display);
        TextView nextPowerUpDisplay = (TextView) view.findViewById(R.id.powerup_display);
        LinearLayout mFactBackground = (LinearLayout) view.findViewById(R.id.fact_background);
        ImageView mRankDisplay = (ImageView)view.findViewById(R.id.rank_display);
        Button mHistoryButton = (Button) view.findViewById(R.id.history_button);
        RelativeLayout mTitleBar = (RelativeLayout) view.findViewById(R.id.title_bar);

        sudokuFacts = new SudokuFacts();
        mFactText.setText(sudokuFacts.getFunFact());
        String color = sudokuFacts.getColorHex();
        mDidYouKnow.setBackgroundColor(Color.parseColor(color));
        mFactBackground.setBackgroundColor(Color.parseColor(color));
        mTitleBar.setBackgroundColor(Color.parseColor(color));
        mTitleName.setText(BaseActivity.getUsername());

        if(BaseActivity.getLevel()==BaseActivity.CUTOFF.length){
            nextPowerUpDisplay.setText(callback.getRank());
        }
        else{
            nextPowerUpDisplay.setText(new StringBuilder().append(getString(R.string.next_levelup)).append(" ").append(String.format("%,d", callback.getCutoff() - BaseActivity.getXp())).append(" XP").toString());
        }
        xpDisplay.setText(new StringBuilder().append(String.format("%,d", Integer.parseInt(String.valueOf(BaseActivity.getXp())))).append(" XP").toString());
        nextPowerUpDisplay.setText(new StringBuilder().append(getString(R.string.next_levelup)).append(" ").append(String.format("%,d", callback.getNextCutoff() - BaseActivity.getXp())).append(" XP").toString());
        mRankDisplay.setImageDrawable(ContextCompat.getDrawable(getActivity(), callback.getBadgetag()));
        ImageView[] verticalBadges = new ImageView[52];
        for(int i = 0; i< Tags.verticalBadges.length;i++){
            verticalBadges[i]=(ImageView)view.findViewById(Tags.verticalBadges[i]);
            verticalBadges[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), Tags.badgeTags[i]));
            if(i<=BaseActivity.getLevel()){
                verticalBadges[i].setVisibility(View.VISIBLE);
            }
            else{
                verticalBadges[i].setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void signInFailed() {
        Toast.makeText(getActivity(), R.string.nolonger_loggedin,Toast.LENGTH_SHORT).show();
        callback.swapFragment(GAMER_PROFILE_FRAGMENT, new Bundle());
    }


    public void signInSucceeded() {
        if(gameHelper.getApiClient().isConnected()) {
            TextView mTitleName = (TextView) view.findViewById(R.id.title_name);
            mTitleName.setText(Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getDisplayName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        gameHelper.onActivityResult(requestCode, resultCode, data);

    }

    //Interface to be implemented by containing activity.
    public interface GamerProfileFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
        int getBadgetag();
        String getRank();
        int getCutoff();
        int getNextCutoff();
    }
}
