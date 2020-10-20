package com.example.voicerecorder;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class listen_freg extends Fragment {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    MediaPlayer mediaPlayer;
    TextView record_stat;
    FloatingActionButton play;
    boolean playing_stat;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listen_layout,container,false);

        play = view.findViewById(R.id.play_btn);
        playing_stat = false;
        record_stat = (TextView)view.findViewById(R.id.record_status);
        listView = (ListView)view.findViewById(R.id.list_item);
        arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.list);
        listView.setAdapter(arrayAdapter);

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
                        for(int i = 0; i < recordingItems.size(); i ++)
                        {
                            arrayAdapter.add(recordingItems.get(i).getPath());
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }
                });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = parent.getAdapter().getItem(position).toString();
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
                mediaPlayer.start();
                Toast.makeText(getActivity(), "Playing...", Toast.LENGTH_SHORT).show();
                record_stat.setText("Playing ...");
                playing_stat = true;
                play.setImageResource(R.drawable.pause_foreground);
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
                    }
                    else {
                        mediaPlayer.start();
                        play.setImageResource(R.drawable.pause_foreground);
                    }
                }
            }
        });

        return view;
    }
}
