package com.example.studentmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private EditText etName, etRoll, etCourse, etId, etAttendanceInput, etResultInput, etAnnouncementInput, etComplaintBody;
    private Button btnSave, btnView, btnUpdate, btnDelete, btnLogout, btnPostAnnouncement, btnSubmitComplaint, btnViewComplaints;

    private LinearLayout layoutAdminFaculty, layoutStudentCard;
    private TextView tvProfileName, tvProfileRoll, tvProfileCourse, tvProfileId, tvDashboardTitle, tvProfileAttendance, tvProfileResult, tvLiveAnnouncement;

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;

    // Core Branches
    private DatabaseReference dbStudents, dbAnnouncements, dbComplaints;

    private String userRole = "Admin";
    private String loggedInUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("ROLE")) { userRole = getIntent().getStringExtra("ROLE"); }
        if (getIntent().hasExtra("USERNAME")) { loggedInUser = getIntent().getStringExtra("USERNAME"); }

        dbStudents = FirebaseDatabase.getInstance().getReference("Students");
        dbAnnouncements = FirebaseDatabase.getInstance().getReference("Announcements");
        dbComplaints = FirebaseDatabase.getInstance().getReference("Complaints");

        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etRoll = findViewById(R.id.etRoll);
        etCourse = findViewById(R.id.etCourse);
        etAttendanceInput = findViewById(R.id.etAttendanceInput);
        etResultInput = findViewById(R.id.etResultInput);
        etAnnouncementInput = findViewById(R.id.etAnnouncementInput);
        etComplaintBody = findViewById(R.id.etComplaintBody);

        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout);
        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);
        btnViewComplaints = findViewById(R.id.btnViewComplaints);

        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvLiveAnnouncement = findViewById(R.id.tvLiveAnnouncement);

        layoutAdminFaculty = findViewById(R.id.layoutAdminFaculty);
        layoutStudentCard = findViewById(R.id.layoutStudentCard);

        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRoll = findViewById(R.id.tvProfileRoll);
        tvProfileCourse = findViewById(R.id.tvProfileCourse);
        tvProfileAttendance = findViewById(R.id.tvProfileAttendance);
        tvProfileResult = findViewById(R.id.tvProfileResult);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listenToAnnouncements();
        applyRoleRestrictions();

        // 📢 POST ANNOUNCEMENT
        btnPostAnnouncement.setOnClickListener(v -> {
            String notice = etAnnouncementInput.getText().toString().trim();
            if(!notice.isEmpty()) {
                dbAnnouncements.setValue(notice)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Announcement Broadcasted!", Toast.LENGTH_SHORT).show();
                            etAnnouncementInput.setText("");
                        });
            }
        });

        // 🚨 SUBMIT COMPLAINT
        btnSubmitComplaint.setOnClickListener(v -> {
            String complaintText = etComplaintBody.getText().toString().trim();
            if(!complaintText.isEmpty()) {
                HashMap<String, String> compMap = new HashMap<>();
                compMap.put("id", loggedInUser);
                compMap.put("NAME", "🚨 Student Complaint");
                compMap.put("roll", "Roll No: " + loggedInUser);
                compMap.put("course", complaintText);

                dbComplaints.child(loggedInUser).setValue(compMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show();
                            etComplaintBody.setText("");
                        });
            }
        });

        // 1. CREATE
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String course = etCourse.getText().toString().trim();
            String att = etAttendanceInput.getText().toString().trim();
            String res = etResultInput.getText().toString().trim();

            if (!name.isEmpty() && !roll.isEmpty() && !course.isEmpty()) {
                HashMap<String, String> studentMap = new HashMap<>();
                studentMap.put("id", roll);
                studentMap.put("NAME", name);
                studentMap.put("roll", roll);
                studentMap.put("course", course);
                studentMap.put("attendance", att.isEmpty() ? "Not Marked" : att);
                studentMap.put("result", res.isEmpty() ? "Awaiting" : res);

                dbStudents.child(roll).setValue(studentMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Student added with Roll No: " + roll, Toast.LENGTH_SHORT).show();
                            clearInputFields();
                        });
            }
        });

        // 2. READ (Student Records Matrix)
        btnView.setOnClickListener(v -> viewStudentsFromFirebase());

        // 🚨 READ NEW COMPLAINTS STREAM CONNECTOR
        btnViewComplaints.setOnClickListener(v -> viewComplaintsOnSystem());

        // 3. UPDATE
        btnUpdate.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (!id.isEmpty()) {
                HashMap<String, Object> updateMap = new HashMap<>();
                if(!etName.getText().toString().isEmpty()) updateMap.put("NAME", etName.getText().toString().trim());
                if(!etRoll.getText().toString().isEmpty()) updateMap.put("roll", etRoll.getText().toString().trim());
                if(!etCourse.getText().toString().isEmpty()) updateMap.put("course", etCourse.getText().toString().trim());
                if(!etAttendanceInput.getText().toString().isEmpty()) updateMap.put("attendance", etAttendanceInput.getText().toString().trim());
                if(!etResultInput.getText().toString().isEmpty()) updateMap.put("result", etResultInput.getText().toString().trim());

                dbStudents.child(id).updateChildren(updateMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "ID " + id + " Updated successfully!", Toast.LENGTH_SHORT).show();
                            clearInputFields();
                        });
            }
        });

        // 4. DELETE
        btnDelete.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (!id.isEmpty()) {
                dbStudents.child(id).removeValue().addOnSuccessListener(aVoid -> etId.setText(""));
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void listenToAnnouncements() {
        dbAnnouncements.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) { tvLiveAnnouncement.setText(snapshot.getValue(String.class)); }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyRoleRestrictions() {
        if (userRole.equals("Faculty")) {
            layoutAdminFaculty.setVisibility(View.VISIBLE);
            layoutStudentCard.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnViewComplaints.setVisibility(View.VISIBLE);
            tvDashboardTitle.setText("Faculty Portal Management");
        } else if (userRole.equals("Student")) {
            layoutAdminFaculty.setVisibility(View.GONE);
            layoutStudentCard.setVisibility(View.VISIBLE);
            tvDashboardTitle.setText("Student ERP Terminal");
            fetchSingleStudentData(loggedInUser);
        } else {
            layoutAdminFaculty.setVisibility(View.VISIBLE);
            layoutStudentCard.setVisibility(View.GONE);
            btnViewComplaints.setVisibility(View.VISIBLE);
            tvDashboardTitle.setText("Admin Command Center");
        }
    }

    private void fetchSingleStudentData(String studentIdKey) {
        dbStudents.child(studentIdKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.child("id").getValue() != null ? snapshot.child("id").getValue().toString() : "";
                    String name = snapshot.child("NAME").getValue() != null ? snapshot.child("NAME").getValue().toString() : "";
                    String roll = snapshot.child("roll").getValue() != null ? snapshot.child("roll").getValue().toString() : "";
                    String course = snapshot.child("course").getValue() != null ? snapshot.child("course").getValue().toString() : "";
                    String attValue = snapshot.hasChild("attendance") ? snapshot.child("attendance").getValue().toString() : "Not Marked";
                    String resValue = snapshot.hasChild("result") ? snapshot.child("result").getValue().toString() : "Awaiting";

                    tvProfileId.setText("Student System ID: " + id);
                    tvProfileName.setText("Full Name: " + name);
                    tvProfileRoll.setText("University Roll No: " + roll);
                    tvProfileCourse.setText("Enrolled Course: " + course.toUpperCase());
                    tvProfileAttendance.setText("📊 Attendance Status: " + attValue);
                    tvProfileResult.setText("🎓 Current Semester Result: " + resValue);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void viewComplaintsOnSystem() {
        dbComplaints.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JSONArray jsonArray = new JSONArray();
                if (!snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "No active complaints found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.child("id").getValue() != null ? ds.child("id").getValue().toString() : "";
                    String name = ds.child("NAME").getValue() != null ? ds.child("NAME").getValue().toString() : "";
                    String roll = ds.child("roll").getValue() != null ? ds.child("roll").getValue().toString() : "";
                    String course = ds.child("course").getValue() != null ? ds.child("course").getValue().toString() : "";

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", id);
                        jsonObject.put("NAME", name);
                        jsonObject.put("roll", roll);
                        jsonObject.put("course", course);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                studentAdapter = new StudentAdapter(jsonArray);
                recyclerView.setAdapter(studentAdapter);
                Toast.makeText(MainActivity.this, "Complaints Loaded!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void viewStudentsFromFirebase() {
        dbStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JSONArray jsonArray = new JSONArray();
                if (!snapshot.exists()) { return; }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.child("id").getValue() != null ? dataSnapshot.child("id").getValue().toString() : "";
                    String name = dataSnapshot.child("NAME").getValue() != null ? dataSnapshot.child("NAME").getValue().toString() : "";
                    String roll = dataSnapshot.child("roll").getValue() != null ? dataSnapshot.child("roll").getValue().toString() : "";
                    String course = dataSnapshot.child("course").getValue() != null ? dataSnapshot.child("course").getValue().toString() : "";
                    String attendance = dataSnapshot.child("attendance").getValue() != null ? dataSnapshot.child("attendance").getValue().toString() : "Not Marked";
                    String result = dataSnapshot.child("result").getValue() != null ? dataSnapshot.child("result").getValue().toString() : "Awaiting";

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", id);
                        jsonObject.put("NAME", name);
                        jsonObject.put("roll", roll);
                        jsonObject.put("course", course);
                        jsonObject.put("attendance", attendance);
                        jsonObject.put("result", result);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                studentAdapter = new StudentAdapter(jsonArray);
                recyclerView.setAdapter(studentAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clearInputFields() {
        etId.setText(""); etName.setText(""); etRoll.setText(""); etCourse.setText("");
        etAttendanceInput.setText(""); etResultInput.setText("");
    }
}