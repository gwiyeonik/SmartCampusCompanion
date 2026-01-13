package com.example.smartcampuscompanion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyReportAdapter
        extends RecyclerView.Adapter<EmergencyReportAdapter.ViewHolder> {

    private Context context;
    private List<EmergencyReport> reportList;

    public EmergencyReportAdapter(Context context, List<EmergencyReport> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_emergency_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        EmergencyReport report = reportList.get(position);

        holder.tvType.setText(report.getType());
        holder.tvDescription.setText(report.getDescription());
        holder.tvTime.setText(report.getTime());

        // Open location in Google Maps
        holder.imgLocation.setOnClickListener(v -> {
            String uri = "https://maps.google.com/?q="
                    + report.getLatitude() + ","
                    + report.getLongitude();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvDescription, tvTime;
        ImageView imgLocation;

        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgLocation = itemView.findViewById(R.id.imgLocation);
        }
    }
}
