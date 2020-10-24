package com.example.voicerecorder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "records_table")
public class RecordingItem implements Serializable {

    @PrimaryKey
    @NonNull
    private String path;
    private String recordName;
    private String recordDuration;
    private String recordSize;
    private String dateTime;


    @Ignore
    public RecordingItem(String path) {
        this.path = path;
    }

    public RecordingItem(@NonNull String path, String recordName, String recordDuration, String recordSize, String dateTime) {
        this.path = path;
        this.recordName = recordName;
        this.recordDuration = recordDuration;
        this.recordSize = recordSize;
        this.dateTime = dateTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public String getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(String recordDuration) {
        this.recordDuration = recordDuration;
    }

    public String getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(String recordSize) {
        this.recordSize = recordSize;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
