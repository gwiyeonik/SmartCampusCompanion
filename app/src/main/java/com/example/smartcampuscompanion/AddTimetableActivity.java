package com.example.smartcampuscompanion;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTimetableActivity extends AppCompatActivity {

    private EditText etSubject, etStartTime, etEndTime, etLocation;
    private Spinner spinnerDay;
    private Button btnSave;

    private FirebaseFirestore db;

    // Used for UPDATE mode
    private String documentId = null;

    private ArrayAdapter<String> dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        // Bind views
        etSubject = findViewById(R.id.et_subject);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        etLocation = findViewById(R.id.et_location);
        spinnerDay = findViewById(R.id.spinner_day);
        btnSave = findViewById(R.id.btn_save_timetable);

        db = FirebaseFirestore.getInstance();

        // Spinner (Day selector)
        String[] days = {
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };

        dayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                days
        );
        spinnerDay.setAdapter(dayAdapter);

        // Time pickers
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        // EDIT MODE (when tapping an existing timetable)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DOC_ID")) {

            documentId = intent.getStringExtra("DOC_ID");

            etSubject.setText(intent.getStringExtra("SUBJECT"));
            etLocation.setText(intent.getStringExtra("LOCATION"));

            String savedDay = intent.getStringExtra("DAY");
            if (savedDay != null) {
                int pos = dayAdapter.getPosition(savedDay);
                spinnerDay.setSelection(pos);
            }

            String time = intent.getStringExtra("TIME");
            if (time != null && time.contains(" - ")) {
                String[] split = time.split(" - ");
                etStartTime.setText(split[0]);
                etEndTime.setText(split[1]);
            }

            btnSave.setText("Update Timetable");
        }

        btnSave.setOnClickListener(v -> saveTimetable());
    }

    // Show TimePickerDialog
    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, h, m) -> {
                    String time = String.format("%02d:%02d", h, m);
                    target.setText(time);
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    // Convert HH:mm to minutes
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    private void saveTimetable() {

        String subject = etSubject.getText().toString().trim();
        String day = spinnerDay.getSelectedItem().toString();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (subject.isEmpty() || startTime.isEmpty()
                || endTime.isEmpty() || location.isEmpty()) {

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDATE START < END
        int startMinutes = timeToMinutes(startTime);
        int endMinutes = timeToMinutes(endTime);

        if (startMinutes >= endMinutes) {
            Toast.makeText(
                    this,
                    "Start time must be earlier than end time",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Combine time after validation
        String time = startTime + " - " + endTime;

        Map<String, Object> timetable = new HashMap<>();
        timetable.put("subject", subject);
        timetable.put("day", day);
        timetable.put("time", time);
        timetable.put("location", location);

        // UPDATE
        if (documentId != null) {
            db.collection("timetables")
                    .document(documentId)
                    .update(timetable)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Timetable updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    );
        }
        // ADD
        else {
            db.collection("timetables")
                    .add(timetable)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Timetable added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add timetable", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
