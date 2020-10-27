package com.example.voicerecorder.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.voicerecorder.RecordModel;

import java.util.List;

@Dao
public interface RecordsDao {

    @Insert
    void insertRecord(RecordModel recordModel);

    @Delete
    void deleteRecord(RecordModel recordModel);

    @Query("delete from records_table")
    void deleteAllRecords();

    @Query("select * from records_table")
    LiveData<List<RecordModel>> getRecords();

}
