package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

public class FoundItemsListActivity extends AppCompatActivity implements LostFoundAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private LostFoundAdapter adapter;
    private List<LostFoundItem> itemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_items_list);

        recyclerView = findViewById(R.id.recycler_view_found_items_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        adapter = new LostFoundAdapter(itemList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        db = FirebaseFirestore.getInstance();

        Button btnAddItem = findViewById(R.id.btn_add_item);
        btnAddItem.setOnClickListener(v -> {
            startActivity(new Intent(FoundItemsListActivity.this, AddFoundItemActivity.class));
        });

        loadFoundItems();
        setupBottomNavigation();
    }

    private void loadFoundItems() {
        db.collection("found_items").addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle error
                return;
            }

            itemList.clear();
            for (QueryDocumentSnapshot doc : value) {
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
                itemList.add(item);
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, FoundItemDetailActivity.class);
        intent.putExtra("documentId", itemList.get(position).getDocumentId());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent loginIntent = new Intent(this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginIntent);
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
