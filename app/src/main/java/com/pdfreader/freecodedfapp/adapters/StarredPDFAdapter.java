package com.pdfreader.freecodedfapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.data.FileDiffCallback;
import com.pdfreader.freecodedfapp.fragments.BottomSheetDialogFragment;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.utils.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class StarredPDFAdapter extends RecyclerView.Adapter<StarredPDFAdapter.SharedPDFViewHolder> {

    private final String TAG = StarredPDFAdapter.class.getSimpleName();
    public boolean isGridViewEnabled;
    private final String THUMBNAILS_DIR;
    private final Context mContext;
    private List<PdfModel> pdfModelFiles;
    private final OnStaredPdfClickListener staredPdfClickListener;

    public StarredPDFAdapter(Context context, List<PdfModel> list) {
        this.pdfModelFiles = list;
        this.mContext = context;
        this.THUMBNAILS_DIR = context.getCacheDir() + "/Thumbnails/";
        this.isGridViewEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.GRID_VIEW_ENABLED, false);
        Context context2 = this.mContext;
        if (context2 instanceof OnStaredPdfClickListener) {
            this.staredPdfClickListener = (OnStaredPdfClickListener) context2;
            return;
        }
        throw new RuntimeException(this.mContext.toString() + " must implement OnStaredPdfClickListener");
    }

    public SharedPDFViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (this.isGridViewEnabled) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
        } else {
            Log.d(TAG, "onCreateViewHolder: ssdsds");
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
        }
        return new SharedPDFViewHolder(view);
    }

    public void onBindViewHolder(SharedPDFViewHolder sharedPDFViewHolder, int i) {
        PdfModel pdfModel = this.pdfModelFiles.get(sharedPDFViewHolder.getAdapterPosition());
        String absolutePath = pdfModel.getAbsolutePath();
        String name = pdfModel.getName();
        Long length = pdfModel.getLength();

        Log.d(TAG, "onBindViewHolder: " + name);
        DbHelper instance = DbHelper.getInstance(this.mContext);
        sharedPDFViewHolder.pdfHeader.setText(name);
        sharedPDFViewHolder.fileSize.setText(Formatter.formatShortFileSize(this.mContext, length.longValue()));
        sharedPDFViewHolder.lastModified.setText(Utils.formatDateToHumanReadable(pdfModel.getLastModified()));
        if (pdfModel.isStarred()) {
            sharedPDFViewHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.ic_bookmark2));
        }
        if (this.isGridViewEnabled) {
            Picasso.get().load(pdfModel.getThumbUri()).into((ImageView) sharedPDFViewHolder.pdfThumbnail);
        }
        sharedPDFViewHolder.toggleStar.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ DbHelper f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ SharedPDFViewHolder f$3;

            {
                this.f$1 = instance;
                this.f$2 = absolutePath;
                this.f$3 = sharedPDFViewHolder;
            }

            public final void onClick(View view) {
                StarredPDFAdapter.this.lambda$onBindViewHolder$0$StarredPDFAdapter(this.f$1, this.f$2, this.f$3, view);
            }
        });
        sharedPDFViewHolder.pdfWrapper.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ SharedPDFViewHolder f$1;

            {
                this.f$1 = sharedPDFViewHolder;
            }

            public final void onClick(View view) {
                StarredPDFAdapter.this.lambda$onBindViewHolder$1$StarredPDFAdapter(this.f$1, view);
            }
        });

        sharedPDFViewHolder.menu.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ SharedPDFViewHolder f$1;

            {
                this.f$1 = sharedPDFViewHolder;
            }

            public final void onClick(View view) {
                StarredPDFAdapter.this.lambda$onBindViewHolder$2$StarredPDFAdapter(this.f$1, view);
            }
        });
    }

    public /* synthetic */ void lambda$onBindViewHolder$0$StarredPDFAdapter(DbHelper dbHelper, String str, SharedPDFViewHolder sharedPDFViewHolder, View view) {
        if (dbHelper.isStared(str)) {
            dbHelper.removeStaredPDF(str);
            this.pdfModelFiles.remove(sharedPDFViewHolder.getAdapterPosition());
            notifyItemRemoved(sharedPDFViewHolder.getAdapterPosition());
        } else {
            dbHelper.addStaredPDF(str);
            sharedPDFViewHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.ic_bookmark2));
        }
        EventBus.getDefault().post(new DataUpdatedEvent.PDFStaredEvent("starred"));
    }

    public /* synthetic */ void lambda$onBindViewHolder$1$StarredPDFAdapter(SharedPDFViewHolder sharedPDFViewHolder, View view) {
        int adapterPosition = sharedPDFViewHolder.getAdapterPosition();
        staredPdfClicked(adapterPosition);
        String str = this.TAG;
        Log.d(str, "Pdf " + adapterPosition + " clicked");
    }

    public /* synthetic */ boolean lambda$onBindViewHolder$2$StarredPDFAdapter(SharedPDFViewHolder sharedPDFViewHolder, View view) {
        showBottomSheet(sharedPDFViewHolder.getAdapterPosition());
        return true;
    }

    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + pdfModelFiles.size());
        return this.pdfModelFiles.size();
    }

    public void filter(List<PdfModel> list) {
        this.pdfModelFiles = list;
        notifyDataSetChanged();
    }

    public void updateData(List<PdfModel> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.pdfModelFiles, list)).dispatchUpdatesTo((RecyclerView.Adapter) this);
        this.pdfModelFiles = list;
    }

    private void staredPdfClicked(int i) {
        OnStaredPdfClickListener onStaredPdfClickListener = this.staredPdfClickListener;
        if (onStaredPdfClickListener != null) {
            onStaredPdfClickListener.onStaredPdfClicked(this.pdfModelFiles.get(i), i);
        }
    }

    private void showBottomSheet(int i) {
        String absolutePath = this.pdfModelFiles.get(i).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("com.example.slimpdfapp.PDF_PATH", absolutePath);
        bundle.putBoolean(BottomSheetDialogFragment.FROM_RECENT, true);
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment(new BottomSheetDialogFragment.onClickLisner() {
            @Override
            public void onClick() {

            }

            @Override
            public void onClickRename(String name, String f$3) {


            }
        });
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) this.mContext).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public interface OnStaredPdfClickListener {
        void onStaredPdfClicked(PdfModel pdfModel, int i);
    }

    public class SharedPDFViewHolder extends RecyclerView.ViewHolder {
        public TextView fileSize;
        public TextView lastModified;
        public TextView pdfHeader;
        public AppCompatImageView pdfThumbnail;
        public RelativeLayout pdfWrapper;
        public AppCompatImageView toggleStar;
        public ImageView menu;

        private SharedPDFViewHolder(View view) {
            super(view);
            if (StarredPDFAdapter.this.isGridViewEnabled) {
                this.pdfThumbnail = (AppCompatImageView) view.findViewById(R.id.pdf_thumbnail);
            }
            this.pdfHeader = (TextView) view.findViewById(R.id.pdf_header);
            this.lastModified = (TextView) view.findViewById(R.id.pdf_last_modified);
            this.fileSize = (TextView) view.findViewById(R.id.pdf_file_size);
            this.toggleStar = (AppCompatImageView) view.findViewById(R.id.toggle_star);
            this.pdfWrapper = (RelativeLayout) view.findViewById(R.id.pdf_wrapper);
            this.menu = (ImageView) view.findViewById(R.id.menu);
        }
    }
}
