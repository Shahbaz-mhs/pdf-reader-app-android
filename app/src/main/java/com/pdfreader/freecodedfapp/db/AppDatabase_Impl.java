package com.pdfreader.freecodedfapp.db;

import android.annotation.SuppressLint;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.google.ads.mediation.facebook.FacebookAdapter;
import com.pdfreader.freecodedfapp.dao.ConfigDao;
import com.pdfreader.freecodedfapp.dao.ConfigDao_Impl;

import java.util.HashMap;
import java.util.HashSet;

public final class AppDatabase_Impl extends AppDatabase {
    private volatile ConfigDao _configDao;

    /* access modifiers changed from: protected */
    public SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration databaseConfiguration) {
        return databaseConfiguration.sqliteOpenHelperFactory.create(SupportSQLiteOpenHelper.Configuration.builder(databaseConfiguration.context).name(databaseConfiguration.name).callback(new RoomOpenHelper(databaseConfiguration, new RoomOpenHelper.Delegate(1) {
            @SuppressLint("RestrictedApi")
            public void createAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `configurations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `key` INTEGER NOT NULL, `value` INTEGER NOT NULL, `is_rewarded` INTEGER NOT NULL)");
                supportSQLiteDatabase.execSQL(RoomMasterTable.CREATE_QUERY);
                supportSQLiteDatabase.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"3f1289e14c8d68f6249fd9c29cdfcfe1\")");
            }

            public void dropAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `configurations`");
            }

            /* access modifiers changed from: protected */
            public void onCreate(SupportSQLiteDatabase supportSQLiteDatabase) {
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onCreate(supportSQLiteDatabase);
                    }
                }
            }

            public void onOpen(SupportSQLiteDatabase supportSQLiteDatabase) {
                SupportSQLiteDatabase unused = AppDatabase_Impl.this.mDatabase = supportSQLiteDatabase;
                AppDatabase_Impl.this.internalInitInvalidationTracker(supportSQLiteDatabase);
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onOpen(supportSQLiteDatabase);
                    }
                }
            }

            /* access modifiers changed from: protected */
            public void validateMigration(SupportSQLiteDatabase supportSQLiteDatabase) {
                HashMap hashMap = new HashMap(4);
                hashMap.put(FacebookAdapter.KEY_ID, new TableInfo.Column(FacebookAdapter.KEY_ID, "INTEGER", true, 1));
                hashMap.put("key", new TableInfo.Column("key", "INTEGER", true, 0));
                hashMap.put("value", new TableInfo.Column("value", "INTEGER", true, 0));
                hashMap.put("is_rewarded", new TableInfo.Column("is_rewarded", "INTEGER", true, 0));
                TableInfo tableInfo = new TableInfo("configurations", hashMap, new HashSet(0), new HashSet(0));
                TableInfo read = TableInfo.read(supportSQLiteDatabase, "configurations");
                if (!tableInfo.equals(read)) {
                    throw new IllegalStateException("Migration didn't properly handle configurations(Config).\n Expected:\n" + tableInfo + "\n Found:\n" + read);
                }
            }
        }, "3f1289e14c8d68f6249fd9c29cdfcfe1", "7a5087be8e26be93fbf2f44c053d807b")).build());
    }

    /* access modifiers changed from: protected */
    public InvalidationTracker createInvalidationTracker() {
        return new InvalidationTracker(this, "configurations");
    }

    public void clearAllTables() {
        super.assertNotMainThread();
        SupportSQLiteDatabase writableDatabase = super.getOpenHelper().getWritableDatabase();
        try {
            super.beginTransaction();
            writableDatabase.execSQL("DELETE FROM `configurations`");
            super.setTransactionSuccessful();
        } finally {
            super.endTransaction();
            writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close();
            if (!writableDatabase.inTransaction()) {
                writableDatabase.execSQL("VACUUM");
            }
        }
    }

    public ConfigDao configDao() {
        ConfigDao configDao;
        if (this._configDao != null) {
            return this._configDao;
        }
        synchronized (this) {
            if (this._configDao == null) {
                this._configDao = new ConfigDao_Impl(this);
            }
            configDao = this._configDao;
        }
        return configDao;
    }
}
