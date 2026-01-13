package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventSchedulerListActivity extends AppCompatActivity {

    public static List<ScheduledEventModel> EVENT_LIST = new ArrayList<>();
    private List<ScheduledEventModel> filteredEvents = new ArrayList<>();

    private RecyclerView recyclerEvents;
    private TextView tvEmpty;
    private Button btnAddEvent;
    private ScheduledEventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_scheduler_list);

        recyclerEvents = findViewById(R.id.recycler_events);
        tvEmpty = findViewById(R.id.tv_empty);
        btnAddEvent = findViewById(R.id.btn_add_event);

        // Initialize adapter
        adapter = new ScheduledEventAdapter(filteredEvents);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(adapter);

        // Add event button
        btnAddEvent.setOnClickListener(v -> startActivity(new Intent(
                this, AddScheduledEventActivity.class
        )));

        // Filter events by selected date
        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        if (selectedDate != null) {
            filterEventsByDate(selectedDate);
        }
    }

    private void filterEventsByDate(String date) {
        filteredEvents.clear();

        for (ScheduledEventModel event : EVENT_LIST) {
            if (event.getDate().equals(date)) {
                filteredEvents.add(event);
            }
        }

        if (filteredEvents.isEmpty()) {
            recyclerEvents.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerEvents.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (filteredEvents.isEmpty()) {
            recyclerEvents.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerEvents.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
