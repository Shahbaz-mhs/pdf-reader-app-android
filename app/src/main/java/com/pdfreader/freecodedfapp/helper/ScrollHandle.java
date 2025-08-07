package com.pdfreader.freecodedfapp.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.Util;
import com.pdfreader.freecodedfapp.R;

public class ScrollHandle extends RelativeLayout implements com.github.barteksc.pdfviewer.scroll.ScrollHandle {

    private static final int DEFAULT_TEXT_SIZE = 16;
    private static final int HANDLE_LONG = 40;
    private static final int HANDLE_SHORT = 30;

    protected Context context;
    protected TextView textView;
    private float currentPos;
    private final Handler handler;
    private final Runnable hidePageScrollerRunnable;
    private final boolean inverted;
    private PDFView pdfView;
    private float relativeHandlerMiddle;

    public ScrollHandle(Context context) {
        this(context, false);
    }

    public ScrollHandle(Context context, boolean inverted) {
        super(context);
        this.relativeHandlerMiddle = 0.0f;
        this.handler = new Handler();
        this.hidePageScrollerRunnable = this::hide;
        this.context = context;
        this.inverted = inverted;
        this.textView = new TextView(context);
        setVisibility(View.INVISIBLE);
        setTextColor(-1);
        setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    public void setupLayout(PDFView pdfView) {
        Drawable drawable;
        int layoutGravity;
        int handleHeight = HANDLE_LONG;
        int handleWidth = HANDLE_SHORT;

        if (!pdfView.isSwipeVertical()) {
            if (this.inverted) {
                layoutGravity = ALIGN_PARENT_TOP;
                drawable = ContextCompat.getDrawable(this.context, R.drawable.default_scroll_handle_top);
            } else {
                layoutGravity = ALIGN_PARENT_BOTTOM;
                drawable = ContextCompat.getDrawable(this.context, R.drawable.default_scroll_handle_bottom);
            }
            handleHeight = HANDLE_SHORT;
            handleWidth = HANDLE_LONG;
        } else if (this.inverted) {
            layoutGravity = ALIGN_PARENT_LEFT;
            drawable = ContextCompat.getDrawable(this.context, R.drawable.default_scroll_handle_left);
        } else {
            layoutGravity = ALIGN_PARENT_RIGHT;
            drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_scroll_handle);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }

        LayoutParams handleParams = new LayoutParams(Util.getDP(this.context, handleWidth), Util.getDP(this.context, handleHeight));
        handleParams.addRule(layoutGravity);
        pdfView.addView(this, handleParams);

        LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textParams.addRule(CENTER_IN_PARENT, TRUE);
        addView(this.textView, textParams);

        this.pdfView = pdfView;
    }

    @Override
    public void destroyLayout() {
        this.pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            this.handler.removeCallbacks(this.hidePageScrollerRunnable);
        }
        setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
    }

    private void setPosition(float position) {
        if (!Float.isNaN(position) && !Float.isInfinite(position)) {
            int viewSize = pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth();
            float adjustedPosition = position - relativeHandlerMiddle;

            // Clamp position to within bounds
            adjustedPosition = Math.max(0, Math.min(adjustedPosition, viewSize - Util.getDP(this.context, HANDLE_SHORT)));

            if (pdfView.isSwipeVertical()) {
                setY(adjustedPosition);
            } else {
                setX(adjustedPosition);
            }

            calculateMiddle();
            show(); // Ensure visibility when position changes
            invalidate();
        }
    }


    private void calculateMiddle() {
        float start = pdfView.isSwipeVertical() ? getY() : getX();
        float size = pdfView.isSwipeVertical() ? getHeight() : getWidth();
        float viewSize = pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth();

        this.relativeHandlerMiddle = ((start + size / 2) / viewSize);
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 1000);
    }

    @Override
    public void setPageNum(int pageNum) {
        String pageText = String.valueOf(pageNum);
        if (!textView.getText().toString().equals(pageText)) {
            textView.setText(pageText);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setTextSize(int size) {
        textView.setTextSize(DEFAULT_TEXT_SIZE, size);
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPDFViewReady()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(hidePageScrollerRunnable);
                show(); // Ensure the handle is visible on touch
                return true;

            case MotionEvent.ACTION_MOVE:
                float position = pdfView.isSwipeVertical() ? event.getRawY() : event.getRawX();
                setPosition(position);

                // Calculate the relative position offset (between 0 and 1)
                float viewDimension = pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth();
                float offset = position / viewDimension;

                // Scroll the PDFView to the calculated position
                pdfView.setPositionOffset(offset, false);
                return true;

            case MotionEvent.ACTION_UP:
                hideDelayed(); // Hide the handle after some delay
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }


}
