package com.example.smartcampuscompanion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmergencyActivity extends AppCompatActivity {

    private EditText etType, etDescription;
    private Button btnSubmit;

    private FirebaseFirestore db;

    // GPS
    private FusedLocationProviderClient locationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        //Home nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            return false;
        });
        // UI
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // GPS
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Submit
        btnSubmit.setOnClickListener(v -> createEmergencyReport());
    }

    // =========================
    // CREATE EMERGENCY REPORT
    // =========================
    private void createEmergencyReport() {

        String type = etType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (type.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // TEMP userId (replace with FirebaseAuth later)
        String userId = "student_001";

        EmergencyReport report = new EmergencyReport(
                type,
                description,
                "Reported",
                time,
                userId,
                latitude,
                longitude
        );

        db.collection("emergency_reports")
                .add(report)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(
                            this,
                            "Emergency report submitted",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to submit report",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================
    // GPS PERMISSION HANDLING
    // =========================
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}
