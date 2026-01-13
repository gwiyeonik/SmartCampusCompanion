package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.smartcampuscompanion.ScheduleEventDayDecorator;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class EventCalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvEmptyEvent;
    private FloatingActionButton fabAddEvent;

    private FirebaseFirestore db;
    private ScheduledEventAdapter adapter;
    private List<ScheduledEventModel> eventList;
    private HashSet<CalendarDay> eventDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_calendar);

        // Views
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recycler_view_events);
        tvEmptyEvent = findViewById(R.id.tv_empty_event);
        fabAddEvent = findViewById(R.id.fab_add_event);

        db = FirebaseFirestore.getInstance();

        eventList = new ArrayList<>();
        eventDays = new HashSet<>();

        adapter = new ScheduledEventAdapter(eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.setVisibility(View.GONE);
        tvEmptyEvent.setVisibility(View.GONE);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String formattedDate = formatDate(date);

            // Pass selected date to EventSchedulerListActivity
            Intent intent = new Intent(EventCalendarActivity.this, EventSchedulerListActivity.class);
            intent.putExtra("SELECTED_DATE", formattedDate);
            startActivity(intent);
        });

        fabAddEvent.setOnClickListener(v -> {
            startActivity(new Intent(
                    EventCalendarActivity.this,
                    AddScheduledEventActivity.class
            ));
        });
    }

    private String formatDate(CalendarDay day) {
        int year = day.getYear();
        int month = day.getMonth() + 1; // CalendarDay is zero-indexed
        int date = day.getDay();

        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, date);
    }

    private void loadEventsForDate(String selectedDate) {
        Log.d("EVENT_DEBUG", "Querying date = " + selectedDate);
        db.collection("events")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eventList.clear();
                    eventDays.clear();

                    if (querySnapshot.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvEmptyEvent.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyEvent.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            ScheduledEventModel event = doc.toObject(ScheduledEventModel.class);
                            eventList.add(event);

                            String[] parts = event.getDate().split("-");
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]) - 1;
                            int day = Integer.parseInt(parts[2]);

                            eventDays.add(CalendarDay.from(year, month, day));
                        }

                        adapter.notifyDataSetChanged();
                    }

                    // Apply dots to calendar
                    calendarView.removeDecorators();
                    calendarView.addDecorator(
                            new ScheduleEventDayDecorator(eventDays)
                    );
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate != null) {
            String formattedDate = formatDate(selectedDate);
            loadEventsForDate(formattedDate);
        }
    }
}
