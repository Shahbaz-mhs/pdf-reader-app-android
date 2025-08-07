package com.pdfreader.freecodedfapp.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManagerRewardedVideo {
    public static final String TAG = AdManagerRewardedVideo.class.getSimpleName();
    /* access modifiers changed from: private */
    public static RewardedAd mRewardedAd;

    public static void createAndLoadRewardedAd(Activity activity) {
        RewardedAd.load((Context) activity, "ca-app-pub-6949253770172194/3015740456", new AdRequest.Builder().build(), (RewardedAdLoadCallback) new RewardedAdLoadCallback() {
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                RewardedAd unused = AdManagerRewardedVideo.mRewardedAd = null;
                String str = AdManagerRewardedVideo.TAG;
                Log.d(str, "Failed to load rewarded video. Error code " + loadAdError.getMessage());
            }

            public void onAdLoaded(RewardedAd rewardedAd) {
                RewardedAd unused = AdManagerRewardedVideo.mRewardedAd = rewardedAd;
                Log.d(AdManagerRewardedVideo.TAG, "Rewarded video loaded");
            }
        });
    }

    public static RewardedAd getAd() {
        return mRewardedAd;
    }
}
