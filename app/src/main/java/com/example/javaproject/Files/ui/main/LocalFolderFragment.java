package com.example.javaproject.Files.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Files.FileExplorerActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocalFolderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private Context mContext;
    private Activity mActivity;
    private View mRoot;
    private PageViewModel pageViewModel;

    private static final int PICK_IMAGE_REQUEST = 1;
    private RecyclerView mRecyclerView;
    private List<LocalFolderModel> mFolderModels;
    private ImageView mPlaceholder;
    private LocalFolderAdapter mAdapter;
    private EncryptionManger mEncryptionManager;
    private SharedPreferencesManager mSharedPreferencesManager;
    private int requestCode;
    private int resultCode;
    FloatingActionButton local_add_folder, local_folder_back;
    @Nullable
    private Intent data;
    private String CURRENT_FOLDER_PATH;
    private String CURRENT_FOLDER;
    private String FOLDER_HASH;
    private String FOLDER_SALT;


    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.local_folder_activity, container, false);
        mContext = getContext();
        mActivity = getActivity();
        mEncryptionManager = new EncryptionManger(mContext);
        mSharedPreferencesManager = new SharedPreferencesManager(mContext);


        //Generate Dir folder
        if (!mSharedPreferencesManager.getBoolPref("filesDirFolderGenerated")) {
            File dir = new File(mContext.getFilesDir(), "eImages");
            dir.mkdirs();
            mSharedPreferencesManager.setBoolPref("folderGenerated", true);
        }

        //Generate Credentials folder
        if (!mSharedPreferencesManager.getBoolPref("credentialsFolderGenerated")) {
            File dir = new File(mContext.getFilesDir(), "credentials");
            dir.mkdirs();
            mSharedPreferencesManager.setBoolPref("credentialsFolderGenerated", true);
        }

//        resetFiles(new File(mContext.getFilesDir() + "/credentials"));
//        File dir = new File(mContext.getFilesDir(), "credentials");
//        dir.mkdirs();
//        resetFiles(new File(mContext.getFilesDir() + "/eImages"));


        InitialiseLayout();

        try {
            LoadData();
        } catch (IOException e) {
            e.printStackTrace();
        }


        local_add_folder = mRoot.findViewById(R.id.local_add_folder);
        local_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFolder();
            }
        });

        return mRoot;
    }

    private void resetFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
            file.delete();
        }
    }


    private void LoadData() throws IOException {
        mFolderModels.clear();

        File[] directories = new File(mContext.getFilesDir() + "/eImages").listFiles(File::isDirectory);
        for (File file : directories) {
            loadFolder(file);
        }
    }

    private void loadFolder(File file) {
        LocalFolderModel folderModel = new LocalFolderModel();
        folderModel.mPath = file.getPath();
        folderModel.mTitle = file.getName();
        mFolderModels.add(folderModel);
        mAdapter.notifyDataSetChanged();
    }

    private void createFolder() {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Create folder");

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText title = new EditText(mContext);
        title.setInputType(InputType.TYPE_CLASS_TEXT);
        title.setHint("Title Name");

        EditText password = new EditText(mContext);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password (OPTIONAL)");

        layout.addView(title);
        layout.addView(password);

        alert.setView(layout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderTitle = title.getText().toString();
                String folderPassword = password.getText().toString();
                if (!TextUtils.isEmpty(folderTitle)) {

                    for (LocalFolderModel folder : mFolderModels) {
                        if (folderTitle.equals(folder.mTitle)) {
                            Toast.makeText(mContext, "Folder already exists, please use a new title", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(folderPassword)) {

                        try {
                            File newFolder = new File(mContext.getFilesDir() + "/eImages", folderTitle);
                            newFolder.mkdirs();
                            mEncryptionManager.generateLocalPW(folderTitle, folderPassword);
                            loadFolder(newFolder);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        File newFolder = new File(mContext.getFilesDir() + "/eImages", folderTitle);
                        newFolder.mkdirs();
                        mEncryptionManager.createFolderCredentials(folderTitle, "AES");
                        loadFolder(newFolder);
                        dialog.dismiss();
                        //TODO Toast
                    }
                }
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void InitialiseLayout() {
        mRecyclerView = mRoot.findViewById(R.id.recyclerview_folders);

        mRecyclerView.setHasFixedSize(true);

        //Create a grid layout of width 4
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mFolderModels = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new LocalFolderAdapter(mActivity, mFolderModels);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new LocalFolderAdapter.OnItemClickListener() {

            @Override
            public void onGetPos(int position) {

            }

            @Override
            public void openFolder(int position) {

                LocalFolderModel folder = mFolderModels.get(position);
                if ((new File(mContext.getFilesDir() + "/credentials", folder.mTitle)).exists()) {
                    passwordDialog(folder);
                } else {
                    CURRENT_FOLDER_PATH = "/eImages/" + folder.mTitle;
                    CURRENT_FOLDER = folder.mTitle;
                    Intent intent = new Intent(mContext, FileExplorerActivity.class);
                    intent.putExtra("CURRENT_FOLDER_PATH", CURRENT_FOLDER_PATH);
                    intent.putExtra("CURRENT_FOLDER", CURRENT_FOLDER);
                    intent.putExtra("MODE", "local");
                    startActivity(intent);
                }


//                CURRENT_FOLDER_PATH = "/eImages/" + folder.mTitle;
//                CURRENT_FOLDER = folder.mTitle;
//                Intent intent = new Intent(mContext, FileExplorerActivity.class);
//                intent.putExtra("CURRENT_FOLDER_PATH", CURRENT_FOLDER_PATH);
//                intent.putExtra("CURRENT_FOLDER", CURRENT_FOLDER);
//                intent.putExtra("MODE", "local");
//
//                intent.putExtra("FOLDER_HASH", FOLDER_HASH);
//                intent.putExtra("FOLDER_SALT", FOLDER_SALT);
//                startActivity(intent);
            }
        });
    }

    private void passwordDialog(LocalFolderModel folder) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setTitle("Enter password for " + folder.mTitle);

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText password = new EditText(mContext);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password");

        ProgressBar progressBar = new ProgressBar(mContext);
        progressBar.setVisibility(GONE);
        layout.addView(progressBar);
        layout.addView(password);

        alert.setView(layout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                password.setVisibility(GONE);
                progressBar.setVisibility(View.VISIBLE);

                String folderPW = password.getText().toString();

                if (!TextUtils.isEmpty(folderPW)) {

                    try {
                        HashMap<String, String> hashMap = mEncryptionManager.AuthenticateLocalFolderPW(folder.mTitle, folderPW, "PBKDF2", "DESede");

                        if (hashMap != null) {
                            CURRENT_FOLDER_PATH = "/eImages/" + folder.mTitle;
                            CURRENT_FOLDER = folder.mTitle;
                            Intent intent = new Intent(mContext, FileExplorerActivity.class);
                            intent.putExtra("CURRENT_FOLDER_PATH", CURRENT_FOLDER_PATH);
                            intent.putExtra("CURRENT_FOLDER", CURRENT_FOLDER);
                            intent.putExtra("FOLDER_SALT", hashMap.get("salt"));
                            intent.putExtra("FOLDER_HASH", hashMap.get("hash"));
                            intent.putExtra("FOLDER_PASSWORD", hashMap.get("password"));
                            intent.putExtra("MODE", "local");
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Snackbar snackbar = Snackbar.make(getView(), "Password Incorrect", 1000);
                        snackbar.show();
                        e.printStackTrace();
                    }
                }
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(LocalFolderActivity.this, folder.mTitle + " failed to delete", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        alert.show();
    }
}