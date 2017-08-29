package com.licktopia.thechampionofsudoku.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jeff on 1/15/17.
 */

public class SudokuSettings {
    SharedPreferences mSharedPreferences;

    public SudokuSettings(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getHighlightPreference(){
        return mSharedPreferences.getBoolean("Highlight", true);
    }

    public void setSharedPreference(boolean highlight){
        mSharedPreferences
                .edit()
                .putBoolean("Highlight", highlight)
                .apply();//asynchronous, commit is not
    }

    public boolean getSoundPreference(){
        return mSharedPreferences.getBoolean("Sounds", false);
    }
    public boolean getAutoSavePreference(){
        return mSharedPreferences.getBoolean("Autosave", true);
    }
    public boolean getLilNumPreference(){
        return mSharedPreferences.getBoolean("LilNumbers", false);
    }

    public void setSoundPreference(boolean sound){
        mSharedPreferences
                .edit()
                .putBoolean("Sounds", sound)
                .apply();//asynchronous, commit is not
    }


}
