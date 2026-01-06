package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- FIX: Import classes from the NEW Applandeo library ---
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
// ---

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";

    // --- FIX: Use the correct CalendarView class ---
    private CalendarView calendarView;
    // ---

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> allEventsList = new ArrayList<>();
    private List<EventModel> filteredEventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Initialize Views
        calendarView = findViewById(R.id.calendar_view); // Make sure ID in XML is 'calendar_view'
        recyclerView = findViewById(R.id.recycler_view_events);
        FloatingActionButton fabAddEvent = findViewById(R.id.fab_create_event);
        ImageView backArrow = findViewById(R.id.iv_back_arrow); // Make sure ID in XML is correct

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // Important for ScrollView
        eventAdapter = new EventAdapter(this, filteredEventList);
        recyclerView.setAdapter(eventAdapter);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // --- FIX: Use the new library's click listener ---
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedCalendar = eventDay.getCalendar();
            filterEventsForSelectedDate(selectedCalendar.getTime());
        });
        // ---

        // Setup Click Listeners
        backArrow.setOnClickListener(v -> finish());
        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch events every time the activity is shown
        fetchEvents();
    }

    // Replace your fetchEvents method with this one:
    private void fetchEvents() {
        db.collection("event") // Use "event" to match your Firestore screenshot
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEventsList.clear();
                    List<EventDay> calendarEventDays = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // One definition of 'event'
                        EventModel event = document.toObject(EventModel.class);
                        event.setEventId(document.getId());

                        // Convert Strings from Firebase to Date Objects for the logic
                        Date d1 = parseFirebaseDate(event.getDate1());
                        Date d2 = parseFirebaseDate(event.getDate2());

                        event.setDate1Obj(d1);
                        event.setDate2Obj(d2);

                        allEventsList.add(event);

                        if (d1 != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(d1);
                            calendarEventDays.add(new EventDay(calendar, android.R.drawable.presence_online));
                        }
                    }

                    calendarView.setEvents(calendarEventDays);
                    filterEventsForSelectedDate(new Date());
                });
    }

    // HELPER: Converts "14 November 2025" into a Java Date object
    private Date parseFirebaseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.ENGLISH);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    // Add this helper method to handle your specific date format
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Matches "14 November 2025"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.ENGLISH);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e(TAG, "Date parsing error: " + dateStr);
            return null;
        }
    }

    private void filterEventsForSelectedDate(Date selectedDate) {
        filteredEventList.clear();
        Calendar calSelected = getCalendarWithoutTime(selectedDate);

        for (EventModel event : allEventsList) {
            // Use the Date Objects (getDate1Obj) not the Strings (getDate1)
            if (event.getDate1Obj() != null) {
                Calendar calEventStart = getCalendarWithoutTime(event.getDate1Obj());

                Calendar calEventEnd = (event.getDate2Obj() != null)
                        ? getCalendarWithoutTime(event.getDate2Obj())
                        : calEventStart;

                if (!calSelected.before(calEventStart) && !calSelected.after(calEventEnd)) {
                    filteredEventList.add(event);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    private Calendar getCalendarWithoutTime(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(EventActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(EventActivity.this, LoginActivity.class);
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
