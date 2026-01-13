package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private RecyclerView urgentItemsRecyclerView;
    private LostFoundAdapter urgentItemsAdapter;
    private List<LostFoundItem> urgentItemsList = new ArrayList<>();
    private RecyclerView happeningRecyclerView;
    private TextView tvUserGreeting;
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

        tvUserGreeting = findViewById(R.id.tv_user_greeting);
        fetchUserName();

        setupRecyclerViews();

        LinearLayout newsInfoButton = findViewById(R.id.btn_news_info);
        newsInfoButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NewsActivity.class));
        });

        LinearLayout classEventButton = findViewById(R.id.btn_class_event);
        classEventButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TimetableListActivity.class));
        });

        LinearLayout btnSafetyEmergency = findViewById(R.id.btn_safety_emergency);

        btnSafetyEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SafetyEmergencyActivity.class));
            }
        });

        LinearLayout lostFoundButton = findViewById(R.id.btn_lost_found);
        lostFoundButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LostFoundActivity.class));
        });


        setupBottomNavigation();
    }

    private void setupRecyclerViews() {
        // Urgent Items RecyclerView
        urgentItemsRecyclerView = findViewById(R.id.recycler_view_happening);
        urgentItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        urgentItemsAdapter = new LostFoundAdapter(urgentItemsList);
        urgentItemsRecyclerView.setAdapter(urgentItemsAdapter);

        urgentItemsAdapter.setOnItemClickListener(position -> {
            LostFoundItem item = urgentItemsList.get(position);
            if ("lost".equals(item.getItemType())) {
                Intent intent = new Intent(MainActivity.this, LostItemDetailActivity.class);
                intent.putExtra("documentId", item.getDocumentId());
                startActivity(intent);
            } else if ("found".equals(item.getItemType())) {
                Intent intent = new Intent(MainActivity.this, FoundItemDetailActivity.class);
                intent.putExtra("documentId", item.getDocumentId());
                startActivity(intent);
            }
        });

        thisWeekRecyclerView = findViewById(R.id.recycler_view_this_week);
        thisWeekRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thisWeekAdapter = new HorizontalEventAdapter(this, thisWeekList);
        thisWeekRecyclerView.setAdapter(thisWeekAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAndFilterEvents();
        fetchUrgentItems();
    }

    private void fetchUrgentItems() {
        urgentItemsList.clear();

        // Fetch urgent lost items
        db.collection("lost_items")
                .whereEqualTo("urgent", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Boolean isUrgent = doc.getBoolean("urgent");
                            LostFoundItem item = new LostFoundItem(
                                    doc.getString("imageUrl"),
                                    doc.getString("name"),
                                    doc.getString("location"),
                                    doc.getString("date"),
                                    isUrgent != null && isUrgent,
                                    "lost"
                            );
                            item.setDocumentId(doc.getId());
                            urgentItemsList.add(item);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing urgent lost item document: " + doc.getId(), e);
                        }
                    }
                    sortAndRefreshUrgentItems();
                });

        // Fetch urgent found items
        db.collection("found_items")
                .whereEqualTo("urgent", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Boolean isUrgent = doc.getBoolean("urgent");
                            LostFoundItem item = new LostFoundItem(
                                    doc.getString("imageUrl"),
                                    doc.getString("name"),
                                    doc.getString("location"),
                                    doc.getString("date"),
                                    isUrgent != null && isUrgent,
                                    "found"
                            );
                            item.setDocumentId(doc.getId());
                            urgentItemsList.add(item);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing urgent found item document: " + doc.getId(), e);
                        }
                    }
                    sortAndRefreshUrgentItems();
                });
    }

    private void sortAndRefreshUrgentItems() {
        Collections.sort(urgentItemsList, (o1, o2) -> {
            try {
                Date date1 = sdf.parse(o1.getDate());
                Date date2 = sdf.parse(o2.getDate());
                return date2.compareTo(date1); // Sort in descending order (newest first)
            } catch (ParseException e) {
                return 0;
            }
        });
        urgentItemsAdapter.notifyDataSetChanged();
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

    private void fetchUserName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the 'name' field from your database
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserGreeting.setText("Hi " + name + "!");
                        } else {
                            tvUserGreeting.setText("Hi User!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user name", e);
                    tvUserGreeting.setText("Hi!");
                });
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