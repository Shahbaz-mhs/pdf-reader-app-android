package com.pdfreader.freecodedfapp.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseClient {
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);
    private static final String DB_NAME = "allpdf_configs.db";
    private static final int NUMBER_OF_THREADS = 4;
    private static DatabaseClient databaseClient;
    RoomDatabase.Callback callback = new RoomDatabase.Callback() {
        public void onCreate(SupportSQLiteDatabase supportSQLiteDatabase) {
            super.onCreate(supportSQLiteDatabase);
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (0, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (1, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (2, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (3, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (4, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (5, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (6, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (7, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (8, 3, 1)");
            supportSQLiteDatabase.execSQL("INSERT INTO configurations (`key`, value, is_rewarded) VALUES (12, 3, 1)");
        }
    };
    private final AppDatabase appDatabase;
    private final Context context;

    private DatabaseClient(Context context2) {
        this.context = context2;
        this.appDatabase = Room.databaseBuilder(context2, AppDatabase.class, DB_NAME).addCallback(this.callback).build();
    }

    public static synchronized DatabaseClient getInstance(Context context2) {
        DatabaseClient databaseClient2;
        synchronized (DatabaseClient.class) {
            if (databaseClient == null) {
                databaseClient = new DatabaseClient(context2);
            }
            databaseClient2 = databaseClient;
        }
        return databaseClient2;
    }

    public AppDatabase getAppDatabase() {
        return this.appDatabase;
    }
}
