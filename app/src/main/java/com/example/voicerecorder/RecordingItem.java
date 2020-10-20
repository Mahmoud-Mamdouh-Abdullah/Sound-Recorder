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
    private int recordDuration;

    @Ignore
    public RecordingItem(){}

    public RecordingItem(String path) {
        this.path = path;
    }

    @Ignore
    public RecordingItem(String path, String recordName, int recordDuration) {
        this.path = path;
        this.recordName = recordName;
        this.recordDuration = recordDuration;
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

    public int getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(int recordDuration) {
        this.recordDuration = recordDuration;
    }
}
