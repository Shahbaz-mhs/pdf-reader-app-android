package com.pdfreader.freecodedfapp.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.pdfreader.freecodedfapp.fragments.SettingsFragment;

public class ThemeUtils {
    public static void setTheme(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.NIGHT_MODE_ENABLED, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
        }
    }
}
