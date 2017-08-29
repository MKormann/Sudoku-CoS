package com.licktopia.thechampionofsudoku.Activities.UIActivities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.licktopia.thechampionofsudoku.R;

import static com.licktopia.thechampionofsudoku.Activities.BaseActivity.phoneSize;

public class InstructionsFragment extends Fragment {

    public static InstructionsFragment newInstance() {
        return new InstructionsFragment();
    }
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(phoneSize==1) {
            view = inflater.inflate(R.layout.activity_instructions_small, container, false);
        }
        else if(phoneSize == 2){
            view = inflater.inflate(R.layout.activity_instructions, container, false);
        }
        else if(phoneSize == 3){
            view = inflater.inflate(R.layout.activity_instructions_phablet, container, false);
        }
        else{
            view = inflater.inflate(R.layout.activity_instructions_large, container, false);
        }
        return view;
    }
}


