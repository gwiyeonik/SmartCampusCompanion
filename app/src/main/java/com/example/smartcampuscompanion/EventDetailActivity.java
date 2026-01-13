package com.example.smartcampuscompanion;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Make sure it's android.util.Log
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.content.pm.PackageManager;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // Import SetOptions

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Firebase & Data
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String eventId;
    private double lat = 0.0, lng = 0.0;

    // UI Views
    private TextView tvTitle, tvDescription, tvRegLink;
    private ImageView ivEventImage, ivStaticMap, ivPlusIcon;
    private RecyclerView imagesRecyclerView, commentsRecyclerView;
    private FloatingActionButton fabEditEvent;
    private LinearLayout creatorActionsLayout, btnOpenReview;

    // --- THIS IS THE FIX, PART 1 ---
    // Adapters and Data Lists are now member variables
    private EventImageAdapter imagesAdapter;
    private CommentAdapter commentAdapter;
    private List<String> imageUrlList = new ArrayList<>();
    private List<Map<String, Object>> commentList = new ArrayList<>();
    private MapView mapView;
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;

    private final ActivityResultLauncher<Intent> updateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 2. If the result code is 99, it means the event was deleted
                if (result.getResultCode() == 99) {
                    finish(); // Close this Detail activity automatically
                } else {
                    // Otherwise, just refresh the data (in case they just updated it)
                    loadEventDetails();
                }
            }
    );
    // --- END OF FIX ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the current selected item (optional, but good for UI consistency)
        // Since this isn't a main navigation screen, we can leave it unselected
        // or select a relevant item if it exists in your menu.

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to MainActivity
                Intent intent = new Intent(EventDetailActivity.this, MainActivity.class);
                // Clear the back stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish current activity
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Handle Chat navigation
                return true;
            } else if (itemId == R.id.nav_profile) {
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(EventDetailActivity.this, LoginActivity.class);
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


        initializeViews(savedInstanceState);
        setupAdaptersAndListeners(); // Combined setup

        if (eventId != null) {
            loadEventDetails();
        }
    }

    private void initializeViews(Bundle savedInstanceState) {
        tvTitle = findViewById(R.id.tv_event_detail_title);
        tvDescription = findViewById(R.id.tv_event_detail_description);
        tvRegLink = findViewById(R.id.tv_registration_link);
        ivEventImage = findViewById(R.id.iv_event_detail_image);
        ivPlusIcon = findViewById(R.id.iv_plus_icon);
        btnOpenReview = findViewById(R.id.btn_open_review_dialog);
        fabEditEvent = findViewById(R.id.fab_edit_event);
        creatorActionsLayout = findViewById(R.id.creator_actions_layout);
        imagesRecyclerView = findViewById(R.id.recycler_view_event_images);
        commentsRecyclerView = findViewById(R.id.recycler_view_comments);

        mapView = findViewById(R.id.map_view);
        // Now this line will work correctly
        mapView.onCreate(savedInstanceState);
        // Pass 'this' as the callback
        mapView.getMapAsync(this);
    }

    private void setupAdaptersAndListeners() {
        // --- THIS IS THE FIX, PART 2 ---
        // Setup adapters ONCE
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new EventImageAdapter(this, imageUrlList); // Use the member list
        imagesRecyclerView.setAdapter(imagesAdapter);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList); // Use the member list
        commentsRecyclerView.setAdapter(commentAdapter);
        // --- END OF FIX ---

        // Click listeners setup
        findViewById(R.id.iv_back_arrow_detail).setOnClickListener(v -> finish());
        findViewById(R.id.btn_location).setOnClickListener(v -> {
            if (lat != 0.0 && lng != 0.0) {
                String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Location)";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });
        btnOpenReview.setOnClickListener(v -> showAddReviewDialog());
        fabEditEvent.setOnClickListener(v -> {
            // 1. Point the Intent to UpdateActivity
            Intent intent = new Intent(this, UpdateActivity.class);

            // 2. Pass the event ID
            intent.putExtra("EVENT_ID_TO_UPDATE", eventId);

            // 3. CRITICAL: Use launch() instead of startActivity()
            // This allows this activity to "listen" for the delete signal (result code 99)
            updateLauncher.launch(intent);
        });

    }

    private void checkProximityToEvent() {
        // FIX: Use PackageManager.PERMISSION_GRANTED instead of getPermissionManager()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && lat != 0.0) {
                float[] results = new float[1];
                android.location.Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lng, results);

                float distanceInMeters = results[0];

                if (distanceInMeters < 50) {
                    Toast.makeText(this, "Welcome! You've arrived at the venue.", Toast.LENGTH_LONG).show();
                } else {
                    String distKm = String.format("%.2f", distanceInMeters / 1000);
                    Toast.makeText(this, "The event is " + distKm + " km away.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // FIX: This prevents the ScrollView from "stealing" touch events from the Map
        final ScrollView mainScrollView = findViewById(R.id.detail_scroll_view); // You need to add this ID to your XML ScrollView
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (mainScrollView != null) {
                mainScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        if (lat != 0.0 && lng != 0.0) {
            updateMapLocation(lat, lng);
        }
    }

    private void loadEventDetails() {
        // 1. Get the current user ID for permission checking
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // 2. Find the "Image not available" TextView (ensure this ID is in your XML)
        TextView tvNoImages = findViewById(R.id.tv_no_images);

        // 3. Fetch data from the 'newsandinformation' collection
        db.collection("newsandinformation").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc != null && doc.exists()) {

                // --- SET BASIC TEXT ---
                tvTitle.setText(doc.getString("title"));
                tvDescription.setText(doc.getString("description"));

                // --- CREATOR PERMISSIONS ---
                String creatorId = doc.getString("organizerId");
                if (currentUserId.equals(creatorId)) {
                    // User IS the creator: show Edit button, hide Review button
                    creatorActionsLayout.setVisibility(View.VISIBLE);
                    btnOpenReview.setVisibility(View.GONE);
                } else {
                    // User is NOT the creator: hide Edit button, show Review button
                    creatorActionsLayout.setVisibility(View.GONE);
                    btnOpenReview.setVisibility(View.VISIBLE);
                }

                // --- MAIN BANNER IMAGE ---
                // Using centerCrop to fill the banner space effectively
                // Using fitCenter shows the entire image within the box
                Glide.with(this)
                        .load(doc.getString("imageUrl"))
                        .into(ivEventImage);


                // --- ADDITIONAL IMAGES GALLERY ---
                imageUrlList.clear();
                Object imgsObject = doc.get("imagesUrl");
                if (imgsObject instanceof Map) {
                    Map<String, String> imgsMap = (Map<String, String>) imgsObject;
                    // Add only valid, non-empty URLs to the list
                    for (String url : imgsMap.values()) {
                        if (url != null && !url.trim().isEmpty()) {
                            imageUrlList.add(url);
                        }
                    }
                }

                // Show list if images exist, otherwise show "not available" message
                if (imageUrlList.isEmpty()) {
                    if (tvNoImages != null) tvNoImages.setVisibility(View.VISIBLE);
                    imagesRecyclerView.setVisibility(View.GONE);
                } else {
                    if (tvNoImages != null) tvNoImages.setVisibility(View.GONE);
                    imagesRecyclerView.setVisibility(View.VISIBLE);
                    imagesAdapter.notifyDataSetChanged();
                }

                // --- REVIEWS & COMMENTS ---
                commentList.clear();
                Object reviewObject = doc.get("review");
                if (reviewObject instanceof List) {
                    List<Map<String, Object>> reviews = (List<Map<String, Object>>) reviewObject;
                    commentList.addAll(reviews);
                }
                commentAdapter.notifyDataSetChanged();

                // --- REGISTRATION LINK ---
                String link = doc.getString("googleForm");
                if (link != null && !link.isEmpty()) {
                    tvRegLink.setText("Click to Register");
                    tvRegLink.setOnClickListener(v -> {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(EventDetailActivity.this, "Could not open link", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    tvRegLink.setText("No registration link provided.");
                    tvRegLink.setOnClickListener(null);
                }

                // --- LOCATION & MAP ---
                Object locObject = doc.get("location");
                if (locObject instanceof Map) {
                    Map<String, Object> loc = (Map<String, Object>) locObject;
                    if (loc.get("latitude") instanceof Number && loc.get("longitude") instanceof Number) {
                        lat = ((Number) loc.get("latitude")).doubleValue();
                        lng = ((Number) loc.get("longitude")).doubleValue();

                        // Check if user is physically at the event
                        checkProximityToEvent();

                        // If Google Map is ready, move the camera to the event location
                        if (googleMap != null) {
                            updateMapLocation(lat, lng);
                        }
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading event details", e);
            Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
        });
    }


    // --- NEW HELPER METHOD TO UPDATE THE MAP ---
    private void updateMapLocation(double latitude, double longitude) {
        if (googleMap == null) return; // Don't do anything if the map isn't ready
        LatLng eventLocation = new LatLng(latitude, longitude);
        googleMap.clear(); // Clear any previous markers
        googleMap.addMarker(new MarkerOptions().position(eventLocation).title(tvTitle.getText().toString()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (eventId != null) {
            loadEventDetails();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void showAddReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);
        final EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        final RatingBar ratingBar = dialogView.findViewById(R.id.dialog_rating_bar);

        new AlertDialog.Builder(this)
                .setTitle("Add Review")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String commentText = etComment.getText().toString().trim();
                    float ratingValue = ratingBar.getRating();
                    saveReview(commentText, ratingValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveReview(String text, float rating) {
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating (at least one star).", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post a review.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nameToDisplay = (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty())
                ? currentUser.getDisplayName()
                : currentUser.getEmail();
        if (nameToDisplay == null) nameToDisplay = "Anonymous";

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userName", nameToDisplay);
        reviewData.put("comment", text);
        reviewData.put("rating", (double) rating);

        // --- FIX: USE REGULAR DATE OBJECT ---
        // serverTimestamp() is NOT allowed inside arrayUnion()
        reviewData.put("timestamp", new java.util.Date());

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("review", FieldValue.arrayUnion(reviewData));

        db.collection("newsandinformation").document(eventId)
                .set(updateMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Review posted!", Toast.LENGTH_SHORT).show();
                    loadEventDetails(); // Refreshes the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error posting review", e);
                    Toast.makeText(this, "Failed to post review", Toast.LENGTH_SHORT).show();
                });
    }
}
