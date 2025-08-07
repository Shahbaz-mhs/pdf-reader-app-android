package com.pdfreader.freecodedfapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.data.FileDiffCallback;
import com.pdfreader.freecodedfapp.fragments.BottomSheetDialogFragment;
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.utils.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class RecentPdfsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PDF = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final int AD_FREQUENCY = 12; // Show ad after every 12 items

    private final String TAG = RecentPdfsAdapter.class.getSimpleName();
    public boolean isGridViewEnabled;
    DbHelper dbHelper;
    private final OnHistoryPdfClickListener historyPdfClickListener;
    private final Context mContext;
    private List<PdfModel> pdfModelFiles;

    public RecentPdfsAdapter(List<PdfModel> list, Context context) {
        this.pdfModelFiles = list;
        this.mContext = context;
        this.dbHelper = DbHelper.getInstance(context);
        this.isGridViewEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.GRID_VIEW_ENABLED, false);
        if (context instanceof OnHistoryPdfClickListener) {
            this.historyPdfClickListener = (OnHistoryPdfClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnHistoryPdfClickListener");
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Check if this is an ad position
        if ((position + 1) % (AD_FREQUENCY + 1) == 0) {
            return VIEW_TYPE_AD;
        } else {
            return VIEW_TYPE_PDF;
        }
    }

    @Override
    public int getItemCount() {
        // Calculate the total items including ad slots
        int itemCount = pdfModelFiles.size();
        int adCount = itemCount / AD_FREQUENCY;
        return itemCount + adCount;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            // Inflate the ad layout
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_ad, viewGroup, false);
            return new AdViewHolder(view);
        } else {
            // Inflate the regular PDF item layout based on grid view setting
            View view;
            if (isGridViewEnabled) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf_grid, viewGroup, false);
            } else {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_pdf, viewGroup, false);
            }
            return new PdfViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
            // Bind the ad view (use an ad network SDK or your custom ad content here)
            //AdViewHolder adHolder = (AdViewHolder) holder;
            // Load and display your ad here

            // Load the ad into the AdView
            AdViewHolder adHolder = (AdViewHolder) holder;

            // Initialize the AdView
            AdView adView = adHolder.adView;

            // Load an ad into the AdView
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

        } else {
            int pdfPosition = position - (position / (AD_FREQUENCY + 1));
            PdfModel pdfModel = pdfModelFiles.get(pdfPosition);

            PdfViewHolder pdfViewHolder = (PdfViewHolder) holder;
            // Bind the PDF data as usual
            bindPdfView(pdfViewHolder, pdfModel, pdfPosition);
        }
    }

    private void bindPdfView(PdfViewHolder pdfViewHolder, PdfModel pdfModel, int position) {
        String absolutePath = pdfModel.getAbsolutePath();
        String name = pdfModel.getName();
        Long length = pdfModel.getLength();
        pdfViewHolder.pdfHeader.setText(name);
        pdfViewHolder.fileSize.setText(Formatter.formatShortFileSize(mContext, length));
        pdfViewHolder.lastModified.setText(Utils.formatDateToHumanReadable(pdfModel.getLastModified()));

        if (pdfModel.isStarred()) {
            pdfViewHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark2));
        }

        if (isGridViewEnabled) {
            Picasso.get().load(pdfModel.getThumbUri()).into(pdfViewHolder.pdfThumbnail);
        }

        pdfViewHolder.toggleStar.setOnClickListener(view -> toggleStar(pdfModel, pdfViewHolder, position));
        pdfViewHolder.pdfWrapper.setOnClickListener(view -> historyPdfClicked(position));
        pdfViewHolder.menu.setOnClickListener(view -> showBottomSheet(position));
    }

//    public void filter(ArrayList arrayList) {
//    }

    public void filter(ArrayList arrayList) {
        this.pdfModelFiles = arrayList;
        notifyDataSetChanged();
    }

//    public void updateData(List<PdfModel> historyPdfModels) {
//    }

    public void updateData(List<PdfModel> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.pdfModelFiles, list)).dispatchUpdatesTo((RecyclerView.Adapter) this);
        this.pdfModelFiles = list;
    }


    public interface OnHistoryPdfClickListener {
        void onHistoryPdfClicked(PdfModel pdfModel, int i);
    }

    private void historyPdfClicked(int i) {
        OnHistoryPdfClickListener onHistoryPdfClickListener = this.historyPdfClickListener;
        if (onHistoryPdfClickListener != null && i >= 0) {
            onHistoryPdfClickListener.onHistoryPdfClicked(this.pdfModelFiles.get(i), i);
        }
    }

    public void showBottomSheet(int i) {
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

    private void toggleStar(PdfModel pdfModel, PdfViewHolder pdfViewHolder, int position) {
        String path = pdfModel.getAbsolutePath();
        if (dbHelper.isStared(path)) {
            dbHelper.removeStaredPDF(path);
            pdfModel.setStarred(false);
            pdfViewHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark));
        } else {
            dbHelper.addStaredPDF(path);
            pdfModel.setStarred(true);
            pdfViewHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark2));
        }
        notifyItemChanged(position);
        EventBus.getDefault().post(new DataUpdatedEvent.PDFStaredEvent("recent"));
    }

    // ViewHolder for PDF items
    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        public TextView fileSize;
        public TextView lastModified;
        public TextView pdfHeader;
        public AppCompatImageView pdfThumbnail;
        public RelativeLayout pdfWrapper;
        public ImageView menu;
        public AppCompatImageView toggleStar;

        public PdfViewHolder(View view) {
            super(view);
            pdfThumbnail = view.findViewById(R.id.pdf_thumbnail);
            pdfHeader = view.findViewById(R.id.pdf_header);
            lastModified = view.findViewById(R.id.pdf_last_modified);
            fileSize = view.findViewById(R.id.pdf_file_size);
            toggleStar = view.findViewById(R.id.toggle_star);
            pdfWrapper = view.findViewById(R.id.pdf_wrapper);
            menu = view.findViewById(R.id.menu);
        }
    }

    // ViewHolder for Ad items
    public static class AdViewHolder extends RecyclerView.ViewHolder {
        public AdView adView;

        public AdViewHolder(View view) {
            super(view);
            adView = view.findViewById(R.id.adView);
        }
    }

}

