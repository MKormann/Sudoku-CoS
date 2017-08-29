package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.licktopia.thechampionofsudoku.R;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;

public class AboutFragment extends Fragment {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_about_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_about, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_about_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_about_large, container, false);
        }

        return view;
    }

}
