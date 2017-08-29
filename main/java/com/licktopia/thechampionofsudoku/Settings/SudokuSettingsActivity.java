package com.licktopia.thechampionofsudoku.Settings;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.licktopia.thechampionofsudoku.R;

import static android.R.attr.id;

/**
 * Created by jeff on 1/15/17.
 */
public class SudokuSettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SudokuSettingsFragment())
                .commit();
        */
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (id == R.id.highlights){

        }

        finish();
        return super.onOptionsItemSelected(item);
    }
}
