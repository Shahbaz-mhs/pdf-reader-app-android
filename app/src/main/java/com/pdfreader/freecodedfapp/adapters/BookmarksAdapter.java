package com.pdfreader.freecodedfapp.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.models.BookmarkModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarksViewHolder> {

    public ActionMode actionMode;
    public ActionModeCallback actionModeCallback;
    public Context mContext;
    private List<BookmarkModel> bookmarkModels;
    private final LinearLayout emptyStateView;
    private final OnBookmarkClickedListener onBookmarkClickedListener;
    private final SparseBooleanArray selectedBookmarks = new SparseBooleanArray();

    public BookmarksAdapter(Context context, List<BookmarkModel> list, LinearLayout linearLayout) {
        this.bookmarkModels = list;
        this.mContext = context;
        this.emptyStateView = linearLayout;
        this.actionModeCallback = new ActionModeCallback();
        Context context2 = this.mContext;
        if (context2 instanceof OnBookmarkClickedListener) {
            this.onBookmarkClickedListener = (OnBookmarkClickedListener) context2;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnBookmarkClickedListener");
    }

    public BookmarksViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new BookmarksViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_bookmark, viewGroup, false));
    }

    public void onBindViewHolder(final BookmarksViewHolder bookmarksViewHolder, int i) {
        final BookmarkModel bookmarkModel = this.bookmarkModels.get(i);
        bookmarksViewHolder.bookmarkTitle.setText(bookmarkModel.getTitle());
        bookmarksViewHolder.bookmarkPage.setText(this.mContext.getString(R.string.page) + " " + bookmarkModel.getPageNumber());
        toggleSelectionBackground(bookmarksViewHolder, i);
        bookmarksViewHolder.bookmarkWrapper.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (BookmarksAdapter.this.actionMode != null) {
                    BookmarksAdapter.this.toggleSelection(bookmarksViewHolder.getAdapterPosition());
                } else {
                    BookmarksAdapter.this.bookmarkSelected(bookmarkModel);
                }
            }
        });
        bookmarksViewHolder.bookmarkWrapper.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (BookmarksAdapter.this.actionMode == null) {
                    BookmarksAdapter bookmarksAdapter = BookmarksAdapter.this;
                    ActionMode unused = bookmarksAdapter.actionMode = ((AppCompatActivity) bookmarksAdapter.mContext).startSupportActionMode(BookmarksAdapter.this.actionModeCallback);
                }
                BookmarksAdapter.this.toggleSelection(bookmarksViewHolder.getAdapterPosition());
                return false;
            }
        });
    }

    public void updateData(List<BookmarkModel> list) {
        this.bookmarkModels = list;
        notifyDataSetChanged();
    }

    public void toggleSelection(int i) {
        if (this.selectedBookmarks.get(i, false)) {
            this.selectedBookmarks.delete(i);
        } else {
            this.selectedBookmarks.put(i, true);
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
        return this.selectedBookmarks.size();
    }

    private List<Integer> getSelectedImages() {
        int size = this.selectedBookmarks.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedBookmarks.keyAt(i)));
        }
        return arrayList;
    }

    public void clearSelection() {
        List<Integer> selectedImages = getSelectedImages();
        this.selectedBookmarks.clear();
        for (Integer intValue : selectedImages) {
            notifyItemChanged(intValue.intValue());
        }
    }

    private boolean isSelected(int i) {
        return getSelectedImages().contains(Integer.valueOf(i));
    }

    private void toggleSelectionBackground(BookmarksViewHolder bookmarksViewHolder, int i) {
        if (isSelected(i)) {
            bookmarksViewHolder.highlightSelectedItem.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.colorSelectedPDFs));
            return;
        }
        TypedValue typedValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843534, typedValue, true);
        bookmarksViewHolder.highlightSelectedItem.setBackgroundResource(typedValue.resourceId);
    }

    public int getItemCount() {
        return this.bookmarkModels.size();
    }

    public void bookmarkSelected(BookmarkModel bookmarkModel) {
        this.onBookmarkClickedListener.onBookmarkClicked(bookmarkModel);
    }

    public void deleteSelectedBookmarks(ActionMode actionMode2) {
        DbHelper instance = DbHelper.getInstance(this.mContext);
        List<Integer> selectedBookmarks2 = getSelectedBookmarks();
        int selectedItemCount = getSelectedItemCount();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < selectedItemCount; i++) {
            arrayList.add(this.bookmarkModels.get(selectedBookmarks2.get(i).intValue()));
        }
        removeItems(selectedBookmarks2);
        actionMode2.finish();
        instance.deleteBookmarks(arrayList);
    }

    private void removeItem(int i) {
        this.bookmarkModels.remove(i);
        setupEmptyState();
        notifyItemRemoved(i);
    }

    private void removeItems(List<Integer> list) {
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
            this.bookmarkModels.remove(i);
        }
        setupEmptyState();
        notifyItemRangeRemoved(i, i2);
    }

    private List<Integer> getSelectedBookmarks() {
        int size = this.selectedBookmarks.size();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            arrayList.add(Integer.valueOf(this.selectedBookmarks.keyAt(i)));
        }
        return arrayList;
    }

    private void setupEmptyState() {
        if (this.bookmarkModels.size() > 0) {
            this.emptyStateView.setVisibility(View.GONE);
        } else {
            this.emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    public interface OnBookmarkClickedListener {
        void onBookmarkClicked(BookmarkModel bookmarkModel);
    }

    public class BookmarksViewHolder extends RecyclerView.ViewHolder {
        public TextView bookmarkPage;
        public TextView bookmarkTitle;
        public RelativeLayout bookmarkWrapper;
        public LinearLayout highlightSelectedItem;

        public BookmarksViewHolder(View view) {
            super(view);
            this.bookmarkTitle = (TextView) view.findViewById(R.id.bookmark_title);
            this.bookmarkPage = (TextView) view.findViewById(R.id.bookmark_page);
            this.bookmarkWrapper = (RelativeLayout) view.findViewById(R.id.bookmark_wrapper);
            this.highlightSelectedItem = (LinearLayout) view.findViewById(R.id.highlight_selected_item);
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        int colorFrom;
        int colorTo;
        int flags;
        View view;

        private ActionModeCallback() {
            View decorView = ((Activity) BookmarksAdapter.this.mContext).getWindow().getDecorView();
            this.view = decorView;
            this.flags = decorView.getSystemUiVisibility();
            this.colorFrom = BookmarksAdapter.this.mContext.getResources().getColor(R.color.colorPrimaryDark);
            this.colorTo = BookmarksAdapter.this.mContext.getResources().getColor(R.color.colorDarkerGray);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.fragment_bookmarks, menu);
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
                        ((Activity) BookmarksAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
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
            BookmarksAdapter.this.deleteSelectedBookmarks(actionMode);
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            BookmarksAdapter.this.clearSelection();
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
                        ((Activity) BookmarksAdapter.this.mContext).getWindow().setStatusBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                ofObject.start();
            }
            ActionMode unused = BookmarksAdapter.this.actionMode = null;
        }
    }
}
