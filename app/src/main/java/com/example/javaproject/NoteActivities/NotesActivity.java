package com.example.javaproject.NoteActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private ImageView addNote;
    private RecyclerView mRecyclerView;
    private NoteAdapter mAdapter;
    private List<NoteModel> mNotes;
    private SharedPreferencesManager mSharedPreferencesManager;
    private EncryptionManger mEncryptionManager;
    private String CURRENT_NOTE_PATH;
    private SearchView searchNotes;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            System.out.println("Resumed");
            LoadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = findViewById(R.id.toolbar_notes);
        toolbar.setTitle("Notes");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSharedPreferencesManager = new SharedPreferencesManager(this);
        mEncryptionManager = new EncryptionManger(this);


        //Generate Dir folder
        if (!mSharedPreferencesManager.getBoolPref("notesDirFolderGenerated")) {
            File dir = new File(getFilesDir(), "eNotes");
            dir.mkdirs();
            mSharedPreferencesManager.setBoolPref("folderGenerated", true);
        }

        //Generate Credentials folder
        if (!mSharedPreferencesManager.getBoolPref("credentialsFolderGenerated")) {
            File dir = new File(getFilesDir(), "credentials");
            dir.mkdirs();
            mSharedPreferencesManager.setBoolPref("credentialsFolderGenerated", true);
        }

        GenerateFolder();
        InitializeLayout();

        searchNotes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    try {
                        UpdateAdapter(s, s.length());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        LoadData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });


        try {
            LoadData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void GenerateFolder() {
        File file = new File(getFilesDir() + "/eNotes");
        if (!file.isDirectory()) {
            file.mkdirs();
        }
    }

    private void UpdateAdapter(String s, int character) throws IOException {
        mNotes.clear();
        File dir = new File(getFilesDir() + "/eNotes");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for (File file : files) {
                String[] split = file.getName().split("[.]");
                if (split[0].contains(s)) loadNote(file);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void LoadData() throws IOException {
        mNotes.clear();
        File dir = new File(getFilesDir() + "/eNotes");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            System.out.println(files.length);
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for (int i = files.length-1; i > -1; i--) {
                System.out.println("File name " + files[i].getName());
                loadNote(files[i]);
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    private void loadNote(File file) throws IOException {

        NoteModel noteModel = new NoteModel();
        String[] splitTitle = file.getName().split("[.]");
        noteModel.mTitle = splitTitle[0];
        noteModel.mPath = file.getPath();
        System.out.println(noteModel.mPath);
        try {

            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(fileTime.toString());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss \nmm/dd/yy ");
            noteModel.mDate = dateTimeFormatter.format(zonedDateTime);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mNotes.add(noteModel);
        // LoadData();
        mAdapter.notifyDataSetChanged();

    }


    private void InitializeLayout() {

        searchNotes = findViewById(R.id.search_notes);

        mRecyclerView = findViewById(R.id.recycler_view_notes);

        mRecyclerView.setHasFixedSize(true);

        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mNotes = new ArrayList<>();

        mAdapter = new NoteAdapter(this, mNotes);

        mRecyclerView.setAdapter(mAdapter);

        addNote = findViewById(R.id.add_notes);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesActivity.this, CreateNoteActivtiy.class);
                startActivity(intent);
            }
        });

        mAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void openNote(int position) {
                NoteModel note = mNotes.get(position);
                Intent intent = new Intent(NotesActivity.this, EditNoteActivity.class);
                intent.putExtra("CURRENT_NOTE", note.mTitle);
                intent.putExtra("CURRENT_NOTE_PATH", note.mPath);
                startActivity(intent);
            }
        });

    }

}
