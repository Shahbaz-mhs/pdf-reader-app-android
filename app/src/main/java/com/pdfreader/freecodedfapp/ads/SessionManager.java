package com.pdfreader.freecodedfapp.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class SessionManager {
    public static final String PREF_APP_RATED = "isAppRated";

    public Context mContext;
    public SharedPreferences mPrefs;
    public Editor editor;

    public SessionManager(Context context) {
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        editor = mPrefs.edit();
    }

    public boolean getBooleanData(String str) {
        return this.mPrefs.getBoolean(str, false);
    }

    public int getIntData(String str) {
        return this.mPrefs.getInt(str, -1);
    }

    public String getStringData(String str) {
        return this.mPrefs.getString(str, "");
    }

    public void logoutUser() {
        this.editor.clear();
        this.editor.commit();
    }

    public void setBooleanData(String str, Boolean bool) {
        this.editor.putBoolean(str, bool.booleanValue());
        this.editor.commit();
    }

    public void setIntData(String str, int i) {
        this.editor.putInt(str, i);
        this.editor.commit();
    }

    public void setStringData(String str, String str2) {
        this.editor.putString(str, str2);
        this.editor.commit();
    }

    public void saveBooleanValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return mPrefs.getBoolean(key, false);
    }

    public void saveStringValue(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return mPrefs.getString(key, "");
    }


}
