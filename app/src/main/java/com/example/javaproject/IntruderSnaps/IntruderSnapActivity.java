package com.example.javaproject.IntruderSnaps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Files.LocalFileAdapter;
import com.example.javaproject.Files.ui.main.LocalFileModel;
import com.example.javaproject.LocalGallery.GalleryActivity;
import com.example.javaproject.Misc.HubActivity;
import com.example.javaproject.Misc.SettingsActivity;
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
import java.util.List;

public class IntruderSnapActivity extends AppCompatActivity {

    private List<LocalFileModel> mItemModels;
    private RecyclerView mRecyclerView;
    private LocalFileAdapter mAdapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(IntruderSnapActivity.this, HubActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_snap);
        Toolbar toolbar = findViewById(R.id.toolbar_intruder_snaps);
        toolbar.setTitle("Intruder Snaps");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });


        InitialiseLayout();
        File file = new File(getFilesDir(), "/IntruderSnaps");
        LoadData(file);

    }

    private void InitialiseLayout() {

        mRecyclerView = findViewById(R.id.recycler_view_intruder);

        mRecyclerView.setHasFixedSize(true);

        //Create a grid layout of width 4
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mItemModels = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new LocalFileAdapter(IntruderSnapActivity.this, mItemModels, 1);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new LocalFileAdapter.OnItemClickListener() {
            @Override
            public void selectImg(int position) {

                Intent intent = new Intent(IntruderSnapActivity.this, GalleryActivity.class);
                intent.putExtra("CURRENT_FOLDER_PATH", "IntruderSnaps");
                intent.putExtra("CURRENT_GALLERY_POSITION", position);
                startActivity(intent);
            }
        });
    }

    private void LoadData(File dir) {
        mItemModels.clear();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                loadImage(file);
            }
            mAdapter.notifyDataSetChanged();
        } else return;
    }

    private void loadImage(File file) {
        LocalFileModel itemModel = new LocalFileModel();
        itemModel.mPath = file.getPath();
        itemModel.mTitle = file.getName();
        itemModel.mURI = Uri.fromFile(file);
        try {

            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(fileTime.toString());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss mm/dd/yy");
            itemModel.mSize = dateTimeFormatter.format(zonedDateTime);

        } catch (IOException e) {
            e.printStackTrace();
        }
        mItemModels.add(itemModel);
        mAdapter.notifyDataSetChanged();
    }
}
