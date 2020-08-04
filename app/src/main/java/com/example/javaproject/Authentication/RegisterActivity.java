package com.example.javaproject.Authentication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.javaproject.FirstLaunchActivities.LaunchActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_PHONE_STATE"};
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private FingerprintManager fingerprintManager;
    private SharedPreferencesManager sharedPreferencesManager;
    private EncryptionManger encryptionManger;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText pinEntry, emailEntry;
    private Button submit;
    private TelephonyManager telephonyManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPreferencesManager = new SharedPreferencesManager(this);
        encryptionManger = new EncryptionManger(this);

        if (!sharedPreferencesManager.getBoolPref("CompletedLaunchActivity")) {
            Intent intent = new Intent(RegisterActivity.this, LaunchActivity.class);
            startActivity(intent);
        }
        hasRegistered();

        emailEntry = findViewById(R.id.email_register_in);
        pinEntry = findViewById(R.id.pin_register_in);
        submit = findViewById(R.id.btn_register_in);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(pinEntry.getText().toString()) || pinEntry.getText().toString().length() > 6) {
                    RegisterUser(emailEntry.getText().toString(), pinEntry.getText().toString());
                    System.out.println("Register");
                } else {
                    pinEntry.setError("Please enter at least a 6 digit pin");
                }
            }
        });

    }

    private void hasRegistered() {
        if (sharedPreferencesManager.getBoolPref("Registered")) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            return;
        }
    }

    private void RegisterUser(String email, String pin) {
        boolean test = false;
        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferencesManager.setStringPref("Email", email);
        firebaseAuth.createUserWithEmailAndPassword(email, pin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                sharedPreferencesManager.setBoolPref("Registered", true);
                Toast.makeText(RegisterActivity.this, "Register successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            };
        });
    }
}
