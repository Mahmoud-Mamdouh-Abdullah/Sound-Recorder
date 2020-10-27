package com.example.voicerecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordsViewHolder> {
    private ArrayList<RecordModel> recordsList = new ArrayList<>();
    private OnRecordClickListener mOnRecordClickListener;

    public interface OnRecordClickListener {
        void onRecordClick(int position);
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        mOnRecordClickListener = listener;
    }

    @NonNull
    @Override
    public RecordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_list_item, parent, false);
        return new RecordsViewHolder(view, mOnRecordClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsViewHolder holder, int position) {
        holder.recordName.setText(recordsList.get(position).getRecordName());
        holder.recordSize.setText(recordsList.get(position).getRecordSize());
        holder.duration.setText(recordsList.get(position).getRecordDuration());
        holder.dateTime.setText(recordsList.get(position).getDateTime());
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    public void setList(ArrayList<RecordModel> recordsList) {
        this.recordsList = recordsList;
        notifyDataSetChanged();
    }

    public RecordModel getRecordAt(int position) {
        return recordsList.get(position);
    }

    public class RecordsViewHolder extends RecyclerView.ViewHolder {
        TextView recordName, dateTime, duration, recordSize;
        public RecordsViewHolder(@NonNull View itemView, final OnRecordClickListener listener) {
            super(itemView);
            recordName = itemView.findViewById(R.id.recordName);
            dateTime = itemView.findViewById(R.id.dateTime);
            duration = itemView.findViewById(R.id.duration_text);
            recordSize = itemView.findViewById(R.id.record_size);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onRecordClick(position);
                        }
                    }
                }
            });
        }
    }
}
