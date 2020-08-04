package com.example.javaproject.NoteActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;

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

public class EditNoteActivity extends AppCompatActivity {

    private TextView mTitle;
    private EditText mDescription;
    private Button mBack;
    private EncryptionManger mEncryptionManager;
    private String CURRENT_NOTE_PATH;
    private String CURRENT_NOTE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);


        mEncryptionManager = new EncryptionManger(this);

        //mTitle = findViewById(R.id.note_title_edit);
        mDescription = findViewById(R.id.note_description_edit);
        mBack = findViewById(R.id.note_edit_done);

        CURRENT_NOTE_PATH = getIntent().getStringExtra("CURRENT_NOTE_PATH");
        CURRENT_NOTE = getIntent().getStringExtra("CURRENT_NOTE");

        Toolbar toolbar = findViewById(R.id.toolbar_edit_notes);
        toolbar.setTitle(CURRENT_NOTE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Dialog
                deleteNote();
                return false;
            }

        });


        File file = new File(getFilesDir() + "/eNotes", CURRENT_NOTE);
        System.out.println(CURRENT_NOTE_PATH);

        String[] split = file.getName().split("[.]");
        String title = split[0];

        //mTitle.setText(title);

        try {
            readNote();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateNote();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(EditNoteActivity.this, NotesActivity.class);
                startActivity(intent);
            }
        });
    }


    private void deleteNote() {
        mEncryptionManager.deleteCredentials(CURRENT_NOTE);
        File file = new File(CURRENT_NOTE_PATH);
        file.delete();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_edit_notes, menu);
        return true;
    }


    private void updateNote() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, ClassNotFoundException {
        File file = new File(getFilesDir() + "/eNotes", CURRENT_NOTE + ".n");
        file.delete();

        try {
            mEncryptionManager.basicNoteEncrypter(mDescription.getText().toString(), file, CURRENT_NOTE, "AES");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private void readNote() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, ClassNotFoundException {
        String description = null;
        try {
            description = mEncryptionManager.basicNoteDecrypter(new File(getFilesDir() + "/eNotes", CURRENT_NOTE + ".n"), CURRENT_NOTE, "AES");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        mDescription.setText(description);
    }
}
