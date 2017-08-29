package com.licktopia.thechampionofsudoku.Activities.UIActivities;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.google.android.gms.games.Games;
import com.licktopia.thechampionofsudoku.Activities.BaseActivity;
import com.licktopia.thechampionofsudoku.Adapters.CompletedGameListAdapter;
import com.licktopia.thechampionofsudoku.CircleTransform;
import com.licktopia.thechampionofsudoku.ListItems.CompletedGameListItem;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.formatExactTime;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.gameHelper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.mapper;
import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;


public class CompletedGameFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private Button tvSortRecent;
    private Button tvSortBest;
    private Button tvSortHigh;
    private Button tvSortTime;
    private TextView tvGpDisplay;
    private ProgressBar mProgressIndicator;
    private TextView mLoadingText;
    public static List<CompletedGameListItem> completedGameListItems;
    private View view;
    private CompletedGameFragment.CompletedGameFragmentListener callback;

    public static CompletedGameFragment newInstance() {
        return new CompletedGameFragment();
    }


    //Checks interface is implemented in the activity.
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            callback = (CompletedGameFragment.CompletedGameFragmentListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement CompletedGameFragmentListener");
        }
    }

    //Interface to be implemented by containing activity.
    public interface CompletedGameFragmentListener {
        void swapFragment(String fragmentName, Bundle args);
    }
    @Override
    public void onResume(){
        super.onResume();
        ImageView iconView = (ImageView)view.findViewById(R.id.icon_comp_usr);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_completedgame_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_completedgame, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_completedgame_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_completedgame_large, container, false);
        }

        mProgressIndicator = (ProgressBar)view.findViewById(R.id.progressBar3);
        mLoadingText = (TextView)view.findViewById(R.id.loading3);
        tvSortRecent = (Button)view.findViewById(R.id.sort_recent);
        tvSortBest = (Button)view.findViewById(R.id.sort_best);
        tvSortHigh = (Button)view.findViewById(R.id.sort_highscore);
        tvSortTime = (Button)view.findViewById(R.id.sort_time);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvGpDisplay = (TextView)view.findViewById(R.id.user_name);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext()).size(12).build());

        tvGpDisplay.setText(new StringBuilder().append(BaseActivity.getUsername()).append(getString(R.string.game_history)).toString());
        queryCompletedGameDb();
        return view;
    }

    private void initializeSorters() {
        tvSortRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(completedGameListItems,new RecentSorter());
                adapter.notifyDataSetChanged();
                //recyclerView.setAdapter(adapter);
            }
        });

        tvSortBest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(completedGameListItems,new BestSorter());
                adapter.notifyDataSetChanged();
                //recyclerView.setAdapter(adapter);
            }
        });

        tvSortHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(completedGameListItems,new HighSorter());
                adapter.notifyDataSetChanged();
               // recyclerView.setAdapter(adapter);
            }
        });

        tvSortTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(completedGameListItems,new TimeSorter());
                adapter.notifyDataSetChanged();
                //recyclerView.setAdapter(adapter);
            }
        });
    }

    private void fillListItems(ArrayList<CompletedGame> completedGames){
        completedGameListItems = new ArrayList<>();
        for(int i = 0; i < completedGames.size(); i++){
            CompletedGameListItem cGLI= new CompletedGameListItem(
                    completedGames.get(i).getAccuracy(),
                    completedGames.get(i).getDate(),
                    completedGames.get(i).getLevel(),
                    completedGames.get(i).getPuzzleNumber(),
                    completedGames.get(i).getRealScore(),
                    completedGames.get(i).getTime(),
                    completedGames.get(i).getXp(),
                    completedGames.get(i).getGamerID(),
                    completedGames.get(i).isColor(),
                    completedGames.get(i).getExactTime());
            completedGameListItems.add(cGLI);

        }
        Collections.sort(completedGameListItems,new RecentSorter());
    }

    public class RecentSorter implements Comparator<CompletedGameListItem> {

        public int compare(CompletedGameListItem one, CompletedGameListItem another){
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

    public class BestSorter implements Comparator<CompletedGameListItem> {

        public int compare(CompletedGameListItem one, CompletedGameListItem another){
            int returnVal = 0;

            if(one.getRealScore() < another.getRealScore()){
                returnVal =  1;
            }else if(one.getRealScore() > another.getRealScore()){
                returnVal =  -1;
            }else if(one.getRealScore() == another.getRealScore()){
                returnVal =  0;
            }
            return returnVal;

        }
    }

    public class HighSorter implements Comparator<CompletedGameListItem> {

        public int compare(CompletedGameListItem one, CompletedGameListItem another){

            int returnVal = 0;

            if(one.getRealScore() < another.getRealScore()){
                returnVal =  1;
            }else if(one.getXp() > another.getXp()){
                returnVal =  -1;
            }else if(one.getXp() == another.getXp()){
                returnVal =  0;
            }

            return returnVal;
        }

    }

    public class TimeSorter implements Comparator<CompletedGameListItem> {

        public int compare(CompletedGameListItem one, CompletedGameListItem another){
            int returnVal = 0;

            if(uFT(one.getTime()) < uFT(another.getTime())){
                returnVal =  -1;
            }else if(uFT(one.getTime()) > uFT(another.getTime())){
                returnVal =  1;
            }else if(one.getTime() == (another.getTime())){
                returnVal =  0;
            }
            return returnVal;

        }
    }

    public int uFT(String time) {
        int timeNumber;
        StringBuilder str = new StringBuilder();
        for(char c : time.toCharArray()){
            if(c != ':'){
                str.append(c);
            }

        }
        timeNumber = Integer.valueOf(str.toString());
        return timeNumber;
    }

    private void queryCompletedGameDb(){
        if(isInternet()) {
            String partitionKey = BaseActivity.getId();
            CompletedGame cg = new CompletedGame();
            cg.setGamerID(partitionKey);

            final DynamoDBQueryExpression<CompletedGame> queryExpression = new DynamoDBQueryExpression<CompletedGame>()
                    .withHashKeyValues(cg)
                    .withConsistentRead(false);

            final List<CompletedGame>[] list = new List[]{new ArrayList<CompletedGame>()};

            // id field is null at this point
            Runnable runnable = new Runnable() {
                public void run() {
                    //DynamoDB calls go here
                    list[0] = mapper.query(CompletedGame.class, queryExpression);
                    final ArrayList<CompletedGame> cGames = new ArrayList<>();
                    for (int i = 0; i < list[0].size(); i++) {
                        cGames.add(list[0].get(i));
                    }
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //execute code on main thread
                                fillListItems(cGames);
                                adapter = new CompletedGameListAdapter(completedGameListItems, getContext());
                                recyclerView.setAdapter(adapter);
                                mProgressIndicator.setVisibility(View.GONE);
                                mLoadingText.setVisibility(View.GONE);
                                initializeSorters();
                            }
                        });
                    }catch(Exception e){

                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();


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

}
