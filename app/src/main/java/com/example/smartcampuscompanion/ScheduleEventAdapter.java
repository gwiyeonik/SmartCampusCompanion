package com.example.smartcampuscompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleEventAdapter
        extends RecyclerView.Adapter<ScheduleEventAdapter.ViewHolder> {

    private final List<ScheduleEventModel> eventList;

    public ScheduleEventAdapter(List<ScheduleEventModel> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_event_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleEventModel model = eventList.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvDate.setText(model.getDate());
        holder.tvType.setText(model.getType());
        holder.tvPriority.setText(model.getPriority());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvType, tvPriority;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvType = itemView.findViewById(R.id.tv_event_type);
            tvPriority = itemView.findViewById(R.id.tv_event_priority);
        }
    }
}
