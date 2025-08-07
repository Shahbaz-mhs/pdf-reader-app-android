package com.pdfreader.freecodedfapp.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.pdfreader.freecodedfapp.adapters.BookmarksAdapter;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.models.BookmarkModel;

import java.util.ArrayList;
import java.util.List;

public class StarredFragment extends Fragment {

    private static final String PDF_PATH = "pdf_path";
    private final String TAG = StarredFragment.class.getSimpleName();
    public LinearLayout emptyState;
    public String mPdfPath;
    BookmarksAdapter adapter;
    RecyclerView bookMarkRecyclerView;
    Context context;

    public static StarredFragment newInstance(String str) {
        StarredFragment bookmarksFragment = new StarredFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_PATH, str);
        bookmarksFragment.setArguments(bundle);
        return bookmarksFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (MyApp.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        this.context = getContext();
        if (getArguments() != null) {
            this.mPdfPath = getArguments().getString(PDF_PATH);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_starred, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.bookMarkRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_bookmarks);
        this.emptyState = (LinearLayout) view.findViewById(R.id.empty_state_bookmark);
        new LoadBookmarks().execute();
    }

    public class LoadBookmarks extends AsyncTask<Void, Void, Void> {
        List<BookmarkModel> bookmarkModels = new ArrayList();

        public LoadBookmarks() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            this.bookmarkModels = DbHelper.getInstance(StarredFragment.this.context).getBookmarks(StarredFragment.this.mPdfPath);
            StarredFragment bookmarksFragment = StarredFragment.this;
            bookmarksFragment.adapter = new BookmarksAdapter(bookmarksFragment.context, this.bookmarkModels, StarredFragment.this.emptyState);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (this.bookmarkModels.size() == 0) {
                StarredFragment.this.emptyState.setVisibility(0);
                return;
            }
            StarredFragment.this.bookMarkRecyclerView.setLayoutManager(new LinearLayoutManager(StarredFragment.this.context, 1, false));
            StarredFragment.this.bookMarkRecyclerView.setAdapter(StarredFragment.this.adapter);
            StarredFragment.this.emptyState.setVisibility(8);
        }
    }
}
