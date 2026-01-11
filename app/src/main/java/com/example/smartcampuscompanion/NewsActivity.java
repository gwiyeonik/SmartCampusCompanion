package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsActivity extends AppCompatActivity {

    private static final String TAG = "NewsActivity";
    private FirebaseFirestore db;

    private RecyclerView monthRecyclerView;
    private HorizontalEventAdapter monthAdapter;
    private List<HorizontalEventModel> monthList = new ArrayList<>();

    private RecyclerView thisWeekRecyclerView;
    private HorizontalEventAdapter thisWeekAdapter;
    private List<HorizontalEventModel> thisWeekList = new ArrayList<>();

    // Format must match your Firestore string: "15 January 2026"
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        setupCategoryClicks();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAndFilterEvents();
    }

    private void setupRecyclerViews() {
        // Setup "This Week" List
        thisWeekRecyclerView = findViewById(R.id.recycler_view_this_week);
        thisWeekRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thisWeekAdapter = new HorizontalEventAdapter(this, thisWeekList);
        thisWeekRecyclerView.setAdapter(thisWeekAdapter);

        // Setup "This Month" List
        monthRecyclerView = findViewById(R.id.recycler_view_happening);
        monthRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        monthAdapter = new HorizontalEventAdapter(this, monthList);
        monthRecyclerView.setAdapter(monthAdapter);
    }

    private void fetchAndFilterEvents() {
        db.collection("event").get().addOnSuccessListener(queryDocumentSnapshots -> {
            thisWeekList.clear();
            monthList.clear();

            Calendar now = Calendar.getInstance();
            int currentWeek = now.get(Calendar.WEEK_OF_YEAR);
            int currentMonth = now.get(Calendar.MONTH);
            int currentYear = now.get(Calendar.YEAR);

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                HorizontalEventModel event = document.toObject(HorizontalEventModel.class);
                event.setEventId(document.getId());

                String d1 = document.getString("date1");
                String d2 = document.getString("date2");

                // Check date1
                filterEventByDate(event, d1, currentWeek, currentMonth, currentYear);

                // Check date2 (if it exists)
                if (d2 != null && !d2.isEmpty()) {
                    filterEventByDate(event, d2, currentWeek, currentMonth, currentYear);
                }
            }

            thisWeekAdapter.notifyDataSetChanged();
            monthAdapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching events", e);
            Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
        });
    }

    private void filterEventByDate(HorizontalEventModel event, String dateStr, int curWeek, int curMonth, int curYear) {
        if (dateStr == null || dateStr.isEmpty()) return;

        try {
            Date date = sdf.parse(dateStr);
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(date);

            boolean isSameYear = eventCal.get(Calendar.YEAR) == curYear;

            // Logic for "This Month"
            if (isSameYear && eventCal.get(Calendar.MONTH) == curMonth) {
                if (!monthList.contains(event)) {
                    monthList.add(event);
                }
            }

            // Logic for "This Week"
            if (isSameYear && eventCal.get(Calendar.WEEK_OF_YEAR) == curWeek) {
                if (!thisWeekList.contains(event)) {
                    thisWeekList.add(event);
                }
            }

        } catch (ParseException e) {
            Log.e(TAG, "Date parse error for: " + dateStr);
        }
    }

    private void setupCategoryClicks() {
        findViewById(R.id.btn_event).setOnClickListener(v ->
                startActivity(new Intent(NewsActivity.this, EventActivity.class))
        );
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(NewsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
    }
}