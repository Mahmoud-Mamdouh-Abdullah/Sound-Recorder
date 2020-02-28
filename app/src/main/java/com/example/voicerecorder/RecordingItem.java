package com.example.voicerecorder;

import java.io.Serializable;

public class RecordingItem implements Serializable {

    private String path;

    public RecordingItem()
    {

    }

    public RecordingItem(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
