package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ViewAllReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EmergencyReportAdapter adapter;
    private List<EmergencyReport> reportList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_reports);
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
        recyclerView = findViewById(R.id.recyclerReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        reportList = new ArrayList<>();
        adapter = new EmergencyReportAdapter(this, reportList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadReports();
    }

    private void loadReports() {
        db.collection("emergency_reports")
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot query) {

                        reportList.clear();

                        for (QueryDocumentSnapshot doc : query) {
                            EmergencyReport report =
                                    doc.toObject(EmergencyReport.class);
                            reportList.add(report);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
