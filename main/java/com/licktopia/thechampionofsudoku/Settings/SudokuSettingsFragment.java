package com.licktopia.thechampionofsudoku.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.licktopia.thechampionofsudoku.Activities.UIActivities.CompletedGameFragment;
import com.licktopia.thechampionofsudoku.R;

/**
 * Created by jeff on 1/15/17.
 */

public class SudokuSettingsFragment extends PreferenceFragmentCompat {

    public static SudokuSettingsFragment newInstance() {
        return new SudokuSettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
