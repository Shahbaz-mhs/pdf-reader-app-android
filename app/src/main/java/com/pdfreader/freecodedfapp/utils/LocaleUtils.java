package com.pdfreader.freecodedfapp.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.pdfreader.freecodedfapp.fragments.SettingsFragment;

import java.util.Locale;

public class LocaleUtils {
    public static void setUpLanguage(Context context) {
        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en");
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = new Locale(string);
        Locale.setDefault(locale);
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String keyToLanguage(Context r2, String r3) {

        throw new UnsupportedOperationException("Method not decompiled: LocaleUtils.keyToLanguage(android.content.Context, java.lang.String):java.lang.String");
    }
}
