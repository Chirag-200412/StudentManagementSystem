package com.example.studentmanagementsystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private EditText etName, etRoll, etCourse, etId, etNewId, etAttendanceInput, etResultInput, etAnnouncementInput, etComplaintBody, etTotalFeeInput, etDueFeeInput, etTimeTableInput;
    private Button btnSave, btnView, btnUpdate, btnDelete, btnLogout, btnPostAnnouncement, btnSubmitComplaint, btnViewComplaints, btnGenerateReceipt, btnPayOnline, btnViewFeeApprovals, btnPostTimeTable;

    private LinearLayout layoutAdminFaculty, layoutStudentCard;
    private TextView tvProfileName, tvProfileRoll, tvProfileCourse, tvProfileId, tvDashboardTitle, tvProfileAttendance, tvProfileResult, tvLiveAnnouncement, tvProfileTotalFee, tvProfileDueFee, tvStudentTimeTable;

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;

    private DatabaseReference dbStudents, dbAnnouncements, dbComplaints, dbApprovals, dbTimeTable;

    private String userRole = "Admin";
    private String loggedInUser = "";

    private String rName="", rRoll="", rCourse="", rTotal="0", rDue="0", rId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().hasExtra("ROLE")) { userRole = getIntent().getStringExtra("ROLE"); }
        if (getIntent().hasExtra("USERNAME")) { loggedInUser = getIntent().getStringExtra("USERNAME"); }

        dbStudents = FirebaseDatabase.getInstance().getReference("Students");
        dbAnnouncements = FirebaseDatabase.getInstance().getReference("Announcements");
        dbComplaints = FirebaseDatabase.getInstance().getReference("Complaints");
        dbApprovals = FirebaseDatabase.getInstance().getReference("PaymentApprovals");
        dbTimeTable = FirebaseDatabase.getInstance().getReference("TimeTable");

        etId = findViewById(R.id.etId);
        etNewId = findViewById(R.id.etNewId);
        etName = findViewById(R.id.etName);
        etRoll = findViewById(R.id.etRoll);
        etCourse = findViewById(R.id.etCourse);
        etAttendanceInput = findViewById(R.id.etAttendanceInput);
        etResultInput = findViewById(R.id.etResultInput);
        etAnnouncementInput = findViewById(R.id.etAnnouncementInput);
        etComplaintBody = findViewById(R.id.etComplaintBody);
        etTotalFeeInput = findViewById(R.id.etTotalFeeInput);
        etDueFeeInput = findViewById(R.id.etDueFeeInput);
        etTimeTableInput = findViewById(R.id.etTimeTableInput);

        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout);
        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);
        btnViewComplaints = findViewById(R.id.btnViewComplaints);
        btnGenerateReceipt = findViewById(R.id.btnGenerateReceipt);
        btnPayOnline = findViewById(R.id.btnPayOnline);
        btnViewFeeApprovals = findViewById(R.id.btnViewFeeApprovals);
        btnPostTimeTable = findViewById(R.id.btnPostTimeTable);

        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvLiveAnnouncement = findViewById(R.id.tvLiveAnnouncement);
        tvStudentTimeTable = findViewById(R.id.tvStudentTimeTable);

        layoutAdminFaculty = findViewById(R.id.layoutAdminFaculty);
        layoutStudentCard = findViewById(R.id.layoutStudentCard);

        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRoll = findViewById(R.id.tvProfileRoll);
        tvProfileCourse = findViewById(R.id.tvProfileCourse);
        tvProfileAttendance = findViewById(R.id.tvProfileAttendance);
        tvProfileResult = findViewById(R.id.tvProfileResult);
        tvProfileTotalFee = findViewById(R.id.tvProfileTotalFee);
        tvProfileDueFee = findViewById(R.id.tvProfileDueFee);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listenToAnnouncements();
        listenToTimeTable();
        applyRoleRestrictions();

        // FACULTY TIMETABLE SUBMISSION CONTROLLER
        btnPostTimeTable.setOnClickListener(v -> {
            String schedule = etTimeTableInput.getText().toString().trim();
            if(!schedule.isEmpty()) {
                dbTimeTable.setValue(schedule)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Academic Schedule Published!", Toast.LENGTH_SHORT).show();
                            etTimeTableInput.setText("");
                        });
            }
        });

        // POST ANNOUNCEMENT
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

        // SUBMIT COMPLAINT
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

        btnGenerateReceipt.setOnClickListener(v -> showReceiptPopup());
        btnPayOnline.setOnClickListener(v -> showPaytmQrPopup());
        btnViewFeeApprovals.setOnClickListener(v -> viewFeeApprovalsOnSystem());

        // 1. CREATE
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String roll = etRoll.getText().toString().trim();
            String course = etCourse.getText().toString().trim();
            String att = etAttendanceInput.getText().toString().trim();
            String res = etResultInput.getText().toString().trim();
            String tFee = etTotalFeeInput.getText().toString().trim();
            String dFee = etDueFeeInput.getText().toString().trim();

            if (!name.isEmpty() && !roll.isEmpty() && !course.isEmpty()) {
                HashMap<String, String> studentMap = new HashMap<>();
                studentMap.put("id", roll);
                studentMap.put("NAME", name);
                studentMap.put("roll", roll);
                studentMap.put("course", course);
                studentMap.put("attendance", att.isEmpty() ? "Not Marked" : att);
                studentMap.put("result", res.isEmpty() ? "Awaiting" : res);
                studentMap.put("total_fee", tFee.isEmpty() ? "0" : tFee);
                studentMap.put("due_fee", dFee.isEmpty() ? "0" : dFee);

                dbStudents.child(roll).setValue(studentMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                            clearInputFields();
                        });
            }
        });

        // 2. READ
        btnView.setOnClickListener(v -> viewStudentsFromFirebase());
        btnViewComplaints.setOnClickListener(v -> viewComplaintsOnSystem());

        // 3. UPDATE WITH etNewId SUPPORT
        btnUpdate.setOnClickListener(v -> {
            String currentId = etId.getText().toString().trim();
            String newId = etNewId.getText().toString().trim();

            if (currentId.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter Current Student ID first!", Toast.LENGTH_SHORT).show();
                return;
            }

            dbStudents.child(currentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(MainActivity.this, "No record found matching this Current ID!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String finalName = !etName.getText().toString().isEmpty() ? etName.getText().toString().trim() :
                            (snapshot.child("NAME").getValue() != null ? snapshot.child("NAME").getValue().toString() : "");

                    String finalRoll = !etRoll.getText().toString().isEmpty() ? etRoll.getText().toString().trim() :
                            (snapshot.child("roll").getValue() != null ? snapshot.child("roll").getValue().toString() : currentId);

                    String finalCourse = !etCourse.getText().toString().isEmpty() ? etCourse.getText().toString().trim() :
                            (snapshot.child("course").getValue() != null ? snapshot.child("course").getValue().toString() : "");

                    String finalAtt = !etAttendanceInput.getText().toString().isEmpty() ? etAttendanceInput.getText().toString().trim() :
                            (snapshot.child("attendance").getValue() != null ? snapshot.child("attendance").getValue().toString() : "Not Marked");

                    String finalRes = !etResultInput.getText().toString().isEmpty() ? etResultInput.getText().toString().trim() :
                            (snapshot.child("result").getValue() != null ? snapshot.child("result").getValue().toString() : "Awaiting");

                    String finalTotal = !etTotalFeeInput.getText().toString().isEmpty() ? etTotalFeeInput.getText().toString().trim() :
                            (snapshot.child("total_fee").getValue() != null ? snapshot.child("total_fee").getValue().toString() : "0");

                    String finalDue = !etDueFeeInput.getText().toString().isEmpty() ? etDueFeeInput.getText().toString().trim() :
                            (snapshot.child("due_fee").getValue() != null ? snapshot.child("due_fee").getValue().toString() : "0");

                    HashMap<String, Object> studentMap = new HashMap<>();
                    studentMap.put("NAME", finalName);
                    studentMap.put("course", finalCourse);
                    studentMap.put("attendance", finalAtt);
                    studentMap.put("result", finalRes);
                    studentMap.put("total_fee", finalTotal);
                    studentMap.put("due_fee", finalDue);

                    if (!newId.isEmpty() && !newId.equals(currentId)) {
                        studentMap.put("id", newId);
                        studentMap.put("roll", finalRoll.equals(currentId) ? newId : finalRoll);

                        dbStudents.child(newId).setValue(studentMap)
                                .addOnSuccessListener(aVoid -> {
                                    dbStudents.child(currentId).removeValue();
                                    dbApprovals.child(currentId).removeValue();
                                    Toast.makeText(MainActivity.this, "ID updated to: " + newId, Toast.LENGTH_LONG).show();
                                    clearInputFields();
                                });
                    } else {
                        studentMap.put("id", currentId);
                        studentMap.put("roll", finalRoll);

                        dbStudents.child(currentId).updateChildren(studentMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Data updated under ID: " + currentId, Toast.LENGTH_SHORT).show();
                                    clearInputFields();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 4. DELETE
        btnDelete.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            if (!id.isEmpty()) {
                dbStudents.child(id).removeValue().addOnSuccessListener(aVoid -> etId.setText(""));
                dbApprovals.child(id).removeValue();
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

    private void listenToTimeTable() {
        dbTimeTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    tvStudentTimeTable.setText(snapshot.getValue(String.class));
                } else {
                    tvStudentTimeTable.setText("No classes scheduled yet for today.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🔥 FIXED: RESTRICTION LOGIC MODIFIED TO EXCLUDE FACULTY FROM FEE APPROVALS
    private void applyRoleRestrictions() {
        if (userRole.equals("Faculty")) {
            layoutAdminFaculty.setVisibility(View.VISIBLE);
            layoutStudentCard.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnViewComplaints.setVisibility(View.VISIBLE);

            // 🔥 REMOVED RIGHT FROM FACULTY: Button is now strictly hidden for Faculty role
            btnViewFeeApprovals.setVisibility(View.GONE);

            tvDashboardTitle.setText("Faculty Portal Management");
        } else if (userRole.equals("Student")) {
            layoutAdminFaculty.setVisibility(View.GONE);
            layoutStudentCard.setVisibility(View.VISIBLE);
            tvDashboardTitle.setText("Student ERP Terminal");
            fetchSingleStudentData(loggedInUser);
        } else {
            // Admin role retains full access
            layoutAdminFaculty.setVisibility(View.VISIBLE);
            layoutStudentCard.setVisibility(View.GONE);
            btnViewComplaints.setVisibility(View.VISIBLE);
            btnViewFeeApprovals.setVisibility(View.VISIBLE); // Admin keeps the button visible
            tvDashboardTitle.setText("Admin Command Center");
        }
    }

    private void fetchSingleStudentData(String studentIdKey) {
        dbStudents.child(studentIdKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    rId = snapshot.child("id").getValue() != null ? snapshot.child("id").getValue().toString() : "";
                    rName = snapshot.child("NAME").getValue() != null ? snapshot.child("NAME").getValue().toString() : "";
                    rRoll = snapshot.child("roll").getValue() != null ? snapshot.child("roll").getValue().toString() : "";
                    rCourse = snapshot.child("course").getValue() != null ? snapshot.child("course").getValue().toString() : "";

                    String attValue = snapshot.hasChild("attendance") ? snapshot.child("attendance").getValue().toString() : "Not Marked";
                    String resValue = snapshot.hasChild("result") ? snapshot.child("result").getValue().toString() : "Awaiting";

                    rTotal = snapshot.hasChild("total_fee") ? snapshot.child("total_fee").getValue().toString() : "0";
                    rDue = snapshot.hasChild("due_fee") ? snapshot.child("due_fee").getValue().toString() : "0";

                    tvProfileId.setText("Student System ID: " + rId);
                    tvProfileName.setText("Full Name: " + rName);
                    tvProfileRoll.setText("University Roll No: " + rRoll);
                    tvProfileCourse.setText("Enrolled Course: " + rCourse.toUpperCase());
                    tvProfileAttendance.setText("📊 Attendance Status: " + attValue + "%");
                    tvProfileResult.setText("🎓 Current Semester Result: " + resValue);

                    tvProfileTotalFee.setText("Total Course Fee: ₹" + rTotal);
                    tvProfileDueFee.setText("Outstanding Due: ₹" + rDue);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showReceiptPopup() {
        int total = Integer.parseInt(rTotal);
        int due = Integer.parseInt(rDue);
        int paid = total - due;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🧾 DIGITAL FEE RECEIPT");
        String report = "----------------------------------------\n" +
                "UNIVERSITY ERP PORTAL CLEARANCE\n" +
                "----------------------------------------\n" +
                "Student Name : " + rName + "\n" +
                "Roll Number  : " + rRoll + "\n" +
                "Course Group : " + rCourse.toUpperCase() + "\n" +
                "----------------------------------------\n" +
                "Total Course Fee  : ₹" + total + "\n" +
                "Amount Paid       : ₹" + paid + "\n" +
                "Outstanding Dues  : ₹" + due + "\n" +
                "----------------------------------------\n" +
                "Status: " + (due <= 0 ? "🟢 FULLY CLEARED" : "🔴 DUES PENDING") + "\n" +
                "----------------------------------------\n" +
                "Generated via live system sync.";
        builder.setMessage(report);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showPaytmQrPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 Paytm UPI Payment Gateway");

        LinearLayout modalLayout = new LinearLayout(this);
        modalLayout.setOrientation(LinearLayout.VERTICAL);
        modalLayout.setPadding(40, 30, 40, 30);
        modalLayout.setGravity(android.view.Gravity.CENTER);

        TextView tvAmountNotice = new TextView(this);
        tvAmountNotice.setText("Please pay your pending due amount: ₹" + rDue);
        tvAmountNotice.setTextSize(16);
        tvAmountNotice.setTextColor(android.graphics.Color.parseColor("#DC2626"));
        tvAmountNotice.setGravity(android.view.Gravity.CENTER);
        tvAmountNotice.setPadding(0, 0, 0, 30);
        modalLayout.addView(tvAmountNotice);

        ImageView ivQrHolder = new ImageView(this);
        ivQrHolder.setImageResource(R.drawable.paytm_qr);

        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(650, 950);
        ivQrHolder.setLayoutParams(imgParams);
        modalLayout.addView(ivQrHolder);

        builder.setView(modalLayout);

        builder.setPositiveButton("I HAVE PAID 👍", (dialog, which) -> {
            HashMap<String, String> approvalMap = new HashMap<>();
            String secureApprovalKey = (rId != null && !rId.trim().isEmpty()) ? rId.trim() : rRoll.trim();

            approvalMap.put("id", secureApprovalKey);
            approvalMap.put("NAME", "Paid Due: " + rName);
            approvalMap.put("roll", "Roll No: " + rRoll);
            approvalMap.put("course", "Claimed clearance for amount: ₹" + rDue);

            dbApprovals.child(secureApprovalKey).setValue(approvalMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Payment notification sent to Admin approval desk!", Toast.LENGTH_LONG).show();
                    });
            dialog.dismiss();
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void viewFeeApprovalsOnSystem() {
        dbApprovals.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                JSONArray jsonArray = new JSONArray();
                if (!snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "No pending fee approvals found!", Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new StudentAdapter(new JSONArray()));
                    return;
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String name = ds.child("NAME").getValue() != null ? ds.child("NAME").getValue().toString() : "Paid Due Request";
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
                Toast.makeText(MainActivity.this, "Fee Requests Loaded!", Toast.LENGTH_SHORT).show();
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
                    String id = dataSnapshot.getKey() != null ? dataSnapshot.getKey() : "";
                    String name = dataSnapshot.child("NAME").getValue() != null ? dataSnapshot.child("NAME").getValue().toString() : "";
                    String roll = dataSnapshot.child("roll").getValue() != null ? dataSnapshot.child("roll").getValue().toString() : "";
                    String course = dataSnapshot.child("course").getValue() != null ? dataSnapshot.child("course").getValue().toString() : "";
                    String attendance = dataSnapshot.child("attendance").getValue() != null ? dataSnapshot.child("attendance").getValue().toString() : "Not Marked";
                    String result = dataSnapshot.child("result").getValue() != null ? dataSnapshot.child("result").getValue().toString() : "Awaiting";

                    if (name.trim().isEmpty() && roll.trim().isEmpty()) {
                        continue;
                    }

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", id);
                        jsonObject.put("NAME", "ID: " + id + " | Name: " + name);
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
        etId.setText(""); etNewId.setText(""); etName.setText(""); etRoll.setText(""); etCourse.setText("");
        etAttendanceInput.setText(""); etResultInput.setText("");
        etTotalFeeInput.setText(""); etDueFeeInput.setText("");
    }
}