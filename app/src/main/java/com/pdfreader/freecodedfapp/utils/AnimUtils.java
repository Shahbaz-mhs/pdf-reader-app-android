package com.pdfreader.freecodedfapp.utils;

import android.animation.Animator;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.LinearInterpolator;

public class AnimUtils {
    private static final String TAG = AnimUtils.class.getSimpleName();

    public static void cycularReveal(View view) {
        int width = view.getWidth();
        int height = view.getHeight() / 2;
        String str = TAG;
        Log.d(str, "x cord " + width);
        String str2 = TAG;
        Log.d(str2, "y cord half " + height);
        int hypot = ((int) Math.hypot((double) width, (double) height)) + -24;
        String str3 = TAG;
        Log.d(str3, "Final radius " + hypot);
        if (Build.VERSION.SDK_INT >= 21) {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(view, width, height, 0.0f, (float) hypot);
            createCircularReveal.setInterpolator(new LinearInterpolator());
            createCircularReveal.setDuration(1000);
            view.setVisibility(0);
            createCircularReveal.start();
        }
    }
}
