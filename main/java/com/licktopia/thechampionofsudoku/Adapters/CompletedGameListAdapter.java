package com.licktopia.thechampionofsudoku.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.licktopia.thechampionofsudoku.ListItems.CompletedGameListItem;
import com.licktopia.thechampionofsudoku.R;
import com.licktopia.thechampionofsudoku.DBOjects.CompletedGame;

import java.util.List;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;

/**
 * Created by do_de on 3/30/2017.
 */

public class CompletedGameListAdapter extends RecyclerView.Adapter<CompletedGameListAdapter.ViewHolder> {

    private List<CompletedGameListItem> completedGameListItems;
    private Context context;
    private String[] mColors = {"#1E91D6","#ea3f6a","#7D1538","#7c8597","#E18335","#4A2772","#F2CA08","#329d53"};
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient ddbClient;
    private DynamoDBMapper mapper;



    public CompletedGameListAdapter(List<CompletedGameListItem> completedGameListItem, Context context) {
        this.completedGameListItems = completedGameListItem;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(phoneSize==1) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.completed_game_list_item_small, parent, false);
        }
        else if(phoneSize == 2){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.completed_game_list_item, parent, false);
        }
        else if(phoneSize == 3){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.completed_game_list_item_phablet, parent, false);
        }
        else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.completed_game_list_item_large, parent, false);
        }

            return new ViewHolder(v);



    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CompletedGameListItem completedGameListItem = completedGameListItems.get(position);
        final long boardNumber = completedGameListItem.getPuzzleNumber();
        final String exactTime = completedGameListItem.getExactTime();
        final String gamerId = completedGameListItem.getGamerId();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("Puzzle number #" + boardNumber)
                        .setCancelable(true)
                        .setPositiveButton("Delete Record", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //delete from database here
                                deleteRecord(exactTime, gamerId);
                                boolean deleted = true;
                                if(deleted){
                                    completedGameListItems.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context,"Item successfully deleted.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(context,"Unable to delete the selected item.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("Keep Record", null)
                        .show();
            }
        });



        holder.tvGameScore.setText(String.format("%1$d", completedGameListItem.getRealScore()) + "XP");
        holder.tvGameTime.setText(completedGameListItem.getTime());
        holder.tvGameDate.setText(completedGameListItem.getDate());
        holder.tvGameSquares.setText(new StringBuilder().append(String.format("%,2.1f",completedGameListItem.getAccuracy() * 100)).append("%").toString());


        if(!completedGameListItem.isColor()) {
            String name = "rocket";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.rocket);
            holder.tvGameType.setImageDrawable(drawable);

        }
        else{
            String name = "peacock";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.peacock);
            holder.tvGameType.setImageDrawable(drawable);

        }


        if(completedGameListItem.getLevel()==0) {
            String name = "beginnericon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.beginnericon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[0]));

        }
        else if(completedGameListItem.getLevel()==1) {
            String name = "easyicon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.easyicon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[6]));
        }
        else if(completedGameListItem.getLevel()==2) {
            String name = "mediumicon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.mediumicon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[1]));
        }
        else if(completedGameListItem.getLevel()==3) {
            String name = "hardicon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.hardicon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[5]));
        }
        else if(completedGameListItem.getLevel()==4) {
            String name = "experticon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.experticon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[2]));
        }
        else //if(completedGameListItem.getLevel()==5) {
        {
            String name = "mastericon";

            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.mastericon);
            holder.tvGameLevel.setImageDrawable(drawable);
            holder.itemView.setBackgroundColor(Color.parseColor(mColors[4]));
        }



    }

    private void deleteRecord(String eTime, String gID){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-2:40802ef0-7537-4910-8442-31e2b30c5a7d", // Identity Pool ID
                Regions.US_EAST_2 // Region
        );

        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_2));

        if(isInternet()) {
            final CompletedGame cg = new CompletedGame();
            cg.setGamerID(gID);
            cg.setExactTime(eTime);



            // id field is null at this point
            Runnable runnable = new Runnable() {
                public void run() {
                    //DynamoDB calls go here
                    mapper.delete(cg);

                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
    }

    private boolean isInternet(){
        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }
    @Override
    public int getItemCount() {
        return completedGameListItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvGameScore;
        public TextView tvGameTime;
        public TextView tvGameDate;
        public TextView tvGameSquares;
        public ImageView tvGameType;
        public ImageView tvGameLevel;

        public ViewHolder(View itemView) {
            super(itemView);

            tvGameScore = (TextView)itemView.findViewById(R.id.game_score);
            tvGameTime = (TextView)itemView.findViewById(R.id.game_time);
            tvGameDate = (TextView)itemView.findViewById(R.id.game_date);
            tvGameSquares = (TextView)itemView.findViewById(R.id.game_squares);
            tvGameType = (ImageView)itemView.findViewById(R.id.game_type);
            tvGameLevel = (ImageView)itemView.findViewById(R.id.game_level);

        }

    }

}
