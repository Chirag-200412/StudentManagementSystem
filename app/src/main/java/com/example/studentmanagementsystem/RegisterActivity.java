package com.example.studentmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends ComponentActivity {

    private EditText etRegUsername, etRegPassword, etRegConfirmPassword;
    private RadioGroup rgRegRole;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // XML elements binding cleanly matching layouts
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        rgRegRole = findViewById(R.id.rgRegRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Pointing database structural tree to "Users" node
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        btnRegister.setOnClickListener(v -> {
            String username = etRegUsername.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();
            String confirmPassword = etRegConfirmPassword.getText().toString().trim();

            int selectedRoleId = rgRegRole.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(RegisterActivity.this, "Please select a role!", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSelected = findViewById(selectedRoleId);
            String role = rbSelected.getText().toString(); // Extracted dynamic strings: Admin, Faculty, Student

            // Validation checks
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Creating nested database authorization tree
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("password", password);
            userMap.put("role", role);

            // Path mapping structures: Users -> [Role] -> [Username]
            dbRef.child(role).child(username).setValue(userMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterActivity.this, "Registration Successful as " + role, Toast.LENGTH_LONG).show();
                        // Shifting path tracking back to Login controller
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Intent transfer back to Login Panel manually
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}