package com.example.javaproject.Files.ui.main;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.CloudGallery.GalleryActivity;
import com.example.javaproject.Files.CloudFileAdapter;
import com.example.javaproject.Files.CloudFileModel;
import com.example.javaproject.Files.FolderExplorerActivity;
import com.example.javaproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CloudFilesFragment extends Fragment {
    private Activity mActivity;
    private Context mContext;
    private View mRoot;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseAuth mFirebaseAuth;
    private List<CloudFileModel> mUploads;
    private CloudFileAdapter mAdapter;
    private String CURRENT_FOLDER, FOLDER_KEY;
    private RecyclerView mRecyclerView;
    private FloatingActionButton cloud_add_files, cloud_download_folder, cloud_delete_folder;
    private int flag = 0;
    Toolbar toolbar;


    public CloudFilesFragment(String currentFolder) {
       CURRENT_FOLDER = currentFolder;
       System.out.print(CURRENT_FOLDER);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_local_files, container, false);

        mContext = getContext();
        mActivity = getActivity();


        initiateFirebase();
        getData();
        InitialiseLayout();



        toolbar = mRoot.findViewById(R.id.toolbar_local_files);
        toolbar.inflateMenu(R.menu.local_folder_menu_main);
        toolbar.setTitle(CURRENT_FOLDER);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                Intent intent = new Intent(mContext, FolderExplorerActivity.class);
                startActivity(intent);
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteFolder();
                        return true;
                }
                return false;
            }
        });


        return mRoot;
    }

    private void deleteFolder() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setMessage("Are you sure you want to delete: " + CURRENT_FOLDER);
        alertDialog.setCancelable(true);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                flag = 1;

                DatabaseReference dRef = mDatabaseReference.child("users/" + mFirebaseAuth.getUid() + "/folders/" + CURRENT_FOLDER);
                DatabaseReference dFolRef = mDatabaseReference.child("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER);

                dFolRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if (flag == 1) {
                                CloudFileModel upload = postSnapshot.getValue(CloudFileModel.class);
                                StorageReference ref = mFirebaseStorage.getReferenceFromUrl((upload.mImageURL));
                                ref.delete();
                            }
                        }
                        if (flag == 1) {
                            dFolRef.removeValue();
                        }

                        flag = 0;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        flag = 0;
                    }
                });

                dRef.removeValue();

                Intent intent = new Intent(mContext, FolderExplorerActivity.class);
                startActivity(intent);
                }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();


        //flag = 0;

    }

    private void InitialiseLayout() {

        mRecyclerView = mRoot.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Create a grid layout of width 4
        //mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mUploads = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new CloudFileAdapter(mContext, mUploads);


        mRecyclerView.setAdapter(mAdapter);

        //Handle on click listener events for each item
        mAdapter.setOnItemClickListener(new CloudFileAdapter.OnItemClickListener() {
            @Override
            public void onGetPos(int position) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putExtra("CURRENT_FOLDER", CURRENT_FOLDER);
                intent.putExtra("CURRENT_GALLERY_POSITION", position);
                startActivity(intent);
            }
        });
    }

    private void initiateFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    private void getData() {

        DatabaseReference ref = mDatabaseReference.child("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    CloudFileModel upload = postSnapshot.getValue(CloudFileModel.class);

                    //Key reference so we can manipulate values later
                    upload.mkey = (postSnapshot.getKey());

                    mUploads.add(upload);

                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error
            }
        });

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
