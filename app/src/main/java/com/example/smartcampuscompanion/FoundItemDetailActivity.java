package com.example.smartcampuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FoundItemDetailActivity extends AppCompatActivity {

    private ImageView itemImageDetail;
    private EditText editItemNameDetail, editItemLocationDetail, editItemDateDetail;
    private Button btnEdit, btnDelete, btnUpdate;

    private FirebaseFirestore db;
    private String documentId;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_item_detail);

        itemImageDetail = findViewById(R.id.item_image_detail);
        editItemNameDetail = findViewById(R.id.edit_item_name_detail);
        editItemLocationDetail = findViewById(R.id.edit_item_location_detail);
        editItemDateDetail = findViewById(R.id.edit_item_date_detail);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpdate = findViewById(R.id.btn_update);

        db = FirebaseFirestore.getInstance();

        documentId = getIntent().getStringExtra("documentId");

        loadItemDetails();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnUpdate.setEnabled(true);
            }
        };

        editItemNameDetail.addTextChangedListener(textWatcher);
        editItemLocationDetail.addTextChangedListener(textWatcher);
        editItemDateDetail.addTextChangedListener(textWatcher);

        btnEdit.setOnClickListener(v -> enableEditing());
        btnDelete.setOnClickListener(v -> deleteItem());
        btnUpdate.setOnClickListener(v -> updateItem());
        setupBottomNavigation();
    }

    private void loadItemDetails() {
        db.collection("found_items").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editItemNameDetail.setText(documentSnapshot.getString("name"));
                        editItemLocationDetail.setText(documentSnapshot.getString("location"));
                        editItemDateDetail.setText(documentSnapshot.getString("date"));
                        imageUrl = documentSnapshot.getString("imageUrl");
                        Glide.with(this).load(imageUrl).into(itemImageDetail);

                        disableEditing();
                    } else {
                        Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void enableEditing() {
        editItemNameDetail.setEnabled(true);
        editItemLocationDetail.setEnabled(true);
        editItemDateDetail.setEnabled(true);
        btnUpdate.setEnabled(false); // Initially disabled
        btnEdit.setEnabled(false);
    }

    private void disableEditing() {
        editItemNameDetail.setEnabled(false);
        editItemLocationDetail.setEnabled(false);
        editItemDateDetail.setEnabled(false);
        btnUpdate.setEnabled(false);
        btnEdit.setEnabled(true);
    }

    private void deleteItem() {
        // First, delete the image from Firebase Storage
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            photoRef.delete().addOnSuccessListener(aVoid -> {
                // Image deleted successfully, now delete the document from Firestore
                deleteFirestoreDocument();
            }).addOnFailureListener(e -> {
                // Handle failure
                Toast.makeText(this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // No image to delete, just delete the document
            deleteFirestoreDocument();
        }
    }

    private void deleteFirestoreDocument() {
        db.collection("found_items").document(documentId).delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }


    private void updateItem() {
        String name = editItemNameDetail.getText().toString().trim();
        String location = editItemLocationDetail.getText().toString().trim();
        String date = editItemDateDetail.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedItem = new HashMap<>();
        updatedItem.put("name", name);
        updatedItem.put("location", location);
        updatedItem.put("date", date);

        db.collection("found_items").document(documentId).update(updatedItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    disableEditing();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
