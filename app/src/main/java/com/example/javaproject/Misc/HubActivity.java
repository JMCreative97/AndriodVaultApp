package com.example.javaproject.Misc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.javaproject.AppLock.AppLockActivity;
import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Files.FolderExplorerActivity;
import com.example.javaproject.IntruderSnaps.IntruderSnapActivity;
import com.example.javaproject.NoteActivities.NotesActivity;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class HubActivity extends AppCompatActivity {

    /**/
    private ImageView settings, notes, files, local_img, intruder_snaps, app_lock, hide_icon;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SharedPreferencesManager sharedPreferencesManager;
    private EncryptionManger mEncryptionManger;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(HubActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        Toolbar toolbar = findViewById(R.id.toolbar_hub);
        toolbar.setTitle("Hub");
        setSupportActionBar(toolbar);

        mEncryptionManger = new EncryptionManger(this);
        firebaseAuth = FirebaseAuth.getInstance();

        //AuthenticateUser();

        files = findViewById(R.id.hub_files);
        notes = findViewById(R.id.hub_notes);
        intruder_snaps = findViewById(R.id.hub_intruder_snap);
        app_lock = findViewById(R.id.hub_app_lock);
        hide_icon = findViewById(R.id.hub_hide_icon);
        settings = findViewById(R.id.settings);


        files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, FolderExplorerActivity.class);
                startActivity(switchView);
            }
        });

        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, NotesActivity.class);
                startActivity(switchView);
            }
        });

        intruder_snaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, IntruderSnapActivity.class);
                startActivity(switchView);
            }
        });

        app_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, AppLockActivity.class);
                startActivity(switchView);
            }
        });

        hide_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, HideIcon.class);
                startActivity(switchView);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchView = new Intent(HubActivity.this, SettingsActivity.class);
                startActivity(switchView);
            }
        });

    }
}
