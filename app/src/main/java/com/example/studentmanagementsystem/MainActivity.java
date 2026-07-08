package com.example.studentmanagementsystem;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase Database Imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class MainActivity extends ComponentActivity {

    private EditText etName, etRoll, etCourse, etId;
    private Button btnSave, btnView, btnUpdate, btnDelete;

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;

    // Firebase Database Reference Variable
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialize kiya (Main Node: "Students")
        dbRef = FirebaseDatabase.getInstance().getReference("Students");

        // UI Views Binding
        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etRoll = findViewById(R.id.etRoll);
        etCourse = findViewById(R.id.etCourse);

        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. CREATE: Save Data to Cloud using Sequential Roll Number as Key
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String course = etCourse.getText().toString().trim();

            if (!name.isEmpty() && !roll.isEmpty() && !course.isEmpty()) {

                // Alphanumeric push key hatakar roll number (1,2,3...) ko hi direct child key banaya
                String id = roll;

                HashMap<String, String> studentMap = new HashMap<>();
                studentMap.put("id", id); // Database entry ke andar bhi numeric ID save hogi
                studentMap.put("NAME", name);
                studentMap.put("roll", roll);
                studentMap.put("course", course);

                dbRef.child(id).setValue(studentMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Student added with ID: " + id, Toast.LENGTH_SHORT).show();
                            // Fields clean up after successful insertion
                            etName.setText("");
                            etRoll.setText("");
                            etCourse.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. READ: Fetch Data from Cloud Firebase
        btnView.setOnClickListener(v -> viewStudentsFromFirebase());

        // 3. UPDATE: Modify Data easily using Sequential Numeric Student ID (1,2,3...)
        btnUpdate.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String course = etCourse.getText().toString().trim();

            if (!id.isEmpty() && !name.isEmpty() && !roll.isEmpty() && !course.isEmpty()) {
                HashMap<String, Object> updateMap = new HashMap<>();
                updateMap.put("NAME", name);
                updateMap.put("roll", roll);
                updateMap.put("course", course);

                // Direct sequential child target (`Students/1`, `Students/2`)
                dbRef.child(id).updateChildren(updateMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "ID " + id + " Updated successfully!", Toast.LENGTH_SHORT).show();
                            etId.setText("");
                            etName.setText("");
                            etRoll.setText("");
                            etCourse.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(MainActivity.this, "Fill ID and all fields to update!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. DELETE: Remove Record directly using Sequential Student ID (1,2,3...)
        btnDelete.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (!id.isEmpty()) {
                // Directly targets the specific numeric child path and wipes it
                dbRef.child(id).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Record with ID " + id + " deleted!", Toast.LENGTH_SHORT).show();
                            etId.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Deletion Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(MainActivity.this, "ID is required for deletion!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Realtime Database Reader Method (Formats to JSON for StudentAdapter)
    private void viewStudentsFromFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JSONArray jsonArray = new JSONArray();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Fetching hashmap mapping from node
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                    if (map != null) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("id", map.get("id"));
                            jsonObject.put("NAME", map.get("NAME"));
                            jsonObject.put("roll", map.get("roll"));
                            jsonObject.put("course", map.get("course"));
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Binding clean array directly to your premium layout adapter
                studentAdapter = new StudentAdapter(jsonArray);
                recyclerView.setAdapter(studentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}