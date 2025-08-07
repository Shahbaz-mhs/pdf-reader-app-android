package com.pdfreader.freecodedfapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pdfreader.freecodedfapp.MyApp;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.StarredPDFAdapter;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.helper.MaterialSearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class StarredPdfFragment extends Fragment implements MaterialSearchView.OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final String TAG = StarredPdfFragment.class.getCanonicalName();
    public DbHelper dbHelper;
    public LinearLayout emptyState;
    public boolean isGridViewEnabled;
    public Context mContext;
    public LinearLayout progressBar;

    public RecyclerView starredPdfRecyclerView;
    public List<PdfModel> starredPdfModels = new ArrayList();
    public StarredPDFAdapter starredPdfsAdapter;
    public SwipeRefreshLayout swipeRefreshLayout;
    private boolean isFragmentVisibleToUser;
    private RecentPdfFragment.OnRecentPdfClickListener mListener;
    private String mParam1;
    private String mParam2;
    private int numberOfColumns;
    private MaterialSearchView searchView;
    private SharedPreferences sharedPreferences;

    public static RecentPdfFragment newInstance(String str, String str2) {
        RecentPdfFragment recentPdfFragment = new RecentPdfFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1, str);
        bundle.putString(ARG_PARAM2, str2);
        recentPdfFragment.setArguments(bundle);
        return recentPdfFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (MyApp.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        this.mContext = getContext();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.sharedPreferences = defaultSharedPreferences;
        this.isGridViewEnabled = defaultSharedPreferences.getBoolean(Constants.GRID_VIEW_ENABLED, false);
        this.numberOfColumns = this.sharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, 2);
        this.starredPdfsAdapter = new StarredPDFAdapter(this.mContext, this.starredPdfModels);
        this.dbHelper = DbHelper.getInstance(this.mContext);
        if (getArguments() != null) {
            this.mParam1 = getArguments().getString(ARG_PARAM1);
            this.mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void onResume() {
        super.onResume();
        new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.starredPdfRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_stared);
        this.emptyState = (LinearLayout) view.findViewById(R.id.empty_state_recent);
        this.searchView = (MaterialSearchView) getActivity().findViewById(R.id.search_view);
        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        this.searchView.setOnQueryTextListener(this);
        if (this.isGridViewEnabled) {
            setupForGridView(this.mContext, this.starredPdfRecyclerView, this.numberOfColumns);
        } else {
            setupForListView(this.mContext, this.starredPdfRecyclerView);
        }
        this.progressBar = (LinearLayout) view.findViewById(R.id.pd);
        new LoadStarredPfdFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public final void onRefresh() {
                StarredPdfFragment.this.lambda$onViewCreated$0$StarredFragment();
            }
        });
    }

    public /* synthetic */ void lambda$onViewCreated$0$StarredFragment() {
        new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_starred_pdf, viewGroup, false);
    }

    public void onButtonPressed(Uri uri) {
        RecentPdfFragment.OnRecentPdfClickListener onRecentPdfClickListener = this.mListener;
        if (onRecentPdfClickListener != null) {
            onRecentPdfClickListener.onRecentPdfClick(uri);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RecentPdfFragment.OnRecentPdfClickListener) {
            this.mListener = (RecentPdfFragment.OnRecentPdfClickListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnRecentPdfClickListener");
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void setUserVisibleHint(boolean z) {
        super.setUserVisibleHint(z);
        if (z) {
            this.isFragmentVisibleToUser = true;
            MaterialSearchView materialSearchView = this.searchView;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener(this);
                return;
            }
            return;
        }
        this.isFragmentVisibleToUser = false;
        MaterialSearchView materialSearchView2 = this.searchView;
        if (materialSearchView2 != null) {
            materialSearchView2.setOnQueryTextListener((MaterialSearchView.OnQueryTextListener) null);
        }
    }

    public boolean onQueryTextSubmit(String str) {
        String str2 = this.TAG;
        Log.d(str2, "Search query from recent fragment " + str);
        if (!this.isFragmentVisibleToUser) {
            return true;
        }
        searchPDFFiles(str);
        return true;
    }

    public boolean onQueryTextChange(String str) {
        String str2 = this.TAG;
        Log.d(str2, "Search query from recent fragment " + str);
        if (!this.isFragmentVisibleToUser) {
            return true;
        }
        searchPDFFiles(str);
        return true;
    }

    @Subscribe
    public void onRecentPdfDeleteEvent(DataUpdatedEvent.RecentPdfDeleteEvent recentPdfDeleteEvent) {
        Log.d(this.TAG, "onRecentPdfDeleteEvent from recent");
        new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPermanetlyDeleteEvent(DataUpdatedEvent.PermanetlyDeleteEvent permanetlyDeleteEvent) {
        Log.d(this.TAG, "onPermanetlyDeleteEvent from recent");
        new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPdfRenameEvent(DataUpdatedEvent.PdfRenameEvent pdfRenameEvent) {
        Log.d(this.TAG, "onPdfRenameEvent from recent");
        new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onPDFStaredEvent(DataUpdatedEvent.PDFStaredEvent pDFStaredEvent) {
        if (!TextUtils.equals(pDFStaredEvent.source, "starred")) {
            Log.d(this.TAG, "onPDFStaredEvent");
            new UpdateStarredPdfFiles().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Subscribe(sticky = true)
    public void onToggleGridViewEvent(DataUpdatedEvent.ToggleGridViewEvent toggleGridViewEvent) {
        Log.d(this.TAG, "onToggleGridViewEvent from recent fragment");
        this.isGridViewEnabled = this.sharedPreferences.getBoolean(Constants.GRID_VIEW_ENABLED, false);
        int i = this.sharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, 2);
        this.numberOfColumns = i;
        if (this.isGridViewEnabled) {
            setupForGridView(this.mContext, this.starredPdfRecyclerView, i);
        } else {
            setupForListView(this.mContext, this.starredPdfRecyclerView);
        }
        String str = this.TAG;
        Log.d(str, "Recent item size " + this.starredPdfModels.size());
        StarredPDFAdapter starredPDFAdapter = new StarredPDFAdapter(this.mContext, this.starredPdfModels);
        this.starredPdfsAdapter = starredPDFAdapter;
        this.starredPdfRecyclerView.setAdapter(starredPDFAdapter);
        this.starredPdfsAdapter.notifyDataSetChanged();
    }

    public void setupForGridView(Context context, RecyclerView recyclerView, int i) {
        float f = getResources().getDisplayMetrics().density;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, 1, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.background_color_day_night));
        int i2 = (int) (4.0f * f);
        recyclerView.setPadding(i2, i2, i2, (int) (f * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setupForListView(Context context, RecyclerView recyclerView) {
        float f = getResources().getDisplayMetrics().density;
        recyclerView.setBackgroundColor(getResources().getColor(R.color.background_color_day_night));
        recyclerView.setPadding(0, 0, (int) (4.0f * f), (int) (f * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void searchPDFFiles(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfModel next : this.starredPdfModels) {
            if (next.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(next);
            }
            this.starredPdfsAdapter.filter(arrayList);
        }
    }

    public class LoadStarredPfdFiles extends AsyncTask<Void, Void, Void> {
        public LoadStarredPfdFiles() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            StarredPdfFragment.this.progressBar.setVisibility(0);
        }

        public Void doInBackground(Void... voidArr) {
            DbHelper instance = DbHelper.getInstance(StarredPdfFragment.this.mContext);
            StarredPdfFragment.this.starredPdfModels.clear();
            List unused = StarredPdfFragment.this.starredPdfModels = instance.getStarredPdfs();
            StarredPdfFragment starredFragment = StarredPdfFragment.this;
            StarredPDFAdapter unused2 = starredFragment.starredPdfsAdapter = new StarredPDFAdapter(starredFragment.mContext, StarredPdfFragment.this.starredPdfModels);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            StarredPdfFragment.this.progressBar.setVisibility(8);
            if (StarredPdfFragment.this.starredPdfModels.isEmpty()) {
                StarredPdfFragment.this.emptyState.setVisibility(0);
            } else {
                StarredPdfFragment.this.emptyState.setVisibility(8);
            }
            StarredPdfFragment.this.starredPdfRecyclerView.setAdapter(StarredPdfFragment.this.starredPdfsAdapter);
        }
    }

    public class UpdateStarredPdfFiles extends AsyncTask<Void, Void, Void> {
        public UpdateStarredPdfFiles() {
        }

        public Void doInBackground(Void... voidArr) {
            if (StarredPdfFragment.this.starredPdfRecyclerView == null) {
                return null;
            }
            StarredPdfFragment starredFragment = StarredPdfFragment.this;
            List unused = starredFragment.starredPdfModels = starredFragment.dbHelper.getStarredPdfs();
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            if (StarredPdfFragment.this.starredPdfModels.isEmpty()) {
                StarredPdfFragment.this.emptyState.setVisibility(0);
            } else {
                StarredPdfFragment.this.emptyState.setVisibility(8);
            }
            StarredPdfFragment.this.swipeRefreshLayout.setRefreshing(false);
            StarredPdfFragment.this.starredPdfsAdapter.updateData(StarredPdfFragment.this.starredPdfModels);
        }
    }
}
