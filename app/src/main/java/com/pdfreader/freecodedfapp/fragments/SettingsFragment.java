package com.pdfreader.freecodedfapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.utils.LocaleUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String AUTO_FULL_SCREEN_ENABLED = "prefs_auto_full_screen";
    public static final String KEY_PREFS_LANGUAGE = "prefs_language";
    public static final String KEY_PREFS_REMEMBER_LAST_PAGE = "prefs_remember_last_page";
    public static final String KEY_PREFS_STAY_AWAKE = "prefs_stay_awake";
    public static final String NIGHT_MODE_ENABLED = "prefs_night_mode_enabled";
    public static final String NIGHT_MODE_ENABLED_PDFVIEW = "prefs_night_mode_enabled_pdfview";
    public static final String SWIPE_HORIZONTAL_ENABLED = "prefs_swipe_horizontal_enabled";
    public Context context;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        /* JADX WARNING: Removed duplicated region for block: B:12:0x0027  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSharedPreferenceChanged(SharedPreferences r4, String r5) {

            throw new UnsupportedOperationException("Method not decompiled: SettingsFragment.AnonymousClass1.onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String):void");
        }
    };

    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.preferences);
        this.context = getContext();
        bindLanguagePreferenceSummaryToValue(findPreference(KEY_PREFS_LANGUAGE));
    }

    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
    }

    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setSummary((CharSequence) getPreferenceScreen().getSharedPreferences().getString(preference.getKey(), ""));
    }

    public void bindLanguagePreferenceSummaryToValue(Preference preference) {
        preference.setSummary((CharSequence) LocaleUtils.keyToLanguage(this.context, getPreferenceScreen().getSharedPreferences().getString(preference.getKey(), "en")));
    }
}
