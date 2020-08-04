package com.example.javaproject.CloudGallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.example.javaproject.Files.CloudFileModel;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.FileProvider.getUriForFile;

public class GalleryActivity extends AppCompatActivity {

    private Integer CURRENT_GALLERY_POSITION;
    private Integer CURRENT_GALLERY_SIZE;
    private String CURRENT_FOLDER;
    private String CURRENT_DATABASE_URL;
    private int SELECT_DIRECTORY = 3;
    private String MOVE_IMG_TITLE;

    private ProgressBar progressBar;
    private ImageView main_display_img;
    private VideoView main_display_video;
    private FrameLayout main_display_video_fl;
    private WebView main_display_pdf_webview;
    private RecyclerView mRecyclerView;
    private List<CloudFileModel> mUploads;
    private GalleryAdapter mAdapter;
    private ImageView left_arrow, send, delete, right_arrow;
    private int dataCollectionFlag = 0;

    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        CURRENT_FOLDER = getIntent().getStringExtra("CURRENT_FOLDER");
        CURRENT_GALLERY_POSITION = getIntent().getIntExtra("CURRENT_GALLERY_POSITION", 0);
        System.out.println(CURRENT_GALLERY_POSITION);

        InitialiseLayout();
        InitiateFirebase();
        InitialiseButtons();
        getData();

    }

    private void InitialiseButtons() {
        left_arrow = findViewById(R.id.gallery_left_arrow);
        left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = CURRENT_GALLERY_POSITION - 1;
                if (temp >= 0 && temp <= mUploads.size() - 1) {
                    CURRENT_GALLERY_POSITION--;
                    updateMainDisplay(CURRENT_GALLERY_POSITION);
                }
            }
        });

        send = findViewById(R.id.gallery_send);
        send.setVisibility(View.GONE);
