package com.example.smartcampuscompanion;import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private final List<EventModel> eventList;

    public EventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This inflates (creates) the view for each item from your XML layout file.
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.eventName.setText(event.getTitle());

        String imagePath = event.getImageUrl();

        // If it's a Drive link, make sure it's the 'uc' (direct) version
        if (imagePath != null && imagePath.contains("drive.google.com") && imagePath.contains("/file/d/")) {
            String fileId = imagePath.split("/d/")[1].split("/")[0];
            imagePath = "https://drive.google.com/uc?export=view&id=" + fileId;
        }

        Glide.with(context)
                .load(imagePath) // Works for both Firebase Storage and fixed Drive links
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.eventImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            context.startActivity(intent);
        });
    }

    public void updateList(List<EventModel> newList) {
        this.eventList.clear();
        this.eventList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        // This tells the RecyclerView how many items are in the list.
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        // These are the views inside your item_event.xml layout
        ImageView eventImage;
        TextView eventName;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure these IDs exist in your item_event.xml
            eventImage = itemView.findViewById(R.id.iv_event_image);
            eventName = itemView.findViewById(R.id.tv_event_name);
        }
    }
}
