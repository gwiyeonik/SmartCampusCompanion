package com.example.smartcampuscompanion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TimetableAdapter
        extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableModel> timetableList;
    private FirebaseFirestore db;

    public TimetableAdapter(List<TimetableModel> timetableList) {
        this.timetableList = timetableList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        TimetableModel model = timetableList.get(position);

        holder.tvSubject.setText(model.getSubject());

        String day = model.getDay() != null ? model.getDay() : "Day";
        String time = model.getTime() != null ? model.getTime() : "Time not set";
        holder.tvDayTime.setText(day + " | " + time);

        holder.tvLocation.setText(model.getLocation());

        // ✅ TAP → EDIT
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddTimetableActivity.class);
            intent.putExtra("DOC_ID", model.getDocumentId());
            intent.putExtra("SUBJECT", model.getSubject());
            intent.putExtra("DAY", model.getDay());
            intent.putExtra("TIME", model.getTime());
            intent.putExtra("LOCATION", model.getLocation());
            v.getContext().startActivity(intent);
        });

        // 🔥 LONG PRESS → DELETE
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(v.getContext(), model, position);
            return true;
        });

        // 🧭 NAVIGATE (STEP 2 – placeholder)
        holder.tvNavigate.setOnClickListener(v -> {

            String location = model.getLocation();

            if (location == null || location.isEmpty()) {
                Toast.makeText(
                        v.getContext(),
                        "Location not available",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Google Maps search intent
            Uri gmmIntentUri = Uri.parse(
                    "geo:0,0?q=" + Uri.encode(location)
            );

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                v.getContext().startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(
                        v.getContext(),
                        "Google Maps not installed",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return timetableList.size();
    }

    private void showDeleteDialog(
            Context context,
            TimetableModel model,
            int position
    ) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Timetable")
                .setMessage("Are you sure you want to delete this timetable?")
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteTimetable(model, position)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTimetable(TimetableModel model, int position) {
        if (model.getDocumentId() == null) return;

        db.collection("timetables")
                .document(model.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    timetableList.remove(position);
                    notifyItemRemoved(position);
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubject, tvDayTime, tvLocation, tvNavigate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvDayTime = itemView.findViewById(R.id.tv_day_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvNavigate = itemView.findViewById(R.id.tv_navigate); // ✅ FIX
        }
    }
}
