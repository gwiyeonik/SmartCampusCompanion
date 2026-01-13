package com.example.smartcampuscompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduledEventAdapter
        extends RecyclerView.Adapter<ScheduledEventAdapter.ViewHolder> {

    private List<ScheduledEventModel> list;

    public ScheduledEventAdapter(List<ScheduledEventModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scheduled_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        ScheduledEventModel e = list.get(position);

        holder.tvTitle.setText(e.getTitle());
        holder.tvDate.setText(e.getDate());
        holder.tvType.setText(e.getType());
        holder.tvPriority.setText(e.getPriority());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvType, tvPriority;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvType = itemView.findViewById(R.id.tv_type);
            tvPriority = itemView.findViewById(R.id.tv_priority);
        }
    }
}
