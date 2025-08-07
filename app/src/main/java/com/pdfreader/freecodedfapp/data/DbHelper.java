package com.pdfreader.freecodedfapp.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent;
import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.models.BookmarkModel;
import com.pdfreader.freecodedfapp.models.PdfModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    public static final String SORT_BY = "prefs_sort_by";
    public static final String SORT_ORDER = "prefs_sort_order";
    private static final String DATABASE_NAME = "pdf_history.db";
    private static final int DATABASE_VERSION = 2;
    private static DbHelper sInstance;
    private final String SQL_CREATE_BOOKMARK = "CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_HISTORY_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String SQL_CREATE_LAST_OPENED_PAGE = "CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)";
    private final String SQL_CREATE_STARED_PDFS_TABLE = "CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))";
    private final String TAG = DbHelper.class.getSimpleName();
    public Context context;
    private final String THUMBNAILS_DIR;
    private SQLiteDatabase mDatabase;
    private int mOpenCounter;
    private File file;
    private List<PdfModel> arrayList;

    public DbHelper(Context context2) {
        super(context2, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
        this.context = context2;
        this.THUMBNAILS_DIR = context2.getCacheDir() + "/Thumbnails/";
    }

    public static synchronized DbHelper getInstance(Context context2) {
        DbHelper dbHelper;
        synchronized (DbHelper.class) {
            if (sInstance == null) {
                sInstance = new DbHelper(context2.getApplicationContext());
            }
            dbHelper = sInstance;
        }
        return dbHelper;
    }

    public synchronized SQLiteDatabase getReadableDb() {
        int i = this.mOpenCounter + 1;
        this.mOpenCounter = i;
        if (i == 1) {
            this.mDatabase = getWritableDatabase();
        }
        return this.mDatabase;
    }

    public synchronized void closeDb() {
        int i = this.mOpenCounter - 1;
        this.mOpenCounter = i;
        if (i == 0) {
            this.mDatabase.close();
        }
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS history_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, last_accessed_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS stared_pdfs ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i == 1) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS last_opened_page ( _id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT UNIQUE, page_number INTEGER)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bookmarks ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, path TEXT, page_number INTEGER UNIQUE, created_at DATETIME DEFAULT (DATETIME('now','localtime')))");
        }
    }

    public List<PdfModel> getAllPdfFromDirectory(String str) {
        String string = PreferenceManager.getDefaultSharedPreferences(this.context).getString(SORT_BY, "name");
        ArrayList arrayList = new ArrayList();
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            Uri contentUri = MediaStore.Files.getContentUri("external");
            char c = 65535;
            int hashCode = string.hashCode();
            if (hashCode != 3373707) {
                if (hashCode != 3530753) {
                    if (hashCode == 1375123195) {
                        if (string.equals("date modified")) {
                            c = 0;
                        }
                    }
                } else if (string.equals("size")) {
                    c = 1;
                }
            } else if (string.equals("name")) {
                c = 2;
            }
            String str2 = c != 0 ? c != 1 ? "title  COLLATE NOCASE ASC" : "_size  COLLATE NOCASE ASC" : "date_modified  COLLATE NOCASE ASC";
            Cursor query = contentResolver.query(contentUri, new String[]{"_data"}, "mime_type=? AND _data LIKE ?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"), "%" + str + "%"}, str2);
            if (query != null && query.moveToFirst()) {
                do {
                    File file = new File(query.getString(query.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        PdfModel pdfModel = new PdfModel();
                        pdfModel.setName(file.getName());
                        pdfModel.setAbsolutePath(file.getAbsolutePath());
                        pdfModel.setPdfUri(file.toString());
                        pdfModel.setLength(Long.valueOf(file.length()));
                        pdfModel.setLastModified(Long.valueOf(file.lastModified()));
                        pdfModel.setThumbUri("imageUriFromPath");
                        pdfModel.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfModel);
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str3 = this.TAG;
        Log.d(str3, "no of files in db " + arrayList.size());
        return arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ArrayList<PdfModel> getAllPdfs() {

        String str;

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String string = defaultSharedPreferences.getString(SORT_BY, "name");
        String string2 = defaultSharedPreferences.getString(SORT_ORDER, "ascending");
        ArrayList<PdfModel> arrayList = new ArrayList();
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri contentUri = MediaStore.Files.getContentUri("external");
        String str2 = TextUtils.equals(string2, "descending") ? "DESC" : "ASC";
        char c = 65535;
        int hashCode = string.hashCode();
        if (hashCode != 3373707) {
            if (hashCode != 3530753) {
                if (hashCode == 1375123195 && string.equals("date modified")) {
                    c = 0;
                }
            } else if (string.equals("size")) {
                c = 1;
            }
        } else if (string.equals("name")) {
            c = 2;
        }
        if (c == 0) {
            str = "date_modified  COLLATE NOCASE " + str2;
        } else if (c != 1) {
            str = "title  COLLATE NOCASE " + str2;
        } else {
            str = "_size  COLLATE NOCASE " + str2;
        }


        try {
            String[] proj = {MediaStore.Images.Media.DATA};

            CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, "mime_type=?", new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")}, str);
            Cursor cursor = cursorLoader.loadInBackground();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    File file = new File(cursor.getString(cursor.getColumnIndex("_data")));
                    if (file.length() != 0) {
                        Log.d(TAG, "getAllPdfs: a " + file.length());
                        PdfModel pdfModel = new PdfModel();
                        pdfModel.setName(file.getName());

                        Log.d(TAG, "getAllPdfs: bb " + file.getName());
                        pdfModel.setAbsolutePath(file.getAbsolutePath());
                        pdfModel.setPdfUri(file.toString());
                        pdfModel.setLength(Long.valueOf(file.length()));
                        pdfModel.setLastModified(Long.valueOf(file.lastModified()));
                        pdfModel.setThumbUri("imageUriFromPath");
                        pdfModel.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfModel);
                    } else {
                        Log.d(TAG, "getAllPdfs: ");
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public void Search_Dir(File dir) {
        String pdfPattern = ".pdf";

        File[] FileList = dir.listFiles();

        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                if (FileList[i].isDirectory()) {
                    Search_Dir(FileList[i]);
                } else {
                    if (FileList[i].getName().endsWith(pdfPattern)) {
                        //here you have that file.
                        Log.d(TAG, "getAllPdfs: aa " + FileList[i].getName());

                        file = FileList[i];

                        PdfModel pdfModel = new PdfModel();
                        pdfModel.setName(file.getName());
                        Log.d(TAG, "getAllPdfs: bb " + file.getName());
                        pdfModel.setAbsolutePath(file.getAbsolutePath());
                        pdfModel.setPdfUri(file.toString());
                        pdfModel.setLength(Long.valueOf(file.length()));
                        pdfModel.setLastModified(Long.valueOf(file.lastModified()));
                        pdfModel.setThumbUri("imageUriFromPath");
                        pdfModel.setStarred(isStared(file.getAbsolutePath()));
                        arrayList.add(pdfModel);

                    }
                }
            }
        }
    }

    public void addRecentPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(DbContract.RecentPDFEntry.TABLE_NAME, (String) null, contentValues);
        closeDb();
    }

    public List<PdfModel> getRecentPDFs() {
        ArrayList arrayList = new ArrayList();
        SQLiteDatabase readableDb = getReadableDb();
        Cursor rawQuery = readableDb.rawQuery("SELECT * FROM history_pdfs ORDER BY last_accessed_at DESC", (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    PdfModel pdfModel = new PdfModel();
                    pdfModel.setName(file.getName());
                    pdfModel.setAbsolutePath(file.getAbsolutePath());
                    pdfModel.setPdfUri(file.toString());
                    pdfModel.setLength(Long.valueOf(file.length()));
                    pdfModel.setLastModified(Long.valueOf(file.lastModified()));
                    pdfModel.setThumbUri("imageUriFromPath");
                    pdfModel.setStarred(isStared(readableDb, file.getAbsolutePath()));
                    arrayList.add(pdfModel);
                } else {
                    deleteRecentPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void deleteRecentPDF(String str) {
        getReadableDb().delete(DbContract.RecentPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
        EventBus.getDefault().post(new DataUpdatedEvent.RecentPdfDeleteEvent());
    }

    public void updateHistory(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.RecentPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            Toast.makeText(this.context, R.string.failed, 1).show();
            e.printStackTrace();
        }
    }

    public void updateStarred(String str, String str2) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str2);
        readableDb.update(DbContract.StarredPDFEntry.TABLE_NAME, contentValues, "path=?", new String[]{str});
        closeDb();
    }

    public void clearRecentPDFs() {
        getReadableDb().delete(DbContract.RecentPDFEntry.TABLE_NAME, (String) null, (String[]) null);
        closeDb();
        EventBus.getDefault().post(new DataUpdatedEvent.RecentPdfClearEvent());
    }

    public List<PdfModel> getStarredPdfs() {
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery = getReadableDb().rawQuery("SELECT * FROM stared_pdfs ORDER BY created_at DESC", (String[]) null);
        if (rawQuery.moveToFirst()) {
            do {
                String string = rawQuery.getString(rawQuery.getColumnIndex("path"));
                File file = new File(string);
                if (file.exists()) {
                    PdfModel pdfModel = new PdfModel();
                    pdfModel.setName(file.getName());
                    pdfModel.setAbsolutePath(file.getAbsolutePath());
                    pdfModel.setPdfUri(file.toString());
                    pdfModel.setLength(Long.valueOf(file.length()));
                    pdfModel.setLastModified(Long.valueOf(file.lastModified()));
                    pdfModel.setThumbUri("imageUriFromPath");
                    pdfModel.setStarred(true);
                    arrayList.add(pdfModel);
                } else {
                    removeStaredPDF(string);
                }
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        closeDb();
        return arrayList;
    }

    public void addStaredPDF(String str) {
        SQLiteDatabase readableDb = getReadableDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", str);
        readableDb.replace(DbContract.StarredPDFEntry.TABLE_NAME, (String) null, contentValues);
        closeDb();
    }

    public void removeStaredPDF(String str) {
        getReadableDb().delete(DbContract.StarredPDFEntry.TABLE_NAME, "path =?", new String[]{str});
        closeDb();
    }

    public void updateStaredPDF(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.StarredPDFEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStared(String str) {
        Cursor query = getReadableDb().query(DbContract.StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, (String) null, (String) null, (String) null);
        boolean moveToFirst = query.moveToFirst();
        query.close();
        closeDb();
        return moveToFirst;
    }

    public boolean isStared(SQLiteDatabase sQLiteDatabase, String str) {
        SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
        Cursor query = sQLiteDatabase2.query(DbContract.StarredPDFEntry.TABLE_NAME, new String[]{"path"}, "path =?", new String[]{str}, (String) null, (String) null, (String) null);
        boolean moveToFirst = query.moveToFirst();
        query.close();
        return moveToFirst;
    }

    public List<Uri> getAllImages(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = this.context.getContentResolver();
            Cursor query = contentResolver.query(uri, (String[]) null, "_data LIKE ? AND mime_type LIKE ? ", new String[]{"%" + str + "%", "%image/%"}, (String) null);
            if (query != null && query.moveToFirst()) {
                do {
                    String string = query.getString(query.getColumnIndex("_data"));
                    Log.d(this.TAG, string);
                    arrayList.add(Uri.fromFile(new File(string)));
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public int getLastOpenedPage(String str) {
        int i = 0;
        try {
            Cursor query = getReadableDb().query(DbContract.LastOpenedPageEntry.TABLE_NAME, new String[]{"page_number"}, "path = ? ", new String[]{str}, (String) null, (String) null, (String) null);
            if (query != null && query.moveToFirst()) {
                i = query.getInt(query.getColumnIndex("page_number"));
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeDb();
        return i;
    }

    public void addLastOpenedPage(String str, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(DbContract.LastOpenedPageEntry.TABLE_NAME, (String) null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLastOpenedPagePath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.LastOpenedPageEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBookmark(String str, String str2, int i) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str);
            contentValues.put("title", str2);
            contentValues.put("page_number", Integer.valueOf(i));
            readableDb.replace(DbContract.BookmarkEntry.TABLE_NAME, (String) null, contentValues);
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookmarkModel> getBookmarks(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            Cursor query = getReadableDb().query(DbContract.BookmarkEntry.TABLE_NAME, new String[]{"title", "path", "page_number"}, "path = ? ", new String[]{str}, (String) null, (String) null, "created_at DESC");
            if (query != null && query.moveToFirst()) {
                do {
                    BookmarkModel bookmarkModel = new BookmarkModel();
                    bookmarkModel.setTitle(query.getString(query.getColumnIndex("title")));
                    bookmarkModel.setPageNumber(query.getInt(query.getColumnIndex("page_number")));
                    bookmarkModel.setPath(query.getString(query.getColumnIndex("path")));
                    arrayList.add(bookmarkModel);
                } while (query.moveToNext());
                query.close();
                closeDb();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void updateBookmarkPath(String str, String str2) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            ContentValues contentValues = new ContentValues();
            contentValues.put("path", str2);
            readableDb.update(DbContract.BookmarkEntry.TABLE_NAME, contentValues, "path =?", new String[]{str});
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBookmarks(List<BookmarkModel> list) {
        try {
            SQLiteDatabase readableDb = getReadableDb();
            int size = list.size();
            readableDb.beginTransaction();
            for (int i = 0; i < size; i++) {
                readableDb.delete(DbContract.BookmarkEntry.TABLE_NAME, "path = ? AND page_number = ? ", new String[]{list.get(i).getPath(), String.valueOf(list.get(i).getPageNumber())});
            }
            readableDb.setTransactionSuccessful();
            readableDb.endTransaction();
            closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
