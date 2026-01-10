package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private static final String TAG = "NewsActivity";
    private FirebaseFirestore db;

    // For "Happening" section
    private RecyclerView happeningRecyclerView;
    private HorizontalEventAdapter happeningAdapter;
    private List<HorizontalEventModel> happeningList = new ArrayList<>();

    // For "This Week" section
    private RecyclerView thisWeekRecyclerView;
    private HorizontalEventAdapter thisWeekAdapter;
    private List<HorizontalEventModel> thisWeekList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup UI components and listeners
        setupCategoryClicks();
        setupRecyclerViews();
        setupBottomNavigation();

        // Fetch data from Firestore
        fetchHappeningEvents();
        fetchThisWeekEvents();
    }

    private void setupCategoryClicks() {
        LinearLayout btnAcademic = findViewById(R.id.btn_academic);
        LinearLayout btnClub = findViewById(R.id.btn_club);
        LinearLayout btnEvent = findViewById(R.id.btn_event);

        btnEvent.setOnClickListener(v ->
                startActivity(new Intent(NewsActivity.this, EventActivity.class))
        );
        // Add listeners for Academic and Club if needed
    }

    private void setupRecyclerViews() {
        // Happening RecyclerView
        happeningRecyclerView = findViewById(R.id.recycler_view_happening);
        happeningRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        happeningAdapter = new HorizontalEventAdapter(this, happeningList);
        happeningRecyclerView.setAdapter(happeningAdapter);

        // This Week RecyclerView
        thisWeekRecyclerView = findViewById(R.id.recycler_view_this_week);
        thisWeekRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thisWeekAdapter = new HorizontalEventAdapter(this, thisWeekList);
        thisWeekRecyclerView.setAdapter(thisWeekAdapter);
    }

    private void fetchHappeningEvents() {
        db.collection("happeningevent") // As per your Firestore structure
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    happeningList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HorizontalEventModel event = document.toObject(HorizontalEventModel.class);
                        event.setEventId(document.getId());
                        happeningList.add(event);
                    }
                    happeningAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Fetched " + happeningList.size() + " happening events.");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting happening documents.", e));
    }

    private void fetchThisWeekEvents() {
        db.collection("thisweekevent") // As per your Firestore structure
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    thisWeekList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HorizontalEventModel event = document.toObject(HorizontalEventModel.class);
                        event.setEventId(document.getId());
                        thisWeekList.add(event);
                    }
                    thisWeekAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Fetched " + thisWeekList.size() + " this week events.");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting this week documents.", e));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to MainActivity
                Intent intent = new Intent(NewsActivity.this, MainActivity.class);
                // Clear the back stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish current activity
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Handle Chat navigation if you have it
                return true;
            } else if (itemId == R.id.nav_profile) {
                // --- THIS IS THE FIX ---
                // Logout the user and redirect to LoginActivity
                new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
                            // Clear all previous activities from the back stack
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
                // --- END OF FIX ---
            }
            return false;
        });
    }
}
