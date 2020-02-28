package com.example.voicerecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingService extends Service {

    String file_name ;
    MediaRecorder mediaRecorder = null;
    DBHelper dbHelper;
    long startingTimeMills = 0;
    long elapsedMills = 0;

    File file;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        file_name = "audio_"+ts;
        file = new File(Environment.getExternalStorageDirectory()+"/MySoundRec/"+file_name+".mp3");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startingTimeMills = System.currentTimeMillis();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if(mediaRecorder != null)
        {
            stopRecording();
        }
        super.onDestroy();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        elapsedMills = (System.currentTimeMillis() - startingTimeMills);
        mediaRecorder.release();
        Toast.makeText(getApplicationContext(), "Recording saved "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        //database
        RecordingItem recordingItem = new RecordingItem(file.getAbsolutePath());
        dbHelper.addRecording(recordingItem);
    }
}
