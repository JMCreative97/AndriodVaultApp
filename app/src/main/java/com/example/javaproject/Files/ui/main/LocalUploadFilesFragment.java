package com.example.javaproject.Files.ui.main;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Files.CloudFileModel;
import com.example.javaproject.Files.UploadAdapter;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalUploadFilesFragment extends Fragment {
    private Activity mActivity;
    private Context mContext;
    private View mRoot;
    private String CURRENT_FOLDER_PATH;
    private String MODE;

    private static final int PICK_IMAGE_REQUEST = 2;
    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private RecyclerView mRecyclerView;
    private List<Uri> mUploads;
    private List<String> mUploadTitles;
    private List<String> mUploadingStatus;
    private List<String> mUploadSize;
    private UploadAdapter mAdapter;
    private String CURRENT_FOLDER;
    private List<Uri> tempUploads;

    @Override
    public void onPause() {
        super.onPause();
        mUploadSize.clear();
        mUploadTitles.clear();
        mUploadingStatus.clear();
        mAdapter.notifyDataSetChanged();
    }

    public LocalUploadFilesFragment(String currentFolder, String MODE) {
        CURRENT_FOLDER = currentFolder;
        this.MODE = MODE;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.activity_cloud_img_selecter, container, false);
        mContext = getContext();
        mActivity = getActivity();

        CURRENT_FOLDER = mActivity.getIntent().getStringExtra("CURRENT_FOLDER");

        InitialiseLayout();

        FloatingActionButton fab = mRoot.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImgs();
            }
        });

        return mRoot;
    }


    private void InitiateFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void InitialiseLayout() {
        mRecyclerView = mRoot.findViewById(R.id.CloudSelecterRecyclerView);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mUploads = new ArrayList<>();
        mUploadTitles = new ArrayList<>();
        mUploadSize = new ArrayList<>();
        mUploadingStatus = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new UploadAdapter(mContext, mUploadTitles, mUploadSize, mUploadingStatus, MODE.equals("local") ? MODE : "cloud");

        mRecyclerView.setAdapter(mAdapter);

    }

    private void selectImgs() {

        //This opens a dialog for the user to chose and upload an image
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //Users browing restricted to files of image type
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*", "application/pdf"});//.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //Opens browser so user can select an image
        startActivityForResult(Intent.createChooser(intent, "Select images to upload"), PICK_IMAGE_REQUEST);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        tempUploads = new ArrayList<>();
        int errCount = 0;


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            if (data.getClipData() != null) {

                System.out.println(data.getClipData().getItemAt(0).getUri());

                int clipSize = data.getClipData().getItemCount();

                for (int i = 0; i < clipSize; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();

                    mUploads.add(uri);
                    tempUploads.add(uri);
                }
            } else {
                if (data.getData() != null) {
                    Uri uri = data.getData();
                    mUploads.add(uri);
                    tempUploads.add(uri);
                }
            }
        }

        if (MODE.equals("cloud")) {
            InitiateFirebase();
            uploadDataFirebase();
        } else if (MODE.equals("local")) {
            try {
                uploadDataLocal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (tempUploads.size() > 0) {
            //Toast.makeText(mContext, "Loaded: " + tempUploads.size() + " Image(s) with " + errCount + "issues", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadDataLocal() throws IOException {

        if (tempUploads.size() != 0) {
            for (int i = 0; i < tempUploads.size(); i++) {

                System.out.println(tempUploads.get(i).toString());

                Uri uri = tempUploads.get(i);
                System.out.println(getImagePath(getContext(), uri));
                File file = new File(getImagePath(getContext(), uri));
                String title = file.getName();

                Date date = new Date();
                long milli = date.getTime();

                mUploadTitles.add(title);
                mUploadSize.add(file.length() / 1024 + " KB");
                mUploadingStatus.add("Uploading..");

                File newFile = new File(mContext.getFilesDir() + "/eImages/" + CURRENT_FOLDER, file.getName());
                FileInputStream in = new FileInputStream(file);
                FileOutputStream out = new FileOutputStream(newFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                mUploadingStatus.remove(i);
                mUploadingStatus.add(i, "Uploaded");

                mAdapter.notifyDataSetChanged();


                in.close();
                out.close();
            }

            deleteUploadsDialog();
        }
    }

    private void uploadDataFirebase() {
        if (tempUploads.size() != 0) {
            StorageReference storageReference = mStorageReference.child("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER);
            DatabaseReference databaseReference = mDatabaseReference.child("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER);
            for (int i = 0; i < tempUploads.size(); i++) {


                Uri uri = tempUploads.get(i);
                File file = new File(getImagePath(getContext(), uri));
                String title = file.getName();

                Date date = new Date();
                long milli = date.getTime();

                mUploadTitles.add(title);
                mUploadSize.add(file.length() / 1024 + " KB");
                mUploadingStatus.add("Uploading..");


                mAdapter.notifyDataSetChanged();

                StorageReference fileReference = storageReference.child(milli + title);
                final int finalI = i;
                fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        Uri downloadUrl = urlTask.getResult();
                        String uploadKey = databaseReference.push().getKey();
                        CloudFileModel upload = new CloudFileModel(milli + title, downloadUrl.toString(), uploadKey, file.length() / 1024 + " KB");
                        databaseReference.child(uploadKey).setValue(upload);


                        mUploadingStatus.remove(finalI);
                        mUploadingStatus.add(finalI, "Uploaded");

                        mAdapter.notifyDataSetChanged();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //TODO
                    }
                });
            }
            deleteUploadsDialog();
        }
    }

    private void deleteUploadsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setMessage("Do you wish to delete the following: " + tempUploads.size() + " items from your device?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < tempUploads.size(); i++) {
                            File file = new File(tempUploads.get(i).getPath());
                            System.out.print(tempUploads.get(i).getPath());
                            file.delete();
                            if (file.exists()) {
                                System.out.print("doesn't work");
                            }
                        }
                        Toast.makeText(mContext, "Items deleted", Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialog.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param //context The context.
     * @param uri The Uri to query.
     * @author paulburke
     **/


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static String getImagePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            System.out.println(uri.toString());
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     *
     * Get Data Column
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     **/

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

}

