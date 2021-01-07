package com.example.voicerecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RecordFragment extends Fragment {

    private FloatingActionButton recordButton = null;
    private TextView recordingPrompt = null;
    private EditText recordName;
    private ImageView completeRecord, cancelRecord;
    private String pathSave = "";
    private MediaRecorder mediaRecorder;
    private long pauseOffset;
    private final int REQUEST_PERMISSION_CODE = 1000;
    private boolean startRecording = false, pauseState = false;
    private Chronometer chronometer = null;

    private RecordsViewModel recordsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.record_layout,container,false);

        init(view);
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

                            File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                            pathSave = path.toString() + "/" + System.currentTimeMillis() + "audio.mp3";
                            Log.e("Record Error", pathSave);

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
                resetChronometer();
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                startRecording = false;
                pauseState = false;

                String name = "";
                if(recordName.getText().toString().isEmpty())
                    name = "audio" + System.currentTimeMillis() + ".mp3";
                else
                    name = recordName.getText().toString() + System.currentTimeMillis() + ".mp3";
                String duration = getRecordDuration(pathSave);
                String recordSize = getRecordSize(pathSave);
                String dateTime = getCurrDateTime();

                RecordModel record = new RecordModel(pathSave, name, duration, recordSize, dateTime);
                recordsViewModel.insertRecord(record);
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

    private void init(View view) {
        chronometer = (Chronometer)view.findViewById(R.id.timer);
        recordingPrompt = view.findViewById(R.id.record_status);
        recordButton = view.findViewById(R.id.record_btn);
        recordName = view.findViewById(R.id.record_name_et);
        completeRecord = view.findViewById(R.id.complete_recordBtn);
        cancelRecord = view.findViewById(R.id.cancel_recordBtn);
        recordsViewModel = ViewModelProviders.of(getActivity()).get(RecordsViewModel.class);
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

    public String getRecordSize(String path)
    {
        String recordSize;
        File file = new File(path);
        int sizeInKB = (int) (file.length() / 1024);
        if(sizeInKB >= 1024) {
            recordSize = String.valueOf(sizeInKB / 1024) + " MB";
        }
        else{
            recordSize = String.valueOf(sizeInKB) + " KB";
        }
        return recordSize;
    }

    public String getRecordDuration(String path)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int timeInMilliSecond = Integer.parseInt(durationStr);
        String finalFormat = formatMilliSeconds(timeInMilliSecond);
        return finalFormat;
    }

    public String formatMilliSeconds(int milliSeconds)
    {
        String finalTimerString = "";
        String secondsString = "";

        int hours = milliSeconds / (1000 * 60 * 60);
        int minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        if(hours > 0) {
            finalTimerString = hours + ":";
        }
        if(seconds < 10) {
            secondsString = "0" + seconds;
        }
        else {
            secondsString = "" + seconds;
        }
        if(minutes > 10)
            finalTimerString = finalTimerString + minutes + ":" + secondsString;
        else
            finalTimerString = finalTimerString + "0" + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public String getCurrDateTime()
    {
        Date currentTime = Calendar.getInstance().getTime();
        String curDate = currentTime.toString();
        String dateTime = "";
        for(int i = 0; i < curDate.length(); i ++)
        {
            if(curDate.charAt(i) == 'G' && curDate.charAt(i+1) == 'M' && curDate.charAt(i+2) == 'T')
                break;
            dateTime += curDate.charAt(i);
        }
        return dateTime;
    }
}
