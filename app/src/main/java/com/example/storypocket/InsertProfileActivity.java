package com.example.storypocket;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InsertProfileActivity extends AppCompatActivity {

    private EditText etfullname, etusername, etphonenumber;
    private Button savebutton;
    private DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        etfullname = findViewById(R.id.etFullName);
        etusername = findViewById(R.id.etUserName);
        etphonenumber = findViewById(R.id.etPhoneNum);
        savebutton = findViewById(R.id.btnSave);

        database = FirebaseDatabase.getInstance().getReference();

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user.getEmail().replace(".","");
                String fullname = etfullname.getText().toString().trim();
                String username = etusername.getText().toString().trim();
                String phonenumber = etphonenumber.getText().toString().trim();

                if (fullname.isEmpty() || username.isEmpty() || phonenumber.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Ada data belum terisi", Toast.LENGTH_SHORT).show();
                } else {
                    database = FirebaseDatabase.getInstance().getReference("users");
                    database.child(email).child("fullname").setValue(fullname);
                    database.child(email).child("username").setValue(username);
                    database.child(email).child("phonenumber").setValue(phonenumber);

                    Toast.makeText(getApplicationContext(), "Register Berhasil", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}