package com.example.voicerecorder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.voicerecorder.RecordingItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface RecordsDao {

    @Insert
    Completable insertRecord(RecordingItem recordingItem);

    @Query("select * from records_table")
    Single<List<RecordingItem>> getRecords();

}
