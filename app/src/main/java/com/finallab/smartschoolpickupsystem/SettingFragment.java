package com.finallab.smartschoolpickupsystem;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.widget.Toast;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load preferences from XML
        setPreferencesFromResource(R.xml.preference, rootKey);

        // Dark mode preference logic
        SwitchPreferenceCompat darkModePreference = findPreference("pref_dark_mode");

        if (darkModePreference != null) {
            // Initialize the switch based on saved preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            boolean isDarkModeEnabled = preferences.getBoolean("pref_dark_mode", false);

            darkModePreference.setChecked(isDarkModeEnabled);

            // Set up a listener for the switch toggle
            darkModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = (Boolean) newValue;

                    // Save the preference
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("pref_dark_mode", isEnabled);
                    editor.apply();

                    // Apply the theme
                    if (isEnabled) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        Toast.makeText(getContext(), "Dark Mode Enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        Toast.makeText(getContext(), "Dark Mode Disabled", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        // Username preference logic
//        EditTextPreference usernamePreference = findPreference("pref_username");
//
//        if (usernamePreference != null) {
//            // Set the current username as summary
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
//            String currentUsername = preferences.getString("pref_username", "No username set");
//            usernamePreference.setSummary(currentUsername);
//
//            // Set up a listener for username changes
//            usernamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    String newUsername = (String) newValue;
//
//                    // Update the summary with the new username
//                    usernamePreference.setSummary(newUsername);
//
//                    // Save the new username in preferences
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putString("pref_username", newUsername);
//                    editor.apply();
//
//                    // Optionally, show a toast for confirmation
//                    Toast.makeText(getContext(), "Username updated to: " + newUsername, Toast.LENGTH_SHORT).show();
//
//                    return true; // Save the new value
//                }
//            });
            // Notification preference logic
            SwitchPreferenceCompat notificationsPreference = findPreference("pref_notifications");

            if (notificationsPreference != null) {
                notificationsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (Boolean) newValue;
                    Toast.makeText(getContext(), "Notifications " + (isEnabled ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
                    return true; // Save the new value
                });
            }
        // Language preference logic
        ListPreference languagePreference = findPreference("pref_language");

        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String languageCode = (String) newValue;
                Toast.makeText(getContext(), "Language set to: " + languageCode, Toast.LENGTH_SHORT).show();
                // Optionally update app's language settings here
                return true; // Save the new value
            });
        }


    }
    }

