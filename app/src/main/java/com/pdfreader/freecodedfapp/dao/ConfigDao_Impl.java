package com.pdfreader.freecodedfapp.dao;

import android.database.Cursor;

import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.sqlite.db.SupportSQLiteStatement;

import com.google.ads.mediation.facebook.FacebookAdapter;
import com.pdfreader.freecodedfapp.models.ConfigModel;

public final class ConfigDao_Impl implements ConfigDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter __insertionAdapterOfConfig;
    private final SharedSQLiteStatement __preparedStmtOfIncrementConfigValue;
    private final SharedSQLiteStatement __preparedStmtOfSetRewarded;

    public ConfigDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfConfig = new EntityInsertionAdapter<ConfigModel>(roomDatabase) {
            public String createQuery() {
                return "INSERT OR ABORT INTO `configurations`(`id`,`key`,`value`,`is_rewarded`) VALUES (nullif(?, 0),?,?,?)";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, ConfigModel configModel) {
                supportSQLiteStatement.bindLong(1, (long) configModel.getId());
                supportSQLiteStatement.bindLong(2, (long) configModel.getKey());
                supportSQLiteStatement.bindLong(3, (long) configModel.getValue());
                supportSQLiteStatement.bindLong(4, configModel.isRewarded() ? 1 : 0);
            }
        };
        this.__preparedStmtOfIncrementConfigValue = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "UPDATE configurations SET value = value + 1 WHERE `key` == ?";
            }
        };
        this.__preparedStmtOfSetRewarded = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "UPDATE configurations SET is_rewarded = ? WHERE `key` == ?";
            }
        };
    }

    public void insert(ConfigModel configModel) {
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfConfig.insert(configModel);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void incrementConfigValue(int i) {
        SupportSQLiteStatement acquire = this.__preparedStmtOfIncrementConfigValue.acquire();
        this.__db.beginTransaction();
        try {
            acquire.bindLong(1, (long) i);
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfIncrementConfigValue.release(acquire);
        }
    }

    public void setRewarded(int i, int i2) {
        SupportSQLiteStatement acquire = this.__preparedStmtOfSetRewarded.acquire();
        this.__db.beginTransaction();
        try {
            acquire.bindLong(1, (long) i2);
            acquire.bindLong(2, (long) i);
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetRewarded.release(acquire);
        }
    }

    public ConfigModel getConfigBykey(int i) {
        ConfigModel configModel;
        boolean z = true;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM configurations WHERE `key` == ?", 1);
        acquire.bindLong(1, (long) i);
        Cursor query = this.__db.query(acquire);
        try {
            int columnIndexOrThrow = query.getColumnIndexOrThrow(FacebookAdapter.KEY_ID);
            int columnIndexOrThrow2 = query.getColumnIndexOrThrow("key");
            int columnIndexOrThrow3 = query.getColumnIndexOrThrow("value");
            int columnIndexOrThrow4 = query.getColumnIndexOrThrow("is_rewarded");
            if (query.moveToFirst()) {
                configModel = new ConfigModel();
                configModel.setId(query.getInt(columnIndexOrThrow));
                configModel.setKey(query.getInt(columnIndexOrThrow2));
                configModel.setValue(query.getInt(columnIndexOrThrow3));
                if (query.getInt(columnIndexOrThrow4) == 0) {
                    z = false;
                }
                configModel.setRewarded(z);
            } else {
                configModel = null;
            }
            return configModel;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
