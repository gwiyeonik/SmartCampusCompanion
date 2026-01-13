package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimetableListActivity extends AppCompatActivity {

    private static final String TAG = "TimetableListActivity";

    private RecyclerView recyclerView;
    private CalendarView calendarView;
    private TextView tvEmpty;

    private TimetableAdapter adapter;
    private List<TimetableModel> timetableList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_list);

        // 🔹 Initialize Views
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recycler_view_timetable);
        tvEmpty = findViewById(R.id.tv_empty);

        // 🔹 Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        timetableList = new ArrayList<>();
        adapter = new TimetableAdapter(timetableList);
        recyclerView.setAdapter(adapter);

        // Hide list initially
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        // 🔹 Add Timetable button
        Button btnAdd = findViewById(R.id.btn_add_timetable);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(TimetableListActivity.this, AddTimetableActivity.class))
        );

        // 🔹 NEW LISTENER: Applandeo uses OnDayClickListener
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDate = eventDay.getCalendar();

            // Get the name of the day (e.g., "Monday")
            String dayName = clickedDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);

            Log.d(TAG, "Selected Day: " + dayName);
            fetchTimetableByDay(dayName);
        });

        // 🔹 Load indicators (dots) on the calendar
        fetchCalendarDots();
    }

    /**
     * Show / hide empty message
     */
    private void toggleEmptyView() {
        if (timetableList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fetch all timetables just to mark which days have classes with dots
     */
    private void fetchCalendarDots() {
        db.collection("timetables")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventDay> events = new ArrayList<>();

                    // For every timetable entry, we add a dot to that day of the week
                    // Note: This marks the "Current Week" specifically.
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String dayInDb = doc.getString("day"); // e.g., "Monday"
                        if (dayInDb != null) {
                            Calendar cal = getCalendarForDayName(dayInDb);
                            // R.drawable.ic_dot is the file we created earlier
                            events.add(new EventDay(cal, R.drawable.ic_dot));
                        }
                    }
                    calendarView.setEvents(events);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading calendar dots", e));
    }

    /**
     * Helper to get a Calendar object for a specific day name in the current week
     */
    private Calendar getCalendarForDayName(String dayName) {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String currentDayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
            if (currentDayName.equalsIgnoreCase(dayName)) {
                return (Calendar) cal.clone();
            }
            cal.add(Calendar.DATE, 1);
        }
        return Calendar.getInstance();
    }

    /**
     * Load timetables from Firestore for the selected day string
     */
    private void fetchTimetableByDay(String day) {
        db.collection("timetables")
                .whereEqualTo("day", day)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    timetableList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TimetableModel model = document.toObject(TimetableModel.class);
                        model.setDocumentId(document.getId());
                        timetableList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    toggleEmptyView();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching timetable", e));
    }
}