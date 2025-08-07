package com.pdfreader.freecodedfapp.utils;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.pdfreader.freecodedfapp.R;

public class AdManagerInterstitial {
    public static final String TAG = AdManagerInterstitial.class.getSimpleName();
    public static boolean adShowed = false;
    /* access modifiers changed from: private */
    public static InterstitialAd mInterstitialAd;

    public static void initialize(Context context) {
        MobileAds.initialize(context);
    }

    public static void createAd(Context context) {
        InterstitialAd.load(context, context.getString(R.string.admob_interstitial_ad_id), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            public void onAdLoaded(InterstitialAd interstitialAd) {
                InterstitialAd unused = AdManagerInterstitial.mInterstitialAd = interstitialAd;
                Log.i(AdManagerInterstitial.TAG, "Interstitial ad loaded");
            }

            public void onAdFailedToLoad(LoadAdError loadAdError) {
                String str = AdManagerInterstitial.TAG;
                Log.d(str, "Error loading interstitial ad " + loadAdError.getMessage());
                InterstitialAd unused = AdManagerInterstitial.mInterstitialAd = null;
            }
        });
    }

    public static InterstitialAd getAd() {
        return mInterstitialAd;
    }
}
