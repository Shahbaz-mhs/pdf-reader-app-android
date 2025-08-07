package com.pdfreader.freecodedfapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.R;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.ContentsViewHolder> {

    private final String TAG = ContentsAdapter.class.getSimpleName();
    private final List<PdfDocument.Bookmark> bookmarks;
    private final Context mContext;
    private final OnContentClickedListener onContentClickedListener;

    public ContentsAdapter(Context context, List<PdfDocument.Bookmark> list) {
        this.bookmarks = list;
        this.mContext = context;
        if (context instanceof OnContentClickedListener) {
            this.onContentClickedListener = (OnContentClickedListener) context;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnContentClickedListener");
    }

    public ContentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ContentsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_contents, viewGroup, false));
    }

    public void onBindViewHolder(ContentsViewHolder contentsViewHolder, int i) {
        final PdfDocument.Bookmark bookmark = this.bookmarks.get(i);
        contentsViewHolder.contentTitle.setText(bookmark.getTitle());
        contentsViewHolder.contentPage.setText(this.mContext.getString(R.string.page) + " " + (bookmark.getPageIdx() + 1));
        contentsViewHolder.contentWrapper.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ContentsAdapter.this.contentClicked(bookmark);
            }
        });
    }

    public int getItemCount() {
        return this.bookmarks.size();
    }

    public void contentClicked(PdfDocument.Bookmark bookmark) {
        this.onContentClickedListener.onContentClicked(bookmark);
    }

    public interface OnContentClickedListener {
        void onContentClicked(PdfDocument.Bookmark bookmark);
    }

    public class ContentsViewHolder extends RecyclerView.ViewHolder {
        public TextView contentPage;
        public TextView contentTitle;
        public RelativeLayout contentWrapper;

        public ContentsViewHolder(View view) {
            super(view);
            this.contentTitle = (TextView) view.findViewById(R.id.content_title);
            this.contentPage = (TextView) view.findViewById(R.id.content_page);
            this.contentWrapper = (RelativeLayout) view.findViewById(R.id.content_wrapper);
        }
    }
}
