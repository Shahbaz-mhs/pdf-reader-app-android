package com.pdfreader.freecodedfapp.utils;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

public class FanAdManagerInterstitial {
    public static final String TAG = FanAdManagerInterstitial.class.getSimpleName();
    public static boolean adCreated = false;
    public static boolean adShowed = false;
    private static InterstitialAd interstitialAd;

    public static void initializeFanAds(Context context) {
        AudienceNetworkAds.initialize(context);
        interstitialAd = new InterstitialAd(context, "602982136828452_602985283494804");
    }

    public static void createAd() {
        adCreated = true;
        AdSettings.addTestDevice("0b819a63-e944-4060-be6c-28aede10f197");
        InterstitialAdListener r0 = new InterstitialAdListener() {
            public void onInterstitialDisplayed(Ad ad) {
                Log.e(FanAdManagerInterstitial.TAG, "Interstitial ad displayed.");
            }

            public void onInterstitialDismissed(Ad ad) {
                FanAdManagerInterstitial.createAd();
                Log.e(FanAdManagerInterstitial.TAG, "Interstitial ad dismissed.");
            }

            public void onError(Ad ad, AdError adError) {
                String str = FanAdManagerInterstitial.TAG;
                Log.e(str, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            public void onAdLoaded(Ad ad) {
                Log.d(FanAdManagerInterstitial.TAG, "Interstitial ad is loaded and ready to be displayed!");
            }

            public void onAdClicked(Ad ad) {
                Log.d(FanAdManagerInterstitial.TAG, "Interstitial ad clicked!");
            }

            public void onLoggingImpression(Ad ad) {
                Log.d(FanAdManagerInterstitial.TAG, "Interstitial ad impression logged!");
            }
        };
        InterstitialAd interstitialAd2 = interstitialAd;
//        interstitialAd2.loadAd(interstitialAd2.buildLoadAdConfig().withAdListener(r0).build());
    }

    public static InterstitialAd getAd() {
        InterstitialAd interstitialAd2 = interstitialAd;
        if (interstitialAd2 == null || !interstitialAd2.isAdLoaded()) {
            return null;
        }
        return interstitialAd;
    }
}
