package com.pdfreader.freecodedfapp.helper;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pdfreader.freecodedfapp.R;

public class MaterialSearchView extends FrameLayout {

    public final String TAG = MaterialSearchView.class.getSimpleName();
    public AppCompatImageView clearSearch;
    public EditText editText;
    int cX;
    int cY;
    OnClickListener clearSearchListner = new OnClickListener() {
        public void onClick(View view) {
            MaterialSearchView.this.editText.setText("");
            MaterialSearchView.this.clearSearch.setVisibility(GONE);
        }
    };
    View parent;
    int radius;
    private AppCompatImageView closeSearch;
    private final Context context;
    OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
        public void onFocusChange(View view, boolean z) {
            if (z) {
                MaterialSearchView materialSearchView = MaterialSearchView.this;
                materialSearchView.showKeyboard(materialSearchView.editText);
            }
        }
    };
    private CharSequence mCurrentQuery;
    private OnQueryTextListener mOnQueryTextListener;
    private ConstraintLayout materialSearchContainer;
    OnClickListener closeListerner = new OnClickListener() {
        public void onClick(View view) {
            MaterialSearchView.this.closeSearch();
            Log.d(MaterialSearchView.this.TAG, "Search is closed");
        }
    };
    private Float parentHeight;
    private int parentWidth;

    public MaterialSearchView(Context context2) {
        super(context2);
        this.context = context2;
        init();
    }

    public MaterialSearchView(Context context2, AttributeSet attributeSet) {
        super(context2, attributeSet);
        this.context = context2;
        init();
    }

    public MaterialSearchView(Context context2, AttributeSet attributeSet, int i) {
        super(context2, attributeSet, i);
        this.context = context2;
        init();
    }

    public MaterialSearchView(Context context2, AttributeSet attributeSet, int i, int i2) {
        super(context2, attributeSet, i, i2);
        this.context = context2;
        init();
    }

    private void init() {
        LayoutInflater.from(this.context).inflate(R.layout.material_search_view, this, true);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.material_search_container);
        this.materialSearchContainer = constraintLayout;
        constraintLayout.setVisibility(8);
        this.parent = (View) this.materialSearchContainer.getParent();
        EditText editText2 = (EditText) this.materialSearchContainer.findViewById(R.id.edit_text_search);
        this.editText = editText2;
        editText2.setOnFocusChangeListener(this.focusChangeListener);
        this.closeSearch = (AppCompatImageView) this.materialSearchContainer.findViewById(R.id.action_close_search);
        this.clearSearch = (AppCompatImageView) this.materialSearchContainer.findViewById(R.id.action_clear_search);
        this.closeSearch.setOnClickListener(this.closeListerner);
        this.clearSearch.setOnClickListener(this.clearSearchListner);
        this.editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                MaterialSearchView.this.onTextChanged(charSequence);
            }
        });
    }

    public void clearFocus() {
        hideKeyboard(this);
        super.clearFocus();
        this.editText.clearFocus();
    }

    public void onTextChanged(CharSequence charSequence) {
        Editable text = this.editText.getText();
        this.mCurrentQuery = text;
        if (!TextUtils.isEmpty(text)) {
            this.clearSearch.setVisibility(0);
        } else {
            this.clearSearch.setVisibility(8);
        }
        OnQueryTextListener onQueryTextListener = this.mOnQueryTextListener;
        if (onQueryTextListener != null) {
            onQueryTextListener.onQueryTextChange(charSequence.toString());
        }
    }

    public void openSearch() {
        this.editText.setText("");
        this.editText.requestFocus();
        if (Build.VERSION.SDK_INT >= 21) {
            this.materialSearchContainer.setVisibility(0);
        } else {
            this.materialSearchContainer.setVisibility(0);
        }
    }

    public void closeSearch() {
        this.materialSearchContainer.setVisibility(8);
        this.editText.setText("");
        this.editText.clearFocus();
        hideKeyboard(this.editText);
    }

    public boolean isSearchOpen() {
        return this.materialSearchContainer.getVisibility() == 0;
    }

    private boolean isHardKeyboardAvailable() {
        return this.context.getResources().getConfiguration().keyboard != 1;
    }

    public void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= 10 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        if (!isHardKeyboardAvailable()) {
            ((InputMethodManager) view.getContext().getSystemService("input_method")).showSoftInput(view, 0);
        }
    }

    private void hideKeyboard(View view) {
        ((InputMethodManager) view.getContext().getSystemService("input_method")).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setOnQueryTextListener(OnQueryTextListener onQueryTextListener) {
        this.mOnQueryTextListener = onQueryTextListener;
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }
}
