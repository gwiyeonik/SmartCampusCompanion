package com.example.smartcampuscompanion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddEvent;
    private MaterialCalendarView calendarView;
    private List<EventModel> allEvents = new ArrayList<>(); // Store full list here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_event);

        db = FirebaseFirestore.getInstance();
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recycler_view_events);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Important: Disable internal scrolling so NestedScrollView can handle it
        recyclerView.setNestedScrollingEnabled(false);

        // FIXED: Method name changed to setOnDateChangedListener in v2.0.1
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US);
            // date.getDate() returns the LocalDate needed for formatting
            String clickedDate = date.getDate().format(formatter);
            filterEventsByDate(clickedDate);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the current selected item (optional, but good for UI consistency)
        // Since this isn't a main navigation screen, we can leave it unselected
        // or select a relevant item if it exists in your menu.

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to MainActivity
                Intent intent = new Intent(EventActivity.this, MainActivity.class);
                // Clear the back stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish current activity
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Handle Chat navigation
                return true;
            } else if (itemId == R.id.nav_profile) {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(EventActivity.this, LoginActivity.class);
                            // Clear all previous activities from the back stack
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
        // --- END: ADD BOTTOM NAVIGATION LOGIC ---


        fabAddEvent = findViewById(R.id.fab_create_event);
        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));

        findViewById(R.id.iv_back_arrow).setOnClickListener(v -> finish());

        fetchEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This runs every time you return to this page from CreateEventActivity
        fetchEvents();
    }

    private void fetchEvents() {
        db.collection("event").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                allEvents.clear(); // Clear list to avoid duplicates
                HashSet<CalendarDay> eventDates = new HashSet<>();

                // This MUST match exactly what you saved: "dd MMMM yyyy"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US);

                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    EventModel event = doc.toObject(EventModel.class);
                    if (event != null) {
                        event.setEventId(doc.getId());
                        allEvents.add(event);

                        try {
                            // Use .trim() to remove accidental spaces
                            if (event.getDate1() != null && !event.getDate1().isEmpty()) {
                                LocalDate d1 = LocalDate.parse(event.getDate1().trim(), formatter);
                                eventDates.add(CalendarDay.from(d1));
                            }
                            if (event.getDate2() != null && !event.getDate2().isEmpty()) {
                                LocalDate d2 = LocalDate.parse(event.getDate2().trim(), formatter);
                                eventDates.add(CalendarDay.from(d2));
                            }
                        } catch (Exception e) {
                            // This prevents the crash! It just logs the error instead.
                            Log.e("DateError", "Skipping date for " + event.getTitle() + ": " + e.getMessage());
                        }
                    }
                }

                calendarView.addDecorator(new EventDecorator(Color.parseColor("#801B5E20"), eventDates));
                adapter = new EventAdapter(this, new ArrayList<>(allEvents));
                recyclerView.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Fetch failed", Toast.LENGTH_SHORT).show());
    }

    private void filterEventsByDate(String dateStr) {
        List<EventModel> filtered = new ArrayList<>();
        for (EventModel event : allEvents) {
            if (dateStr.equals(event.getDate1()) || dateStr.equals(event.getDate2())) {
                filtered.add(event);
            }
        }
        if (adapter != null) adapter.updateList(filtered);
    }
}