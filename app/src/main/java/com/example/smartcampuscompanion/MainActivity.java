package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;

    private RecyclerView happeningRecyclerView;
    private HorizontalEventAdapter happeningAdapter;
    private List<HorizontalEventModel> happeningList = new ArrayList<>();

    private RecyclerView thisWeekRecyclerView;
    private HorizontalEventAdapter thisWeekAdapter;
    private List<HorizontalEventModel> thisWeekList = new ArrayList<>();

    // Match your Firestore date string format
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();

        LinearLayout newsInfoButton = findViewById(R.id.btn_news_info);
        newsInfoButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NewsActivity.class));
        });

        LinearLayout btnSafetyEmergency = findViewById(R.id.btn_safety_emergency);

        btnSafetyEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(
                        new Intent(MainActivity.this, SafetyEmergencyActivity.class)
                );
            }
        });


        setupBottomNavigation();
    }

    private void setupRecyclerViews() {
        happeningRecyclerView = findViewById(R.id.recycler_view_happening);
        happeningRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        happeningAdapter = new HorizontalEventAdapter(this, happeningList);
        happeningRecyclerView.setAdapter(happeningAdapter);

        thisWeekRecyclerView = findViewById(R.id.recycler_view_this_week);
        thisWeekRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thisWeekAdapter = new HorizontalEventAdapter(this, thisWeekList);
        thisWeekRecyclerView.setAdapter(thisWeekAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAndFilterEvents();
    }

    private void fetchAndFilterEvents() {
        // Pointing to the main "event" collection
        db.collection("event").get().addOnSuccessListener(queryDocumentSnapshots -> {
            happeningList.clear();
            thisWeekList.clear();

            Calendar now = Calendar.getInstance();
            int currentWeek = now.get(Calendar.WEEK_OF_YEAR);
            int currentMonth = now.get(Calendar.MONTH);
            int currentYear = now.get(Calendar.YEAR);

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                HorizontalEventModel event = document.toObject(HorizontalEventModel.class);
                event.setEventId(document.getId());

                String d1 = document.getString("date1");
                String d2 = document.getString("date2");

                // Process first date
                filterByDate(event, d1, currentWeek, currentMonth, currentYear);

                // Process second date if it exists
                if (d2 != null && !d2.isEmpty()) {
                    filterByDate(event, d2, currentWeek, currentMonth, currentYear);
                }
            }

            happeningAdapter.notifyDataSetChanged();
            thisWeekAdapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: ", e);
        });
    }

    private void filterByDate(HorizontalEventModel event, String dateStr, int curWeek, int curMonth, int curYear) {
        if (dateStr == null || dateStr.isEmpty()) return;

        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            boolean isSameYear = cal.get(Calendar.YEAR) == curYear;

            // "Happening" = This Month
            if (isSameYear && cal.get(Calendar.MONTH) == curMonth) {
                if (!happeningList.contains(event)) {
                    happeningList.add(event);
                }
            }

            // "This Week" = Current Calendar Week
            if (isSameYear && cal.get(Calendar.WEEK_OF_YEAR) == curWeek) {
                if (!thisWeekList.contains(event)) {
                    thisWeekList.add(event);
                }
            }

        } catch (ParseException e) {
            Log.e(TAG, "Parsing error: " + dateStr);
        }
    }
    private void setupBottomNavigation() {
    BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            return true;
        } else if (itemId == R.id.nav_profile) {
            new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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