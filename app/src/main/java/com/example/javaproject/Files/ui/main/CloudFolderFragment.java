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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Files.FileExplorerActivity;
import com.example.javaproject.Files.FolderAdapter;
import com.example.javaproject.Files.FolderModel;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class CloudFolderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private Context mContext;
    private Activity mActivity;
    private View mRoot;
    private PageViewModel pageViewModel;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    public DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private Button reauthenticte;
    private LinearLayout offline_mode;
    private RecyclerView recyclerView;
    public List<FolderModel> mFolders;
    private FolderAdapter mAdapter;

    private EncryptionManger mEncryptionManager;

    private Button newFolder;
    private EditText title;

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
        mRoot = inflater.inflate(R.layout.activity_cloud_folder, container, false);
        mContext = getContext();
        mActivity = getActivity();

        offline_mode = mRoot.findViewById(R.id.cloud_offline_layout);
        offline_mode.setVisibility(View.GONE);
        reauthenticte = mRoot.findViewById(R.id.cloud_btn_reauthenticate);
        reauthenticte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
            }
        });


        initiateFirebase();
        if (firebaseAuth.getUid() == null) {
            System.out.println("does");
            offline_mode.findViewById(R.id.cloud_offline_layout);
            offline_mode.setVisibility(View.VISIBLE);
        }
        CreateRecyclerView();
        getData();

        return mRoot;

    }

    private void initiateFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    private void CreateRecyclerView() {
        recyclerView = mRoot.findViewById(R.id.recycler_view_folders);

        recyclerView.setHasFixedSize(true);

        //Create a grid layout of width 4
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));

        mFolders = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new FolderAdapter(mContext, mFolders);

        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onGetPos(int position) {
                FolderModel folder = mFolders.get(position);
                if (folder.mPassword == null) {
                    Intent intent = new Intent(mContext, FileExplorerActivity.class);
                    intent.putExtra("CURRENT_FOLDER", folder.mTitle);
                    intent.putExtra("MODE", "cloud");
                    startActivity(intent);
                } else {
                    createPasswordDialog(folder);
                    //TODO
                    // Get folder pw  Enc(salt hash)
                    // sd
                    // generateFolderPW(String folderTitle, String pw, String hashAlgorithm, String encAlgorithm)
                    // getFolderPW(String folderTitle, String pw)

                }
            }
        });
    }

    private void createPasswordDialog(FolderModel folder) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setTitle("Enter password for " + folder.mTitle);

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText password = new EditText(mContext);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password");

        layout.addView(password);

        alert.setView(layout);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pwEntry = password.getText().toString();
                if (!TextUtils.isEmpty(pwEntry)) {
                    try {
                        mEncryptionManager = new EncryptionManger(getContext());
                        HashMap<String, String> hashMap = mEncryptionManager.AuthenticateCloudFolderPW(folder.mPassword, pwEntry, "PBKDF2", "Blowfish");
                        if (hashMap != null) {
                            Intent intent = new Intent(mContext, FileExplorerActivity.class);
                            intent.putExtra("CURRENT_FOLDER", folder.mTitle);
                            intent.putExtra("FOLDER_SALT", hashMap.get("salt"));
                            intent.putExtra("FOLDER_HASH", hashMap.get("hash"));
                            intent.putExtra("FOLDER_PASSWORD", hashMap.get("password"));
                            intent.putExtra("MODE", "cloud");
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar snackbar = Snackbar.make(getView(), "Incorrect password", 1000);
                        snackbar.show();
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

    private void getData() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getUid() + "/folders");
        databaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFolders.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    FolderModel folder = postSnapshot.getValue(FolderModel.class);

                    //folder.mKey = (postSnapshot.getKey());

                    folder.mTitle = postSnapshot.getKey();

                    if (postSnapshot.child("password").getValue() == null) {
                        folder.mPassword = null;
                    } else {
                        folder.mPassword = (postSnapshot.child("password").getValue().toString());
                    }

                    mFolders.add(folder);

                }

                //Notify adapter of uploaded items
                mAdapter.notifyDataSetChanged();

                //TODO IF MFOLDER == 0 SHOW IMAGE INSTRUCTING TO BACKUP FOLDERSS
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });

    }


}