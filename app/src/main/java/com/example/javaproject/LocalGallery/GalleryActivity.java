package com.example.javaproject.LocalGallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.Misc.SettingsActivity;
import com.example.javaproject.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private Integer CURRENT_GALLERY_POSITION;
    private Integer CURRENT_GALLERY_SIZE;
    private String CURRENT_FOLDER_PATH;
    private String CURRENT_DATABASE_URL;

    private ImageView main_display_img;
    private VideoView main_display_video;
    private FrameLayout main_display_video_fl;
    private PDFView main_display_pdf;
    private RelativeLayout main_display_pdf_rl;
    private RecyclerView mRecyclerView;
    private GalleryAdapter mAdapter;
    private List<GalleryModel> mImgs;
    private ImageView left_arrow, send, delete, right_arrow;

    //Logout the user if they exit the app
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth != null){
            firebaseAuth.signOut();
        }
        Intent intent = new Intent(GalleryActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        CURRENT_FOLDER_PATH = getIntent().getStringExtra("CURRENT_FOLDER_PATH");
        CURRENT_GALLERY_POSITION = getIntent().getIntExtra("CURRENT_GALLERY_POSITION", 0);

        InitialiseButtons();
        InitialiseLayout();
        LoadData(new File(getFilesDir(), CURRENT_FOLDER_PATH));

        if (CURRENT_GALLERY_POSITION != null) {
            updateMainDisplay(CURRENT_GALLERY_POSITION);
        }
    }


    private void InitialiseButtons() {
        left_arrow = findViewById(R.id.gallery_left_arrow);
        left_arrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int temp = CURRENT_GALLERY_POSITION - 1;
                if (temp >= 0 && temp <= mImgs.size() - 1) {
                    CURRENT_GALLERY_POSITION--;
                    updateMainDisplay(CURRENT_GALLERY_POSITION);
                }
            }
        });


        send = findViewById(R.id.gallery_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CURRENT_GALLERY_POSITION != null) {
                    //Intent shareImg = new Intent(Intent.ACTION_SEND);
                    GalleryModel img = mImgs.get(CURRENT_GALLERY_POSITION);
                    File file = new File(img.mPath);

                    if (file.exists()) {

                        Uri contentUri = FileProvider.getUriForFile(GalleryActivity.this, "com.example.javaproject.fileprovider", file);

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        //shareIntent.setData(contentUri);
                        //shareIntent.putExtra(Intent.EXTRA_STREAM, temp);
                        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        shareIntent.setType("images/*");
                        // shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
                        //startActivity(Intent.createChooser(shareIntent, "Share :  " + img.mTitle));
                        Intent chooser = Intent.createChooser(shareIntent, "Share file");

                        List<ResolveInfo> resInfoList = GalleryActivity.this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            GalleryActivity.this.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        startActivity(chooser);

                        //TODO Whys permission not working?
                    }

                }
            }
        });

        delete = findViewById(R.id.gallery_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CURRENT_GALLERY_POSITION != null) {
                    GalleryModel img = mImgs.get(CURRENT_GALLERY_POSITION);
                    File file = new File(img.mPath);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalleryActivity.this);
                    alertDialog.setMessage("Do you wish to delete the following: " + img.mTitle);
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (file.exists()) {

                                        //TODO Better way to do this?
                                        if (mImgs.size() == 1) {
                                            file.delete();
                                            finish();
//                                           Intent intent = new Intent(GalleryActivity.this, LocalImgActivity.class);
//                                           startActivity(intent);
                                        } else {

                                            //Update backend
                                            file.delete();
                                            mImgs.remove(CURRENT_GALLERY_POSITION);
                                            mAdapter.updateAdapterList(CURRENT_GALLERY_POSITION);

                                            //Update Front enc
                                            CURRENT_GALLERY_POSITION--;
                                            updateMainDisplay(CURRENT_GALLERY_POSITION);
                                        }
                                    }
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
            }
        });


        right_arrow = findViewById(R.id.gallery_right_arrow);
        right_arrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int temp = CURRENT_GALLERY_POSITION + 1;
                if (temp >= 0 && temp <= mImgs.size() - 1) {
                    CURRENT_GALLERY_POSITION++;
                    updateMainDisplay(CURRENT_GALLERY_POSITION);
                }
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 9999:

                GalleryModel img = mImgs.get(CURRENT_GALLERY_POSITION);
                Uri uri = data.getData();
                File file = new File(uri.getPath());

                try {

                    FileInputStream inputStream = new FileInputStream(img.mPath);
                    Files.copy(inputStream, file.toPath());
                    inputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private void updateMainDisplay(int i) {

        GalleryModel img = mImgs.get(i);

        String[] split = img.mTitle.split("[.]");

        if (split[split.length - 1].equals("mp3") || split[split.length - 1].equals("mp4") || split[split.length - 1].equals("mkv") || split[split.length - 1].equals("webm")) {
            main_display_pdf_rl.setVisibility(View.GONE);
            main_display_img.setVisibility(View.GONE);
            main_display_video_fl.setVisibility(View.VISIBLE);


            main_display_video.getVideoControlsCore();

            main_display_video.seekTo(100);
            main_display_video.setVideoPath(img.mPath);


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

            main_display_video_fl.setVisibility(View.GONE);
            main_display_img.setVisibility(View.GONE);
            main_display_pdf_rl.setVisibility(View.VISIBLE);

            main_display_pdf.fromFile(new File(img.mPath))
                    .defaultPage(1)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .defaultPage(1)
                    .showPageWithAnimation(true)
                    .load();


        } else if (split[split.length - 1].equals("png") || split[split.length - 1].equals("bmp") || split[split.length - 1].equals("webp") || split[split.length - 1].equals("jpg") || split[split.length - 1].equals("gif")) {
            if (main_display_video != null) {
                main_display_video.stopPlayback();
            }

            main_display_pdf_rl.setVisibility(View.GONE);
            main_display_video_fl.setVisibility(View.GONE);
            main_display_img.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(mImgs.get(i).mPath);
            main_display_img.setImageBitmap(bitmap);
        }
    }


    private void LoadData(File dir) {
        mImgs.clear();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                loadImage(file);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadImage(File file) {
        GalleryModel img = new GalleryModel();
        img.mPath = file.getPath();
        img.mTitle = file.getName();
        img.mURI = Uri.fromFile(file);
        mImgs.add(img);
        mAdapter.notifyDataSetChanged();
    }

    private void InitialiseLayout() {

        main_display_pdf_rl = findViewById(R.id.gallery_main_display_pdf_rl);

        main_display_pdf = findViewById(R.id.gallery_main_display_pdf);

        main_display_video_fl = findViewById(R.id.gallery_main_display_video_fl);

        main_display_video = findViewById(R.id.gallery_main_display_video);

        main_display_img = findViewById(R.id.gallery_main_display_img);

        mRecyclerView = findViewById(R.id.recycler_view_gallery);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mImgs = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new GalleryAdapter(GalleryActivity.this, mImgs, 0);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            public void switchImg(int position) {
                updateMainDisplay(position);
                CURRENT_GALLERY_POSITION = position;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main_display_video.release();
    }
}
