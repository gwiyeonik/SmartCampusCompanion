package com.example.smartcampuscompanion;

import android.app.DatePickerDialog;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etTitle, etDesc, etLink, etLat, etLng;
    private TextView tvDate1, tvDate2;
    private ImageButton ibMain, ibExtra1, ibExtra2, ibExtra3;
    private ImageView ivBack;
    private Button btnCreate;

    private Uri uriMain, uri1, uri2, uri3;
    private String urlMain = "", url1 = "", url2 = "", url3 = "";
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Same Picker Logic for all 4 slots
    private final ActivityResultLauncher<String> pickMain = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> { if(uri!=null){ uriMain=uri; ibMain.setImageURI(uri); }});
    private final ActivityResultLauncher<String> pick1 = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> { if(uri!=null){ uri1=uri; ibExtra1.setImageURI(uri); }});
    private final ActivityResultLauncher<String> pick2 = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> { if(uri!=null){ uri2=uri; ibExtra2.setImageURI(uri); }});
    private final ActivityResultLauncher<String> pick3 = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> { if(uri!=null){ uri3=uri; ibExtra3.setImageURI(uri); }});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ivBack = findViewById(R.id.iv_back_arrow_create);
        etTitle = findViewById(R.id.et_event_title);
        etDesc = findViewById(R.id.et_event_description);
        etLink = findViewById(R.id.et_google_form_link);
        etLat = findViewById(R.id.et_latitude);
        etLng = findViewById(R.id.et_longitude);
        tvDate1 = findViewById(R.id.tv_selected_date1);
        tvDate2 = findViewById(R.id.tv_selected_date2);
        ibMain = findViewById(R.id.ib_add_main_image);
        ibExtra1 = findViewById(R.id.ib_extra_1);
        ibExtra2 = findViewById(R.id.ib_extra_2);
        ibExtra3 = findViewById(R.id.ib_extra_3);
        btnCreate = findViewById(R.id.btn_create_event);

        // Clickable Back Button
        ivBack.setOnClickListener(v -> finish());

        // Image Pickers
        ibMain.setOnClickListener(v -> pickMain.launch("image/*"));
        ibExtra1.setOnClickListener(v -> pick1.launch("image/*"));
        ibExtra2.setOnClickListener(v -> pick2.launch("image/*"));
        ibExtra3.setOnClickListener(v -> pick3.launch("image/*"));

        btnCreate.setOnClickListener(v -> startUploads());
    }

    private void startUploads() {
        btnCreate.setEnabled(false);
        uploadFile(uriMain, link -> {
            urlMain = link;
            uploadFile(uri1, l1 -> {
                url1 = l1;
                uploadFile(uri2, l2 -> {
                    url2 = l2;
                    uploadFile(uri3, l3 -> {
                        url3 = l3;
                        saveToFirestore();
                    });
                });
            });
        });
    }

    private void uploadFile(Uri uri, OnUploadSuccess listener) {
        if (uri == null) { listener.onSuccess(""); return; }
        StorageReference ref = storage.getReference("event_images/" + UUID.randomUUID().toString());
        ref.putFile(uri).addOnSuccessListener(t -> ref.getDownloadUrl().addOnSuccessListener(u -> listener.onSuccess(u.toString())));
    }

    private void saveToFirestore() {
        String id = etTitle.getText().toString().replace(" ", "").toLowerCase() + System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("title", etTitle.getText().toString());
        data.put("description", etDesc.getText().toString());
        data.put("imageUrl", urlMain);
        data.put("googleForm", etLink.getText().toString());

        Map<String, String> extraImgs = new HashMap<>();
        extraImgs.put("imagesUrl1", url1);
        extraImgs.put("imagesUrl2", url2);
        extraImgs.put("imagesUrl3", url3);
        data.put("imagesUrl", extraImgs);

        db.collection("newsandinformation").document(id).set(data).addOnSuccessListener(a -> finish());
    }

    interface OnUploadSuccess { void onSuccess(String link); }
}