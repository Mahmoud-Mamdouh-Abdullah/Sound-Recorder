package com.example.voicerecorder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;


public class RecordsViewModel extends AndroidViewModel {
    private RecordRepository recordRepository;
    private LiveData<List<RecordModel>> allRecords;

    public RecordsViewModel(@NonNull Application application) {
        super(application);
        recordRepository = new RecordRepository(application);
        allRecords = recordRepository.getAllRecords();
    }

    public void insertRecord(RecordModel recordModel) {
        recordRepository.InsertRecord(recordModel);
    }

    public void deleteRecord(RecordModel recordModel) {
        recordRepository.DeleteRecord(recordModel);
    }

    public void deleteAllRecords() {
        recordRepository.deleteAllRecords();
    }

    public LiveData<List<RecordModel>> getAllRecords() {
        return  allRecords;
    }
}
