package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TimetableListActivity extends AppCompatActivity {

    private static final String TAG = "TimetableListActivity";

    private RecyclerView recyclerView;
    private MaterialCalendarView calendarView;
    private TextView tvEmpty;

    private TimetableAdapter adapter;
    private List<TimetableModel> timetableList;

    private HashSet<CalendarDay> classDays = new HashSet<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_list);

        // 🔹 Views
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recycler_view_timetable);
        tvEmpty = findViewById(R.id.tv_empty);

        // 🔹 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        timetableList = new ArrayList<>();
        adapter = new TimetableAdapter(timetableList);
        recyclerView.setAdapter(adapter);

        // 🔴 KEY UX FIX — hide list initially
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // 🔹 Firestore
        db = FirebaseFirestore.getInstance();

        // 🔹 Add Timetable button
        Button btnAdd = findViewById(R.id.btn_add_timetable);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(
                        TimetableListActivity.this,
                        AddTimetableActivity.class
                ))
        );

        // 🔹 Calendar date click → show + filter list
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            recyclerView.setVisibility(View.VISIBLE);
            fetchTimetableByDay(getDayFromCalendarDay(date));
        });

        // 🔹 Load dots only (NOT list)
        fetchCalendarDots();
    }

    /**
     * Convert CalendarDay → "Monday", "Tuesday", etc.
     */
    private String getDayFromCalendarDay(CalendarDay calendarDay) {
        String day = calendarDay.getDate()
                .getDayOfWeek()
                .toString(); // MONDAY

        return day.substring(0, 1).toUpperCase()
                + day.substring(1).toLowerCase();
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
     * Fetch ONLY calendar dots (clean UX)
     */
    private void fetchCalendarDots() {
        db.collection("timetables")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    classDays.clear();

                    // TEMP: mark today if any timetable exists
                    if (!queryDocumentSnapshots.isEmpty()) {
                        classDays.add(CalendarDay.today());
                    }

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new EventDayDecorator(classDays));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading calendar dots", e)
                );
    }

    /**
     * Load timetables for selected day
     */
    private void fetchTimetableByDay(String day) {
        db.collection("timetables")
                .whereEqualTo("day", day)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    timetableList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TimetableModel model =
                                document.toObject(TimetableModel.class);
                        model.setDocumentId(document.getId());
                        timetableList.add(model);
                    }

                    adapter.notifyDataSetChanged();
                    toggleEmptyView();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error fetching timetable by day", e)
                );
    }
}
