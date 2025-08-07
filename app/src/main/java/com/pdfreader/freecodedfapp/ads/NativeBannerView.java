package com.pdfreader.freecodedfapp.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.pdfreader.freecodedfapp.R;


public class NativeBannerView extends LinearLayout {
    public NativeBannerView(Context context) {
        super(context);
        initNative();

    }

    public NativeBannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initNative();

    }

    public NativeBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initNative();

    }

    public void initNative() {
        new Fi_CustomNativeAdsBanner(getContext(), this, R.layout.google_native_banner, R.layout.fb_native_banner);
    }


}
