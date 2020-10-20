package com.example.voicerecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.CompletableObserver;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class record_freg extends Fragment {

    private FloatingActionButton recordButton = null;
    private TextView recordingPrompt = null;
    private ImageButton completeRecord, cancelRecord;
    private String pathSave = "";
    private MediaRecorder mediaRecorder;
    private long pauseOffset;
    private final int REQUEST_PERMISSION_CODE = 1000;

    int RecordingPromptCount  = 0;
    boolean startRecording = false, pauseState = false;
    Chronometer chronometer = null;
    long timeWhenPaused  = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.record_layout,container,false);
        chronometer = (Chronometer)view.findViewById(R.id.timer);
        recordingPrompt = view.findViewById(R.id.record_status);
        recordButton = view.findViewById(R.id.record_btn);
        completeRecord = view.findViewById(R.id.complete_recordBtn);
        cancelRecord = view.findViewById(R.id.cancel_recordBtn);
        recordButton.setBackgroundColor(getResources().getColor(R.color.color_btn));
        recordButton.setRippleColor(getResources().getColor(R.color.color_hover));

        final RecordsDatabase recordsDatabase = RecordsDatabase.getInstance(getActivity());


        if(!checkPermissionFromDevice())
            requestPermissions();

        recordButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                if(startRecording == false)
                {
                    if(checkPermissionFromDevice())
                    {
                        startChronometer();
                        if(pauseState == true)
                        {
                            mediaRecorder.resume();
                            recordButton.setImageResource(R.drawable.pause_foreground);
                        }
                        else
                        {
                            recordButton.setImageResource(R.drawable.pause_foreground);
                            completeRecord.setVisibility(View.VISIBLE);
                            cancelRecord.setVisibility(View.VISIBLE);
                            pathSave = getActivity().getExternalCacheDir().getAbsolutePath();
                            pathSave += "/" + System.currentTimeMillis() + "audio.mp3";

                            setupMediaRecorder();

                            try {
                                mediaRecorder.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaRecorder.start();
                            Toast.makeText(getActivity(), "Recording", Toast.LENGTH_SHORT).show();
                        }
                        startRecording = true;

                    }
                    else
                    {
                        requestPermissions();
                    }
                }
                else
                {
                    pauseChronometer();
                    mediaRecorder.pause();
                    recordButton.setImageResource(R.drawable.mic_foreground);
                    pauseState = true;
                    startRecording = false;
                }
            }
        });

        completeRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeInSecond = SystemClock.elapsedRealtime() - chronometer.getBase();
                timeInSecond = timeInSecond / 1000 + 1;
                resetChronometer();
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                startRecording = false;
                pauseState = false;
                RecordingItem record = new RecordingItem(pathSave);
                recordsDatabase.recordsDao().insertRecord(record)
                        .subscribeOn(Schedulers.computation())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            }
                        });
                recordButton.setImageResource(R.drawable.mic_foreground);
                completeRecord.setVisibility(View.INVISIBLE);
                cancelRecord.setVisibility(View.INVISIBLE);
            }
        });

        cancelRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetChronometer();
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                startRecording = false;
                pauseState = false;
                recordButton.setImageResource(R.drawable.mic_foreground);
                completeRecord.setVisibility(View.INVISIBLE);
                cancelRecord.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);

    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
                break;

        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    public void startChronometer()
    {
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        recordingPrompt.setVisibility(View.INVISIBLE);
    }
    public void resetChronometer()
    {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        chronometer.setVisibility(View.INVISIBLE);
        recordingPrompt.setVisibility(View.VISIBLE);
    }
    
    public void pauseChronometer()
    {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
    }
}
