package com.example.javaproject.Files;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Files.ui.main.SectionsPagerAdapter;
import com.example.javaproject.IntruderSnaps.IntruderSnapActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.Misc.HubActivity;
import com.example.javaproject.Misc.SettingsActivity;
import com.example.javaproject.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class FolderExplorerActivity extends AppCompatActivity {

    private String CURRENT_FOLDER_PATH;
    private String CURRENT_FOLDER;
    private EncryptionManger mEncryptionManager;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(FolderExplorerActivity.this, HubActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_explorer);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        mEncryptionManager = new EncryptionManger(this);


        //TODO
        // ONRESUME : Get current folder & encrypt

    }

}