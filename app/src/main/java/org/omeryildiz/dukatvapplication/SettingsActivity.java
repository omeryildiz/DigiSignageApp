package org.omeryildiz.dukatvapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    EditTextPreference videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment())
                .commit();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }
}