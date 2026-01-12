package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyReportsAdapter adapter;
    private List<EmergencyReport> reportList;
    private FirebaseFirestore db;

    // TEMP userId (replace with FirebaseAuth later)
    private final String userId = "student_001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_report);

        // ===== Bottom Navigation (HOME only) =====
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // ===== Back Button =====
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ===== RecyclerView =====
        recyclerView = findViewById(R.id.recyclerMyReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        adapter = new MyReportsAdapter(this, reportList);
        recyclerView.setAdapter(adapter);

        // ===== Firestore =====
        db = FirebaseFirestore.getInstance();
    }

    // ✅ VERY IMPORTANT: reload when returning from Update screen
    @Override
    protected void onResume() {
        super.onResume();
        loadMyReports();
    }

    private void loadMyReports() {
        db.collection("emergency_reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    reportList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        EmergencyReport report = doc.toObject(EmergencyReport.class);
                        report.setId(doc.getId()); // REQUIRED for update/delete
                        reportList.add(report);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
