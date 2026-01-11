package com.example.smartcampuscompanion;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CreateEventActivity";
    private static final int STORAGE_PERMISSION_CODE = 101;

    // UI Elements
    private EditText etEventTitle, etEventDescription, etGoogleFormLink;
    private TextView tvSelectedDate1, tvSelectedDate2;
    private ImageButton ibAddMainImage, ibExtra1, ibExtra2, ibExtra3;
    private Button btnCreateEvent;
    private ImageView ivBackArrow;
    private android.app.ProgressDialog progressDialog;

    // Map Elements
    private MapView mapPicker;
    private GoogleMap googleMap;
    private LatLng selectedLatLng;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    // Data
    private Uri mainImageUri;
    private final Map<String, Uri> extraImageUris = new HashMap<>();
    private Date selectedEventDate1, selectedEventDate2;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageButton currentImageButton;
    private String currentImageKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        mapPicker.onCreate(savedInstanceState);
        mapPicker.getMapAsync(this);

        etEventDescription.setOnTouchListener((v, event) -> {
            if (v.getId() == R.id.et_event_description) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
            return false;
        });

        initializeImagePicker();
        setupClickListeners();
    }

    private void initializeViews() {
        etEventTitle = findViewById(R.id.et_event_title);
        etEventDescription = findViewById(R.id.et_event_description);
        etGoogleFormLink = findViewById(R.id.et_google_form_link);
        ivBackArrow = findViewById(R.id.iv_back_arrow_create);
        btnCreateEvent = findViewById(R.id.btn_create_event);
        tvSelectedDate1 = findViewById(R.id.tv_selected_date1);
        tvSelectedDate2 = findViewById(R.id.tv_selected_date2);
        ibAddMainImage = findViewById(R.id.ib_add_main_image);
        ibExtra1 = findViewById(R.id.ib_extra_1);
        ibExtra2 = findViewById(R.id.ib_extra_2);
        ibExtra3 = findViewById(R.id.ib_extra_3);
        mapPicker = findViewById(R.id.map_picker);
    }

    private void setupClickListeners() {
        ivBackArrow.setOnClickListener(v -> finish());
        btnCreateEvent.setOnClickListener(v -> createEvent());

        ibAddMainImage.setOnClickListener(v -> checkPermissionAndOpenPicker("main", ibAddMainImage));
        ibExtra1.setOnClickListener(v -> checkPermissionAndOpenPicker("extra1", ibExtra1));
        ibExtra2.setOnClickListener(v -> checkPermissionAndOpenPicker("extra2", ibExtra2));
        ibExtra3.setOnClickListener(v -> checkPermissionAndOpenPicker("extra3", ibExtra3));

        tvSelectedDate1.setOnClickListener(v -> showDatePickerDialog(true));
        tvSelectedDate2.setOnClickListener(v -> showDatePickerDialog(false));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        LatLng campus = new LatLng(3.5437, 103.4289);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 15f));

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Event Location"));
            selectedLatLng = latLng;
            Toast.makeText(this, "Location Pinned!", Toast.LENGTH_SHORT).show();
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

    private void checkPermissionAndOpenPicker(String key, ImageButton button) {
        currentImageKey = key;
        currentImageButton = button;
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && currentImageButton != null) {
                Uri imageUri = result.getData().getData();
                currentImageButton.setImageURI(imageUri);
                if ("main".equals(currentImageKey)) mainImageUri = imageUri;
                else extraImageUris.put(currentImageKey, imageUri);
            }
        });
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

            if (isStartDate) {
                selectedEventDate1 = selectedDate;
                tvSelectedDate1.setText(sdf.format(selectedEventDate1));
            } else {
                selectedEventDate2 = selectedDate;
                tvSelectedDate2.setText(sdf.format(selectedEventDate2));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        if (!isStartDate && selectedEventDate1 != null) datePickerDialog.getDatePicker().setMinDate(selectedEventDate1.getTime());
        datePickerDialog.show();
    }

    private void createEvent() {
        if (etEventTitle.getText().toString().isEmpty() || selectedEventDate1 == null || mainImageUri == null) {
            Toast.makeText(this, "Title, Date, and Main Image are required!", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadAllImagesAndSaveData(auth.getCurrentUser().getUid());
    }

    private void uploadAllImagesAndSaveData(String userId) {
        btnCreateEvent.setEnabled(false);
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Uploading images and creating event... Please wait.");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference storageRef = storage.getReference().child("event_images");
        Map<String, Uri> allUris = new HashMap<>();
        allUris.put("main", mainImageUri);
        allUris.putAll(extraImageUris);

        Map<String, String> downloadUrls = new HashMap<>();
        List<Task<Uri>> tasks = new ArrayList<>();

        for (Map.Entry<String, Uri> entry : allUris.entrySet()) {
            StorageReference ref = storageRef.child(UUID.randomUUID().toString());
            tasks.add(ref.putFile(entry.getValue()).continueWithTask(task -> ref.getDownloadUrl())
                    .addOnSuccessListener(url -> downloadUrls.put(entry.getKey(), url.toString())));
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(res -> saveDataToFirestore(userId, downloadUrls))
                .addOnFailureListener(e -> { btnCreateEvent.setEnabled(true); Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show(); });
    }

    private void saveDataToFirestore(String userId, Map<String, String> urls) {
        String title = etEventTitle.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

        // newsandinformation data
        Map<String, Object> newsData = new HashMap<>();
        newsData.put("organizerId", userId);
        newsData.put("title", title);
        newsData.put("googleForm", etGoogleFormLink.getText().toString());
        newsData.put("description", etEventDescription.getText().toString());
        newsData.put("imageUrl", urls.get("main"));

        Map<String, String> extra = new HashMap<>();
        extra.put("imagesUrl1", urls.getOrDefault("extra1", ""));
        extra.put("imagesUrl2", urls.getOrDefault("extra2", ""));
        extra.put("imagesUrl3", urls.getOrDefault("extra3", ""));
        newsData.put("imagesUrl", extra);

        if (selectedLatLng != null) {
            Map<String, Double> loc = new HashMap<>();
            loc.put("latitude", selectedLatLng.latitude);
            loc.put("longitude", selectedLatLng.longitude);
            newsData.put("location", loc);
        }

        // event data (Dates as Strings)
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", title);
        eventData.put("date1", sdf.format(selectedEventDate1));
        eventData.put("date2", selectedEventDate2 != null ? sdf.format(selectedEventDate2) : "");
        eventData.put("imageUrl", urls.get("main"));

        DocumentReference newsRef = db.collection("newsandinformation").document();
        String sharedId = newsRef.getId();

        Task<Void> t1 = newsRef.set(newsData);
        Task<Void> t2 = db.collection("event").document(sharedId).set(eventData);

        // 4. Wait for both operations to finish
        Tasks.whenAll(t1, t2).addOnSuccessListener(v -> {
            // Dismiss dialog before finishing
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, "Event Created Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            // Dismiss dialog on error so user can fix issues
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            btnCreateEvent.setEnabled(true);
            Log.e(TAG, "Error saving to Firestore", e);
            Toast.makeText(this, "Save Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override protected void onResume() { super.onResume(); mapPicker.onResume(); }
    @Override protected void onPause() { super.onPause(); mapPicker.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapPicker.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapPicker.onLowMemory(); }
}
