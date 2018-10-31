package com.splitv.railtime;

import org.holoeverywhere.preference.PreferenceActivity;
import android.os.Bundle;
 
public class PreferencesActivity extends PreferenceActivity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Update App so that it can use Androids latest utilities for dealing with Preferences
        addPreferencesFromResource(R.xml.preferences);
 
    }
}