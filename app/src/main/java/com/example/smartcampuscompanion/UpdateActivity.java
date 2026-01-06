package com.example.smartcampuscompanion;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateActivity extends AppCompatActivity {

    // Added missing fields: Desc, Link, Lat, Lng, and Dates
    private EditText etTitle, etDesc, etLink, etLat, etLng;
    private TextView tvDate1, tvDate2;
    private ImageButton ibMain, ib1, ib2, ib3;
    private Button btnUpdate, btnDelete;
    private ImageView ivBack;

    private String eventId, urlMain, url1, url2, url3;
    private Uri uriMain, uri1, uri2, uri3;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> pickMain = registerForActivityResult(new ActivityResultContracts.GetContent(), u -> { if(u!=null){ uriMain=u; ibMain.setImageURI(u); }});
    private final ActivityResultLauncher<String> pick1 = registerForActivityResult(new ActivityResultContracts.GetContent(), u -> { if(u!=null){ uri1=u; ib1.setImageURI(u); }});
    private final ActivityResultLauncher<String> pick2 = registerForActivityResult(new ActivityResultContracts.GetContent(), u -> { if(u!=null){ uri2=u; ib2.setImageURI(u); }});
    private final ActivityResultLauncher<String> pick3 = registerForActivityResult(new ActivityResultContracts.GetContent(), u -> { if(u!=null){ uri3=u; ib3.setImageURI(u); }});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");

        ivBack = findViewById(R.id.iv_back_update);
        etTitle = findViewById(R.id.et_update_title);

        // Corrected: Initializing the new fields from your XML
        etDesc = findViewById(R.id.et_update_description);
        etLink = findViewById(R.id.et_update_link);
        etLat = findViewById(R.id.et_update_latitude);
        etLng = findViewById(R.id.et_update_longitude);
        tvDate1 = findViewById(R.id.tv_update_date1);
        tvDate2 = findViewById(R.id.tv_update_date2);

        ibMain = findViewById(R.id.ib_update_main_image);
        ib1 = findViewById(R.id.ib_extra_1);
        ib2 = findViewById(R.id.ib_extra_2);
        ib3 = findViewById(R.id.ib_extra_3);
        btnUpdate = findViewById(R.id.btn_perform_update);
        btnDelete = findViewById(R.id.btn_delete_post);

        ivBack.setOnClickListener(v -> finish());
        ibMain.setOnClickListener(v -> pickMain.launch("image/*"));
        ib1.setOnClickListener(v -> pick1.launch("image/*"));
        ib2.setOnClickListener(v -> pick2.launch("image/*"));
        ib3.setOnClickListener(v -> pick3.launch("image/*"));

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        loadData();
        btnUpdate.setOnClickListener(v -> startUpdateUploads());
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("newsandinformation").document(eventId).delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadData() {
        db.collection("newsandinformation").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etTitle.setText(doc.getString("title"));

                // Corrected: Loading the new fields from Firestore
                etDesc.setText(doc.getString("description"));
                etLink.setText(doc.getString("googleForm"));
                etLat.setText(doc.getString("latitude"));
                etLng.setText(doc.getString("longitude"));

                urlMain = doc.getString("imageUrl");
                Glide.with(this).load(urlMain).placeholder(android.R.drawable.ic_menu_gallery).into(ibMain);

                Map<String, String> imgs = (Map<String, String>) doc.get("imagesUrl");
                if (imgs != null) {
                    url1 = imgs.get("imagesUrl1");
                    url2 = imgs.get("imagesUrl2");
                    url3 = imgs.get("imagesUrl3");
                    Glide.with(this).load(url1).placeholder(android.R.drawable.ic_menu_gallery).into(ib1);
                    Glide.with(this).load(url2).placeholder(android.R.drawable.ic_menu_gallery).into(ib2);
                    Glide.with(this).load(url3).placeholder(android.R.drawable.ic_menu_gallery).into(ib3);
                }
            }
        });
    }

    private void startUpdateUploads() {
        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        uploadFile(uriMain, urlMain, link -> {
            urlMain = link;
            uploadFile(uri1, url1, link1 -> {
                url1 = link1;
                uploadFile(uri2, url2, link2 -> {
                    url2 = link2;
                    uploadFile(uri3, url3, link3 -> {
                        url3 = link3;
                        saveUpdate();
                    });
                });
            });
        });
    }

    private void uploadFile(Uri newUri, String oldUrl, OnUploadSuccess listener) {
        if (newUri == null) { listener.onSuccess(oldUrl); return; }
        StorageReference ref = FirebaseStorage.getInstance().getReference("event_images/" + UUID.randomUUID().toString());
        ref.putFile(newUri).addOnSuccessListener(t -> ref.getDownloadUrl().addOnSuccessListener(u -> listener.onSuccess(u.toString())));
    }

    private void saveUpdate() {
        Map<String, Object> news = new HashMap<>();
        news.put("title", etTitle.getText().toString());

        // Corrected: Adding the missing fields to the update map
        news.put("description", etDesc.getText().toString());
        news.put("googleForm", etLink.getText().toString());
        news.put("latitude", etLat.getText().toString());
        news.put("longitude", etLng.getText().toString());

        news.put("imageUrl", urlMain);

        Map<String, String> extras = new HashMap<>();
        extras.put("imagesUrl1", url1);
        extras.put("imagesUrl2", url2);
        extras.put("imagesUrl3", url3);
        news.put("imagesUrl", extras);

        db.collection("newsandinformation").document(eventId).update(news)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }

    interface OnUploadSuccess { void onSuccess(String link); }
}