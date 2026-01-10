package com.example.smartcampuscompanion;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UpdateActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etTitle, etDesc, etLink;
    private TextView tvDate1, tvDate2;
    private ImageButton ibMain, ib1, ib2, ib3;
    private Button btnUpdate, btnDelete;
    private MapView mapPicker;
    private GoogleMap googleMap;
    private LatLng selectedLatLng;
    private ImageView ivBackArrow;

    private String eventId;
    private Date selectedDate1, selectedDate2;
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
    private final Map<String, Uri> newImageUris = new HashMap<>();
    private final Map<String, String> existingImageUrls = new HashMap<>();

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageButton currentImageButton;
    private String currentImageKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID_TO_UPDATE");

        initializeViews();
        mapPicker.onCreate(savedInstanceState);
        mapPicker.getMapAsync(this);
        initializeImagePicker();
        setupClickListeners();

        if (eventId != null) loadEventData();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_update_title);
        etDesc = findViewById(R.id.et_update_description);
        etLink = findViewById(R.id.et_update_link);
        tvDate1 = findViewById(R.id.tv_update_date1);
        tvDate2 = findViewById(R.id.tv_update_date2);
        ibMain = findViewById(R.id.ib_update_main_image);
        ib1 = findViewById(R.id.ib_extra_1);
        ib2 = findViewById(R.id.ib_extra_2);
        ib3 = findViewById(R.id.ib_extra_3);
        btnUpdate = findViewById(R.id.btn_perform_update);
        btnDelete = findViewById(R.id.btn_delete_post);
        mapPicker = findViewById(R.id.map_picker);
        ivBackArrow = findViewById(R.id.iv_back_update);
    }

    private void setupClickListeners() {
        ivBackArrow.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> startUpdateProcess());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
        ibMain.setOnClickListener(v -> checkPermissionAndOpenPicker("main", ibMain));
        ib1.setOnClickListener(v -> checkPermissionAndOpenPicker("imagesUrl1", ib1));
        ib2.setOnClickListener(v -> checkPermissionAndOpenPicker("imagesUrl2", ib2));
        ib3.setOnClickListener(v -> checkPermissionAndOpenPicker("imagesUrl3", ib3));
        ib1.setOnLongClickListener(v -> { showRemoveImageDialog("imagesUrl1", ib1); return true; });
        ib2.setOnLongClickListener(v -> { showRemoveImageDialog("imagesUrl2", ib2); return true; });
        ib3.setOnLongClickListener(v -> { showRemoveImageDialog("imagesUrl3", ib3); return true; });
        tvDate1.setOnClickListener(v -> showDatePicker(true));
        tvDate2.setOnClickListener(v -> showDatePicker(false));
    }

    private void showRemoveImageDialog(String key, ImageButton button) {
        // Check if there is actually an image there to delete
        if (!existingImageUrls.containsKey(key) && !newImageUris.containsKey(key)) {
            return;
        }

        new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                .setTitle("Remove Image")
                .setMessage("Do you want to remove this additional image?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // 1. Remove from local tracking maps
                    existingImageUrls.put(key, ""); // Mark as empty for DB
                    newImageUris.remove(key);       // Remove any newly picked URI

                    // 2. Update UI back to placeholder
                    button.setImageResource(android.R.drawable.ic_menu_gallery);
                    Toast.makeText(this, "Image removed. Click Save to apply.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            selectedLatLng = latLng;
        });
        final ScrollView mainScrollView = findViewById(R.id.update_scroll_view);

        // THIS IS THE FIX: Disable ScrollView intercept when map is touched
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (mainScrollView != null) {
                mainScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        // Optional: Re-enable it when the map is idle (optional)
        googleMap.setOnCameraIdleListener(() -> {
            if (mainScrollView != null) {
                mainScrollView.requestDisallowInterceptTouchEvent(false);
            }
        });
    }

    private void loadEventData() {
        db.collection("newsandinformation").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etTitle.setText(doc.getString("title"));
                etDesc.setText(doc.getString("description"));
                etLink.setText(doc.getString("googleForm"));

                Map<String, Object> loc = (Map<String, Object>) doc.get("location");
                if (loc != null && googleMap != null) {
                    selectedLatLng = new LatLng((double)loc.get("latitude"), (double)loc.get("longitude"));
                    googleMap.addMarker(new MarkerOptions().position(selectedLatLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                }

                String mainUrl = doc.getString("imageUrl");
                existingImageUrls.put("main", mainUrl);
                Glide.with(this).load(mainUrl).into(ibMain);

                Map<String, String> imgs = (Map<String, String>) doc.get("imagesUrl");

                if (imgs != null) {
                    // 2. Extract using the EXACT keys from your database
                    String url1 = imgs.get("imagesUrl1");
                    String url2 = imgs.get("imagesUrl2");
                    String url3 = imgs.get("imagesUrl3");

                    // 3. Update the local tracking map (Important for saving later!)
                    if (url1 != null) existingImageUrls.put("imagesUrl1", url1);
                    if (url2 != null) existingImageUrls.put("imagesUrl2", url2);
                    if (url3 != null) existingImageUrls.put("imagesUrl3", url3);

                    // 4. Use Glide to display them in the boxes
                    if (url1 != null && !url1.isEmpty()) {
                        Glide.with(this).load(url1).centerCrop().into(ib1);
                    }
                    if (url2 != null && !url2.isEmpty()) {
                        Glide.with(this).load(url2).centerCrop().into(ib2);
                    }
                    if (url3 != null && !url3.isEmpty()) {
                        Glide.with(this).load(url3).centerCrop().into(ib3);
                    }
                }
                loadEventDates();
            }
        });
    }

    private void loadEventDates() {
        db.collection("event").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String d1 = doc.getString("date1");
                String d2 = doc.getString("date2");
                try {
                    if (d1 != null && !d1.isEmpty()) { selectedDate1 = sdf.parse(d1); tvDate1.setText(d1); }
                    if (d2 != null && !d2.isEmpty()) { selectedDate2 = sdf.parse(d2); tvDate2.setText(d2); }
                } catch (ParseException ignored) {}
            }
        });
    }

    private void startUpdateProcess() {
        btnUpdate.setEnabled(false);
        Map<String, String> finalUrls = new HashMap<>(existingImageUrls);
        List<Task<Uri>> tasks = new ArrayList<>();
        StorageReference storageRef = storage.getReference().child("event_images");

        for (Map.Entry<String, Uri> entry : newImageUris.entrySet()) {
            StorageReference ref = storageRef.child(UUID.randomUUID().toString());
            tasks.add(ref.putFile(entry.getValue()).continueWithTask(task -> ref.getDownloadUrl())
                    .addOnSuccessListener(url -> finalUrls.put(entry.getKey(), url.toString())));
        }

        if (tasks.isEmpty()) saveData(finalUrls);
        else Tasks.whenAllSuccess(tasks).addOnSuccessListener(res -> saveData(finalUrls));
    }

    private void saveData(Map<String, String> urls) {
        String title = etTitle.getText().toString();
        Map<String, Object> newsData = new HashMap<>();
        newsData.put("title", title);
        newsData.put("googleForm", etLink.getText().toString().trim());
        newsData.put("description", etDesc.getText().toString());
        newsData.put("imageUrl", urls.get("main"));

        Map<String, String> extra = new HashMap<>();
        extra.put("imagesUrl1", urls.getOrDefault("imagesUrl1", ""));
        extra.put("imagesUrl2", urls.getOrDefault("imagesUrl2", ""));
        extra.put("imagesUrl3", urls.getOrDefault("imagesUrl3", ""));
        newsData.put("imagesUrl", extra);


        if (selectedLatLng != null) {
            Map<String, Double> loc = new HashMap<>();
            loc.put("latitude", selectedLatLng.latitude);
            loc.put("longitude", selectedLatLng.longitude);
            newsData.put("location", loc);
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", title);
        eventData.put("date1", sdf.format(selectedDate1));
        eventData.put("date2", selectedDate2 != null ? sdf.format(selectedDate2) : "");
        eventData.put("imageUrl", urls.get("main"));

        db.collection("newsandinformation").document(eventId).update(newsData);
        db.collection("event").document(eventId).update(eventData).addOnSuccessListener(v -> finish());
    }

    private void showDatePicker(boolean isStart) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            c.set(y, m, d);
            if (isStart) { selectedDate1 = c.getTime(); tvDate1.setText(sdf.format(selectedDate1)); }
            else { selectedDate2 = c.getTime(); tvDate2.setText(sdf.format(selectedDate2)); }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void checkPermissionAndOpenPicker(String key, ImageButton button) {
        currentImageKey = key; currentImageButton = button;
        String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) openImagePicker();
        else ActivityCompat.requestPermissions(this, new String[]{perm}, 102);
    }

    private void openImagePicker() {
        imagePickerLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                currentImageButton.setImageURI(uri);
                newImageUris.put(currentImageKey, uri);
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this, R.style.MaterialAlertDialog_Delete)
                .setTitle("Delete?")
                .setMessage("Do you want to remove this additional image?")
                .setPositiveButton("Yes", (d, w) -> {
                    db.collection("newsandinformation").document(eventId).delete();
                    db.collection("event").document(eventId).delete().addOnSuccessListener(v -> finish());
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    @Override protected void onResume() { super.onResume(); mapPicker.onResume(); }
    @Override protected void onPause() { super.onPause(); mapPicker.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapPicker.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapPicker.onLowMemory(); }
}
