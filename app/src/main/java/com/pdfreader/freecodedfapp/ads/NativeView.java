package com.pdfreader.freecodedfapp.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class NativeView extends LinearLayout {
    public NativeView(Context context) {
        super(context);
        initNative();

    }

    public NativeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initNative();

    }

    public NativeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initNative();

    }

    public void initNative() {
        new NativeAdClass(getContext(), this);
    }


}
