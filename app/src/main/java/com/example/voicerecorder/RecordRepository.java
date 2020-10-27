package com.example.voicerecorder;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.voicerecorder.database.RecordsDao;
import com.example.voicerecorder.database.RecordsDatabase;

import java.util.List;

public class RecordRepository {
    private RecordsDao recordsDao;
    private LiveData<List<RecordModel>> allRecords;

    public RecordRepository(Context context) {
        RecordsDatabase recordsDatabase = RecordsDatabase.getInstance(context);
        recordsDao = recordsDatabase.recordsDao();
        allRecords = recordsDao.getRecords();
    }

    public void InsertRecord(RecordModel recordModel) {
        new InsertRecordAsyncTask(recordsDao).execute(recordModel);
    }

    public void DeleteRecord(RecordModel recordModel) {
        new DeleteRecordAsyncTask(recordsDao).execute(recordModel);
    }

    public void deleteAllRecords(){
        new DeleteAllRecordAsyncTask(recordsDao).execute();
    }
    public LiveData<List<RecordModel>> getAllRecords() {
        return allRecords;
    }

    private static class InsertRecordAsyncTask extends AsyncTask<RecordModel, Void, Void> {
        private RecordsDao recordsDao;
        private InsertRecordAsyncTask(RecordsDao recordsDao) {
            this.recordsDao = recordsDao;
        }
        @Override
        protected Void doInBackground(RecordModel... recordModels) {
            recordsDao.insertRecord(recordModels[0]);
            return null;
        }
    }

    private static class DeleteRecordAsyncTask extends AsyncTask<RecordModel, Void, Void> {
        private RecordsDao recordsDao;
        private DeleteRecordAsyncTask(RecordsDao recordsDao) {
            this.recordsDao = recordsDao;
        }
        @Override
        protected Void doInBackground(RecordModel... recordModels) {
            recordsDao.deleteRecord(recordModels[0]);
            return null;
        }
    }

    private static class DeleteAllRecordAsyncTask extends AsyncTask<Void, Void, Void> {
        private RecordsDao recordsDao;
        private DeleteAllRecordAsyncTask(RecordsDao recordsDao) {
            this.recordsDao = recordsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recordsDao.deleteAllRecords();
            return null;
        }
    }
}