//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (CURRENT_GALLERY_POSITION != null) {
//
//                    //Get title of image which is used for creating the new file
//                    CloudFileModel img = mUploads.get(CURRENT_GALLERY_POSITION);
//                    MOVE_IMG_TITLE = img.mTitle;
//
//                    StorageReference ref = mStorageReference.child(CURRENT_FOLDER).child(img.mTitle);
//
//                    /*ref.putFile(Uri.fromFile(new File(getFilesDir() + "/files/temp", img.mTitle)));
//
//                    if (file.exists()) {
//
//                        System.out.println("Works");
//
//                       file.delete();
//
//                        /*Uri temp = getUri(img.mTitle);
//
//                        Intent shareIntent = new Intent();
//
//                        List<ResolveInfo> resInfoList = com.example.javaproject.CloudImageGallery.GalleryActivity.this.getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
//
//                        for (ResolveInfo resolveInfo : resInfoList) {
//                            String packageName = resolveInfo.activityInfo.packageName;
//                            com.example.javaproject.CloudImageGallery.GalleryActivity.this.grantUriPermission(packageName, temp, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        }
//
//                        shareIntent.setAction(Intent.ACTION_SEND);
//                        shareIntent.putExtra(Intent.EXTRA_STREAM, temp);
//                        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);*/
//                    //shareIntent.setType("*/*");
//                    // shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
//                    //startActivity(Intent.createChooser(shareIntent, "Share :  " + img.mTitle));
//
//
//                    //}
//                }
//            }
//        });

        delete = findViewById(R.id.gallery_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CURRENT_GALLERY_POSITION != null) {
                    CloudFileModel img = mUploads.get(CURRENT_GALLERY_POSITION);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalleryActivity.this);
                    alertDialog.setMessage("Do you wish to delete the following: " + img.mTitle);
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    CloudFileModel img = mUploads.get(CURRENT_GALLERY_POSITION);
                                    //StorageReference storageref = mStorageReference.child(CURRENT_FOLDER)
                                    StorageReference r = mStorage.getReferenceFromUrl(img.mImageURL);
                                    r.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Update backend
                                            mDatabaseReference.child(img.mkey).removeValue();
                                            System.out.println(mUploads.size());
                                            mUploads.remove(CURRENT_GALLERY_POSITION);

                                            if (mUploads.size() == 1) {
                                                finish();
                                            } else {
                                                System.out.println(mUploads.size());
                                                mAdapter.updateAdapterList(CURRENT_GALLERY_POSITION);

                                                //Update Front enc
                                                CURRENT_GALLERY_POSITION--;
                                                updateMainDisplay(CURRENT_GALLERY_POSITION);
                                            }

                                            Toast.makeText(GalleryActivity.this, "Successfully removed item", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(GalleryActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                    alertDialog.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });

                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }
            }
        });

        right_arrow = findViewById(R.id.gallery_right_arrow);
        right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = CURRENT_GALLERY_POSITION + 1;
                if (temp >= 0 && temp <= mUploads.size() - 1) {
                    CURRENT_GALLERY_POSITION++;
                    updateMainDisplay(CURRENT_GALLERY_POSITION);
                }
            }
        });
    }

    private Uri getUri(String title) {

        File imagePath = new File(getFilesDir() + "/files/temp", title);
        File file = new File(imagePath, title);
        Uri contentUri = getUriForFile(com.example.javaproject.CloudGallery.GalleryActivity.this, "com.example.javaproject.fileprovider", file);
        return contentUri;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_DIRECTORY && resultCode == RESULT_OK) {
            String rawURI = data.toUri(Intent.URI_ALLOW_UNSAFE);
            String[] split = rawURI.split("#");
            String uri = split[0];
            System.out.println(uri);
            //downloadFile(uri);
        }
    }

    private void downloadFile(String uri) {
        File file = new File(uri, MOVE_IMG_TITLE);
        StorageReference ref = mStorageReference.child("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER + "/" + MOVE_IMG_TITLE);

        /*ref.putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(GalleryActivity.this, "File created @ " + uri, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GalleryActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void updateMainDisplay(int i) {

        CloudFileModel img = mUploads.get(i);

        String mTitle = img.mTitle;
        String[] split = img.mTitle.split("[.]");

        if (split[split.length - 1].equals("mp3") || split[split.length - 1].equals("mp4") || split[split.length - 1].equals("mkv") || split[split.length - 1].equals("webm")) {
            main_display_pdf_webview.setVisibility(View.GONE);
            main_display_img.setVisibility(View.GONE);
            main_display_video_fl.setVisibility(View.VISIBLE);

            main_display_video.getVideoControlsCore();
            main_display_video.seekTo(100);
            main_display_video.setVideoURI(Uri.parse(img.mImageURL));


            main_display_video.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared() {
                    main_display_video.start();
                }
            });
        } else if ((split[split.length - 1].equals("pdf"))) {
            if (main_display_video != null) {
                main_display_video.stopPlayback();
            }

            progressBar.setVisibility(View.VISIBLE);

            main_display_video_fl.setVisibility(View.GONE);
            main_display_img.setVisibility(View.GONE);
            main_display_pdf_webview.setVisibility(View.VISIBLE);
            main_display_pdf_webview.getSettings().setJavaScriptEnabled(true);
            main_display_pdf_webview.getSettings().setBuiltInZoomControls(true);
            String url = "https://drive.google.com/viewerng/viewer?embedded=true&url=";
            try {
                url += URLEncoder.encode(img.mImageURL, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(url);
            if (main_display_pdf_webview.getProgress() == 100) progressBar.setVisibility(View.GONE);
            main_display_pdf_webview.loadUrl(url);
        } else if (split[split.length - 1].equals("png") || split[split.length - 1].equals("bmp") || split[split.length - 1].equals("webp") || split[split.length - 1].equals("jpg") || split[split.length - 1].equals("gif")) {
            if (main_display_video != null) {
                main_display_video.stopPlayback();
            }

            main_display_pdf_webview.setVisibility(View.GONE);
            main_display_video_fl.setVisibility(View.GONE);
            main_display_img.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(img.mImageURL)
                    .into(main_display_img);
        }

    }

    private void InitiateFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
    }

    private void InitialiseLayout() {

        progressBar = findViewById(R.id.gallery_progressBar);

        main_display_pdf_webview = findViewById(R.id.gallery_main_display_pdf_webview);

        main_display_video_fl = findViewById(R.id.gallery_main_display_video_fl);

        main_display_video = findViewById(R.id.gallery_main_display_video);

        main_display_img = findViewById(R.id.gallery_main_display_img);

        mRecyclerView = findViewById(R.id.recycler_view_gallery);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mUploads = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new GalleryAdapter(GalleryActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void switchImg(int position) {
                updateMainDisplay(position);
                CURRENT_GALLERY_POSITION = position;
            }
        });
    }

    private void getData() {
        dataCollectionFlag = 1;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users/" + mFirebaseAuth.getUid() + "/" + CURRENT_FOLDER);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataCollectionFlag == 1) {
                    mUploads.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        CloudFileModel upload = postSnapshot.getValue(CloudFileModel.class);

                        //Key reference so we can manipulate values later
                        upload.mkey = (postSnapshot.getKey());

                        mUploads.add(upload);

                    }
                    if (CURRENT_GALLERY_POSITION != null) {
                        updateMainDisplay(CURRENT_GALLERY_POSITION);
                    }
                    mAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error
            }
        });
        dataCollectionFlag = 0;
    }

}
