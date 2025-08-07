package com.pdfreader.freecodedfapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.helper.AppOpenManager;
import com.onesignal.OneSignal;

public class MyApp extends Application {

    private static AppOpenManager appOpenManager;
    private static Context context;

    public static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

    public static Context getContext() {
        return context;
    }

    private static MyApp singleton = null;

    public static MyApp getInstance() {

        if(singleton == null)
        {
            singleton = new MyApp();
        }
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        singleton = this;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        appOpenManager = new AppOpenManager(this);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID);
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.apply();
    }

    public void attachBaseContext(Context context2) {
        super.attachBaseContext(context2);
    }
}
