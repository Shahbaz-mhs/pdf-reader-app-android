package com.pdfreader.freecodedfapp.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.InterstitialAd;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.pdfreader.freecodedfapp.MyApp;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.OrganizeImagesAdapter;
import com.pdfreader.freecodedfapp.ads.MyInterstitialAds;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.models.ImagePageModel;
import com.pdfreader.freecodedfapp.repository.ConfigRepository;
import com.pdfreader.freecodedfapp.utils.FanAdManagerInterstitial;
import com.pdfreader.freecodedfapp.utils.ImageUtils;
import com.pdfreader.freecodedfapp.utils.Utils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrganizeImagesActivity extends AppCompatActivity implements MyInterstitialAds.InterAdClickListner {

    final String TAG = OrganizeImagesActivity.class.getSimpleName();
    public OrganizeImagesAdapter adapter;
    public String allPdfDocuments;
    public AppCompatButton btnCancelProgress;
    public AppCompatImageView closeProgressView;
    public Context context;
    public TextView currentAction;
    public List<ImagePageModel> imagePageModels = new ArrayList();
    public ArrayList<String> imageUris;

    public RelativeLayout infoTapMoreOptions;
    public ConstraintLayout mProgressView;
    public ProgressBar progressBar;
    public ProgressBar progressBarLoadPagePreview;
    MyInterstitialAds myInterstitialAds;
    public RecyclerView recyclerView;
    public SharedPreferences sharedPreferences;
    TextView btnSave;
    View.OnClickListener closeMoreInfo = new View.OnClickListener() {
        public void onClick(View view) {
            OrganizeImagesActivity.this.infoTapMoreOptions.setVisibility(8);
            OrganizeImagesActivity.this.infoTapMoreOptions.animate().translationY((float) (-OrganizeImagesActivity.this.infoTapMoreOptions.getHeight())).alpha(0.0f).setListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    OrganizeImagesActivity.this.infoTapMoreOptions.setVisibility(8);
                    SharedPreferences.Editor edit = OrganizeImagesActivity.this.sharedPreferences.edit();
                    edit.putBoolean(Constants.ORGANIZE_PAGES_TIP, false);
                    edit.apply();
                }
            });
        }
    };
    boolean showOrganizePagesTip;
    private AppCompatButton btnOpenFile;
    private AppCompatImageView closeInfo;
    private TextView percent;
    private ConstraintLayout progressView;
    private TextView savedPath;
    private AppCompatImageView successIcon;
    private boolean isClickPdf = false;
    private Context context2;
    private String str;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (MyApp.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView((int) R.layout.activity_organize_images);
        myInterstitialAds = new MyInterstitialAds(this, this);
        this.allPdfDocuments = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_organize_pages));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.context = this;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.sharedPreferences = defaultSharedPreferences;
        this.showOrganizePagesTip = defaultSharedPreferences.getBoolean(Constants.ORGANIZE_PAGES_TIP, true);
        this.recyclerView = (RecyclerView) findViewById(R.id.recycler_view_organize_pages);
        this.progressBarLoadPagePreview = (ProgressBar) findViewById(R.id.progress_bar_organize_pages);
        this.btnSave = (TextView) findViewById(R.id.fab_save);
        this.progressView = (ConstraintLayout) findViewById(R.id.progress_view);
        this.infoTapMoreOptions = (RelativeLayout) findViewById(R.id.info_tap_more_options);
        AppCompatImageView appCompatImageView = (AppCompatImageView) findViewById(R.id.info_close);
        this.closeInfo = appCompatImageView;
        appCompatImageView.setOnClickListener(this.closeMoreInfo);
        this.imageUris = getIntent().getStringArrayListExtra(Constants.IMAGE_URIS);
        if (this.showOrganizePagesTip) {
            this.infoTapMoreOptions.setVisibility(0);
        } else {
            this.infoTapMoreOptions.setVisibility(8);
        }
        new LoadPageThumbnails(this.imageUris).execute();
        this.btnSave.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                OrganizeImagesActivity.this.lambda$onCreate$0$OrganizeImagesActivity(view);
            }
        });
    }

    public /* synthetic */ void lambda$onCreate$0$OrganizeImagesActivity(View view) {
        if (this.imagePageModels.size() >= 1) {
            showFileNameDialog();
        } else {
            Toast.makeText(this.context, R.string.select_at_least_one_image, 1).show();
        }
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        switch (i) {
            case 2:
                matrix.setScale(-1.0f, 1.0f);
                break;
            case 3:
                matrix.setRotate(180.0f);
                break;
            case 4:
                matrix.setRotate(180.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 5:
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 6:
                matrix.setRotate(90.0f);
                break;
            case 7:
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case 8:
                matrix.setRotate(-90.0f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return createBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Integer> getOrganizedPages(List<ImagePageModel> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            arrayList.add(Integer.valueOf(list.get(i).getPageNumber()));
        }
        return arrayList;
    }

    public void initializeProgressView() {
        this.percent = (TextView) this.mProgressView.findViewById(R.id.percent);
        this.currentAction = (TextView) this.mProgressView.findViewById(R.id.current_action);
        this.progressBar = (ProgressBar) this.mProgressView.findViewById(R.id.progress_bar);
        this.savedPath = (TextView) this.mProgressView.findViewById(R.id.saved_path);
        this.successIcon = (AppCompatImageView) this.mProgressView.findViewById(R.id.success_icon);
        this.btnOpenFile = (AppCompatButton) this.mProgressView.findViewById(R.id.open_file);
        this.btnCancelProgress = (AppCompatButton) this.mProgressView.findViewById(R.id.cancel_progress);
        this.closeProgressView = (AppCompatImageView) this.mProgressView.findViewById(R.id.close_progress_view);
    }

    public void processingFinished(Context context2, String str) {
        this.percent.setVisibility(4);
        this.progressBar.setVisibility(4);
        this.successIcon.setVisibility(0);
        this.closeProgressView.setVisibility(0);
        this.btnOpenFile.setVisibility(0);
        this.btnCancelProgress.setVisibility(8);
        this.savedPath.setText(context2.getString(R.string.saved_to) + " " + str);
    }

    public void closeProgressView(Context context2) {
        this.mProgressView.setVisibility(8);
        this.successIcon.setVisibility(8);
        this.btnOpenFile.setVisibility(8);
        this.closeProgressView.setVisibility(8);
        this.progressBar.setVisibility(0);
        this.percent.setVisibility(0);
        this.btnCancelProgress.setVisibility(0);
        this.progressBar.setProgress(0);
        this.percent.setText("0%");
        this.savedPath.setText("");
        Utils.clearLightStatusBar(context2);
    }

    public void closeProgressView(View view) {
        this.progressView.setVisibility(8);
        this.progressView.findViewById(R.id.success_icon).setVisibility(8);
        this.progressView.findViewById(R.id.open_file).setVisibility(8);
        this.progressView.findViewById(R.id.close_progress_view).setVisibility(8);
        this.progressView.findViewById(R.id.progress_bar).setVisibility(0);
        this.progressView.findViewById(R.id.percent).setVisibility(0);
        this.progressView.findViewById(R.id.cancel_progress).setVisibility(0);
        ((ProgressBar) this.progressView.findViewById(R.id.progress_bar)).setProgress(0);
        ((TextView) this.progressView.findViewById(R.id.percent)).setText("0%");
        ((TextView) this.progressView.findViewById(R.id.saved_path)).setText("");
        Utils.clearLightStatusBar(this);
    }

    public void updateProgressPercent(int i, int i2) {
        int i3 = ((int) (((float) i) * 100.0f)) / i2;
        this.percent.setText(i3 + "%");
        this.progressBar.setProgress(i);
    }

    public void showInterstialAd(final Context context2, final String str) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                InterstitialAd ad = FanAdManagerInterstitial.getAd();
                if (ad != null) {

                    ad.show();
                    return;
                }
                OrganizeImagesActivity.this.processingFinished(context2, str);
            }
        }, 1000);
    }

    public void setupOpenPath(Context context2, String str, String str2, boolean z) {
        this.btnOpenFile.setText(str);
        OrganizeImagesActivity.this.lambda$setupOpenPath$1$OrganizeImagesActivity(z, str2, context2);

    }

    public /* synthetic */ void lambda$setupOpenPath$1$OrganizeImagesActivity(boolean z, String str, Context context2) {
        if (z) {
            isClickPdf = true;
            this.context2 = context2;
            this.str = str;
            myInterstitialAds.showAds();
            return;
        }
    }

    public void showFileNameDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this.context);
        String str = "Image_PDF_" + System.currentTimeMillis();
        materialAlertDialogBuilder.setTitle((int) R.string.enter_file_name).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) null).setView((int) R.layout.dialog_edit_text).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) null);

        Drawable aa = ContextCompat.getDrawable(context, R.drawable.popup_bg);
        aa.setTint(ContextCompat.getColor(context, R.color.background_color_day_night));
        materialAlertDialogBuilder.setBackground(aa);

        AlertDialog create = materialAlertDialogBuilder.create();
        create.show();
        TextInputEditText textInputEditText = (TextInputEditText) create.findViewById(R.id.input_text);
        if (textInputEditText != null) {
            textInputEditText.setText(str);
            textInputEditText.setSelectAllOnFocus(true);
        }
        create.getButton(-1).setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ TextInputEditText f$1;
            public final /* synthetic */ AlertDialog f$2;

            {
                this.f$1 = textInputEditText;
                this.f$2 = create;
            }

            public final void onClick(View view) {
                OrganizeImagesActivity.this.lambda$showFileNameDialog$2$OrganizeImagesActivity(this.f$1, this.f$2, view);
            }
        });
    }

    public /* synthetic */ void lambda$showFileNameDialog$2$OrganizeImagesActivity(TextInputEditText textInputEditText, AlertDialog alertDialog, View view) {
        if (textInputEditText != null && textInputEditText.getText() != null) {
            String obj = textInputEditText.getText().toString();
            if (Utils.isFileNameValid(obj)) {
                alertDialog.dismiss();
                new SaveOrganizedPages(getOrganizedPages(this.imagePageModels), obj, this.progressView).execute();
                return;
            }
            textInputEditText.setError(getString(R.string.invalid_file_name));
        }
    }

    @Override
    public void onAdClosed() {
        if (isClickPdf) {
            isClickPdf = false;
            File file = new File(str);
            Intent intent = new Intent(context2, PDFViewerActivity.class);
            intent.putExtra(Constants.PDF_LOCATION, file.getAbsolutePath());
            String str2 = this.TAG;
            Log.d(str2, "Open PDF from location " + file.getAbsolutePath());
            context2.startActivity(intent);
        }
    }

    @Override
    public void onAdFail() {
        if (isClickPdf) {
            isClickPdf = false;
            File file = new File(str);
            Intent intent = new Intent(context2, PDFViewerActivity.class);
            intent.putExtra(Constants.PDF_LOCATION, file.getAbsolutePath());
            String str2 = this.TAG;
            Log.d(str2, "Open PDF from location " + file.getAbsolutePath());
            context2.startActivity(intent);
        }
    }

    public class LoadPageThumbnails extends AsyncTask<Void, Void, Void> {
        public LoadPageThumbnails(ArrayList<String> arrayList) {
            ArrayList unused = OrganizeImagesActivity.this.imageUris = arrayList;
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public Void doInBackground(Void... voidArr) {
            int i = 0;
            while (i < OrganizeImagesActivity.this.imageUris.size()) {
                int i2 = i + 1;
                OrganizeImagesActivity.this.imagePageModels.add(new ImagePageModel(i2, Uri.parse((String) OrganizeImagesActivity.this.imageUris.get(i))));
                i = i2;
            }
            OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
            OrganizeImagesAdapter unused = organizeImagesActivity.adapter = new OrganizeImagesAdapter(organizeImagesActivity.context, OrganizeImagesActivity.this.imagePageModels);
            return null;
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            OrganizeImagesActivity.this.recyclerView.setLayoutManager(new GridLayoutManager(OrganizeImagesActivity.this.context, Utils.isTablet(OrganizeImagesActivity.this.context) ? 6 : 3, 1, false));
            OrganizeImagesActivity.this.progressBarLoadPagePreview.setVisibility(8);
            OrganizeImagesActivity.this.recyclerView.setAdapter(OrganizeImagesActivity.this.adapter);
            OrganizeImagesActivity.this.btnSave.setVisibility(0);
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(15, 0) {
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
                }

                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
                    int adapterPosition = viewHolder.getAdapterPosition();
                    int adapterPosition2 = viewHolder2.getAdapterPosition();
                    OrganizeImagesActivity.this.imagePageModels.add(adapterPosition, (ImagePageModel) OrganizeImagesActivity.this.imagePageModels.remove(adapterPosition2));
                    OrganizeImagesActivity.this.adapter.notifyItemMoved(adapterPosition2, adapterPosition);
                    String str = OrganizeImagesActivity.this.TAG;
                    Log.d(str, "moved from " + adapterPosition + " to position " + adapterPosition2);
                    return true;
                }

                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    String str = OrganizeImagesActivity.this.TAG;
                    Log.d(str, "Page order after swap " + OrganizeImagesActivity.this.getOrganizedPages(OrganizeImagesActivity.this.imagePageModels).toString());
                }
            }).attachToRecyclerView(OrganizeImagesActivity.this.recyclerView);
        }
    }

    public class SaveOrganizedPages extends AsyncTask<Void, Integer, Void> {
        private String generatedPDFPath;
        private final String newFileName;
        private final int numPages;
        private final List<Integer> organizedPages;

        public SaveOrganizedPages(List<Integer> list, String str, ConstraintLayout constraintLayout) {
            this.organizedPages = list;
            this.newFileName = str;
            this.numPages = list.size();
            ConstraintLayout unused = OrganizeImagesActivity.this.mProgressView = constraintLayout;
            OrganizeImagesActivity.this.initializeProgressView();
            Utils.setLightStatusBar(OrganizeImagesActivity.this.context);
            OrganizeImagesActivity.this.btnCancelProgress.setOnClickListener(view -> {
                SaveOrganizedPages.this.cancel(true);
                OrganizeImagesActivity.this.closeProgressView(OrganizeImagesActivity.this.context);
            });
        }

        public void onPreExecute() {
            super.onPreExecute();
            OrganizeImagesActivity.this.progressBar.setMax(this.numPages);
            OrganizeImagesActivity.this.currentAction.setText(R.string.converting);
        }

        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(OrganizeImagesActivity.this.context).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            try {
                File file = new File(OrganizeImagesActivity.this.allPdfDocuments);
                this.generatedPDFPath = OrganizeImagesActivity.this.allPdfDocuments + this.newFileName + ".pdf";
                if (!file.exists()) {
                    file.mkdirs();
                }
                PDFBoxResourceLoader.init(OrganizeImagesActivity.this.context);
                PDDocument pDDocument = new PDDocument();
                int i = 0;
                while (i < this.numPages && !isCancelled()) {
                    String path = ((ImagePageModel) OrganizeImagesActivity.this.imagePageModels.get(i)).getImageUri().getPath();
                    Bitmap rotateBitmap = OrganizeImagesActivity.this.rotateBitmap(ImageUtils.getInstant().getCompressedBitmap(path), new ExifInterface(path).getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 0));
                    float width = (float) rotateBitmap.getWidth();
                    float height = (float) rotateBitmap.getHeight();
                    PDPage pDPage = new PDPage(new PDRectangle(width, height));
                    pDDocument.addPage(pDPage);
                    PDImageXObject createFromImage = JPEGFactory.createFromImage(pDDocument, rotateBitmap);
                    PDPageContentStream pDPageContentStream = new PDPageContentStream(pDDocument, pDPage, true, true, true);
                    pDPageContentStream.drawImage(createFromImage, 0.0f, 0.0f, width, height);
                    pDPageContentStream.close();
                    i++;
                    publishProgress(Integer.valueOf(i));
                }
                pDDocument.save(this.generatedPDFPath);
                pDDocument.close();
                if (z) {
                    Utils.generatePDFThumbnail(OrganizeImagesActivity.this.context, this.generatedPDFPath);
                }
                MediaScannerConnection.scanFile(OrganizeImagesActivity.this.context, new String[]{this.generatedPDFPath}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
                String str = OrganizeImagesActivity.this.TAG;
                Log.d(str, "Page order" + this.organizedPages);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            OrganizeImagesActivity.this.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            new ConfigRepository(OrganizeImagesActivity.this.getApplication()).incrementConfigValue(8);
            OrganizeImagesActivity.this.currentAction.setText(R.string.done);
            OrganizeImagesActivity.this.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            OrganizeImagesActivity organizeImagesActivity = OrganizeImagesActivity.this;
            organizeImagesActivity.showInterstialAd(organizeImagesActivity.context, OrganizeImagesActivity.this.allPdfDocuments);
            OrganizeImagesActivity organizeImagesActivity2 = OrganizeImagesActivity.this;
            organizeImagesActivity2.setupOpenPath(organizeImagesActivity2.context, OrganizeImagesActivity.this.context.getString(R.string.open_file), this.generatedPDFPath, true);
        }
    }
}
