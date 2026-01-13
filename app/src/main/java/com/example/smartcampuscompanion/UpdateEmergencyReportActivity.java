package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateEmergencyReportActivity extends AppCompatActivity {

    private EditText etType, etDescription;
    private Button btnUpdate;

    private FirebaseFirestore db;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_report);
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
        etType = findViewById(R.id.etUpdateType);
        etDescription = findViewById(R.id.etUpdateDescription);
        btnUpdate = findViewById(R.id.btnUpdate);

        db = FirebaseFirestore.getInstance();

        // Receive data
        reportId = getIntent().getStringExtra("reportId");
        etType.setText(getIntent().getStringExtra("type"));
        etDescription.setText(getIntent().getStringExtra("description"));

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateReport());
    }

    private void updateReport() {

        String type = etType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (type.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("type", type);
        updates.put("description", description);

        db.collection("emergency_reports")
                .document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }
}
