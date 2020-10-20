package com.example.voicerecorder;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = RecordingItem.class, version = 1, exportSchema = false)
public abstract class RecordsDatabase extends RoomDatabase {
    private static RecordsDatabase instance;
    public abstract RecordsDao recordsDao();

    public static synchronized RecordsDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    RecordsDatabase.class, "records_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
