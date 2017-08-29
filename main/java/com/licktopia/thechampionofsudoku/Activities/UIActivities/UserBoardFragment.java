package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.google.android.gms.games.Games;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.Adapters.UserGameListAdapter;
import com.licktopia.thechampionofsudoku.CircleTransform;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;
import com.licktopia.thechampionofsudoku.Database.DatabaseAccess;
import com.licktopia.thechampionofsudoku.ListItems.UserGameListItem;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.Tags;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.View.GONE;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;


public class UserBoardFragment extends Fragment {

    private int DISPLAY_AT_A_TIME = 14;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<UserGameListItem> userGameListItems;
    private ArrayList<UserGameListItem> ShuffledList;
    private ArrayList[] LevelLists;
    public static UserBoardFragment newInstance() {return new UserBoardFragment();}
    private View view;
    private int levelNow = 6;
    private DatabaseAccess databaseAccess;
    private ProgressBar mProgressBar;
    private boolean color = false;
    private ImageButton colorSwitcher;
    private int gridNum = 2;
    private GridLayoutManager gridLayoutManager;
    private List<Integer> smallRange;
    private List<Integer> bigRange;
    private TextView titleLabel;
    private UserBoardFragment.UserBoardFragmentListener callback;
    private HashSet<Long> completedGameIds;


    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (UserBoardFragment.UserBoardFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement BoardFragmentListener");
        }
    }

    //Interface to be implemented by containing activity.
    public interface UserBoardFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.fragment_user_board_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.fragment_user_board, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.fragment_user_board_phablet, container, false);
            gridNum = 3;
            DISPLAY_AT_A_TIME++;
        }
        else{
            view = inflater.inflate(R.layout.fragment_user_board_large, container, false);
            gridNum = 4;
            DISPLAY_AT_A_TIME+=2;
        }
        resetBigRange();
        titleLabel = (TextView)view.findViewById(R.id.board_layout_title);
        titleLabel.setText(R.string.all_level);
        return view;
    }

    private void resetBigRange() {
        bigRange = new ArrayList<>();
        for(int i = 1; i < 60001; i++){
            bigRange.add(i);
        }
        Collections.shuffle(bigRange);
        bigRange = bigRange.subList(0,DISPLAY_AT_A_TIME);
    }

    private void resetSmallRange(int level) {
        smallRange = databaseAccess.getIdentifiersFromLevel(level);
        Collections.shuffle(smallRange);
        smallRange = smallRange.subList(0,DISPLAY_AT_A_TIME);
    }

    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
        ImageView iconView = (ImageView)view.findViewById(R.id.user_icon_usr);
        if(gameHelper.isSignedIn()){
            CircleTransform cf = new CircleTransform();
            try {
                String me = Games.Players.getCurrentPlayer(gameHelper.getApiClient()).getIconImageUrl();
                Picasso.with(getActivity()).load(me).transform(cf).into((ImageView) iconView);
            }catch (Exception e){

            }
        }
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.swapFragment(BaseActivity.MAIN_MENU_FRAGMENT, new Bundle());
            }
        });
    }
    @Override
    public void onStart(){
        super.onStart();
        if(ShuffledList==null) {
            mProgressBar = (ProgressBar) view.findViewById(R.id.usr_progress);
            recyclerView = (RecyclerView) view.findViewById(R.id.user_board_rv);
            gridLayoutManager = new GridLayoutManager(getContext(), gridNum);
            recyclerView.setLayoutManager((gridLayoutManager));
            LevelLists = new ArrayList[Tags.Levels.length];
            completedGameIds = new HashSet<>();
            /*
            DividerItemDecoration itemDecoratorVer = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            itemDecoratorVer.setDrawable(getResources().getDrawable(R.drawable.spacer_vertical, null));
            DividerItemDecoration itemDecoratorHor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
            itemDecoratorHor.setDrawable(getResources().getDrawable(R.drawable.spacer_horizontal, null));
            recyclerView.addItemDecoration(itemDecoratorVer);
            recyclerView.addItemDecoration(itemDecoratorHor);
            */


            Runnable runnable = new Runnable() {
                public void run() {
                    String partitionKey = BaseActivity.getId();
                    CompletedGame cg = new CompletedGame();
                    cg.setGamerID(partitionKey);
                    final DynamoDBQueryExpression<CompletedGame> queryExpression = new DynamoDBQueryExpression<CompletedGame>()
                            .withHashKeyValues(cg)
                            .withConsistentRead(false);

                    final List<CompletedGame>[] list = new List[]{new ArrayList<>()};
                    try {
                        list[0] = BaseActivity.mapper.query(CompletedGame.class, queryExpression);
                    } catch (Exception e) {

                    }
                    completedGameIds.clear();
                    for (int i = 0; i < list[0].size(); i++) {
                        completedGameIds.add(list[0].get(i).getPuzzleNumber());
                    }





                    databaseAccess = DatabaseAccess.getInstance(getContext());
                    databaseAccess.open();
                    ShuffledList = new ArrayList<>();
                    for (int i = 0; i < DISPLAY_AT_A_TIME; i++) {
                        ShuffledList.add(databaseAccess.getSpecificBoardGame(bigRange.get(i)));
                    }

                    adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                    initializeButtons();
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //execute code on main thread
                                mProgressBar.setVisibility(GONE);
                                recyclerView.setAdapter(adapter);
                            }
                        });
                    } catch (Exception e) {

                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
    }

    private void initializeButtons() {
        ImageButton[] ButtArray = new ImageButton[Tags.levelSorter.length];
        for(int i = 0; i < ButtArray.length; i++){
            ButtArray[i]=(ImageButton)view.findViewById(Tags.levelSorter[i]);
            final int finalI = i;
            final int finalI1 = i;
            ButtArray[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    levelNow = finalI;
                    titleLabel.setText(new StringBuilder().append(Tags.Levels[levelNow]).append(" Sudokus").toString());
                    ShuffledList.clear();
                    //DisplayNowList = LevelLists[finalI].subList(0,DISPLAY_AT_A_TIME);
                    resetSmallRange(finalI1);
                    for(int j = 0; j < DISPLAY_AT_A_TIME; j++){
                        ShuffledList.add(databaseAccess.getSpecificBoardGame(smallRange.get(j)));
                    }
                    adapter = null;
                    adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                    recyclerView.setAdapter(adapter);
                }
            });
        }
        LinearLayout ranButton = (LinearLayout)view.findViewById(R.id.ran);
        ranButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelNow = 6;
                titleLabel.setText(R.string.all_level);
                resetBigRange();
                //DisplayNowList = ShuffledList.subList(0, DISPLAY_AT_A_TIME);
                ShuffledList.clear();
                for(int i = 0; i < DISPLAY_AT_A_TIME; i++){
                    ShuffledList.add(databaseAccess.getSpecificBoardGame(bigRange.get(i)));
                }
                adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                recyclerView.setAdapter(adapter);
            }
        });

        LinearLayout moreButton = (LinearLayout) view.findViewById(R.id.more);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(levelNow<6) {
                    resetSmallRange(levelNow);
                    ShuffledList.clear();
                    for(int j = 0; j < DISPLAY_AT_A_TIME; j++){
                        ShuffledList.add(databaseAccess.getSpecificBoardGame(smallRange.get(j)));
                    }
                    //DisplayNowList = LevelLists[levelNow].subList(0, DISPLAY_AT_A_TIME);
                }
                else{
                    resetBigRange();
                    ShuffledList.clear();
                    for(int i = 0; i < DISPLAY_AT_A_TIME; i++){
                        ShuffledList.add(databaseAccess.getSpecificBoardGame(bigRange.get(i)));
                    }
                    //DisplayNowList = ShuffledList.subList(0, DISPLAY_AT_A_TIME);
                }
                adapter = null;
                adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                recyclerView.setAdapter(adapter);
            }
        });
        colorSwitcher = (ImageButton)view.findViewById(R.id.color_switcher);
        colorSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(color){
                    color = false;
                    adapter = null;
                    adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                    colorSwitcher.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.peacock));
                    recyclerView.setAdapter(adapter);
                }
                else{
                    color = true;
                    adapter = null;
                    adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                    colorSwitcher.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.rocket));
                    recyclerView.setAdapter(adapter);
                }

            }
        });
        LinearLayout userPlayedButton = (LinearLayout) view.findViewById(R.id.my_completed_button);
        userPlayedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShuffledList.clear();
                for(Long i : completedGameIds){
                    ShuffledList.add(databaseAccess.getSpecificBoardGame(i));
                }
                adapter = null;
                adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                recyclerView.setAdapter(adapter);
            }
        });
        final RelativeLayout enterBoard = (RelativeLayout)view.findViewById(R.id.num_picker);
        final EditText boardNum = (EditText) view.findViewById(R.id.num_board);
        Button okButton = (Button)view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredNum = boardNum.getText().toString();
                UserGameListItem usgli;
                try {
                    usgli = databaseAccess.getSpecificBoardGame(Long.valueOf(enteredNum));
                    ShuffledList.clear();
                    ShuffledList.add(usgli);
                    adapter = null;
                    adapter = new UserGameListAdapter(ShuffledList, getContext(), color, completedGameIds);
                    recyclerView.setAdapter(adapter);
                }catch(Exception e){
                    Log.d(TAG, "onClick: Couldn't retrieve user entered board");
                    Toast.makeText(getContext(), new StringBuilder().append(getString(R.string.couldnt_find)).append(enteredNum).toString(), Toast.LENGTH_SHORT).show();
                }
                try  {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
                enterBoard.setVisibility(GONE);
            }
        });
        LinearLayout enterBoardNumber = (LinearLayout) view.findViewById(R.id.enter_board_key);
        enterBoardNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterBoard.setVisibility(View.VISIBLE);
            }
        });
    }



}
