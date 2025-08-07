package com.pdfreader.freecodedfapp.db;

import androidx.room.RoomDatabase;

import com.pdfreader.freecodedfapp.dao.ConfigDao;

public abstract class AppDatabase extends RoomDatabase {
    public abstract ConfigDao configDao();
}
