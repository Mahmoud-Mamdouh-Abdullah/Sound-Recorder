package com.example.voicerecorder;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.CompletableObserver;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ListenFragment extends Fragment {

    private MediaPlayer mediaPlayer;
    private FloatingActionButton play;
    private boolean playing_stat;
    private RecyclerView recordRecyclerView;
    private ArrayList<RecordingItem> recordsList;
    private Chronometer chronometer = null;
    private long pauseOffset;
    private SeekBar seekBar;

    Runnable runnable;
    Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listen_layout,container,false);

        play = view.findViewById(R.id.play_btn);
        chronometer = view.findViewById(R.id.timer);
        seekBar = view.findViewById(R.id.seekBar);
        playing_stat = false;
        recordRecyclerView = (RecyclerView) view.findViewById(R.id.recordRecyclerView);
        final RecordsAdapter recordsAdapter = new RecordsAdapter();
        recordRecyclerView.setAdapter(recordsAdapter);

        handler = new Handler();
        final RecordsDatabase recordsDatabase = RecordsDatabase.getInstance(getActivity());
        recordsDatabase.recordsDao().getRecords()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<RecordingItem>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull List<RecordingItem> recordingItems) {
                        recordsAdapter.setList((ArrayList<RecordingItem>) recordingItems);
                        recordsList = (ArrayList<RecordingItem>) recordingItems;
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }
                });

        recordsAdapter.setOnRecordClickListener(new RecordsAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(int position) {
                String filePath = recordsList.get(position).getPath();
                if(mediaPlayer!= null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                resetChronometer();
                startChronometer();
                updateSeekBar();
                playing_stat = true;
                play.setImageResource(R.drawable.pause_foreground);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playing_stat = false;
                        play.setImageResource(R.drawable.play_foreground);
                        pauseChronometer();
                    }
                });
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing_stat == true)
                {
                    if(mediaPlayer != null && mediaPlayer.isPlaying())
                    {
                        mediaPlayer.pause();
                        play.setImageResource(R.drawable.play_foreground);
                        pauseChronometer();
                    }
                    else {
                        mediaPlayer.start();
                        play.setImageResource(R.drawable.pause_foreground);
                        startChronometer();
                    }
                }
            }
        });

        return view;
    }

    public void startChronometer()
    {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
    }
    public void resetChronometer()
    {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    public void pauseChronometer()
    {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
    }

    public void updateSeekBar() {
        int curPos = mediaPlayer.getCurrentPosition();
        seekBar.setProgress(curPos);

        runnable = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
            }
        };
        handler.postDelayed(runnable, 1000);
    }
}
