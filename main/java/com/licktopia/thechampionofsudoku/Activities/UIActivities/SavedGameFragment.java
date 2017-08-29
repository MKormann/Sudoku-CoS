package com.licktopia.thechampionofsudoku.Activities.UIActivities;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.android.gms.games.Games;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.CircleTransform;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.Adapters.SavedGameListAdapter;
import com.licktopia.thechampionofsudoku.ListItems.SavedGameListItem;
import com.licktopia.thechampionofsudoku.DBOjects.SavedGame;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.formatExactTime;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;


public class SavedGameFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ProgressBar mProgressIndicator;
    private TextView mLoadingText;
    private TextView savedUser;
    public static List<SavedGameListItem> savedGameListItems;
    private View view;
    private SavedGameFragment.SavedGameFragmentListener callback;

    public static SavedGameFragment newInstance() {
        return new SavedGameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_savedgame_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_savedgame, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_savedgame_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_savedgame_large, container, false);
        }

        mProgressIndicator = (ProgressBar)view.findViewById(R.id.progressBar2);
        mLoadingText = (TextView)view.findViewById(R.id.loading);
        mProgressIndicator.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView)view.findViewById(R.id.savedrecyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedUser = (TextView)view.findViewById(R.id.saveduser_name);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext()).size(12).build());

        savedUser.setText(new StringBuilder().append(BaseActivity.getUsername()).append(getString(R.string.your_saved_games)).toString());

        return view;
    }

    private void fillListItems(ArrayList<SavedGame> savedGames){
        savedGameListItems = new ArrayList<>();
        for(int i = 0; i < savedGames.size(); i++){
            SavedGameListItem sGLI= new SavedGameListItem(
                    savedGames.get(i).getDate(),
                    savedGames.get(i).getTime(),
                    savedGames.get(i).getColorMode(),
                    savedGames.get(i).getExactTime(),
                    savedGames.get(i).getLevel(),
                    savedGames.get(i).getPuzzleNumber(),
                    savedGames.get(i).getGamerID(),
                    savedGames.get(i).getStoppedTime()
                    );
            savedGameListItems.add(sGLI);
        }
        Collections.sort(savedGameListItems, new RecentSorter());
    }

    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (SavedGameFragment.SavedGameFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement SavedGameFragmentListener");
        }
    }

    //Interface to be implemented by containing activity.
    public interface SavedGameFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
    }
    @Override
    public void onResume(){
        super.onResume();
        ImageView iconView = (ImageView)view.findViewById(R.id.icon_user_sav);
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
    }

    @Override
    public void onStart(){
        super.onStart();
        querySavedGameDb();

    }

    public void querySavedGameDb() {

        String partitionKey = BaseActivity.getId();
        SavedGame sg = new SavedGame();
        sg.setGamerID(partitionKey);
        final DynamoDBQueryExpression<SavedGame> queryExpression = new DynamoDBQueryExpression<SavedGame>()
                .withHashKeyValues(sg);


        final List[] list = new List[]{new ArrayList<>()};

        // id field is null at this point
        Runnable runnable = new Runnable() {
            public void run() {
                //DynamoDB calls go here
                try {
                    list[0] = BaseActivity.mapper.query(SavedGame.class, queryExpression);
                }
                catch (Exception e){

                }
                final ArrayList<SavedGame> sGames = new ArrayList<SavedGame>();
                for (int i = 0; i < list[0].size(); i++) {
                    sGames.add((SavedGame) list[0].get(i));
                }

                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //execute code on main thread
                            fillListItems(sGames);
                            adapter = new SavedGameListAdapter(savedGameListItems, getContext());
                            recyclerView.setAdapter(adapter);
                            mProgressIndicator.setVisibility(View.GONE);
                            mLoadingText.setVisibility(View.GONE);
                        }
                    });
                }catch(Exception e){

                }

            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    public class RecentSorter implements Comparator<SavedGameListItem> {

        public int compare(SavedGameListItem one, SavedGameListItem another){
            int returnVal = 0;

            if(formatExactTime(one.getExactTime()) < formatExactTime(another.getExactTime())){
                returnVal =  1;
            }else if(formatExactTime(one.getExactTime()) > formatExactTime(another.getExactTime())){
                returnVal =  -1;
            }else if(formatExactTime(one.getExactTime()) == formatExactTime(another.getExactTime())){
                returnVal =  0;
            }

            return returnVal;

        }
    }


}
