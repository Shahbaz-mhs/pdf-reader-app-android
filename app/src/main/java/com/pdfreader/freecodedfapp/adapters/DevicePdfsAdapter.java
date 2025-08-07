package com.pdfreader.freecodedfapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.data.FileDiffCallback;
import com.pdfreader.freecodedfapp.fragments.BottomSheetDialogFragment;
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DevicePdfsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PDF = 0;
    private static final int VIEW_TYPE_AD = 1;
    private static final int AD_FREQUENCY = 12; // Show an ad after every 12 items

    private final DbHelper dbHelper;
    private final Context mContext;
    private OnPdfClickListener pdfClickListener;
    private List<PdfModel> pdfModelFiles = new ArrayList<>();

    public DevicePdfsAdapter(Context context) {
        this.mContext = context;
        this.dbHelper = DbHelper.getInstance(this.mContext);

        // Check if the context implements OnPdfClickListener
        if (context instanceof OnPdfClickListener) {
            this.pdfClickListener = (OnPdfClickListener) context;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position > 0 && position % AD_FREQUENCY == 0) ? VIEW_TYPE_AD : VIEW_TYPE_PDF;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            View adView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ad, parent, false);
            return new AdViewHolder(adView);
            // above 2 lines commented by sbz for load pdf after every 1,3,6,9,etc.

//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pdf, parent, false);
//            return new PdfViewHolder(view);

        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pdf, parent, false);
            return new PdfViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
            AdViewHolder adHolder = (AdViewHolder) holder;
            AdRequest adRequest = new AdRequest.Builder().build();
            adHolder.adView.loadAd(adRequest);
        } else {
            PdfViewHolder pdfHolder = (PdfViewHolder) holder;
            PdfModel pdfModel = pdfModelFiles.get(getPdfPosition(position));

            // Set PDF details
            pdfHolder.pdfHeader.setText(pdfModel.getName());
            pdfHolder.fileSize.setText(Formatter.formatShortFileSize(mContext, pdfModel.getLength()));
            pdfHolder.lastModified.setText(Utils.formatDateToHumanReadable(pdfModel.getLastModified()));

            // Set star icon
            if (pdfModel.isStarred()) {
                pdfHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark2));
            } else {
                pdfHolder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark));
            }

            // Star click listener
            pdfHolder.toggleStar.setOnClickListener(v -> toggleStarStatus(pdfModel, pdfHolder, getPdfPosition(position)));

            // PDF item click listener
            pdfHolder.pdfWrapper.setOnClickListener(v -> {
                // Use the correct PDF index to avoid offset issues due to ads
                pdfClicked(getPdfPosition(position));
            });

            // Menu click listener to show the bottom sheet
            pdfHolder.menu.setOnClickListener(v -> showBottomSheet(pdfHolder, getPdfPosition(position)));
        }
    }

    @Override
    public int getItemCount() {
        return pdfModelFiles.size() + (pdfModelFiles.size() / AD_FREQUENCY);
    }

    private int getPdfPosition(int position) {
        return position - (position / AD_FREQUENCY);
    }

    private void toggleStarStatus(PdfModel pdfModel, PdfViewHolder holder, int position) {
        if (dbHelper.isStared(pdfModel.getAbsolutePath())) {
            dbHelper.removeStaredPDF(pdfModel.getAbsolutePath());
            pdfModel.setStarred(false);
            holder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark));
        } else {
            dbHelper.addStaredPDF(pdfModel.getAbsolutePath());
            pdfModel.setStarred(true);
            holder.toggleStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark2));
        }
        notifyItemChanged(position);
        EventBus.getDefault().post(new DataUpdatedEvent.PDFStaredEvent("device"));
    }

    private void pdfClicked(int position) {
        pdfClickListener.onPdfClicked(pdfModelFiles.get(position), position);
//        if (pdfClickListener != null && position >= 0) {
//            pdfClickListener.onPdfClicked(pdfModelFiles.get(position), position);
//        }
    }

    private void showBottomSheet(PdfViewHolder pdfViewHolder, int i) {
        String absolutePath = this.pdfModelFiles.get(i).getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("com.example.slimpdfapp.PDF_PATH", absolutePath);
        bundle.putBoolean(BottomSheetDialogFragment.FROM_RECENT, false);
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment(new BottomSheetDialogFragment.onClickLisner() {
            @Override
            public void onClick() {
                pdfModelFiles.remove(pdfModelFiles.get(i));
                notifyDataSetChanged();
            }

            @Override
            public void onClickRename(String name, String f$3) {
                pdfModelFiles.get(i).setAbsolutePath(f$3);
                pdfViewHolder.pdfHeader.setText(name);
                notifyDataSetChanged(); // Ensure UI is refreshed after rename
            }
        });
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(((AppCompatActivity) this.mContext).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    // Add this method after the addData method
    public void updateData(List<PdfModel> list) {
        DiffUtil.calculateDiff(new FileDiffCallback(this.pdfModelFiles, list)).dispatchUpdatesTo(this);
        this.pdfModelFiles = list;
    }

//    public void filter(ArrayList arrayList) {
//    }
//
    public void filter(List<PdfModel> list) {
        this.pdfModelFiles = list;
        notifyDataSetChanged();
    }

//    public void filter(ArrayList<PdfModel> filteredList) {
//        this.myPdfModels.clear();
//        this.myPdfModels.addAll(filteredList);
//        notifyDataSetChanged(); // Notify the adapter that the data has changed
//    }

//    public void addData(List<PdfModel> myPdfModels) {
//    }

    // Method to update data in the adapter
    public void addData(List<PdfModel> myPdfModels) {
        this.pdfModelFiles.clear(); // Clear the old data if you want to replace it
        this.pdfModelFiles.addAll(myPdfModels); // Add the new data
        notifyDataSetChanged(); // Refresh the adapter to display the new data
    }

    public interface OnPdfClickListener {
        void onPdfClicked(PdfModel pdfModel, int position);
    }

    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        public TextView pdfHeader, fileSize, lastModified;
        public AppCompatImageView toggleStar, pdfThumbnail;
        public RelativeLayout pdfWrapper;
        public ImageView menu;

        public PdfViewHolder(View view) {
            super(view);
            pdfHeader = view.findViewById(R.id.pdf_header);
            lastModified = view.findViewById(R.id.pdf_last_modified);
            fileSize = view.findViewById(R.id.pdf_file_size);
            toggleStar = view.findViewById(R.id.toggle_star);
            pdfWrapper = view.findViewById(R.id.pdf_wrapper);
            menu = view.findViewById(R.id.menu);
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        public AdView adView;

        public AdViewHolder(View view) {
            super(view);
            adView = view.findViewById(R.id.adView);
        }
    }




}
