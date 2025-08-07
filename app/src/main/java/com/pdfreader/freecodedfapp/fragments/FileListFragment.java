package com.pdfreader.freecodedfapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.freecodedfapp.MyApp;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.FileBrowserAdapter;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.pdfreader.freecodedfapp.utils.Utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileListFragment extends Fragment implements FileBrowserAdapter.OnPdfClickListener {
    private static final String FILE_PATH = "file_path";
    public String mFilePath;
    String THUMBNAILS_DIR;
    FileBrowserAdapter adapter;
    Context context;
    List<PdfModel> dirList = new ArrayList();
    LinearLayout emptyDirectory;
    FileFilter fileFilter = $$Lambda$FileListFragment$F29H4ZYSxUcIoqgeMprzN8rf9M.INSTANCE;
    boolean isGridViewEnabled;
    int numberOfColumns;
    ProgressBar progressBarlistDirectory;
    RecyclerView recyclerView;

    public static FileListFragment newInstance(String str) {
        FileListFragment fileListFragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FILE_PATH, str);
        fileListFragment.setArguments(bundle);
        return fileListFragment;
    }

    static /* synthetic */ int lambda$getFiles$0(PdfModel pdfModel, PdfModel pdfModel2) {
        if (pdfModel.isDirectory() && !pdfModel2.isDirectory()) {
            return -1;
        }
        if (pdfModel.isDirectory() || !pdfModel2.isDirectory()) {
            return pdfModel.getName().compareToIgnoreCase(pdfModel2.getName());
        }
        return 1;
    }

    static /* synthetic */ boolean lambda$new$1(File file) {
        return (file.isDirectory() && !file.isHidden() && hasPdfs(file.getAbsolutePath())) || TextUtils.equals(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString()).toLowerCase(), "pdf");
    }

    public static boolean hasPdfs(String str) {
        File[] listFiles = new File(str).listFiles();
        if (listFiles == null) {
            return false;
        }
        boolean z = false;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                z = hasPdfs(file.getAbsolutePath());
            } else if (TextUtils.equals(FilenameUtils.getExtension(file.getName()).toLowerCase(), "pdf")) {
                return true;
            }
        }
        return z;
    }

    public void onPdfClicked(PdfModel pdfModel) {
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
            this.mFilePath = getArguments().getString(FILE_PATH);
            this.THUMBNAILS_DIR = this.context.getCacheDir() + "/Thumbnails/";
            int i = Utils.isTablet(this.context) ? 6 : 3;
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            this.numberOfColumns = defaultSharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, i);
            this.isGridViewEnabled = defaultSharedPreferences.getBoolean(Constants.GRID_VIEW_ENABLED, false);
        }
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_browse_pdf);
        this.progressBarlistDirectory = (ProgressBar) view.findViewById(R.id.progress_bar_list_dir);
        this.emptyDirectory = (LinearLayout) view.findViewById(R.id.empty_state_directory);
        new ListDirectory().execute();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_file_list, viewGroup, false);
    }

    public void onAttach(Context context2) {
        super.onAttach(context2);
    }

    public void onDetach() {
        super.onDetach();
    }

    public List<PdfModel> getFiles(String str) {
        File[] listFiles;
        Uri uri;
        File file = new File(str);
        ArrayList arrayList = new ArrayList();
        if (file.isDirectory() && (listFiles = file.listFiles(this.fileFilter)) != null) {
            for (File file2 : listFiles) {
                if (!file2.isDirectory()) {
                    uri = Utils.getImageUriFromPath(this.THUMBNAILS_DIR + Utils.removeExtension(file2.getName()) + ".jpg");
                } else {
                    uri = null;
                }
                int size = file2.isDirectory() ? getFiles(file2.getAbsolutePath()).size() : 0;
                PdfModel pdfModel = new PdfModel();
                pdfModel.setName(file2.getName());
                pdfModel.setAbsolutePath(file2.getAbsolutePath());
                pdfModel.setPdfUri(file2.toString());
                pdfModel.setLength(Long.valueOf(file2.length()));
                pdfModel.setLastModified(Long.valueOf(file2.lastModified()));
                pdfModel.setThumbUri("uri");
                pdfModel.setDirectory(file2.isDirectory());
                pdfModel.setNumItems(size);
                arrayList.add(pdfModel);
            }
        }
        Collections.sort(arrayList, $$Lambda$FileListFragment$Wo1W1swgq93XtK0EGKc95SiGbg.INSTANCE);
        return arrayList;
    }

    public class ListDirectory extends AsyncTask<Void, Void, Void> {
        public ListDirectory() {
        }

        public void onPreExecute() {
            super.onPreExecute();
            FileListFragment.this.progressBarlistDirectory.setVisibility(0);
            if (FileListFragment.this.isGridViewEnabled) {
                FileListFragment.this.recyclerView.setBackgroundColor(FileListFragment.this.getResources().getColor(R.color.background_color_day_night));
                FileListFragment.this.recyclerView.setLayoutManager(new GridLayoutManager(FileListFragment.this.context, FileListFragment.this.numberOfColumns, 1, false));
                return;
            }
            FileListFragment.this.recyclerView.setBackgroundColor(FileListFragment.this.getResources().getColor(R.color.background_color_day_night));
            FileListFragment.this.recyclerView.setLayoutManager(new LinearLayoutManager(FileListFragment.this.context, 1, false));
        }

        public Void doInBackground(Void... voidArr) {
            FileListFragment fileListFragment = FileListFragment.this;
            fileListFragment.dirList = fileListFragment.getFiles(fileListFragment.mFilePath);
            FileListFragment fileListFragment2 = FileListFragment.this;
            fileListFragment2.adapter = new FileBrowserAdapter(fileListFragment2.context, FileListFragment.this.dirList);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            FileListFragment.this.progressBarlistDirectory.setVisibility(View.GONE);
            FileListFragment.this.recyclerView.setAdapter(FileListFragment.this.adapter);
            if (FileListFragment.this.dirList.size() == 0) {
                FileListFragment.this.emptyDirectory.setVisibility(View.VISIBLE);
            } else {
                FileListFragment.this.emptyDirectory.setVisibility(View.GONE);
            }
        }
    }
}
