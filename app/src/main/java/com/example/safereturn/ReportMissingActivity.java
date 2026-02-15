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

public class ReportMissingActivity extends AppCompatActivity {

    EditText etName, etAge, etLocation, etContact, etDescription;
    Spinner spGender;
    ImageView imageView;
    Button btnSelect, btnUpload;

    Uri imageUri;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_missing);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etLocation = findViewById(R.id.etLocation);
        etContact = findViewById(R.id.etContact);
        etDescription = findViewById(R.id.etDescription);
        spGender = findViewById(R.id.spGender);
        imageView = findViewById(R.id.imageView);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpload = findViewById(R.id.btnUpload);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spGender.setAdapter(adapter);

        btnSelect.setOnClickListener(v -> selectImage());
        btnUpload.setOnClickListener(v -> uploadData());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
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
                .child("missing_images/" + System.currentTimeMillis());

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {

                            Map<String, Object> data = new HashMap<>();
                            data.put("name", etName.getText().toString());
                            data.put("age", etAge.getText().toString());
                            data.put("gender", spGender.getSelectedItem().toString());
                            data.put("photoUrl", uri.toString());
                            data.put("lastSeenLocation", etLocation.getText().toString());
                            data.put("contactNumber", etContact.getText().toString());
                            data.put("description", etDescription.getText().toString());

                            db.collection("MissingPersons")
                                    .add(data)
                                    .addOnSuccessListener(documentReference ->
                                            Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show());

                        }));
    }
}
