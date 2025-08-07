package com.pdfreader.freecodedfapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.MyApp;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.ContentsAdapter;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TableContentsFragment extends Fragment {
    public static final String SAVED_STATE = "prefs_saved_state";
    private static final String PDF_PATH = "pdf_path";
    final String TAG = TableContentsFragment.class.getSimpleName();
    public LinearLayout emptyState;
    public String mPdfPath;
    ContentsAdapter adapter;
    Context context;
    int lastFirstVisiblePosition = 0;
    SharedPreferences preferences;
    RecyclerView recyclerView;

    public static TableContentsFragment newInstance(String str) {
        TableContentsFragment tableContentsFragment = new TableContentsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        tableContentsFragment.setArguments(bundle);
        return tableContentsFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (MyApp.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Context context2 = getContext();
        this.context = context2;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context2);
        if (getArguments() != null) {
            this.mPdfPath = getArguments().getString(PDF_PATH);
        }
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_contents);
        this.emptyState = (LinearLayout) view.findViewById(R.id.empty_state_contents);
        new LoadTableOfContents().execute();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_table_contents, viewGroup, false);
    }

    public void onDestroy() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) this.recyclerView.getLayoutManager();
        if (linearLayoutManager != null) {
            this.preferences.edit().putInt(SAVED_STATE, linearLayoutManager.findFirstCompletelyVisibleItemPosition()).apply();
        }
        super.onDestroy();
    }

    public class LoadTableOfContents extends AsyncTask<Void, Void, Void> {
        List<PdfDocument.Bookmark> contents = new ArrayList();
        private PdfDocument pdfDocument;
        private PdfiumCore pdfiumCore;

        public LoadTableOfContents() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            TableContentsFragment tableContentsFragment = TableContentsFragment.this;
            tableContentsFragment.lastFirstVisiblePosition = tableContentsFragment.preferences.getInt(TableContentsFragment.SAVED_STATE, 0);
            try {
                this.pdfiumCore = new PdfiumCore(TableContentsFragment.this.context);
                PdfDocument newDocument = this.pdfiumCore.newDocument(TableContentsFragment.this.context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(TableContentsFragment.this.mPdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER));
                this.pdfDocument = newDocument;
                this.contents = this.pdfiumCore.getTableOfContents(newDocument);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (StackOverflowError e2) {
                e2.printStackTrace();
            }
            TableContentsFragment tableContentsFragment2 = TableContentsFragment.this;
            tableContentsFragment2.adapter = new ContentsAdapter(tableContentsFragment2.context, this.contents);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (this.contents.size() == 0) {
                TableContentsFragment.this.emptyState.setVisibility(0);
            } else {
                TableContentsFragment.this.emptyState.setVisibility(8);
            }
            TableContentsFragment.this.recyclerView.setLayoutManager(new LinearLayoutManager(TableContentsFragment.this.context, 1, false));
            TableContentsFragment.this.recyclerView.setAdapter(TableContentsFragment.this.adapter);
            TableContentsFragment.this.recyclerView.getLayoutManager().scrollToPosition(TableContentsFragment.this.lastFirstVisiblePosition);
        }
    }
}
