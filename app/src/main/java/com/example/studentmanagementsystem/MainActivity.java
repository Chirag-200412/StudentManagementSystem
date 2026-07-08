package com.example.studentmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private Button btnSave, btnView, btnUpdate, btnDelete, btnLogout; // Added btnLogout

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private DatabaseReference dbRef;
    private String userRole = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("ROLE")) {
            userRole = getIntent().getStringExtra("ROLE");
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Students");

        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etRoll = findViewById(R.id.etRoll);
        etCourse = findViewById(R.id.etCourse);

        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout); // Binded Logout Button

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        applyRoleRestrictions();

        // 🔥 LOGOUT BUTTON LOGIC
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                // User ko wapas clear stack ke sath Login screen par bhej dega
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // 1. CREATE
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String course = etCourse.getText().toString().trim();

            if (!name.isEmpty() && !roll.isEmpty() && !course.isEmpty()) {
                String id = roll;
                HashMap<String, String> studentMap = new HashMap<>();
                studentMap.put("id", id);
                studentMap.put("NAME", name);
                studentMap.put("roll", roll);
                studentMap.put("course", course);

                dbRef.child(id).setValue(studentMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Student added with ID: " + id, Toast.LENGTH_SHORT).show();
                            etName.setText("");
                            etRoll.setText("");
                            etCourse.setText("");
                        });
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. READ
        btnView.setOnClickListener(v -> viewStudentsFromFirebase());

        // 3. UPDATE
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

                dbRef.child(id).updateChildren(updateMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "ID " + id + " Updated successfully!", Toast.LENGTH_SHORT).show();
                            etId.setText("");
                            etName.setText("");
                            etRoll.setText("");
                            etCourse.setText("");
                        });
            } else {
                Toast.makeText(MainActivity.this, "Fill ID and all fields to update!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. DELETE
        btnDelete.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (!id.isEmpty()) {
                dbRef.child(id).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Record with ID " + id + " deleted!", Toast.LENGTH_SHORT).show();
                            etId.setText("");
                        });
            } else {
                Toast.makeText(MainActivity.this, "ID is required for deletion!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRoleRestrictions() {
        if (userRole.equals("Faculty")) {
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            Toast.makeText(this, "Welcome Faculty! (Edit Access Only)", Toast.LENGTH_LONG).show();

        } else if (userRole.equals("Student")) {
            btnSave.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            etId.setEnabled(false);
            etName.setEnabled(false);
            etRoll.setEnabled(false);
            etCourse.setEnabled(false);

            Toast.makeText(this, "Welcome Student! (View Only Access)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Welcome Admin! (Full Privilege)", Toast.LENGTH_LONG).show();
        }
    }

    private void viewStudentsFromFirebase() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JSONArray jsonArray = new JSONArray();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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
                studentAdapter = new StudentAdapter(jsonArray);
                recyclerView.setAdapter(studentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}