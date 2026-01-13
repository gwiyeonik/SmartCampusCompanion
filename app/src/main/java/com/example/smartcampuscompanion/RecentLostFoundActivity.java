package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentLostFoundActivity extends AppCompatActivity implements LostFoundAdapter.OnItemClickListener {

    private static final String TAG = "RecentLostFoundActivity";

    private RecyclerView recyclerView;
    private LostFoundAdapter adapter;
    private List<LostFoundItem> combinedList = new ArrayList<>();
    private List<LostFoundItem> recentLostList = new ArrayList<>();
    private List<LostFoundItem> recentFoundList = new ArrayList<>();

    private FirebaseFirestore db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_lost_found);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRecentItems();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_recent_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LostFoundAdapter(combinedList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void fetchRecentItems() {
        // Listener for recent lost items
        db.collection("lost_items").orderBy("date", Query.Direction.DESCENDING).limit(5)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed for lost_items.", e);
                        return;
                    }
                    recentLostList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
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
                            recentLostList.add(item);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing lost item document: " + doc.getId(), ex);
                        }
                    }
                    combineAndSortLists();
                });

        // Listener for recent found items
        db.collection("found_items").orderBy("date", Query.Direction.DESCENDING).limit(5)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed for found_items.", e);
                        return;
                    }
                    recentFoundList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
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
                            recentFoundList.add(item);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing found item document: " + doc.getId(), ex);
                        }
                    }
                    combineAndSortLists();
                });
    }

    private void combineAndSortLists() {
        combinedList.clear();
        combinedList.addAll(recentLostList);
        combinedList.addAll(recentFoundList);

        // Sort the combined list by date
        Collections.sort(combinedList, (o1, o2) -> {
            try {
                Date date1 = sdf.parse(o1.getDate());
                Date date2 = sdf.parse(o2.getDate());
                return date2.compareTo(date1); // Descending order
            } catch (ParseException e) {
                Log.e(TAG, "Date parsing error", e);
                return 0;
            }
        });

        // Trim the list to the 5 most recent items
        if (combinedList.size() > 5) {
            combinedList.subList(5, combinedList.size()).clear();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        if (position >= combinedList.size()) return;

        LostFoundItem item = combinedList.get(position);
        Intent intent;

        if ("lost".equals(item.getItemType())) {
            intent = new Intent(this, LostItemDetailActivity.class);
        } else if ("found".equals(item.getItemType())) {
            intent = new Intent(this, FoundItemDetailActivity.class);
        } else {
            return; // Should not happen
        }

        intent.putExtra("documentId", item.getDocumentId());
        startActivity(intent);
    }
}
