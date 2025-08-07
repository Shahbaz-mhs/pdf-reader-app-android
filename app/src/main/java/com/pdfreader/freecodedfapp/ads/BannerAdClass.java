package com.pdfreader.freecodedfapp.ads;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.pdfreader.freecodedfapp.R;


public class BannerAdClass {
    private static final String TAG = "bannerads";
    private String google;
    private String facebook;

    private final Context context;
    private final LinearLayout adMainLayout;
    private FrameLayout adsContainer;
    private com.facebook.ads.AdView adViewfb;
    SessionManager sessionManager;

    public BannerAdClass(Context context, LinearLayout adMainLayout) {
        this.context = context;
        this.adMainLayout = adMainLayout;

        Log.d(TAG, "BannerAdClass: ");
        sessionManager = new SessionManager(context);
        if (!sessionManager.getBooleanData(Const.isVip)) {
            initAds();
        }
    }

    private void initAds() {


        AdView adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.admob_banner_ad_id));
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdListener(new AdListener() {

//            public void onAdFailedToLoad(int i) {
//                super.onAdFailedToLoad(i);
//                Log.d(TAG, "onAdFailedToLoad: google banner " + i);
//
//            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "onAdFailedToLoad: google banner " + loadAdError);

                initFacebook();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded: google banner");
                adMainLayout.removeAllViews();
                adMainLayout.addView(adView);
            }

        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void initFacebook() {
        adViewfb = new com.facebook.ads.AdView(context, context.getString(R.string.fb_banner_id), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        adViewfb.loadAd(adViewfb.buildLoadAdConfig().withAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "onError: facebook banner " + adError);

            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "onAdLoaded: facebook banner");
                adMainLayout.removeAllViews();
                adMainLayout.addView(adViewfb);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).build());

    }
}
