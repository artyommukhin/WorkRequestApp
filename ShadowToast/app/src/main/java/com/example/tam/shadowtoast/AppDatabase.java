package com.example.tam.shadowtoast;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = WorkRequest.class, version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter.class)

public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkRequestDao getWorkRequestDao();
}
