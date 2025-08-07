package com.pdfreader.freecodedfapp.activities;

import static com.pdfreader.freecodedfapp.R.id.swipe_refresh;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pdfreader.freecodedfapp.MyApp;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.DevicePdfsAdapter;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.fragments.RecentPdfFragment;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.helper.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements RecentPdfFragment.OnRecentPdfClickListener, DevicePdfsAdapter.OnPdfClickListener, MaterialSearchView.OnQueryTextListener {

    final String TAG = SearchActivity.class.getSimpleName();
    public FragmentActivity activityCompat;
    public DbHelper dbHelper;
    public RecyclerView devicePdfRecyclerView;
    public LinearLayout emptyStateDevice;
    public RelativeLayout infoTapMoreOptions;
    public boolean isGridViewEnabled;
    public LinearLayout loadingProgressBar;
    public DevicePdfsAdapter pdfsAdapter;
    public boolean showMoreOptionsTip;
    public SwipeRefreshLayout swipeRefresh;
    List<PdfModel> myPdfModels = new ArrayList();
    int numberOfColumns;
    SharedPreferences sharedPreferences;
    View.OnClickListener closeMoreInfo = new View.OnClickListener() {
        public void onClick(View view) {
            SearchActivity.this.infoTapMoreOptions.setVisibility(View.GONE);
            SearchActivity.this.infoTapMoreOptions.animate().translationY((float) (-SearchActivity.this.infoTapMoreOptions.getHeight())).alpha(0.0f).setListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    SearchActivity.this.infoTapMoreOptions.setVisibility(android.view.View.GONE);
                    SharedPreferences.Editor edit = SearchActivity.this.sharedPreferences.edit();
                    edit.putBoolean(Constants.MORE_OPTIONS_TIP, false);
                    edit.apply();
                }
            });
        }
    };
    private AppCompatImageView closeInfo;
    private boolean isFragmentVisibleToUser;
    private String mParam1;
    private String mParam2;
    private MaterialSearchView searchView;
    private Context context;
    private PdfModel pdfModel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApp.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_search);


        this.dbHelper = DbHelper.getInstance(this);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.sharedPreferences = defaultSharedPreferences;
        this.isGridViewEnabled = defaultSharedPreferences.getBoolean(Constants.GRID_VIEW_ENABLED, false);
        this.numberOfColumns = this.sharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, 2);
        this.showMoreOptionsTip = this.sharedPreferences.getBoolean(Constants.MORE_OPTIONS_TIP, true);


        MaterialSearchView materialSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        this.searchView = materialSearchView;
        materialSearchView.setOnQueryTextListener(this);

        materialSearchView.openSearch();
        setUserVisibleHint(true);


        // Find the back button in your top bar
        AppCompatImageView backButton = (AppCompatImageView) findViewById(R.id.action_close_search); // Ensure this ID matches your XML

// Set an OnClickListener to handle the back button press
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous activity
                finish(); // This will close the current activity and return to the previous one
            }
        });




        this.emptyStateDevice = (LinearLayout) findViewById(R.id.empty_state_device);
        this.devicePdfRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_device_pdf);
        this.loadingProgressBar = (LinearLayout) findViewById(R.id.pd);
        this.infoTapMoreOptions = (RelativeLayout) findViewById(R.id.info_tap_more_options);
        AppCompatImageView appCompatImageView = (AppCompatImageView) findViewById(R.id.info_close);
        this.closeInfo = appCompatImageView;
        appCompatImageView.setOnClickListener(this.closeMoreInfo);
        this.swipeRefresh = (SwipeRefreshLayout) findViewById(swipe_refresh);
        if (this.showMoreOptionsTip) {
            this.infoTapMoreOptions.setVisibility(View.VISIBLE);
        } else {
            this.infoTapMoreOptions.setVisibility(View.GONE);
        }
        if (this.isGridViewEnabled) {
            setupForGridView(this.activityCompat, this.devicePdfRecyclerView, this.numberOfColumns);
        } else {
            setupForListView(this.activityCompat, this.devicePdfRecyclerView);
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadpdfFiles();
            }
        }, 100);

    }

    private void loadpdfFiles() {
        myPdfModels = dbHelper.getAllPdfs();

        pdfsAdapter = new DevicePdfsAdapter(this);
        pdfsAdapter.addData(myPdfModels);
        devicePdfRecyclerView.setAdapter(pdfsAdapter);
        loadingProgressBar.setVisibility(View.GONE);
        Log.d(TAG, "loadPdfFiles: " + myPdfModels.size());
    }

    public void searchPDFFiles(String str) {
        ArrayList arrayList = new ArrayList();
        for (PdfModel next : this.myPdfModels) {
            if (next.getName().toLowerCase().contains(str.toLowerCase())) {
                arrayList.add(next);
            }
            this.pdfsAdapter.filter(arrayList);
        }
    }

//    public void searchPDFFiles(String str) {
//        ArrayList<PdfModel> filteredList = new ArrayList<>();
//        for (PdfModel pdfModel : myPdfModels) {
//            if (pdfModel.getName().toLowerCase().contains(str.toLowerCase())) {
//                filteredList.add(pdfModel);
//            }
//        }
//        pdfsAdapter.filter(filteredList);
//    }



    public void setupForListView(Context context, RecyclerView recyclerView) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.background_color_day_night));
        recyclerView.setPadding(0, 0, (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setupForGridView(Context context, RecyclerView recyclerView, int i) {
        Float valueOf = Float.valueOf(getResources().getDisplayMetrics().density);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, i, RecyclerView.VERTICAL, false);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.background_color_day_night));
        recyclerView.setPadding((int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 4.0f), (int) (valueOf.floatValue() * 80.0f));
        recyclerView.setLayoutManager(gridLayoutManager);
    }



    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }


    public void requestStoragePermission() {
        String[] strArr = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
        if (ActivityCompat.shouldShowRequestPermissionRationale(SearchActivity.this, "android.permission.READ_EXTERNAL_STORAGE")) {
            Toast.makeText(this.activityCompat, "Read storage permission is required to list files", Toast.LENGTH_SHORT).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(strArr, 1);
        }


        ActivityCompat.requestPermissions(
                SearchActivity.this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                },
                2
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

    // If you don't have access, launch a new activity to show the user the system's dialog
    // to allow access to the external storage
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    public void setUserVisibleHint(boolean z) {
        if (z) {
            this.isFragmentVisibleToUser = true;
            MaterialSearchView materialSearchView = this.searchView;
            if (materialSearchView != null) {
                materialSearchView.setOnQueryTextListener((MaterialSearchView.OnQueryTextListener) SearchActivity.this);
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

        searchPDFFiles(str);

        return true;
    }

    public boolean onQueryTextChange(String str) {
        searchPDFFiles(str);
        return true;
    }

    @Override
    public void onPdfClicked(PdfModel pdfModel, int i) {

        boolean isPdfClick = false;
        isPdfClick = true;
        this.pdfModel = pdfModel;

        // openFileInPdfView(pdfModel!!.absolutePath)
        Intent intent = new Intent(this, PDFViewerActivity.class);
        intent.putExtra(Constants.PDF_LOCATION, pdfModel.getAbsolutePath());
        startActivity(intent);

//        Toast.makeText(activityCompat, "Search Pdf CLicked", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRecentPdfClick(Uri uri) {

    }




}