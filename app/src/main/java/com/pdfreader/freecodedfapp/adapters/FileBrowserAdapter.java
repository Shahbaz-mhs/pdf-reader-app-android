package com.pdfreader.freecodedfapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.data.FileDiffCallback;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.PdfViewHolder> {
    private final String TAG = FileBrowserAdapter.class.getSimpleName();
    public int folderColor;
    public boolean isGridViewEnabled;
    private final Context mContext;
    private final OnPdfClickListener pdfClickListener;
    private List<PdfModel> pdfModelFiles;

    public FileBrowserAdapter(Context context, List<PdfModel> list) {
        this.pdfModelFiles = list;
        this.mContext = context;
        this.isGridViewEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.GRID_VIEW_ENABLED, false);
        Context context2 = this.mContext;
        if (context2 instanceof OnPdfClickListener) {
            this.pdfClickListener = (OnPdfClickListener) context2;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnPdfClickListener");
    }

    public PdfViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isGridViewEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf_grid, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_browse_pdf, viewGroup, false);
        }
        return new PdfViewHolder(view);
    }

    public void onBindViewHolder(PdfViewHolder pdfViewHolder, int i) {
        PdfModel pdfModel = this.pdfModelFiles.get(i);
        String name = pdfModel.getName();
        Long length = pdfModel.getLength();
        if (this.isGridViewEnabled) {
            if (pdfModel.isDirectory()) {
                pdfViewHolder.folderGrid.setVisibility(View.INVISIBLE);
                pdfViewHolder.imageThumbGrid.setVisibility(View.GONE);
                pdfViewHolder.pdfHeaderGrid.setText(name);
                pdfViewHolder.fileSizeGrid.setText(pdfModel.getNumItems() + " " + this.mContext.getString(R.string.items));
            } else {
                pdfViewHolder.folderGrid.setVisibility(android.view.View.GONE);
                pdfViewHolder.imageThumbGrid.setVisibility(View.VISIBLE);
                Picasso.get().load(pdfModel.getThumbUri()).into((ImageView) pdfViewHolder.pdfThumbnail);
                pdfViewHolder.pdfHeader.setText(name);
                pdfViewHolder.lastModified.setText(Utils.formatDateToHumanReadable(pdfModel.getLastModified()));
                pdfViewHolder.fileSize.setText(Formatter.formatShortFileSize(this.mContext, length.longValue()));
            }
            pdfViewHolder.folderGrid.setOnClickListener(new View.OnClickListener() {
                public final /* synthetic */ PdfViewHolder f$1;

                {
                    this.f$1 = pdfViewHolder;
                }

                public final void onClick(View view) {
                    FileBrowserAdapter.this.lambda$onBindViewHolder$0$FileBrowserAdapter(this.f$1, view);
                }
            });
        } else {
            pdfViewHolder.pdfHeader.setText(name);
            if (pdfModel.isDirectory()) {
                pdfViewHolder.pdfIcon.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.ic_folder_closed));
                pdfViewHolder.lastModified.setText(pdfModel.getNumItems() + " " + this.mContext.getString(R.string.items));
            } else {
                pdfViewHolder.pdfIcon.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.ic_pdf_icon));
                pdfViewHolder.lastModified.setText(Utils.formatDateToHumanReadable(pdfModel.getLastModified()));
                pdfViewHolder.fileSize.setText(Formatter.formatShortFileSize(this.mContext, length.longValue()));
            }
        }
        pdfViewHolder.pdfWrapper.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ PdfViewHolder f$1;

            {
                this.f$1 = pdfViewHolder;
            }

            public final void onClick(View view) {
                FileBrowserAdapter.this.lambda$onBindViewHolder$1$FileBrowserAdapter(this.f$1, view);
            }
        });
    }

    public /* synthetic */ void lambda$onBindViewHolder$0$FileBrowserAdapter(PdfViewHolder pdfViewHolder, View view) {
        pdfClicked(pdfViewHolder.getAdapterPosition());
    }

    public /* synthetic */ void lambda$onBindViewHolder$1$FileBrowserAdapter(PdfViewHolder pdfViewHolder, View view) {
        pdfClicked(pdfViewHolder.getAdapterPosition());
    }

    public int getItemCount() {
        return this.pdfModelFiles.size();
    }

    private void pdfClicked(int i) {
        OnPdfClickListener onPdfClickListener = this.pdfClickListener;
        if (onPdfClickListener != null) {
            onPdfClickListener.onPdfClicked(this.pdfModelFiles.get(i));
        }
    }

    public void updateData(List<PdfModel> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.pdfModelFiles, list)).dispatchUpdatesTo((RecyclerView.Adapter) this);
        this.pdfModelFiles = list;
    }

    public interface OnPdfClickListener {
        void onPdfClicked(PdfModel pdfModel);
    }

    public class PdfViewHolder extends RecyclerView.ViewHolder {
        public TextView fileSize;
        public TextView fileSizeGrid;
        public ConstraintLayout folderGrid;
        public MaterialCardView imageThumbGrid;
        public TextView lastModified;
        public TextView pdfHeader;
        public TextView pdfHeaderGrid;
        public AppCompatImageView pdfIcon;
        public AppCompatImageView pdfThumbnail;
        public RelativeLayout pdfWrapper;
        private AppCompatImageView folderIcon;
        private TextView lastModifiedGrid;

        private PdfViewHolder(View view) {
            super(view);
            if (FileBrowserAdapter.this.isGridViewEnabled) {
                this.pdfThumbnail = (AppCompatImageView) view.findViewById(R.id.pdf_thumbnail);
                this.folderIcon = (AppCompatImageView) view.findViewById(R.id.folder_icon);
                this.pdfHeaderGrid = (TextView) view.findViewById(R.id.pdf_header_grid);
                this.lastModifiedGrid = (TextView) view.findViewById(R.id.pdf_last_modified_grid);
                this.fileSizeGrid = (TextView) view.findViewById(R.id.pdf_file_size_grid);
                this.pdfHeader = (TextView) view.findViewById(R.id.pdf_header);
                this.lastModified = (TextView) view.findViewById(R.id.pdf_last_modified);
                this.fileSize = (TextView) view.findViewById(R.id.pdf_file_size);
                FileBrowserAdapter.this.folderColor = Color.parseColor("#FFED8B28");
            } else {
                this.pdfIcon = (AppCompatImageView) view.findViewById(R.id.pdf_icon);
                this.pdfHeader = (TextView) view.findViewById(R.id.pdf_header);
                this.lastModified = (TextView) view.findViewById(R.id.pdf_last_modified);
                this.fileSize = (TextView) view.findViewById(R.id.pdf_file_size);
            }
            this.pdfWrapper = (RelativeLayout) view.findViewById(R.id.pdf_wrapper);
            this.imageThumbGrid = (MaterialCardView) view.findViewById(R.id.image_thumb_grid);
            this.folderGrid = (ConstraintLayout) view.findViewById(R.id.folder_grid);
        }
    }
}
