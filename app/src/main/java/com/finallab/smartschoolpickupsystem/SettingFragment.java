package com.finallab.smartschoolpickupsystem;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class SettingFragment extends PreferenceFragmentCompat {

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from the XML file
        setPreferencesFromResource(R.xml.preference, rootKey);

        // Access preferences
        SwitchPreferenceCompat enableFeaturePreference = findPreference("pref_enable_feature");
        EditTextPreference usernamePreference = findPreference("pref_username");

        // Example: Handle preference change event
        if (enableFeaturePreference != null) {
            enableFeaturePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = (Boolean) newValue;
                    // You can perform some action based on this preference change
                    Toast.makeText(getContext(), "Feature Enabled: " + isEnabled, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        if (usernamePreference != null) {
            usernamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String username = (String) newValue;
                    // Perform action based on username change
                    Toast.makeText(getContext(), "Username Changed: " + username, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    // Example: Retrieve preferences at runtime
    private void retrievePreferences() {
        // Get default shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get specific preferences
        boolean isFeatureEnabled = preferences.getBoolean("pref_enable_feature", false);
        String username = preferences.getString("pref_username", "");

        // Use preferences values (show in UI, etc.)
        Toast.makeText(getContext(), "Feature Enabled: " + isFeatureEnabled, Toast.LENGTH_SHORT).show();
        Toast.makeText(getContext(), "Username: " + username, Toast.LENGTH_SHORT).show();
    }
}
