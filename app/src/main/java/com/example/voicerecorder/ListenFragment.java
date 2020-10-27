package com.example.voicerecorder;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListenFragment extends Fragment {

    private MediaPlayer mediaPlayer;
    private FloatingActionButton play;
    private boolean playing_stat;
    private RecyclerView recordRecyclerView;
    private ArrayList<RecordModel> recordsList;
    private Chronometer chronometer = null;
    private long pauseOffset;
    private SeekBar seekBar;
    private RecordsAdapter recordsAdapter;
    private Runnable runnable;
    private Handler handler;

    private RecordsViewModel recordsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listen_layout,container,false);


        init(view);

        recordsViewModel = ViewModelProviders.of(getActivity()).get(RecordsViewModel.class);
        recordsViewModel.getAllRecords().observe(this, new Observer<List<RecordModel>>() {
            @Override
            public void onChanged(List<RecordModel> recordModels) {
                recordsAdapter.setList((ArrayList<RecordModel>) recordModels);
                recordsList = (ArrayList<RecordModel>) recordModels;
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
                    if(mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                recordsViewModel.deleteRecord(recordsAdapter.getRecordAt(viewHolder.getAdapterPosition()));
                Toast.makeText(getActivity(), "Record Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recordRecyclerView);



        return view;
    }

    private void init(View view) {
        play = view.findViewById(R.id.play_btn);
        chronometer = view.findViewById(R.id.timer);
        seekBar = view.findViewById(R.id.seekBar);
        playing_stat = false;
        recordRecyclerView = (RecyclerView) view.findViewById(R.id.recordRecyclerView);
        recordsAdapter = new RecordsAdapter();
        recordRecyclerView.setAdapter(recordsAdapter);
        handler = new Handler();
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
