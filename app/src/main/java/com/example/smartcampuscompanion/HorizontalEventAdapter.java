package com.example.smartcampuscompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import android.content.Intent;

public class HorizontalEventAdapter extends RecyclerView.Adapter<HorizontalEventAdapter.ViewHolder> {

    private Context context;
    private List<HorizontalEventModel> eventList;

    public HorizontalEventAdapter(Context context, List<HorizontalEventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Make sure you have a layout file named 'item_horizontal_event.xml'
        View view = LayoutInflater.from(context).inflate(R.layout.item_horizontal_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HorizontalEventModel event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        Glide.with(context).load(event.getImageUrl()).centerCrop().into(holder.ivImage);
        // Set a click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            // Create an Intent to open EventDetailActivity
            Intent intent = new Intent(context, EventDetailActivity.class);

            // Pass the unique event ID to the detail activity
            intent.putExtra("EVENT_ID", event.getEventId());

            // Start the activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure these IDs exist in 'item_horizontal_event.xml'
            ivImage = itemView.findViewById(R.id.iv_horizontal_image);
            tvTitle = itemView.findViewById(R.id.tv_horizontal_title);
        }
    }
}
    