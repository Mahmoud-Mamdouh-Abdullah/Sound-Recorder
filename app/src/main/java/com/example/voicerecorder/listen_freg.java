package com.example.voicerecorder;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class listen_freg extends Fragment {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    DBHelper dbHelper;
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
        dbHelper = new DBHelper(getContext());
        Cursor cursor = dbHelper.fetchAllRecords();
        while (!cursor.isAfterLast())
        {
            arrayAdapter.add(cursor.getString(0));
            cursor.moveToNext();
        }
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
