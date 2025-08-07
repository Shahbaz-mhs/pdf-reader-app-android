package com.pdfreader.freecodedfapp.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SelectImagesAdapter extends RecyclerView.Adapter<SelectImagesAdapter.SelectPDFViewHolder> {

    private final String TAG = SelectImagesAdapter.class.getSimpleName();
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context mContext;
    private List<Uri> imageUris;
    private final OnImageSelectedListener onMultiSelectedImageListener;
    private final SparseBooleanArray selectedImages = new SparseBooleanArray();

    public SelectImagesAdapter(Context context, List<Uri> list) {
        this.imageUris = list;
        this.mContext = context;
        this.actionModeCallback = new ActionModeCallback();
        Context context2 = this.mContext;
        if (context2 instanceof OnImageSelectedListener) {
            this.onMultiSelectedImageListener = (OnImageSelectedListener) context2;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnImageSelectedListener");
    }

    public SelectPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SelectPDFViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_select_images_grid, viewGroup, false));
    }

    public void onBindViewHolder(final SelectPDFViewHolder selectPDFViewHolder, int i) {
        toggleSelectionBackground(selectPDFViewHolder, i);
        Picasso.get().load(this.imageUris.get(i)).fit().centerCrop().into((ImageView) selectPDFViewHolder.imageThumbnail);
        selectPDFViewHolder.imageThumbnail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (SelectImagesAdapter.this.actionMode == null) {
                    SelectImagesAdapter selectImagesAdapter = SelectImagesAdapter.this;
                    ActionMode unused = selectImagesAdapter.actionMode = ((AppCompatActivity) selectImagesAdapter.mContext).startSupportActionMode(SelectImagesAdapter.this.actionModeCallback);
                }
                SelectImagesAdapter.this.toggleSelection(selectPDFViewHolder.getAdapterPosition());
            }
        });
    }

    public void updateData(List<Uri> list) {
        this.imageUris = list;
        notifyDataSetChanged();
    }

    public void toggleSelection(int i) {
        if (this.selectedImages.get(i, false)) {
            this.selectedImages.delete(i);
        } else {
            this.selectedImages.put(i, true);
        }
        notifyItemChanged(i);
        int selectedItemCount = getSelectedItemCount();
        if (selectedItemCount == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        actionMode2.setTitle(selectedItemCount + " " + this.mContext.getString(R.string.selected));
        this.actionMode.invalidate();
    }

    private int getSelectedItemCount() {
        return this.selectedImages.size();
    }

    private List<Integer> getSelectedImages() {
        int size = this.selectedImages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedImages.keyAt(i)));
        }
        return arrayList;
    }

    public void clearSelection() {
        List<Integer> selectedImages2 = getSelectedImages();
        this.selectedImages.clear();
        for (Integer intValue : selectedImages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelected(int i) {
        return getSelectedImages().contains(Integer.valueOf(i));
    }

    private void toggleSelectionBackground(SelectPDFViewHolder selectPDFViewHolder, int i) {
        if (isSelected(i)) {
            selectPDFViewHolder.highlightSelectedItem.setVisibility(View.VISIBLE);
        } else {
            selectPDFViewHolder.highlightSelectedItem.setVisibility(View.INVISIBLE);
        }
    }

    public ArrayList<String> selectedImages() {
        List<Integer> selectedImages2 = getSelectedImages();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer intValue : selectedImages2) {
            arrayList.add(this.imageUris.get(intValue.intValue()).toString());
        }
        return arrayList;
    }

    public int getItemCount() {
        return this.imageUris.size();
    }

    public void multiSelectedPDF(ArrayList<String> arrayList) {
        OnImageSelectedListener onImageSelectedListener = this.onMultiSelectedImageListener;
        if (onImageSelectedListener != null) {
            onImageSelectedListener.onMultiSelectedPDF(arrayList);
        }
    }

    public interface OnImageSelectedListener {
        void onMultiSelectedPDF(ArrayList<String> arrayList);
    }

    public class SelectPDFViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout highlightSelectedItem;
        public AppCompatImageView imageThumbnail;

        public SelectPDFViewHolder(View view) {
            super(view);
            this.imageThumbnail = (AppCompatImageView) view.findViewById(R.id.image_thumb);
            this.highlightSelectedItem = (LinearLayout) view.findViewById(R.id.highlight_selected_item);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        private ActionModeCallback() {
            View decorView = ((Activity) SelectImagesAdapter.this.mContext).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = SelectImagesAdapter.this.mContext.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = SelectImagesAdapter.this.mContext.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.selected_pdfs, menu);
            if (Build.VERSION.SDK_INT >= 21) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int i = this.flags & -8193;
                    this.flags = i;
                    this.view.setSystemUiVisibility(i);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorFrom), Integer.valueOf(this.colorTo));
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) SelectImagesAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() != R.id.action_select) {
                return true;
            }
            SelectImagesAdapter selectImagesAdapter = SelectImagesAdapter.this;
            selectImagesAdapter.multiSelectedPDF(selectImagesAdapter.selectedImages());
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            SelectImagesAdapter.this.clearSelection();
            if (Build.VERSION.SDK_INT >= 21) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int i = this.flags | 8192;
                    this.flags = i;
                    this.view.setSystemUiVisibility(i);
                }
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(this.colorTo), Integer.valueOf(this.colorFrom));
                ofObject.setDuration(300);
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ((Activity) SelectImagesAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            ActionMode unused = SelectImagesAdapter.this.actionMode = null;
        }
    }
}
