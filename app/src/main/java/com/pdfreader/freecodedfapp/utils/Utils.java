package com.pdfreader.freecodedfapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.print.PrintHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.adapters.PrintDocumentAdapter;
import com.pdfreader.freecodedfapp.data.Constants;
import com.pdfreader.freecodedfapp.data.DbHelper;
import com.pdfreader.freecodedfapp.models.PdfModel;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;
import com.shockwave.pdfium.PdfiumCore;
import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDWindowsLaunchParams;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String mPath;
    public static int permissionRequest = 21;

    public static String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String FILE_AUTHORITY = "com.example.pdfreader.freecodedfapp";
    private static final String TAG = Utils.class.getSimpleName();
    private AlertDialog AlertDialog;

    private static ImageView cursor;

    static /* synthetic */ void lambda$setUpRateUsDialog$4(DialogInterface dialogInterface, int i) {
    }

    public static boolean isExternalStorageWritable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String externalStorageState = Environment.getExternalStorageState();
        return "mounted".equals(externalStorageState) || "mounted_ro".equals(externalStorageState);
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    public static String formatDateToHumanReadable(Long l) {
        return new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date(l.longValue()));
    }

    public static String formatDateLongFormat(String str) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date);
    }

    public static String formatMetadataDate(Context context, String str) {
        try {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).parse(str.split("\\+")[0].split(":")[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.unknown);
        }
    }

    public static long getTimeInMills(String str) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(str).getTime();
    }

    public static Double dateDiffInDays(Long l, Long l2) {
        return Double.valueOf(((double) (l.longValue() - l2.longValue())) / 8.64E7d);
    }

    public static String formatToSystemDateFormat(Context context) {
        return new SimpleDateFormat(((SimpleDateFormat) DateFormat.getDateFormat(context)).toLocalizedPattern(), Locale.getDefault()).format(Calendar.getInstance(Locale.getDefault()).getTime());
    }

    public static String formatColorToHex(int i) {
        return String.format("#%06X", Integer.valueOf(i & ViewCompat.MEASURED_SIZE_MASK));
    }

    public static String removeExtension(String str) {
        int lastIndexOf = str.lastIndexOf(System.getProperty("file.separator"));
        if (lastIndexOf != -1) {
            str = str.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = str.lastIndexOf(".");
        if (lastIndexOf2 == -1) {
            return str;
        }
        return str.substring(0, lastIndexOf2);
    }

    public static void shareFile(Context context, Uri uri) {
        try {
            Intent intent = ShareCompat.IntentBuilder.from((Activity) context).setType(context.getContentResolver().getType(uri)).setStream(uri).getIntent();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.share_this_file_via));
            createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (createChooser.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(createChooser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_share_file, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isFileNameValid(String str) {
        String trim = str.trim();
        return !TextUtils.isEmpty(trim) && trim.matches("[a-zA-Z0-9-_ ]*");
    }

    public static void deleteFiles(String str) {
        File file = new File(str);
        if (file.exists() && file.isDirectory()) {
            try {
                Runtime.getRuntime().exec("find " + str + " -xdev -mindepth 1 -delete");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Uri getImageUriFromPath(String str) {
        return Uri.fromFile(new File(str.replace(".pdf", ".jpg")));
    }

    public static boolean isThumbnailPresent(Context context, String str) {
        String name = new File(str).getName();
        return new File((context.getCacheDir() + "/Thumbnails/") + removeExtension(name) + ".jpg").exists();
    }

    @SuppressLint("WrongConstant")
    public static void generatePDFThumbnail(Context context, String str) {
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        File file = new File(str);
        String name = file.getName();
        try {
            PdfDocument newDocument = pdfiumCore.newDocument(context.getContentResolver().openFileDescriptor(Uri.fromFile(file), PDPageLabelRange.STYLE_ROMAN_LOWER));
            String str2 = context.getCacheDir() + "/Thumbnails/";
            File file2 = new File(str2);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            String str3 = str2 + removeExtension(name) + ".jpg";
            Log.d(TAG, "Generating thumb img " + str3);
            FileOutputStream fileOutputStream = new FileOutputStream(str3);
            pdfiumCore.openPage(newDocument, 0);
            int pageWidthPoint = pdfiumCore.getPageWidthPoint(newDocument, 0) / 2;
            int pageHeightPoint = pdfiumCore.getPageHeightPoint(newDocument, 0) / 2;
            try {
                Bitmap createBitmap = Bitmap.createBitmap(pageWidthPoint, pageHeightPoint, Bitmap.Config.RGB_565);
                pdfiumCore.renderPageBitmap(newDocument, createBitmap, 0, 0, 0, pageWidthPoint, pageHeightPoint, true);
                createBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
            } catch (OutOfMemoryError e) {
                Toast.makeText(context, R.string.failed_low_memory, 1).show();
                e.printStackTrace();
            }
            pdfiumCore.closeDocument(newDocument);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void print(Context context, Uri uri) {
        try {
            new PdfiumCore(context).newDocument(context.getContentResolver().openFileDescriptor(uri, PDPageLabelRange.STYLE_ROMAN_LOWER));
            if (PrintHelper.systemSupportsPrint()) {
                @SuppressLint("WrongConstant") PrintManager printManager = (PrintManager) context.getSystemService(PDWindowsLaunchParams.OPERATION_PRINT);
                String str = context.getString(R.string.app_name) + " Document";
                if (printManager != null) {
                    printManager.print(str, new PrintDocumentAdapter(context, uri), (PrintAttributes) null);
                    return;
                }
                return;
            }
            Toast.makeText(context, R.string.device_does_not_support_printing, Toast.LENGTH_LONG).show();
        } catch (PdfPasswordException e) {
            Toast.makeText(context, R.string.cant_print_password_protected_pdf, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e2) {
            Toast.makeText(context, R.string.cannot_print_malformed_pdf, Toast.LENGTH_LONG).show();
            e2.printStackTrace();
        } catch (Exception e3) {
            Toast.makeText(context, R.string.cannot_print_unknown_error, android.widget.Toast.LENGTH_LONG).show();
            e3.printStackTrace();
        }
    }

    public static List<String> getPDFPathsFromFiles(List<File> list) {
        ArrayList arrayList = new ArrayList();
        for (File absolutePath : list) {
            arrayList.add(absolutePath.getAbsolutePath());
        }
        return arrayList;
    }

    public static void startShareActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.TEXT", "Want to read and manipulate PDF files? " + Constants.APP_NAME + " is here to help. Download NOW! " + Constants.URL_APP_PLAY_STORE);
        intent.setType("text/plain");
        Intent createChooser = Intent.createChooser(intent, context.getResources().getString(R.string.chooser_title));
        createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(new Intent(createChooser));
        }
    }

    public static void showSubscriptionOptions(Context context) {

    }

    public static void setLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.colorAccent);
        int color2 = context.getResources().getColor(R.color.colorDarkGreen);
        if (Build.VERSION.SDK_INT >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility & -8193);
            activity.getWindow().setStatusBarColor(color);
            activity.getWindow().setNavigationBarColor(color2);
        }
    }

    public static void clearLightStatusBar(Context context) {
        Activity activity = (Activity) context;
        View decorView = activity.getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int color = context.getResources().getColor(R.color.colorPrimaryDark);
        if (Build.VERSION.SDK_INT >= 23) {
            decorView.setSystemUiVisibility(systemUiVisibility | 8192);
            activity.getWindow().setStatusBarColor(color);
            activity.getWindow().setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getPath(Context r11, Uri r12) throws java.net.URISyntaxException {

        throw new UnsupportedOperationException("Method not decompiled: Utils.getPath(android.content.Context, android.net.Uri):java.lang.String");
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static ActivityManager.MemoryInfo getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public static void showPremiumFeatureDialog(Context context) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setView(((Activity) context).getLayoutInflater().inflate(R.layout.dialog_premium_feature, (ViewGroup) null));
        AlertDialog create = materialAlertDialogBuilder.create();
        create.show();
        ((AppCompatButton) create.findViewById(R.id.btn_get_premium)).setOnClickListener(new View.OnClickListener() {
            public final /* synthetic */ Context f$1;

            {
                this.f$1 = context;
            }

            public final void onClick(View view) {

            }
        });
        ((AppCompatButton) create.findViewById(R.id.btn_later)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {

            }
        });
    }

    static /* synthetic */ void lambda$showPremiumFeatureDialog$0(AlertDialog alertDialog, Context context, View view) {
        alertDialog.dismiss();
        showSubscriptionOptions(context);
    }

    public static int getRunTimes(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("prefs_run_times", 0);
    }

    public static int getResumeTimes(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("prefs_resume_times", 0);
    }

    public static void incrementResumeTimes(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultSharedPreferences.edit().putInt("prefs_resume_times", defaultSharedPreferences.getInt("prefs_resume_times", 0) + 1).apply();
    }

    public static void setUpRateUsDialog(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int runTimes = getRunTimes(context);
        boolean z = defaultSharedPreferences.getBoolean("prefs_show_rate_us", true);
        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        int i = runTimes + 1;
        edit.putInt("prefs_run_times", i);
        edit.apply();
        if (i % 2 == 0 && z) {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
            materialAlertDialogBuilder.setView((int) R.layout.dialog_rate_us).setPositiveButton((int) R.string.rate, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public final /* synthetic */ Context f$0;

                {
                    this.f$0 = context;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    Utils.launchMarket(this.f$0);
                }
            }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public final /* synthetic */ SharedPreferences.Editor f$0;

                {
                    this.f$0 = edit;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    Utils.lambda$setUpRateUsDialog$3(this.f$0, dialogInterface, i);
                }
            }).setNeutralButton((int) R.string.later, (DialogInterface.OnClickListener) $$Lambda$Utils$fO0S9k_HX7fEcRsaiiOLKVp8WEY.INSTANCE);

            Drawable aa = ContextCompat.getDrawable(context, R.drawable.popup_bg);
            aa.setTint(ContextCompat.getColor(context, R.color.background_color_day_night));
            materialAlertDialogBuilder.setBackground(aa);

            materialAlertDialogBuilder.create().show();
        }
    }

    static /* synthetic */ void lambda$setUpRateUsDialog$3(SharedPreferences.Editor editor, DialogInterface dialogInterface, int i) {
        editor.putBoolean("prefs_show_rate_us", false);
        editor.apply();
    }

    public static void launchMarket(Context context) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean("prefs_show_rate_us", false);
        edit.apply();
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, R.string.unable_to_find_play_store, Toast.LENGTH_LONG).show();
        }
    }

    public static void openScanner(Context context) {
        if (isPackageInstalled("tz.co.wadau.scanner.pdf", context.getPackageManager())) {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage("tz.co.wadau.scanner.pdf"));
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + "tz.co.wadau.scanner.pdf"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, R.string.unable_to_find_play_store, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isPackageInstalled(String str, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static class BackgroundGenerateThumbnails extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        public BackgroundGenerateThumbnails(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voidArr) {
            List<PdfModel> allPdfModels = DbHelper.getInstance(this.mContext).getAllPdfs();
            int size = allPdfModels.size();
            for (int i = 0; i < size; i++) {
                String absolutePath = allPdfModels.get(i).getAbsolutePath();
                if (!Utils.isThumbnailPresent(this.mContext, absolutePath)) {
                    Utils.generatePDFThumbnail(this.mContext, absolutePath);
                }
            }
            return null;
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void mediaScanner(Context context, String newFilePath, String oldFilePath, String fileType) {
        try {
            MediaScannerConnection.scanFile(context, new String[]{newFilePath + new File(oldFilePath).getName()}, new String[]{fileType},
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        public void onMediaScannerConnected() {
                        }

                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBack(String paramString1, String paramString2) {
        Matcher localMatcher = Pattern.compile(paramString2).matcher(paramString1);
        if (localMatcher.find()) {
            return localMatcher.group(1);
        }
        return "";
    }

    public static boolean download(Context context, String path) {
        if (copyFileInSavedDir(context, path)) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }


    static boolean copyFileInSavedDir(Context context, String file) {
        try {
            if (isImageFile(file)) {
                FileUtils.copyFileToDirectory(new File(file), getDir(context,"Images"));
                mediaScanner(context, getDir(context,"Images") + "/", file, "image/*");
            } else {
                FileUtils.copyFileToDirectory(new File(file), getDir(context,"Videos"));
                mediaScanner(context, getDir(context,"Videos") + "/", file, "video/*");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static File getDir(Context context, String folder) {

        File rootFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + context.getResources().getString(R.string.app_name) + File.separator + folder);
        rootFile.mkdirs();

        return rootFile;

    }

    public static void setLanguage(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public static boolean isPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();

        } else {
            int readExtStorage = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            return readExtStorage == PackageManager.PERMISSION_GRANTED;
        }
    }

}
