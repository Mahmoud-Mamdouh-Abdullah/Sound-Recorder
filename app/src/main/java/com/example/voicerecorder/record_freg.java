package com.example.voicerecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class record_freg extends Fragment {

    FloatingActionButton recordButton = null;
    TextView recordingPrompt = null;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    final int REQUEST_PERMISSION_CODE = 1000;

    int RecordingPromptCount  = 0;
    boolean startRecording = false;
    Chronometer chronometer = null;
    long timeWhenPaused  = 0;

    DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.record_layout,container,false);
        chronometer = (Chronometer)view.findViewById(R.id.timer);
        recordingPrompt = view.findViewById(R.id.record_status);
        recordButton = view.findViewById(R.id.record_btn);
        recordButton.setBackgroundColor(getResources().getColor(R.color.color_btn));
        recordButton.setRippleColor(getResources().getColor(R.color.color_hover));

        dbHelper = new DBHelper(getActivity());

        if(!checkPermissionFromDevice())
            requestPermissions();

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(startRecording == false)
                {
                    start_timer();
                    if(checkPermissionFromDevice())
                    {
                        recordButton.setImageResource(R.drawable.stop_foreground);
                        pathSave = Environment.getExternalStorageDirectory()
                                .getAbsolutePath()+"/"
                                + UUID.randomUUID().toString() + "_audio_record.mp3";
                        setupMediaRecorder();
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                        Toast.makeText(getActivity(), "Recording", Toast.LENGTH_SHORT).show();
                        startRecording = true;
                    }
                    else
                    {
                        requestPermissions();
                    }
                }
                else
                {
                    stop_timer();
                    mediaRecorder.stop();
                    recordButton.setImageResource(R.drawable.mic_foreground);
                    RecordingItem record = new RecordingItem(pathSave);
                    dbHelper.addRecording(record);
                    startRecording = false;
                }
            }
        });
        return view;
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
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

    public void start_timer()
    {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(RecordingPromptCount == 0)
                {
                    recordingPrompt.setText("Recording" + ".");
                }
                else if(RecordingPromptCount == 1)
                {
                    recordingPrompt.setText("Recording" + "..");
                }
                else  if(RecordingPromptCount == 2)
                {
                    recordingPrompt.setText("Recording" + "...");
                    RecordingPromptCount = -1;
                }
                RecordingPromptCount ++;
            }
        });
        recordingPrompt.setText("Recording"+".");
    }
    public void stop_timer()
    {
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        recordingPrompt.setText("Tap the button to start recording");
    }














    private void onRecord(boolean start) {
        Intent i = new Intent(getActivity(),RecordingService.class);
        if(start)
        {
            recordButton.setImageResource(R.drawable.stop_foreground);
            Toast.makeText(getActivity(),"Recording started",Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory()+"/SoundRecorder");
            if(!folder.exists())
            {
                folder.mkdir();

            }
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if(RecordingPromptCount == 0)
                    {
                        recordingPrompt.setText("Recording" + ".");
                    }
                    else if(RecordingPromptCount == 1)
                    {
                        recordingPrompt.setText("Recording" + "..");
                    }
                    else  if(RecordingPromptCount == 2)
                    {
                        recordingPrompt.setText("Recording" + "...");
                        RecordingPromptCount = -1;
                    }
                    RecordingPromptCount ++;
                }
            });

            getActivity().startService(i);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            recordingPrompt.setText("Recording"+".");
            RecordingPromptCount++;

        }
        else
        {
            recordButton.setImageResource(R.drawable.mic_foreground);
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            recordingPrompt.setText("Tap the button to start recording");
            Objects.requireNonNull(getActivity()).startService(i);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
