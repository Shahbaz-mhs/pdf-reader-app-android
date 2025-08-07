package com.pdfreader.freecodedfapp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.pdfreader.freecodedfapp.activities.PDFViewerActivity;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.models.CoordinateModel;
import com.pdfreader.freecodedfapp.repository.ConfigRepository;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission;
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException;
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDAction;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class PDFTools {
    /* access modifiers changed from: private */
    public static final String TAG = PDFTools.class.getSimpleName();
    public static PDFTools pDFTools = new PDFTools();
    /* access modifiers changed from: private */
    public AppCompatButton btnCancelProgress;
    /* access modifiers changed from: private */
    public AppCompatImageView closeProgressView;
    /* access modifiers changed from: private */
    public TextView currentAction;
    /* access modifiers changed from: private */
    public ConstraintLayout mProgressView;
    /* access modifiers changed from: private */
    public TextView percent;
    /* access modifiers changed from: private */
    public ProgressBar progressBar;
    private AppCompatButton btnOpenFile;
    private TextView description;
    private TextView savedPath;
    private AppCompatImageView successIcon;

    String allPdfPictureDir;

    /* access modifiers changed from: private */
    public static void deleteFiles(String str) {
        if (new File(str).exists()) {
            try {
                Runtime.getRuntime().exec("rm -r " + str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static /* synthetic */ void lambda$setupOpenPath$1(boolean z, String str, Context context, View view) {
        if (z) {
            File file = new File(str);
            Intent intent = new Intent(context, PDFViewerActivity.class);
            intent.putExtra(Constants.PDF_LOCATION, file.getAbsolutePath());
            String str2 = TAG;
            Log.d(str2, "Open PDF from location " + file.getAbsolutePath());
            context.startActivity(intent);
            return;
        }
//        Intent intent2 = new Intent(context, SelectPDFActivity.class);
//        intent2.putExtra(PDFToolsActivity.IS_DIRECTORY, true);
//        context.startActivity(intent2.putExtra(PDFToolsActivity.DIRECTORY_PATH, str));
    }

    static /* synthetic */ void lambda$openSpecifiedFolder$2(String str, Context context, View view) {
        Uri parse = Uri.parse(str);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(parse, "resource/folder");
        String str2 = TAG;
        Log.d(str2, "Open directory " + str);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Can't open directory", 1).show();
        }
    }

    static /* synthetic */ void lambda$openImageDirectory$3(Context context, String str, View view) {

    }

    /* access modifiers changed from: private */
    public void initializeProgressView(ConstraintLayout constraintLayout) {

        pDFTools.mProgressView = constraintLayout;

        this.percent = (TextView) constraintLayout.findViewById(R.id.percent);
        this.currentAction = (TextView) constraintLayout.findViewById(R.id.current_action);
        this.progressBar = (ProgressBar) constraintLayout.findViewById(R.id.progress_bar);
        this.description = (TextView) constraintLayout.findViewById(R.id.description);
        this.savedPath = (TextView) constraintLayout.findViewById(R.id.saved_path);
        this.successIcon = (AppCompatImageView) constraintLayout.findViewById(R.id.success_icon);
        this.btnOpenFile = (AppCompatButton) constraintLayout.findViewById(R.id.open_file);
        this.btnCancelProgress = (AppCompatButton) constraintLayout.findViewById(R.id.cancel_progress);
        this.closeProgressView = (AppCompatImageView) constraintLayout.findViewById(R.id.close_progress_view);
    }

    /* access modifiers changed from: private */
    @SuppressLint("WrongConstant")
    public void processingFinished(Context context, String str, String str2, String str3) {
        this.percent.setVisibility(4);
        this.progressBar.setVisibility(4);
        this.successIcon.setVisibility(0);
        this.closeProgressView.setVisibility(0);
        this.btnOpenFile.setVisibility(0);
        this.btnCancelProgress.setVisibility(8);
        String str4 = context.getString(R.string.saved_to) + " " + str3;
        if (!TextUtils.isEmpty(str)) {
            this.currentAction.setText(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            this.description.setText(str2);
            this.description.setVisibility(0);
        }
        this.savedPath.setText(str4);
    }

    /* access modifiers changed from: private */
    @SuppressLint("WrongConstant")
    public void closeProgressView(Context context) {
        this.mProgressView.setVisibility(8);
        this.successIcon.setVisibility(8);
        this.btnOpenFile.setVisibility(8);
        this.closeProgressView.setVisibility(8);
        this.description.setVisibility(8);
        this.progressBar.setVisibility(0);
        this.percent.setVisibility(0);
        this.btnCancelProgress.setVisibility(0);
        this.progressBar.setProgress(0);
        this.percent.setText("0%");
        this.description.setText("");
        this.savedPath.setText("");
        Utils.clearLightStatusBar(context);
    }

    public void showInterstialAd(Activity activity, String str, String str2, String str3) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public final /* synthetic */ Activity f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ String f$4;

            {
                this.f$1 = activity;
                this.f$2 = str;
                this.f$3 = str2;
                this.f$4 = str3;
            }

            public final void run() {
                pDFTools.lambda$showInterstialAd$0$PDFTools(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, 1000);
    }

    public /* synthetic */ void lambda$showInterstialAd$0$PDFTools(Activity activity, String str, String str2, String str3) {
        InterstitialAd ad = AdManagerInterstitial.getAd();
        if (ad != null) {
            final Activity activity2 = activity;
            final String str4 = str;
            final String str5 = str2;
            final String str6 = str3;
            ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    AdManagerInterstitial.createAd(activity2);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public final /* synthetic */ Activity f$1;
                        public final /* synthetic */ String f$2;
                        public final /* synthetic */ String f$3;
                        public final /* synthetic */ String f$4;

                        {
                            this.f$1 = activity2;
                            this.f$2 = str4;
                            this.f$3 = str5;
                            this.f$4 = str6;
                        }

                        public final void run() {
                            lambda$onAdDismissedFullScreenContent$1$PDFTools$1(this.f$1, this.f$2, this.f$3, this.f$4);
                        }
                    }, 800);
                }

                public /* synthetic */ void lambda$onAdDismissedFullScreenContent$1$PDFTools$1(Activity activity, String str, String str2, String str3) {
                    pDFTools.processingFinished(activity, str, str2, str3);
                    Snackbar.make((View) pDFTools.closeProgressView, (int) R.string.dont_like_ads, 4000).setAction((int) R.string.remove, (View.OnClickListener) new View.OnClickListener() {
                        public final /* synthetic */ Activity f$0;

                        {
                            this.f$0 = activity;
                        }

                        public final void onClick(View view) {
                            Utils.showSubscriptionOptions(this.f$0);
                        }
                    }).show();
                }
            });
            ad.show(activity);
            return;
        }
        processingFinished(activity, str, str2, str3);
    }

    /* access modifiers changed from: private */
    public void updateProgressPercent(int i, int i2) {
        try {
            int i3 = ((int) (((float) i) * 100.0f)) / i2;
            this.percent.setText(i3 + "%");
            this.progressBar.setProgress(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void setupOpenPath(Context context, String str, String str2, boolean z) {
        this.btnOpenFile.setText(str);
        this.btnOpenFile.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ boolean f$0;
            public final /* synthetic */ String f$1;
            public final /* synthetic */ Context f$2;

            {
                this.f$0 = z;
                this.f$1 = str2;
                this.f$2 = context;
            }

            public final void onClick(View view) {
                PDFTools.lambda$setupOpenPath$1(this.f$0, this.f$1, this.f$2, view);
            }
        });
    }

    public void openSpecifiedFolder(Context context, String str, String str2) {
        this.btnOpenFile.setText(str);
        this.btnOpenFile.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ Context f$1;

            {
                this.f$0 = str2;
                this.f$1 = context;
            }

            public final void onClick(View view) {
                PDFTools.lambda$openSpecifiedFolder$2(this.f$0, this.f$1, view);
            }
        });
    }

    public void openImageDirectory(Context context, String str, String str2) {
        this.btnOpenFile.setText(str);
        this.btnOpenFile.setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ Context f$0;
            public final /* synthetic */ String f$1;

            {
                this.f$0 = context;
                this.f$1 = str2;
            }

            public final void onClick(View view) {
                PDFTools.lambda$openImageDirectory$3(this.f$0, this.f$1, view);
            }
        });
    }

    public void removeProgressBarIndeterminate(Context context, ProgressBar progressBar2) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public final /* synthetic */ ProgressBar f$0;

            {
                this.f$0 = progressBar2;
            }

            public final void run() {
                this.f$0.setIndeterminate(false);
            }
        });
    }

    public CoordinateModel calculatePageNumberPositon(PDRectangle pDRectangle, int i, float f, float f2, int i2) {
        if (i == 0) {
            return setPagenumberTop(pDRectangle, f, f2, i2);
        }
        return setPagenumberBottom(pDRectangle, f, i2);
    }

    public CoordinateModel setPagenumberBottom(PDRectangle pDRectangle, float f, int i) {
        CoordinateModel coordinateModel = new CoordinateModel();
        float lowerLeftX = pDRectangle.getLowerLeftX();
        float lowerLeftY = pDRectangle.getLowerLeftY();
        if (i == 0) {
            float f2 = (float) 30;
            return new CoordinateModel(lowerLeftX + f2, lowerLeftY + f2);
        } else if (i == 1) {
            return new CoordinateModel(lowerLeftX + ((pDRectangle.getWidth() - f) / 2.0f), lowerLeftY + ((float) 30));
        } else {
            if (i != 2) {
                return coordinateModel;
            }
            float f3 = (float) 30;
            return new CoordinateModel(((lowerLeftX + pDRectangle.getWidth()) - f) - f3, lowerLeftY + f3);
        }
    }

    public CoordinateModel setPagenumberTop(PDRectangle pDRectangle, float f, float f2, int i) {
        CoordinateModel coordinateModel = new CoordinateModel();
        float lowerLeftX = pDRectangle.getLowerLeftX();
        float lowerLeftY = pDRectangle.getLowerLeftY();
        if (i == 0) {
            float f3 = (float) 30;
            return new CoordinateModel(lowerLeftX + f3, ((lowerLeftY + pDRectangle.getHeight()) - f2) - f3);
        } else if (i == 1) {
            return new CoordinateModel(lowerLeftX + ((pDRectangle.getWidth() - f) / 2.0f), ((lowerLeftY + pDRectangle.getHeight()) - f2) - ((float) 30));
        } else {
            if (i != 2) {
                return coordinateModel;
            }
            float width = (lowerLeftX + pDRectangle.getWidth()) - f;
            float f4 = (float) 30;
            return new CoordinateModel(width - f4, ((lowerLeftY + pDRectangle.getHeight()) - f2) - f4);
        }
    }

    public String getFormattedNumber(int i, int i2) {
        if (i2 == 1) {
            return NumberUtils.toRoman(i);
        }
        return String.valueOf(i);
    }

    public void UnProtectDocument(final Context context, final String str, final ConstraintLayout constraintLayout) {
        new AsyncTask<Void, Void, Void>() {
            PDDocument doc;
            final ProgressDialog progressDialog = new ProgressDialog(context);

            /* access modifiers changed from: protected */
            public void onPreExecute() {
                super.onPreExecute();
                this.progressDialog.setMessage(context.getString(R.string.loading_wait));
                this.progressDialog.show();
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                PDFBoxResourceLoader.init(context);
                try {
                    this.doc = PDDocument.load(new File(str), "");
                    return null;
                } catch (Exception e) {
                    if (e instanceof InvalidPasswordException) {

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public final /* synthetic */ Context f$1;
                            public final /* synthetic */ String f$2;
                            public final /* synthetic */ ConstraintLayout f$3;

                            {
                                this.f$1 = context;
                                this.f$2 = str;
                                this.f$3 = constraintLayout;
                            }

                            public final void run() {
                                lambda$doInBackground$0$PDFTools$2(this.f$1, this.f$2, this.f$3);
                            }
                        });
                        return null;
                    }
                    e.printStackTrace();
                    return null;
                }
            }

            public /* synthetic */ void lambda$doInBackground$0$PDFTools$2(Context context, String str, ConstraintLayout constraintLayout) {
                pDFTools.showEnterPasswordDialog(context, str, constraintLayout);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void voidR) {
                super.onPostExecute(voidR);
                this.progressDialog.dismiss();
                PDDocument pDDocument = this.doc;
                if (pDDocument != null) {
                    pDFTools.removeFileProtections(context, pDDocument, str, "", constraintLayout);
                }
                try {
                    if (this.doc != null) {
                        this.doc.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /* access modifiers changed from: private */
    public void showEnterPasswordDialog(Context context, String str, ConstraintLayout constraintLayout) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle((int) R.string.unprotect_pdf).setPositiveButton((int) R.string.ok, (DialogInterface.OnClickListener) null).setView((int) R.layout.dialog_edit_password).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) null);
        AlertDialog create = materialAlertDialogBuilder.create();
        create.show();
        create.getButton(-1).setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ TextInputEditText f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ AlertDialog f$3;
            public final /* synthetic */ Context f$4;
            public final /* synthetic */ ConstraintLayout f$5;

            {
                this.f$1 = (TextInputEditText) create.findViewById(R.id.input_text);
                this.f$2 = str;
                this.f$3 = create;
                this.f$4 = context;
                this.f$5 = constraintLayout;
            }

            public final void onClick(View view) {
                pDFTools.lambda$showEnterPasswordDialog$5$PDFTools(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, view);
            }
        });
    }

    @SuppressLint("WrongConstant")
    public /* synthetic */ void lambda$showEnterPasswordDialog$5$PDFTools(TextInputEditText textInputEditText, String str, AlertDialog alertDialog, Context context, ConstraintLayout constraintLayout, View view) {
        if (textInputEditText != null && textInputEditText.getText() != null) {
            String obj = textInputEditText.getText().toString();
            if (!TextUtils.isEmpty(obj)) {
                try {
                    PDDocument load = PDDocument.load(new File(str), obj);
                    alertDialog.dismiss();
                    removeFileProtections(context, load, str, obj, constraintLayout);
                } catch (Exception e) {
                    if (e instanceof InvalidPasswordException) {
                        textInputEditText.setError(context.getString(R.string.invalid_password));
                        Log.d(TAG, "Invalid Password");
                        return;
                    }
                    Toast.makeText(context, R.string.invalid_password, 0).show();
                }
            } else {
                textInputEditText.setError(context.getString(R.string.invalid_password));
                Log.d(TAG, "Invalid Password");
            }
        }
    }

    /* access modifiers changed from: private */
    @SuppressLint("WrongConstant")
    public void removeFileProtections(Context context, PDDocument pDDocument, String str, String str2, ConstraintLayout constraintLayout) {
        AccessPermission currentAccessPermission = pDDocument.getCurrentAccessPermission();
        if (pDDocument.isEncrypted() || !currentAccessPermission.canAssembleDocument() || !currentAccessPermission.canExtractContent() || !currentAccessPermission.canExtractForAccessibility() || !currentAccessPermission.canFillInForm() || !currentAccessPermission.canModify() || !currentAccessPermission.canModifyAnnotations() || !currentAccessPermission.canPrint() || !currentAccessPermission.canPrintDegraded()) {
            try {
                PDFTools pDFTools = new PDFTools();
                pDFTools.getClass();
                new UnProtectPDF(context, str, str2, constraintLayout).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                pDDocument.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else {
            Toast.makeText(context, R.string.file_not_protected, 1).show();
        }
    }

    private void processAnnotations(PDPage pDPage) throws IOException {
        for (PDAnnotation next : pDPage.getAnnotations()) {
            if (next instanceof PDAnnotationLink) {
                PDAnnotationLink pDAnnotationLink = (PDAnnotationLink) next;
                PDDestination destination = pDAnnotationLink.getDestination();
                if (destination == null && pDAnnotationLink.getAction() != null) {
                    PDAction action = pDAnnotationLink.getAction();
                    if (action instanceof PDActionGoTo) {
                        destination = ((PDActionGoTo) action).getDestination();
                    }
                }
                if (destination instanceof PDPageDestination) {
                    ((PDPageDestination) destination).setPage((PDPage) null);
                }
            } else {
                next.setPage((PDPage) null);
            }
        }
    }

    public static class ExtractImagesImproved extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        Context mContext;
        int numPages;
        String pdfPath;

        public ExtractImagesImproved(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
//            ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    ExtractImagesImproved.this.lambda$new$0$PDFTools$ExtractImagesImproved(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$ExtractImagesImproved(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.currentAction.setText(R.string.extracting);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            File file = new File(this.pdfPath);
            String name = file.getName();
            this.allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/" + Utils.removeExtension(name) + "/";
            File file2 = new File(this.allPdfPictureDir);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            PDFBoxResourceLoader.init(this.mContext);
            try {
                PDDocument load = PDDocument.load(file);
                this.numPages = load.getNumberOfPages();
                PDPageTree pages = load.getPages();
                pDFTools.progressBar.setMax(this.numPages);
                Iterator<PDPage> it = pages.iterator();
                int i = 1;
                while (it.hasNext()) {
                    PDResources resources = it.next().getResources();
                    int i2 = 1;
                    for (COSName xObject : resources.getXObjectNames()) {
                        PDXObject xObject2 = resources.getXObject(xObject);
                        if (xObject2 instanceof PDImageXObject) {
                            try {
                                String str = this.allPdfPictureDir + "image-" + i + "_" + i2 + ".png";
                                ((PDImageXObject) xObject2).getImage().compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(str));
                                arrayList.add(str);
                                arrayList2.add("image/png");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i2++;
                        }
                    }
                    publishProgress(Integer.valueOf(i));
                    i++;
                }
                if (arrayList.size() > 0) {
                    MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), (MediaScannerConnection.OnScanCompletedListener) null);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(2);
            pDFTools.percent.setText(R.string.hundred_percent);
            pDFTools.progressBar.setProgress(this.numPages);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.showInterstialAd((Activity) this.mContext, "", "", this.allPdfPictureDir);
            PDFTools pDFTools = PDFTools.pDFTools;
            Context context = this.mContext;
            pDFTools.openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class ConvertPDFAsPictures extends AsyncTask<Void, Integer, Void> {
        String allPdfPictureDir;
        String fileName;
        Context mContext;
        int numPages;
        PdfDocument pdfDocument;
        String pdfPath;
        PdfiumCore pdfiumCore;

        public ConvertPDFAsPictures(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    ConvertPDFAsPictures.this.lambda$new$0$PDFTools$ConvertPDFAsPictures(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$ConvertPDFAsPictures(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.currentAction.setText(R.string.converting);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            int i;
            String str;
            int i2;
            this.fileName = Utils.removeExtension(new File(this.pdfPath).getName());
            String name = new File(this.pdfPath).getName();
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            this.allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/" + Utils.removeExtension(name) + "/";
            File file = new File(this.allPdfPictureDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            this.pdfiumCore = new PdfiumCore(this.mContext);
            try {
                PdfDocument newDocument = this.pdfiumCore.newDocument(this.mContext.getContentResolver().openFileDescriptor(Uri.fromFile(new File(this.pdfPath)), PDPageLabelRange.STYLE_ROMAN_LOWER));
                this.pdfDocument = newDocument;
                this.numPages = this.pdfiumCore.getPageCount(newDocument);
                pDFTools.progressBar.setMax(this.numPages);
                for (int i3 = 0; i3 < this.numPages && !isCancelled(); i3 = i) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.allPdfPictureDir);
                    sb.append(Utils.removeExtension(name));
                    sb.append("-Page");
                    int i4 = i3 + 1;
                    sb.append(i4);
                    sb.append(".jpg");
                    String sb2 = sb.toString();
                    FileOutputStream fileOutputStream = new FileOutputStream(sb2);
                    String access$800 = PDFTools.TAG;
                    Log.d(access$800, "Creating page image " + sb2);
                    this.pdfiumCore.openPage(this.pdfDocument, i3);
                    int pageWidthPoint = this.pdfiumCore.getPageWidthPoint(this.pdfDocument, i3) * 2;
                    int pageHeightPoint = this.pdfiumCore.getPageHeightPoint(this.pdfDocument, i3) * 2;
                    try {
                        Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.ARGB_8888);
                        Bitmap bitmap = createBitmap;
                        i2 = 1;
                        str = sb2;
                        i = i4;
                        try {
                            this.pdfiumCore.renderPageBitmap(this.pdfDocument, createBitmap, i3, 0, 0, pageWidthPoint, pageHeightPoint, true);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fileOutputStream);
                        } catch (OutOfMemoryError e) {
                            e = e;
                        }
                    } catch (OutOfMemoryError e2) {

                        str = sb2;
                        i = i4;
                        i2 = 1;
                        Toast.makeText(this.mContext, R.string.failed_low_memory, i2).show();
                        e2.printStackTrace();
                        arrayList.add(str);
                        arrayList2.add("image/jpg");
                        Integer[] numArr = new Integer[i2];
                        numArr[0] = Integer.valueOf(i);
                        publishProgress(numArr);
                    }
                    arrayList.add(str);
                    arrayList2.add("image/jpg");
                    Integer[] numArr2 = new Integer[i2];
                    numArr2[0] = Integer.valueOf(i);
                    publishProgress(numArr2);
                }
                this.pdfiumCore.closeDocument(this.pdfDocument);
                try {
                    MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), (MediaScannerConnection.OnScanCompletedListener) null);
                    return null;
                } catch (Exception unused) {
                    return null;
                }
            } catch (Exception unused2) {
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(3);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.showInterstialAd((Activity) this.mContext, "", "", this.allPdfPictureDir);
            PDFTools pDFTools = PDFTools.pDFTools;
            Context context = this.mContext;
            pDFTools.openImageDirectory(context, context.getString(R.string.open_directory), this.allPdfPictureDir);
        }
    }

    public static class SplitPDF extends AsyncTask<Void, Integer, Void> {
        private final Context mContext;
        private int numPages;
        private final String pdfPath;
        private int splitAt = 0;
        private int splitFrom = 0;
        private int splitTo = 0;
        private String splittedPdfDocumentDir;
        private String allPdfPictureDir;

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout) throws IOException {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    SplitPDF.this.lambda$new$0$PDFTools$SplitPDF(view);
                }
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i, int i2) throws IOException {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            this.splitFrom = i;
            this.splitTo = i2;
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    SplitPDF.this.lambda$new$1$PDFTools$SplitPDF(view);
                }
            });
        }

        public SplitPDF(Context context, String str, ConstraintLayout constraintLayout, int i) {
            this.mContext = context;
            this.pdfPath = str;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            this.splitAt = i;
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    SplitPDF.this.lambda$new$2$PDFTools$SplitPDF(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$SplitPDF(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        public /* synthetic */ void lambda$new$1$PDFTools$SplitPDF(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        public /* synthetic */ void lambda$new$2$PDFTools$SplitPDF(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.progressBar.setIndeterminate(true);
            pDFTools.currentAction.setText(R.string.splitting);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't wrap try/catch for region: R(3:25|26|38) */
        /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
            android.widget.Toast.makeText(r13.mContext, com.example.my_pdf_maker.R.string.no_enough_disk_space, 1).show();
            cancel(true);
         */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0117 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Void doInBackground(Void... r14) {
            ArrayList arrayList = new ArrayList();

            ArrayList arrayList2 = new ArrayList();
            File file = new File(this.pdfPath);
            String name = file.getName();
            allPdfPictureDir = Environment.getExternalStorageDirectory() + "/Pictures/AllPdf/" + Utils.removeExtension(name) + "/";
            File file2 = new File(this.allPdfPictureDir);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            PDFBoxResourceLoader.init(this.mContext);
            try {
                PDDocument load = PDDocument.load(file);
                this.numPages = load.getNumberOfPages();
                PDPageTree pages = load.getPages();
                Iterator<PDPage> it = pages.iterator();
                int i = 1;
                while (it.hasNext()) {
                    PDResources resources = it.next().getResources();
                    int i2 = 1;
                    for (COSName xObject : resources.getXObjectNames()) {
                        PDXObject xObject2 = resources.getXObject(xObject);
                        if (xObject2 instanceof PDImageXObject) {
                            try {
                                String str = this.allPdfPictureDir + "image-" + i + "_" + i2 + ".png";
                                ((PDImageXObject) xObject2).getImage().compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(str));
                                arrayList.add(str);
                                arrayList2.add("image/png");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i2++;
                        }
                    }
                    publishProgress(Integer.valueOf(i));
                    i++;
                }
                if (arrayList.size() > 0) {
                    MediaScannerConnection.scanFile(this.mContext, (String[]) arrayList.toArray(new String[arrayList.size()]), (String[]) arrayList2.toArray(new String[arrayList2.size()]), (MediaScannerConnection.OnScanCompletedListener) null);
                }
            } catch (
                    Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }


        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            pDFTools.currentAction.setText(R.string.done);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(1);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.showInterstialAd((Activity) this.mContext, "", "", this.splittedPdfDocumentDir);
            PDFTools pDFTools = PDFTools.pDFTools;
            Context context = this.mContext;
            pDFTools.setupOpenPath(context, context.getString(R.string.open_directory), this.splittedPdfDocumentDir, false);
        }
    }

    public static class CompressPDFImproved extends AsyncTask<Void, Integer, Void> {
        String allPdfDocumentDir;
        Long compressedFileLength;
        String compressedFileSize;
        String compressedPDF;
        int compressionQuality;
        boolean isEcrypted = false;
        Context mContext;
        int numPages;
        String pdfPath;
        String reducedPercent;
        Long uncompressedFileLength;
        String uncompressedFileSize;

        public CompressPDFImproved(Context context, String str, int i, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            this.compressionQuality = i;
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    CompressPDFImproved.this.lambda$new$0$PDFTools$CompressPDFImproved(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$CompressPDFImproved(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.currentAction.setText(R.string.compressing);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            File file = new File(this.pdfPath);
            String name = file.getName();
            Long valueOf = Long.valueOf(file.length());
            this.uncompressedFileLength = valueOf;
            this.uncompressedFileSize = Formatter.formatShortFileSize(this.mContext, valueOf.longValue());
            this.allPdfDocumentDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";
            this.compressedPDF = this.allPdfDocumentDir + Utils.removeExtension(name) + "-Compressed.pdf";
            File file2 = new File(this.allPdfDocumentDir);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            PDFBoxResourceLoader.init(this.mContext);
            try {
                PDDocument load = PDDocument.load(file);
                this.numPages = load.getNumberOfPages();
                PDPageTree pages = load.getPages();
                pDFTools.progressBar.setMax(this.numPages);
                Iterator<PDPage> it = pages.iterator();
                int i = 1;
                while (it.hasNext()) {
                    PDResources resources = it.next().getResources();
                    Bitmap bitmap = null;
                    for (COSName next : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(next);
                        if (xObject instanceof PDImageXObject) {
                            try {
                                bitmap = ((PDImageXObject) xObject).getImage();
                                resources.put(next, (PDXObject) JPEGFactory.createFromImage(load, bitmap, ((float) this.compressionQuality) / 100.0f));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    }
                    publishProgress(Integer.valueOf(i));
                    i++;
                }
                File file3 = new File(this.compressedPDF);
                load.save(file3);
                load.close();
                Long valueOf2 = Long.valueOf(new File(this.compressedPDF).length());
                this.compressedFileLength = valueOf2;
                this.compressedFileSize = Formatter.formatShortFileSize(this.mContext, valueOf2.longValue());
                int longValue = 100 - ((int) ((this.compressedFileLength.longValue() * 100) / this.uncompressedFileLength.longValue()));
                this.reducedPercent = longValue + "%";
                if (longValue < 0) {
                    this.reducedPercent = "0%";
                    FileUtils.copyFile(file, file3);
                }
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.compressedPDF}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.compressedPDF);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(6);
            if (!this.isEcrypted) {
                pDFTools.percent.setText(R.string.hundred_percent);
                pDFTools.progressBar.setProgress(this.numPages);
                pDFTools.currentAction.setText(R.string.done);
                pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
                pDFTools.showInterstialAd((Activity) this.mContext, this.reducedPercent, this.mContext.getString(R.string.reduced_from) + " " + this.uncompressedFileSize + " " + this.mContext.getString(R.string.to) + " " + this.compressedFileSize, this.allPdfDocumentDir);
                PDFTools pDFTools = PDFTools.pDFTools;
                Context context = this.mContext;
                pDFTools.setupOpenPath(context, context.getString(R.string.open_file), this.compressedPDF, true);
                return;
            }
            pDFTools.closeProgressView(this.mContext);
            Toast.makeText(this.mContext, R.string.file_protected_unprotect, 1).show();
        }
    }

    public static class AddPageNumbers extends AsyncTask<Void, Integer, Void> {
        String allPdfDocumentDir;
        Context mContext;
        int numPages;
        String pageNumberedPDF;
        String pdfPath;
        private final int fromPage;
        private final int numberAlignment;
        private final int numberFormat;
        private final int numberPosition;
        private int startAt;
        private final int toPage;

        public AddPageNumbers(Context context, String str, int i, int i2, int i3, int i4, int i5, int i6, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            this.numberFormat = i;
            this.numberPosition = i2;
            this.numberAlignment = i3;
            this.fromPage = i4;
            this.toPage = i5;
            this.startAt = i6;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    AddPageNumbers.this.lambda$new$0$PDFTools$AddPageNumbers(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$AddPageNumbers(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.currentAction.setText(R.string.processing);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            String name = new File(this.pdfPath).getName();
            this.allPdfDocumentDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/";
            this.pageNumberedPDF = this.allPdfDocumentDir + Utils.removeExtension(name) + "-Page-Numbers.pdf";
            File file = new File(this.allPdfDocumentDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                PDFBoxResourceLoader.init(this.mContext);
                PDDocument load = PDDocument.load(new File(this.pdfPath));
                this.numPages = load.getNumberOfPages();
                pDFTools.progressBar.setMax(this.numPages);
                PDType1Font pDType1Font = PDType1Font.TIMES_BOLD;
                int i = this.fromPage - 1;
                while (i < this.toPage && !isCancelled()) {
                    PDPage page = load.getPage(i);
                    PDPageContentStream pDPageContentStream = null;
                    PDPageContentStream pDPageContentStream2 = new PDPageContentStream(load, page, true, true, false);
                    pDPageContentStream.beginText();
                    float f = (float) 10;
                    PDPageContentStream pDPageContentStream3 = pDPageContentStream;
                    pDPageContentStream3.setFont(pDType1Font, f);
                    String formattedNumber = pDFTools.getFormattedNumber(this.startAt, this.numberFormat);
                    float stringWidth = (pDType1Font.getStringWidth(formattedNumber) / 1000.0f) * f;
                    float capHeight = (pDType1Font.getFontDescriptor().getCapHeight() / 1000.0f) * f;
                    CoordinateModel calculatePageNumberPositon = pDFTools.calculatePageNumberPositon(page.getMediaBox(), this.numberPosition, stringWidth, capHeight, this.numberAlignment);
                    pDPageContentStream3.newLineAtOffset(calculatePageNumberPositon.getX(), calculatePageNumberPositon.getY());
                    pDPageContentStream3.showText(formattedNumber);
                    pDPageContentStream3.endText();
                    pDPageContentStream3.close();
                    this.startAt++;
                    i++;
                    publishProgress(Integer.valueOf(i));
                }
                load.save(new File(this.pageNumberedPDF));
                load.close();
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.pageNumberedPDF}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.pageNumberedPDF);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numPages);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(12);
            pDFTools.percent.setText(R.string.hundred_percent);
            pDFTools.progressBar.setProgress(this.numPages);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            Context context = this.mContext;
            pDFTools.setupOpenPath(context, context.getString(R.string.open_file), this.pageNumberedPDF, true);
        }
    }

    public static class PasswordProtectPDF extends AsyncTask<Void, Integer, Void> {
        String allPdfProtectedDir;
        String encryptedFilePath;
        Context mContext;
        boolean mergeSuccess = true;
        String ownerPassword;
        String pdfPath;
        String userPassword;
        private final boolean canAssebleDocument;
        private final boolean canComment;
        private final boolean canCopyContent;
        private final boolean canCopyContentForAccessbility;
        private final boolean canEdit;
        private final boolean canFillFormFields;
        private final boolean canPrint;

        public PasswordProtectPDF(Context context, String str, String str2, String str3, ConstraintLayout constraintLayout, AppCompatCheckBox appCompatCheckBox, AppCompatCheckBox appCompatCheckBox2, AppCompatCheckBox appCompatCheckBox3, AppCompatCheckBox appCompatCheckBox4, AppCompatCheckBox appCompatCheckBox5, AppCompatCheckBox appCompatCheckBox6, AppCompatCheckBox appCompatCheckBox7) {
            this.mContext = context;
            this.pdfPath = str;
            this.ownerPassword = str3;
            this.userPassword = str2;
            this.canPrint = !appCompatCheckBox.isChecked();
            this.canAssebleDocument = !appCompatCheckBox2.isChecked();
            this.canCopyContent = !appCompatCheckBox3.isChecked();
            this.canCopyContentForAccessbility = !appCompatCheckBox4.isChecked();
            this.canEdit = !appCompatCheckBox5.isChecked();
            this.canComment = !appCompatCheckBox6.isChecked();
            this.canFillFormFields = !appCompatCheckBox7.isChecked();
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    PasswordProtectPDF.this.lambda$new$0$PDFTools$PasswordProtectPDF(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$PasswordProtectPDF(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.progressBar.setMax(100);
            pDFTools.progressBar.setIndeterminate(true);
            pDFTools.currentAction.setText(R.string.protecting);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            this.allPdfProtectedDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/Protected/";
            File file = new File(this.allPdfProtectedDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                PDFBoxResourceLoader.init(this.mContext);
                String removeExtension = Utils.removeExtension(new File(this.pdfPath).getName());
                this.encryptedFilePath = this.allPdfProtectedDir + removeExtension + "-Protected.pdf";
                String access$800 = PDFTools.TAG;
                Log.d(access$800, "Encrypted file path " + this.encryptedFilePath);
                PDDocument load = PDDocument.load(new File(this.pdfPath));
                AccessPermission accessPermission = new AccessPermission();
                accessPermission.setCanModify(this.canEdit);
                accessPermission.setCanPrint(this.canPrint);
                accessPermission.setCanAssembleDocument(this.canAssebleDocument);
                accessPermission.setCanExtractContent(this.canCopyContent);
                accessPermission.setCanExtractForAccessibility(this.canCopyContentForAccessbility);
                accessPermission.setCanPrintDegraded(false);
                accessPermission.setCanFillInForm(this.canFillFormFields);
                accessPermission.setCanModifyAnnotations(this.canComment);
                StandardProtectionPolicy standardProtectionPolicy = new StandardProtectionPolicy(this.ownerPassword, this.userPassword, accessPermission);
                standardProtectionPolicy.setEncryptionKeyLength(128);
                standardProtectionPolicy.setPermissions(accessPermission);
                load.protect(standardProtectionPolicy);
                load.save(this.encryptedFilePath);
                load.close();
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.encryptedFilePath}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.encryptedFilePath);
                }
                publishProgress(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), 100);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.removeProgressBarIndeterminate(this.mContext, pDFTools.progressBar);
            pDFTools.progressBar.setProgress(100);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.processingFinished(this.mContext, "", "", this.allPdfProtectedDir);
            PDFTools pDFTools2 = pDFTools;
            Context context = this.mContext;
            pDFTools2.setupOpenPath(context, context.getString(R.string.open_file), this.encryptedFilePath, true);
            if (!this.mergeSuccess) {
                Toast.makeText(this.mContext, R.string.merge_failed, 1).show();
            }
        }
    }

    public class MergePDFFiles extends AsyncTask<Void, Integer, Void> {
        String allPdfMergedDir;
        Context mContext;
        boolean mergeSuccess = true;
        String mergedFileName;
        String mergedFilePath;
        int numFiles;
        ArrayList<String> pdfPaths;

        public MergePDFFiles(Context context, ArrayList<String> arrayList, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPaths = arrayList;
            this.mergedFileName = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    MergePDFFiles.this.lambda$new$0$PDFTools$MergePDFFiles(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$MergePDFFiles(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.progressBar.setIndeterminate(true);
            pDFTools.currentAction.setText(R.string.merging);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            this.allPdfMergedDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/Merged/";
            File file = new File(this.allPdfMergedDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            this.mergedFilePath = this.allPdfMergedDir + this.mergedFileName + ".pdf";
            this.numFiles = this.pdfPaths.size();
            pDFTools.progressBar.setMax(this.numFiles + 1);
            PDFBoxResourceLoader.init(this.mContext);
            PDFMergerUtility pDFMergerUtility = new PDFMergerUtility();
            pDFMergerUtility.setDestinationFileName(this.mergedFilePath);
            PDFTools pDFTools = PDFTools.pDFTools;
            pDFTools.removeProgressBarIndeterminate(this.mContext, pDFTools.progressBar);
            int i = 0;
            while (i < this.numFiles && !isCancelled()) {
                try {
                    pDFMergerUtility.addSource(new File(this.pdfPaths.get(i)));
                    i++;
                    publishProgress(Integer.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.mergeSuccess = false;
                }
            }
            try {
                pDFMergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress(Integer.valueOf(this.numFiles + 1));
            if (isCancelled()) {
                new File(this.mergedFilePath).delete();
            }
            MediaScannerConnection.scanFile(this.mContext, new String[]{this.mergedFilePath}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
            if (z) {
                Utils.generatePDFThumbnail(this.mContext, this.mergedFilePath);
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), this.numFiles + 1);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.showInterstialAd((Activity) this.mContext, "", "", this.allPdfMergedDir);
            PDFTools pDFTools = PDFTools.pDFTools;
            Context context = this.mContext;
            pDFTools.setupOpenPath(context, context.getString(R.string.open_file), this.mergedFilePath, true);
            new ConfigRepository(((Activity) this.mContext).getApplication()).incrementConfigValue(0);
            if (!this.mergeSuccess) {
                Toast.makeText(this.mContext, R.string.merge_failed, 1).show();
            }
        }
    }

    public class ExtractText extends AsyncTask<Void, Integer, Void> {
        String errorMessage;
        String extractedTextDir;
        String extractedTextFilePath;
        Context mContext;
        String pdfPath;
        boolean textExtractSuccess = true;

        public ExtractText(Context context, String str, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            //    ConstraintLayout unused = pDFTools.mProgressView = constraintLayout;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    ExtractText.this.lambda$new$0$PDFTools$ExtractText(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$ExtractText(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.progressBar.setIndeterminate(true);
            pDFTools.currentAction.setText(R.string.extracting);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            this.extractedTextDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/Texts/";
            File file = new File(this.extractedTextDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                File file2 = new File(this.pdfPath);
                String name = file2.getName();
                this.extractedTextFilePath = this.extractedTextDir + Utils.removeExtension(name) + ".txt";
                PDFBoxResourceLoader.init(this.mContext);
                PDDocument load = PDDocument.load(file2);
                if (!load.isEncrypted()) {
                    String text = new PDFTextStripper().getText(load);
                    FileOutputStream fileOutputStream = new FileOutputStream(this.extractedTextFilePath);
                    fileOutputStream.write(text.getBytes());
                    load.close();
                    fileOutputStream.close();
                    return null;
                }
                this.errorMessage = this.mContext.getString(R.string.file_protected_unprotect);
                this.textExtractSuccess = false;
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                this.errorMessage = this.mContext.getString(R.string.extraction_failed);
                this.textExtractSuccess = false;
                return null;
            }
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            pDFTools.currentAction.setText(R.string.done);
            pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            pDFTools.showInterstialAd((Activity) this.mContext, "", "", this.extractedTextDir);
            if (!this.textExtractSuccess) {
                Toast.makeText(this.mContext, this.errorMessage, 1).show();
            }
        }
    }

    public class UnProtectPDF extends AsyncTask<Void, Integer, Void> {
        String allPdfUnProtectedDir;
        Context mContext;
        boolean mergeSuccess = true;
        String password;
        String pdfPath;
        String unProtectedFilePath;

        public UnProtectPDF(Context context, String str, String str2, ConstraintLayout constraintLayout) {
            this.mContext = context;
            this.pdfPath = str;
            this.password = str2;
            pDFTools.initializeProgressView(constraintLayout);
            Utils.setLightStatusBar(context);
            pDFTools.btnCancelProgress.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    UnProtectPDF.this.lambda$new$0$PDFTools$UnProtectPDF(view);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$PDFTools$UnProtectPDF(View view) {
            cancel(true);
            pDFTools.closeProgressView(this.mContext);
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            pDFTools.progressBar.setMax(100);
            pDFTools.progressBar.setIndeterminate(true);
            pDFTools.currentAction.setText(R.string.uprotectiong);
            pDFTools.mProgressView.setVisibility(0);
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            boolean z = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.GRID_VIEW_ENABLED, false);
            this.allPdfUnProtectedDir = Environment.getExternalStorageDirectory() + "/Documents/AllPdf/Unprotected/";
            File file = new File(this.allPdfUnProtectedDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                PDFBoxResourceLoader.init(this.mContext);
                String removeExtension = Utils.removeExtension(new File(this.pdfPath).getName());
                this.unProtectedFilePath = this.allPdfUnProtectedDir + removeExtension + "-Unprotected.pdf";
                try {
                    PDDocument load = PDDocument.load(new File(this.pdfPath), this.password);
                    load.setAllSecurityToBeRemoved(true);
                    load.save(this.unProtectedFilePath);
                    load.close();
                } catch (Exception e) {
                    Log.d(PDFTools.TAG, "Cannot decrypt");
                    e.printStackTrace();
                }
                MediaScannerConnection.scanFile(this.mContext, new String[]{this.unProtectedFilePath}, new String[]{"application/pdf"}, (MediaScannerConnection.OnScanCompletedListener) null);
                if (z) {
                    Utils.generatePDFThumbnail(this.mContext, this.unProtectedFilePath);
                }
                publishProgress(100);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            pDFTools.updateProgressPercent(numArr[0].intValue(), 100);
        }

        /* access modifiers changed from: protected */
        @SuppressLint("WrongConstant")
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            pDFTools.currentAction.setText(R.string.done);
            PDFTools pDFTools = PDFTools.pDFTools;
            pDFTools.removeProgressBarIndeterminate(this.mContext, pDFTools.progressBar);
            PDFTools.pDFTools.progressBar.setProgress(100);
            PDFTools.pDFTools.btnCancelProgress.setOnClickListener((View.OnClickListener) null);
            PDFTools.pDFTools.processingFinished(this.mContext, "", "", this.allPdfUnProtectedDir);
            PDFTools pDFTools2 = PDFTools.pDFTools;
            Context context = this.mContext;
            pDFTools2.setupOpenPath(context, context.getString(R.string.open_file), this.unProtectedFilePath, true);
            if (!this.mergeSuccess) {
                Toast.makeText(this.mContext, R.string.merge_failed, 1).show();
            }
        }
    }
}
