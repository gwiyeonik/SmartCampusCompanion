package com.example.smartcampuscompanion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyReportsAdapter extends RecyclerView.Adapter<MyReportsAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyReport> reportList;
    private FirebaseFirestore db;

    public MyReportsAdapter(Context context, List<EmergencyReport> reportList) {
        this.context = context;
        this.reportList = reportList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_my_reports, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        EmergencyReport report = reportList.get(position);

        holder.tvType.setText(report.getType());
        holder.tvDescription.setText(report.getDescription());

        // UPDATE
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateEmergencyReportActivity.class);
            intent.putExtra("reportId", report.getId());
            intent.putExtra("type", report.getType());
            intent.putExtra("description", report.getDescription());
            context.startActivity(intent);
        });

        // DELETE
        holder.btnDelete.setOnClickListener(v -> {
            db.collection("emergency_reports")
                    .document(report.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(
                                context,
                                "Report deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                        reportList.remove(position);
                        notifyItemRemoved(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvDescription;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
