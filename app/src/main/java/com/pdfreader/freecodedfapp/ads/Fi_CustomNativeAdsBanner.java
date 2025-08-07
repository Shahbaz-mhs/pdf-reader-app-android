package com.pdfreader.freecodedfapp.ads;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.ads.formats.NativeAdOptions.ADCHOICES_TOP_RIGHT;

//import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.pdfreader.freecodedfapp.R;

import java.util.ArrayList;
import java.util.List;

public class Fi_CustomNativeAdsBanner {


    private final SessionManager sessionManager;
    public boolean isEnabledAds = true;
    private String facebook;
    private String google;
    private String hendler;
    private final Context context;
    private final LinearLayout adsContainer;
    private final int admobNative;
    private final int facebookNative;
    private NativeAdsManager mNativeAdsManager;
    private AdLoader adLoader;
    private int index;


    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout adView;


    public Fi_CustomNativeAdsBanner(Context context, LinearLayout adsContainer, int admobNative, int facebookNative) {
        this.context = context;
        this.adsContainer = adsContainer;
        this.admobNative = admobNative;
        this.facebookNative = facebookNative;

        sessionManager = new SessionManager(context);
        if (!sessionManager.getBooleanData(Const.isVip)) {
            initAds();
        }

    }

    private void initAds() {
        AdLoader.Builder builder = null;
        builder = new AdLoader.Builder(context, context.getString(R.string.admob_native_ad_id));
        AdLoader adLoader = builder.forNativeAd(
                unifiedNativeAd -> {
                    Log.d(TAG, "onUnifiedNativeAdLoaded: ");

                    showAdmobAds(unifiedNativeAd);


                }).withAdListener(
                new AdListener() {

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e(TAG, "The previous native ad failed to load. Attempting to"
                                + " load another." + loadAdError.getCode());

                        loadFbNativeAds();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setRequestCustomMuteThisAd(true)
                        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                        .build()).build();



        adLoader.loadAds(new AdRequest.Builder().build(), 1);


    }


    private void loadFbNativeAds() {
        Log.d(TAG, "loadFbNativeAds: " + facebook);
        if (context != null && isEnabledAds) {
            NativeAdsManager mNativeAdsManager = new NativeAdsManager(context, context.getString(R.string.fb_native_id), 1);
            mNativeAdsManager.setListener(new NativeAdsManager.Listener() {
                @Override
                public void onAdsLoaded() {
                    Log.d(TAG, "onAdsLoaded: fb ");
                    if (isEnabledAds) {
                        showFaceBookAds(mNativeAdsManager);
                    }
                }


                @Override
                public void onAdError(AdError adError) {
                    Log.e(TAG, "AdError: " + adError.getErrorMessage());
                }
            });

            mNativeAdsManager.loadAds(NativeAdBase.MediaCacheFlag.ALL);
        }

    }

    private void showFaceBookAds(NativeAdsManager mNativeAdsManager) {
        NativeAd ad = mNativeAdsManager.nextNativeAd();
        NativeAdLayout nativeAdLayout = new NativeAdLayout(context);
        adsContainer.removeAllViews();
        if (ad != null) {
            View view = LayoutInflater.from(context).inflate(R.layout.fb_native_banner, null, false);
            nativeAdLayout.addView(view);
            NativeAdLayout linearLayout = view.findViewById(R.id.ad_choices_container);
            linearLayout.removeAllViews();
            MediaView mvAdMedia;
            MediaView ivAdIcon;
            TextView tvAdTitle;
            TextView tvAdBody;
            TextView btnAdCallToAction;
            mvAdMedia = (MediaView) view.findViewById(R.id.ad_media);
            tvAdTitle = (TextView) view.findViewById(R.id.ad_title);
            tvAdBody = (TextView) view.findViewById(R.id.ad_description);
            btnAdCallToAction = (TextView) view.findViewById(R.id.btnApply);

            tvAdTitle.setText(ad.getAdvertiserName());
            tvAdBody.setText(ad.getAdBodyText());
            btnAdCallToAction.setText("  " + ad.getAdCallToAction());
            btnAdCallToAction.setVisibility(
                    ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
            AdOptionsView adChoicesView = new AdOptionsView(context,
                    ad, nativeAdLayout);


            List<View> clickableViews = new ArrayList<>();

            clickableViews.add(mvAdMedia);
            clickableViews.add(btnAdCallToAction);
            ad.registerViewForInteraction(
                    view,
                    mvAdMedia,
                    clickableViews);

            linearLayout.addView(adChoicesView, 0);
            adsContainer.addView(nativeAdLayout, 0);
        }

    }

    private void showAdmobAds(com.google.android.gms.ads.nativead.NativeAd unifiedNativeAd) {
        if (context != null) {
            View view = LayoutInflater.from(context).inflate(R.layout.google_native_banner, null, false);
            NativeAdView adView = view.findViewById(R.id.ad_view);


            adView.setMediaView(adView.findViewById(R.id.ad_media));


            adView.setHeadlineView(adView.findViewById(R.id.ad_title));
            adView.setBodyView(adView.findViewById(R.id.ad_description));
            adView.setCallToActionView(adView.findViewById(R.id.btnApply));

            adsContainer.removeAllViews();
            adsContainer.addView(view, 0);
            populateNativeAdView(unifiedNativeAd, adView);
        }

    }

    private void populateNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {


        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());


        com.google.android.gms.ads.nativead.NativeAd.Image icon = nativeAd.getIcon();


        adView.setNativeAd(nativeAd);

    }

}
