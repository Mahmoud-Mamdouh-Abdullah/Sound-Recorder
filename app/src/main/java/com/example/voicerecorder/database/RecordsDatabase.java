package com.example.voicerecorder.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.voicerecorder.RecordModel;

@Database(entities = RecordModel.class, version = 1, exportSchema = false)
public abstract class RecordsDatabase extends RoomDatabase {
    private static RecordsDatabase instance;
    public abstract RecordsDao recordsDao();

    public static synchronized RecordsDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    RecordsDatabase.class, "record_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private RecordsDao recordsDao;

        private PopulateDbAsyncTask(RecordsDatabase recordsDatabase) {
            recordsDao = recordsDatabase.recordsDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
