package com.example.javaproject.Files;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Files.ui.main.CloudFilesFragment;
import com.example.javaproject.Files.ui.main.LocalFileModel;
import com.example.javaproject.Files.ui.main.LocalFilesFragment;
import com.example.javaproject.Files.ui.main.LocalUploadFilesFragment;
import com.example.javaproject.IntruderSnaps.IntruderSnapActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.Misc.HubActivity;
import com.example.javaproject.Misc.SettingsActivity;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileExplorerActivity extends AppCompatActivity {

    private int folderFlag = 0;
    private int dataFlag = 0;
    private int downloadFlag = 0;
    int encryptionFlag = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;

    private EncryptionManger encryptionManger;
    private SharedPreferencesManager sharedPreferencesManager;

    private List<LocalFileModel> mItemModels;
    private String CURRENT_FOLDER, CURRENT_FOLDER_PATH, FOLDER_PASSWORD, FOLDER_HASH, FOLDER_SALT, MODE;
    MenuItem backUp;
    MenuItem download;
    private ProgressBar progressBar;

    BottomNavigationView.OnNavigationItemSelectedListener navListener;
    BottomNavigationView bottomNavigationMenu;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(FileExplorerActivity.this, HubActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ON DESTROY");
        if (bottomNavigationMenu != null)
            bottomNavigationMenu.setVisibility(View.GONE);
        if (MODE.equals("local")) {
            if (FOLDER_PASSWORD != null) {
                encryptData();
            } else {
                basicEncryptData();
            }
        }
    }

    public void basicDecryptData() {
        System.out.println("BASIC DECRYPTING DATA");
        File dir = new File(getFilesDir() + CURRENT_FOLDER_PATH);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String splitName = file.getName();
                String[] parts = splitName.split("[.]");
                if (parts.length == 3) {
                    encryptionManger.basicFileDecrypter(CURRENT_FOLDER, file, new File(getFilesDir() + CURRENT_FOLDER_PATH, parts[0] + "." + parts[1]), "AES");
                    file.delete();
                }
            }
        }
    }

    public void basicEncryptData() {
        System.out.println("BASIC ENCRYPTING DATA");
        File dir = new File(getFilesDir() + CURRENT_FOLDER_PATH);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String splitName = file.getName();
                String[] parts = splitName.split("[.]");
                if (parts.length == 2) {
                    encryptionManger.basicFileEncrypter(CURRENT_FOLDER, file, new File(getFilesDir() + CURRENT_FOLDER_PATH, file.getName() + ".e"), "AES");
                    file.delete();
                }
            }
        }
    }


    public void encryptData() {
        System.out.println("ENCRYPTING DATA");
        String iv = sharedPreferencesManager.getStringPref(FOLDER_SALT);
        File dir = new File(getFilesDir() + "/eImages", CURRENT_FOLDER);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            System.out.println("Folder size : " + files.length);
            for (File file : files) {
                String splitName = file.getName();
                String[] parts = splitName.split("[.]");
                if (parts.length == 2) {
                    System.out.println("Encrypted");
                    encryptionManger.fileEncrypter(file, FOLDER_HASH, new File(getFilesDir() + "/eImages/" + CURRENT_FOLDER, file.getName() + ".e"), iv, FOLDER_SALT);
                    file.delete();
                    //fileEncrypter(File file, String stringKey, String algorithm, OutputStream out, String optionalIV, String optionalSalt)
                }
            }
        }
    }

    public void decryptData() {
        System.out.println("DECRYPTING DATA");
        String iv = sharedPreferencesManager.getStringPref(FOLDER_SALT);
        File dir = new File(getFilesDir() + CURRENT_FOLDER_PATH);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String splitName = file.getName();
                String[] parts = splitName.split("[.]");
                if (parts.length == 3) {
                    encryptionManger.fileDecrypter(file, FOLDER_HASH, "DESede", new File(getFilesDir() + CURRENT_FOLDER_PATH, parts[0] + "." + parts[1]), iv, FOLDER_SALT);
                    file.delete();
                    //fileEncrypter(File file, String stringKey, String algorithm, OutputStream out, String optionalIV, String optionalSalt)
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("ON PAUSE");
        if (backUp != null)
            backUp.setIcon(R.drawable.ic_backup);
        if (download != null)
            download.setIcon(R.drawable.ic_file_download);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file_explorer);
        progressBar = findViewById(R.id.files_progress_bar);


        CURRENT_FOLDER_PATH = getIntent().getStringExtra("CURRENT_FOLDER_PATH");
        CURRENT_FOLDER = getIntent().getStringExtra("CURRENT_FOLDER");
        FOLDER_HASH = getIntent().getStringExtra("FOLDER_HASH");
        FOLDER_SALT = getIntent().getStringExtra("FOLDER_SALT");
        FOLDER_PASSWORD = getIntent().getStringExtra("FOLDER_PASSWORD");
        MODE = getIntent().getStringExtra("MODE");

//        System.out.println("CURRENT_FOLDER_PATH " + CURRENT_FOLDER_PATH);
//        System.out.println("CURRENT_FOLDER " + CURRENT_FOLDER);
        System.out.println("FOLDER_PW " + FOLDER_PASSWORD);
        System.out.println("FOLDER_HASH " + FOLDER_HASH);
        System.out.println("FOLDER_SALT " + FOLDER_SALT);
//        System.out.println("MODES " + MODE);

        encryptionManger = new EncryptionManger(this);
        sharedPreferencesManager = new SharedPreferencesManager(this);


        if (MODE.equals("local")) {
            if (FOLDER_PASSWORD != null)
                decryptData();
            else basicDecryptData();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new LocalFilesFragment(CURRENT_FOLDER_PATH, CURRENT_FOLDER)).commit();
        } else
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new CloudFilesFragment(CURRENT_FOLDER)).commit();

        initiateFirebase();

        if (MODE.equals("local")) {
            navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    System.out.println("1" + menuItem.toString());
                    switch (menuItem.toString()) {
                        case "Files":
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new LocalFilesFragment(CURRENT_FOLDER_PATH, CURRENT_FOLDER)).commit();
                            return true;
                        case "Back up":
                            backUp = menuItem;
                            backupItems();
                            return false;
                        case "Upload":
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new LocalUploadFilesFragment(CURRENT_FOLDER, "local")).commit();
                            return true;
                    }

                    return false;
                }
            };
        } else {
            navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    System.out.println("1" + menuItem.toString());
                    switch (menuItem.toString()) {
                        case "Files":
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new CloudFilesFragment(CURRENT_FOLDER)).commit();
                            return true;
                        case "Download":
                            download = menuItem;
                            downloadFolder();
                            return false;
                        case "Upload":
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout1, new LocalUploadFilesFragment(CURRENT_FOLDER, "cloud")).commit();
                            return true;
                    }

                    return false;
                }
            };

        }

        bottomNavigationMenu = findViewById(MODE.equals("local") ? R.id.nav_view_local : R.id.nav_view_cloud);
        bottomNavigationMenu.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationMenu.setVisibility(View.VISIBLE);
    }


    private void initiateFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
    }


    private interface collectFoldersListener {
        void collectFolderTitles(List<String> list);
    }

    private void listFolders(FileExplorerActivity.collectFoldersListener listener) {
        //TODO Check folder title to look for duplicates
        //-if so then dialog to change title
        List<String> mFolderTitles = new ArrayList<>();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getUid() + "/folders");
        databaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    mFolderTitles.add(postSnapshot.getKey());

                }

                if (folderFlag == 1) {
                    listener.collectFolderTitles(mFolderTitles);
                }

                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });

    }

    private String uploadTitle;

    private void backupItems() {

        folderFlag = 1;

        listFolders(new FileExplorerActivity.collectFoldersListener() {

            @Override
            public void collectFolderTitles(List<String> folderTitles) {
                //onValueEventListener is Async and is called every time databse is changed
                //Flag 0 prevents this from happening

                if (uploadTitle == null) uploadTitle = CURRENT_FOLDER;

                if (folderTitles.contains(uploadTitle)) {

                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getUid() + "/folders").child(uploadTitle);
                    databaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String cPassword = null;
                            if (dataSnapshot.child("password").getValue() != null) {
                                cPassword = dataSnapshot.child("password").getValue().toString();
                            }

                            if (folderFlag == 1) {

                                if (cPassword == null) {
                                    UpdateBackUpContents();
                                }
                                else if (cPassword.equals(FOLDER_PASSWORD)) {
                                    UpdateBackUpContents();
                                } else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(FileExplorerActivity.this);
                                    alertDialog.setMessage("Folder already exists, please rename folder upload ");
                                    alertDialog.setCancelable(true);

                                    LinearLayout layout = new LinearLayout(FileExplorerActivity.this);
                                    layout.setOrientation(LinearLayout.VERTICAL);

                                    final EditText title = new EditText(FileExplorerActivity.this);
                                    title.setInputType(InputType.TYPE_CLASS_TEXT);
                                    title.setHint("New backup folder title : ");

                                    layout.addView(title);

                                    alertDialog.setView(layout);

                                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!TextUtils.isEmpty(title.getText().toString())) {
                                                uploadTitle = title.getText().toString();
                                                backupItems();
                                            } else {
                                                title.setError("Please enter a title");
                                            }
                                        }
                                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            dialog.dismiss();
                                        }
                                    });

                                    alertDialog.show();
                                    //backupItems();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    return;

                } else {
                    folderFlag = 0;
                    Map folderModel = new HashMap();


                    folderModel.put("title", uploadTitle);

                    if (FOLDER_PASSWORD == null) {
                        folderModel.put("password", null);
                    } else {
                        folderModel.put("password", FOLDER_PASSWORD);
                    }

                    DatabaseReference ref = databaseReference.child("users/" + firebaseAuth.getUid() + "/folders").child(uploadTitle);

                    ref.setValue(folderModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Toast.makeText(FileExplorerActivity.this, uploadTitle + " folder uploaded", Toast.LENGTH_SHORT).show();

                            File dir = new File(getFilesDir() + "/eImages/", CURRENT_FOLDER);

                            if (dir.exists()) {
                                File[] files = dir.listFiles();
                                for (File file : files) {
                                    uploadFile(file);
                                }
                            }

                            if (uploadTitle != CURRENT_FOLDER) {
                                renameDir(new File(getFilesDir(), CURRENT_FOLDER_PATH), new File(getFilesDir() + "/eImages", uploadTitle));
                            }

                            uploadTitle = null;
                            backUp.setIcon(R.drawable.ic_check);
                            // local_back_up_folder.setImageDrawable(getResources().getDrawable(R.drawable.ic_done, getApplicationContext().getTheme()));
                            //TODO anmiated icon
                            //Toast.makeText(LocalImgActivity.this, "folder created", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FileExplorerActivity.this, "Error uploading " + CURRENT_FOLDER, Toast.LENGTH_SHORT).show();
                        }
                    });

                    //return;
                }
            }
        });
    }


    private void renameDir(File dir, File newDir) {
        if (dir.exists()) {
            dir.renameTo(newDir);
            dir.delete();
        }
    }

    private void uploadFile(File file) {
        StorageReference storageRef = storageReference.child("users/" + firebaseAuth.getUid() + "/" + CURRENT_FOLDER);
        DatabaseReference databaseRef = databaseReference.child("users/" + firebaseAuth.getUid() + "/" + CURRENT_FOLDER);
        StorageReference fileRef = storageRef.child(file.getName());
        System.out.println("Enters");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileRef.putStream(fileInputStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Enters1");
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    String uploadKey = databaseRef.push().getKey();
                    CloudFileModel upload = new CloudFileModel(file.getName(), downloadUrl.toString(), uploadKey, file.length() / 1024 + " KB");
                    databaseRef.child(uploadKey).setValue(upload);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FileExplorerActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private interface collectDataListener {
        void collectDataTitles(List<String> list);
    }

    private void listData(FileExplorerActivity.collectDataListener collectDataListener) {
        List<String> mDataTitles = new ArrayList<>();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getUid() + "/" + CURRENT_FOLDER);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    mDataTitles.add(postSnapshot.child("mTitle").getValue().toString());
                }

                if (dataFlag == 1) {
                    collectDataListener.collectDataTitles(mDataTitles);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void LoadData(File dir) {
        mItemModels = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                System.out.println("loaded");
                loadImage(file);
            }
        } else return;
    }

    private void loadImage(File file) {
        LocalFileModel itemModel = new LocalFileModel();
        itemModel.mPath = file.getPath();
        itemModel.mTitle = file.getName();
        itemModel.mURI = Uri.fromFile(file);
        mItemModels.add(itemModel);
    }

    private void UpdateBackUpContents() {
        dataFlag = 1;
        LoadData(new File(getFilesDir(), CURRENT_FOLDER_PATH));
        List<LocalFileModel> mUploads = new ArrayList<>();
        listData(new FileExplorerActivity.collectDataListener() {
            @Override
            public void collectDataTitles(List<String> list) {
                dataFlag = 0;
                for (LocalFileModel file : mItemModels) {
                    if (!list.contains(file.mTitle)) {
                        mUploads.add(file);
                        System.out.println("UpdateBackupContents " + file.mTitle);
                    }
                }

                if (mUploads.size() > 0) {
                    for (LocalFileModel file : mUploads) {
                        File upload = new File(file.mPath);
                        uploadFile(upload);
                    }
                    Toast.makeText(FileExplorerActivity.this, mUploads.size() + " files where backed up", Toast.LENGTH_SHORT).show();
                    backUp.setIcon(R.drawable.ic_check);
                    //local_back_up_folder.setImageDrawable(getResources().getDrawable(R.drawable.ic_done, getApplicationContext().getTheme()));
                } else {
                    backUp.setIcon(R.drawable.ic_check);
                    //local_back_up_folder.setImageDrawable(getResources().getDrawable(R.drawable.ic_done, getApplicationContext().getTheme()));
                    //Toast.makeText(LocalImgActivity.this, "All files are backed up", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        });

    }

    private void downloadFolder() {

        downloadFlag = 1;

        //Check if folder exists, if not generate folder & iv
        File dir = new File(getFilesDir() + "/eImages/" + CURRENT_FOLDER);
        if (dir.exists()) {
            //Snackbar snackbar = new Snackbar(getBaseContext(), "Folder already exists", 1000);
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show();
            return;
        } else {
            dir.mkdirs();
        }
        progressBar.setVisibility(View.VISIBLE);
        encryptionManger = new EncryptionManger(this);

        //Check if folder is PW protected, if so create new pw file
        DatabaseReference dFolRef1 = databaseReference.child("users/" + firebaseAuth.getUid() + "/folders/" + CURRENT_FOLDER);
        dFolRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (downloadFlag == 1) {

                    if (dataSnapshot.child("password").getValue() != null) {

                        try {
                            encryptionManger.copyCloudPassword(CURRENT_FOLDER, dataSnapshot.child("password").getValue().toString());
                            encryptionFlag = 1;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        encryptionManger.createFolderCredentials(CURRENT_FOLDER, "AES");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });

        DatabaseReference dFolRef = databaseReference.child("users/" + firebaseAuth.getUid() + "/" + CURRENT_FOLDER);
        dFolRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (downloadFlag == 1) {
                        CloudFileModel upload = postSnapshot.getValue(CloudFileModel.class);
                        File file = new File(dir, upload.mTitle);
                        StorageReference ref = firebaseStorage.getReferenceFromUrl(upload.mImageURL);
                        ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                if(encryptionFlag == 1)
                                encryptData();
                                else encryptData();
                            }
                        });
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                downloadFlag = 0;
            }
        });


        download.setIcon(R.drawable.ic_check);
    }
}