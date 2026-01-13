package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassEventActivity extends AppCompatActivity {

    private RecyclerView recyclerToday;
    private TimetableAdapter adapter;
    private List<TimetableModel> todayList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_event);

        // 🔹 Firestore
        db = FirebaseFirestore.getInstance();

        // 🔹 Buttons
        LinearLayout btnTimetable = findViewById(R.id.btn_timetable);
        LinearLayout btnEventScheduler = findViewById(R.id.btn_event_scheduler);

        btnTimetable.setOnClickListener(v ->
                startActivity(new Intent(
                        ClassEventActivity.this,
                        TimetableListActivity.class
                ))
        );

        btnEventScheduler.setOnClickListener(v ->
                startActivity(new Intent(ClassEventActivity.this, EventSchedulerListActivity.class))
        );


        // 🔹 RecyclerView for today's classes
        recyclerToday = findViewById(R.id.recycler_today_timetable);
        recyclerToday.setLayoutManager(new LinearLayoutManager(this));

        todayList = new ArrayList<>();
        adapter = new TimetableAdapter(todayList);
        recyclerToday.setAdapter(adapter);

        // 🔹 Load today's classes
        loadTodayClasses();
    }

    /**
     * Load today's classes from Firestore
     */
    private void loadTodayClasses() {

        // Example: MONDAY, TUESDAY, etc.
        String today = LocalDate.now().getDayOfWeek().toString();

        // Convert "MONDAY" → "Monday"
        today = today.substring(0, 1)
                + today.substring(1).toLowerCase();

        db.collection("timetables")
                .whereEqualTo("day", today)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    todayList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        TimetableModel model =
                                doc.toObject(TimetableModel.class);
                        todayList.add(model);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
