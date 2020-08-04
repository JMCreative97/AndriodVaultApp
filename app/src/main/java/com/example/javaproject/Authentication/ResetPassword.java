package com.example.javaproject.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    /**/
    private EditText emailReset;
    private Button btnReset;

    private SharedPreferencesManager sharedPreferencesManager;
    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Toolbar toolbar = findViewById(R.id.toolbar_reset_password);
        toolbar.setTitle("Reset Password");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        emailReset = findViewById(R.id.reset_confirm_email);
        btnReset = findViewById(R.id.reset_btn_confirm);

        sharedPreferencesManager = new SharedPreferencesManager(this);
        fireAuth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(emailReset.getText().toString()))
                    if (emailReset.getText().toString().equals(sharedPreferencesManager.getStringPref("Email")))
                        fireAuth.sendPasswordResetEmail(emailReset.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "password reset link sent", Toast.LENGTH_LONG).show();
                                    Intent switchView = new Intent(ResetPassword.this, LoginActivity.class);
                                    startActivity(switchView);
                                } else {
                                    Toast.makeText(getApplicationContext(), "password resent failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
