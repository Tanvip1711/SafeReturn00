package com.example.safereturn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ReportFoundActivity extends AppCompatActivity {

    EditText etLocation, etDate, etContact, etDescription;
    ImageView imageView;
    Button btnSelect, btnUpload;

    Uri imageUri;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);

        etLocation = findViewById(R.id.etFoundLocation);
        etDate = findViewById(R.id.etFoundDate);
        etContact = findViewById(R.id.etFoundContact);
        etDescription = findViewById(R.id.etFoundDescription);
        imageView = findViewById(R.id.imageViewFound);
        btnSelect = findViewById(R.id.btnSelectFound);
        btnUpload = findViewById(R.id.btnUploadFound);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        btnSelect.setOnClickListener(v -> selectImage());
        btnUpload.setOnClickListener(v -> uploadData());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadData() {

        if (imageUri == null) {
            Toast.makeText(this, "Select Image", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference ref = storage.getReference()
                .child("found_images/" + System.currentTimeMillis());

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            Map<String, Object> data = new HashMap<>();
                            data.put("photoUrl", uri.toString());
                            data.put("foundLocation", etLocation.getText().toString());
                            data.put("foundDate", etDate.getText().toString());
                            data.put("contactNumber", etContact.getText().toString());
                            data.put("description", etDescription.getText().toString());

                            db.collection("FoundPersons")
                                    .add(data)
                                    .addOnSuccessListener(documentReference ->
                                            Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show());

                        }));
    }
}
