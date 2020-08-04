package com.example.javaproject.Authentication;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.javaproject.Misc.HubActivity;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;


public class LoginActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private EditText pinEntry;
    private Button submit;

    private KeyguardManager keyguardManager;
    private TextView lock_msg_handler, forgotten_password;
    private ImageView fingerprintImgHandler;

    private int counter;
    private int limit;
    private int timer;

    private FirebaseAuth firebaseAuth;
    private SharedPreferencesManager sharedPreferencesManager;

    //Anti-theft
    private Preview preview;
    private ImageCaptureConfig imageCaptureConfig;
    private ImageCapture imageCapture;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        lock_msg_handler = findViewById(R.id.lock_message_handler);
        pinEntry = findViewById(R.id.pin_sign_in);
        submit = findViewById(R.id.btn_sign_in);
        forgotten_password = findViewById(R.id.forgotten_password);
        forgotten_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPassword.class);
                startActivity(intent);
            }
        });
        //error_msg_handler = findViewById(R.id.login_error_handler);
        counter = 0;

        for (String string : REQUIRED_PERMISSIONS) {
            checkPermission(string, REQUEST_CODE_PERMISSIONS);
        }
        sharedPreferencesManager = new SharedPreferencesManager(this);

        //Initialize Camera
        if (allPermissionGranted()) {
            InitializeCamera();
        } else {
            ActivityCompat.requestPermissions(LoginActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(TextUtils.isEmpty(pinEntry.getText().toString()) && pinEntry.getText().toString().length() < 4)) {
                    //Automatically enabled, can be disabled in the settings
                    if (sharedPreferencesManager.getBoolPref("AUTHENTICATION_SIGN_IN_LOCK")) {
                        //If this pref is default val (0), then set to default value of 4, else use what has been set in settings
                        limit = sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_ATTEMPTS") == 0 ? 2 : (sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_ATTEMPTS") - 1);
                        if (counter == limit) {
                            //Only snap if this has been specified in settings
                            if (sharedPreferencesManager.getBoolPref("AUTHENTICATION_INTRUDER_SNAP")) {

                                File file = new File(getFilesDir(), "IntruderSnaps/" + "Intruder" + System.currentTimeMillis() + ".jpg");
                                // file.delete();

                                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                                    @Override
                                    public void onImageSaved(@NonNull File file) {
                                        System.out.println(file.getName() + " created.");
                                    }

                                    @Override
                                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                                        System.out.println("Error snapping file.");
                                    }
                                });
                            }

                            submit.setVisibility(View.GONE);
                            forgotten_password.setVisibility(View.GONE);
                            pinEntry.setText("");
                            pinEntry.setFocusable(false);

                            //If this is default value (0), set to default value which is 0

                            timer = sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_TIMER") == 0 ? 10000 : (sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_TIMER") * 1000) + 1000 ;
                            System.out.println(timer);

                            new CountDownTimer(timer, 1000) {

                                @Override
                                public void onTick(long millisUntilFinished) {
                                    lock_msg_handler.setVisibility(View.VISIBLE);
                                    lock_msg_handler.setText("LOGIN ATTEMPTS LOCKED FOR : " + millisUntilFinished / 1000);
                                }

                                @Override
                                public void onFinish() {
                                    lock_msg_handler.setVisibility(View.GONE);
                                    pinEntry.setFocusable(true);
                                    forgotten_password.setVisibility(View.VISIBLE);
                                    submit.setVisibility(View.VISIBLE);
                                    counter = 0;
                                }
                            }.start();
                        } else {
                            authUser(pinEntry.getText().toString());
                        }

                    } else {
                        authUser(pinEntry.getText().toString());
                    }
                } else {
                    pinEntry.setError("Please enter at least a 4 digit pin");
                }
            }
        });

        CheckVersion();
    }

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode) {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            this,
                            new String[]{permission},
                            requestCode);
        } else {
            System.out.println("Permission Granted");
//            Toast
//                    .makeText(this,
//                            "Permission already granted",
//                            Toast.LENGTH_SHORT)
//                    .show();
        }
    }

    private void authUser(String password) {

        firebaseAuth.signInWithEmailAndPassword(sharedPreferencesManager.getStringPref("Email"), password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                pinEntry.setText("");
                Toast.makeText(LoginActivity.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HubActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pinEntry.setText("");
                counter++;
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean allPermissionGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                return false;

            }
        }
        return true;
    }

    private void InitializeCamera() {

        CameraX.unbindAll();

        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .build();

        preview = new Preview(previewConfig);

        imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();


        imageCapture = new ImageCapture(imageCaptureConfig);

        //Check directory is created
        File dir = new File(getFilesDir(), "IntruderSnaps");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        CameraX.bindToLifecycle(this, preview, imageCapture);

    }

    private void CheckVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
            fingerprintHandler.authentication(fingerprintManager, null);
        }
    }

}

