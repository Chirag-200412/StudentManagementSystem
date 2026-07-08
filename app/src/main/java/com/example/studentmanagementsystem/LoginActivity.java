package com.example.studentmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends ComponentActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnLogin;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(LoginActivity.this, "Please select a role!", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSelected = findViewById(selectedRoleId);
            String role = rbSelected.getText().toString(); // Admin, Faculty, Student

            if (!username.isEmpty() && !password.isEmpty()) {
                performLogin(username, password, role);
            } else {
                Toast.makeText(LoginActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin(String username, String password, String role) {
        // Role ke hisab se sahi node ko target karenge (e.g., Users/Admin/chirag)
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(role).child(username);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbPassword = snapshot.child("password").getValue(String.class);

                    if (password.equals(dbPassword)) {
                        Toast.makeText(LoginActivity.this, role + " Login Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if (role.equals("Admin")) {
                            // Admin ko tumhare purane main crud panel par bhejenge
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        } else {
                            // Faculty aur Student ke liye basic routing (abhi ke liye same ya custom dashboard)
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("ROLE", role); // Role pass kar rahe hain restricted access ke liye
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, role + " Username not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}