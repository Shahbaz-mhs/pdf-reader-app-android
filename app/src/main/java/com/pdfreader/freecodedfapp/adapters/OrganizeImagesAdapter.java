package com.pdfreader.freecodedfapp.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.models.ImagePageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrganizeImagesAdapter extends RecyclerView.Adapter<OrganizeImagesAdapter.OrganizePagesViewHolder> {

    public final String TAG = OrganizeImagesAdapter.class.getSimpleName();
    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context mContext;
    private final List<ImagePageModel> imagePageModels;
    private final SparseBooleanArray selectedPages = new SparseBooleanArray();

    public OrganizeImagesAdapter(Context context, List<ImagePageModel> list) {
        this.imagePageModels = list;
        this.mContext = context;
        this.actionModeCallback = new ActionModeCallback();
    }

    public OrganizePagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new OrganizePagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_organize_pages_grid, viewGroup, false));
    }

    public void onBindViewHolder(final OrganizePagesViewHolder organizePagesViewHolder, int i) {
        ImagePageModel imagePageModel = this.imagePageModels.get(i);
        Picasso.get().load(imagePageModel.getImageUri()).fit().into((ImageView) organizePagesViewHolder.thumbnail);
        organizePagesViewHolder.pageNumber.setText(String.valueOf(imagePageModel.getPageNumber()));
        toggleSelectionBackground(organizePagesViewHolder, i);
        organizePagesViewHolder.pdfWrapper.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = organizePagesViewHolder.getAdapterPosition();
                if (OrganizeImagesAdapter.this.actionMode == null) {
                    OrganizeImagesAdapter organizeImagesAdapter = OrganizeImagesAdapter.this;
                    ActionMode unused = organizeImagesAdapter.actionMode = ((AppCompatActivity) organizeImagesAdapter.mContext).startSupportActionMode(OrganizeImagesAdapter.this.actionModeCallback);
                }
                OrganizeImagesAdapter.this.toggleSelection(adapterPosition);
                String access$700 = OrganizeImagesAdapter.this.TAG;
                Log.d(access$700, "Clicked position " + adapterPosition);
            }
        });
    }

    public void toggleSelection(int i) {
        if (this.selectedPages.get(i, false)) {
            this.selectedPages.delete(i);
        } else {
            this.selectedPages.put(i, true);
        }
        notifyItemChanged(i);
        int size = this.selectedPages.size();
        if (size == 0) {
            this.actionMode.finish();
            return;
        }
        ActionMode actionMode2 = this.actionMode;
        actionMode2.setTitle(size + " " + this.mContext.getString(R.string.selected));
        this.actionMode.invalidate();
    }

    private void toggleSelectionBackground(OrganizePagesViewHolder organizePagesViewHolder, int i) {
        if (isSelected(i)) {
            organizePagesViewHolder.highlightSelectedItem.setVisibility(View.VISIBLE);
        } else {
            organizePagesViewHolder.highlightSelectedItem.setVisibility(View.GONE);
        }
    }

    private boolean isSelected(int i) {
        return getSelectedPages().contains(Integer.valueOf(i));
    }

    public int getItemCount() {
        return this.imagePageModels.size();
    }

    public void clearSelection() {
        List<Integer> selectedPages2 = getSelectedPages();
        this.selectedPages.clear();
        for (Integer intValue : selectedPages2) {
            notifyItemChanged(intValue.intValue());
        }
    }

    public List<Integer> getSelectedPages() {
        int size = this.selectedPages.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedPages.keyAt(i)));
        }
        return arrayList;
    }

    private void removeItem(int i) {
        this.imagePageModels.remove(i);
        notifyItemRemoved(i);
    }

    public void removeItems(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer num, Integer num2) {
                return num2.intValue() - num.intValue();
            }
        });
        while (!list.isEmpty()) {
            if (list.size() == 1) {
                removeItem(list.get(0).intValue());
                list.remove(0);
            } else {
                int i = 1;
                while (list.size() > i && list.get(i).equals(Integer.valueOf(list.get(i - 1).intValue() - 1))) {
                    i++;
                }
                if (i == 1) {
                    removeItem(list.get(0).intValue());
                } else {
                    removeRange(list.get(i - 1).intValue(), i);
                }
                for (int i2 = 0; i2 < i; i2++) {
                    list.remove(0);
                }
            }
        }
    }

    private void removeRange(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            this.imagePageModels.remove(i);
        }
        notifyItemRangeRemoved(i, i2);
    }

    public class OrganizePagesViewHolder extends RecyclerView.ViewHolder {
        public TextView pageNumber;
        public RelativeLayout pdfWrapper;
        LinearLayout highlightSelectedItem;
        AppCompatImageView thumbnail;

        private OrganizePagesViewHolder(View view) {
            super(view);
            this.pdfWrapper = (RelativeLayout) view.findViewById(R.id.pdf_wrapper);
            this.pageNumber = (TextView) view.findViewById(R.id.page_number);
            this.thumbnail = (AppCompatImageView) view.findViewById(R.id.pdf_thumbnail);
            this.highlightSelectedItem = (LinearLayout) view.findViewById(R.id.highlight_selected_item);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        private ActionModeCallback() {
            View decorView = ((Activity) OrganizeImagesAdapter.this.mContext).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = OrganizeImagesAdapter.this.mContext.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = OrganizeImagesAdapter.this.mContext.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.activity_organize_pages_action_mode, menu);
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
                        ((Activity) OrganizeImagesAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() != R.id.action_delete) {
                return true;
            }
            OrganizeImagesAdapter organizeImagesAdapter = OrganizeImagesAdapter.this;
            organizeImagesAdapter.removeItems(organizeImagesAdapter.getSelectedPages());
            actionMode.finish();
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            OrganizeImagesAdapter.this.clearSelection();
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
                        ((Activity) OrganizeImagesAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            ActionMode unused = OrganizeImagesAdapter.this.actionMode = null;
        }
    }
}
