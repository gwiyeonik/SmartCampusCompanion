package com.example.smartcampuscompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar; // Import RatingBar
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Map<String, Object>> commentsList;

    public CommentAdapter(List<Map<String, Object>> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = commentsList.get(position);

        String name = (String) data.get("userName");
        String comment = (String) data.get("comment");
        Object ratingObj = data.get("rating");
        Object timestampObj = data.get("timestamp");

        holder.tvAuthor.setText(name != null ? name : "Anonymous");
        holder.tvComment.setText(comment != null ? comment : "");

        // --- SAFE TIMESTAMP HANDLING ---
        java.util.Date dateValue = null;

        if (timestampObj instanceof com.google.firebase.Timestamp) {
            dateValue = ((com.google.firebase.Timestamp) timestampObj).toDate();
        } else if (timestampObj instanceof java.util.Date) {
            dateValue = (java.util.Date) timestampObj;
        }

        if (dateValue != null && holder.tvDate != null) {
            String formattedDate = android.text.format.DateFormat.format("dd MMM yyyy", dateValue).toString();
            holder.tvDate.setText(formattedDate);
            holder.tvDate.setVisibility(View.VISIBLE);
        } else if (holder.tvDate != null) {
            holder.tvDate.setText("Just now");
        }

        // --- SAFE RATING HANDLING ---
        if (ratingObj instanceof Number) {
            float ratingValue = ((Number) ratingObj).floatValue();
            holder.ratingBar.setRating(ratingValue);
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvComment, tvDate;
        RatingBar ratingBar; // Add RatingBar variable

        public ViewHolder(View itemView) {
            super(itemView);
            // --- THIS IS THE FIX ---
            // Ensure these IDs EXACTLY match your item_comment.xml
            tvAuthor = itemView.findViewById(R.id.tv_comment_author);
            tvComment = itemView.findViewById(R.id.tv_comment_text);
            ratingBar = itemView.findViewById(R.id.comment_rating_bar);
            tvDate = itemView.findViewById(R.id.tv_comment_date);
            // --- END OF FIX ---
        }
    }
}
