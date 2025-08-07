package com.pdfreader.freecodedfapp.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public class BannerView extends LinearLayout {
    public BannerView(Context context) {
        super(context);
        initAds();
    }


    public BannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAds();
    }

    public BannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAds();
    }

    private void initAds() {
        new BannerAdClass(getContext(), this);
    }

}
