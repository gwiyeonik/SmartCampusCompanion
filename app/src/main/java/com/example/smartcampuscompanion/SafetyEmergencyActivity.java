package com.example.smartcampuscompanion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

public class SafetyEmergencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_emergency);

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

        // Make Emergency Report
        findViewById(R.id.btnMakeReport).setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyActivity.class)));

        // View All Reports
        findViewById(R.id.btnViewAll).setOnClickListener(v ->
                startActivity(new Intent(this, ViewAllReportsActivity.class)));

        // My Reports
        findViewById(R.id.btnMyReports).setOnClickListener(v ->
                startActivity(new Intent(this, MyReportsActivity.class)));

        // SOS Call
        findViewById(R.id.btnSOS).setOnClickListener(v ->
                callEmergency());
    }

    private void callEmergency() {

        // Malaysia emergency number
        String emergencyNumber = "+609-4315005";

        // If you want campus security instead, use:
        // String emergencyNumber = "091234567";

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + emergencyNumber));
        startActivity(intent);
    }
}
