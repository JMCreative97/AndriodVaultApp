package com.example.javaproject.NoteActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CreateNoteActivtiy extends AppCompatActivity {

    private EditText mTitle;
    private EditText mDescription;
    private Button mSubmit;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private EncryptionManger mEncryptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        Toolbar toolbar = findViewById(R.id.toolbar_create_note);
        toolbar.setTitle("Create Note");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEncryptionManager = new EncryptionManger(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users/" + mFirebaseAuth.getUid() + "/notes");

        mTitle = findViewById(R.id.note_title);
        mDescription = findViewById(R.id.note_description);
        mSubmit = findViewById(R.id.note_submit);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = mTitle.getText().toString().trim();
                String description = mDescription.getText().toString().trim();

                if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(description)) {
                    try {
                        CreateNote(title, description);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CreateNoteActivtiy.this, "Please fill out a title and description", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CreateNote(String title, String description) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, ClassNotFoundException {
        int noteFlag = 0;
        String titleTrimed = trimTitle(title);
        File dir = new File(getFilesDir() + "/eNotes");
        File[] files = dir.listFiles();
        for (File f : files) {
            String[] split = f.getName().split("[.]");
            if (split[0].equals(titleTrimed)) {
                Toast.makeText(this, "Note already exists", Toast.LENGTH_SHORT).show();
                noteFlag = 1;
            }
        }
        if (noteFlag == 0) {
            try {
                mEncryptionManager.createNoteCredentials(titleTrimed, "AES");
                mEncryptionManager.basicNoteEncrypter(description, new File(getFilesDir() + "/eNotes", titleTrimed + ".n"), titleTrimed, "AES");
                finish();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        noteFlag = 0;
    }

    private String trimTitle(String title){
        String[] split = title.split(" ");
        String trimmedTitle = "";
        for(String string : split){
            trimmedTitle += string;
        }
        return trimmedTitle;
    }

}