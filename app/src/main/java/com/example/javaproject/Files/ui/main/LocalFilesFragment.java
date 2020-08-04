package com.example.javaproject.Files.ui.main;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Files.FolderExplorerActivity;
import com.example.javaproject.Files.LocalFileAdapter;
import com.example.javaproject.LocalGallery.GalleryActivity;
import com.example.javaproject.Misc.EncryptionManger;
import com.example.javaproject.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalFilesFragment extends Fragment {
    private Context mContext;
    private View mRoot;
    private static final int PICK_IMAGE_REQUEST = 1;
    private RecyclerView mRecyclerView;
    private List<LocalFileModel> mItemModels;
    private ImageView mBackupIcon;
    private LocalFileAdapter mAdapter;
    private EncryptionManger mEncryptionManager;
    private int requestCode;
    private int resultCode;
    private List<Uri> tempUploads;
    private String CURRENT_FOLDER_PATH;
    private String CURRENT_FOLDER;
    Toolbar toolbar;

    @Override
    public void onResume() {
        super.onResume();
        LoadData(new File(getContext().getFilesDir(), CURRENT_FOLDER_PATH));
    }


    public LocalFilesFragment(String currentFolderPath, String currentFolder) {
        CURRENT_FOLDER_PATH = currentFolderPath;
        CURRENT_FOLDER = currentFolder;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRoot = inflater.inflate(R.layout.fragment_local_files, container, false);
        mContext = getContext();

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


        mEncryptionManager = new EncryptionManger(getContext());
        InitialiseLayout();
        LoadData(new File(getContext().getFilesDir(), CURRENT_FOLDER_PATH));

        return mRoot;
    }

    private void deleteFolder() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setMessage("Are you sure you want to delete: " + CURRENT_FOLDER);
        alertDialog.setCancelable(true);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                File dir = new File(mContext.getFilesDir() + CURRENT_FOLDER_PATH);
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    for (File f : files) {
                        f.delete();
                    }
                    File password = new File(mContext.getFilesDir() + "/credentials", CURRENT_FOLDER);
                    if (password.exists()) {
                        password.delete();
                    }

                    dir.delete();
                    mEncryptionManager.deleteCredentials(CURRENT_FOLDER);


                    Intent intent = new Intent(mContext, FolderExplorerActivity.class);
                    startActivity(intent);
                }
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.local_folder_menu_main, toolbar.getMenu());
        //return;
    }

    private void InitialiseLayout() {

        mRecyclerView = mRoot.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        //Create a grid layout of width 4
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mItemModels = new ArrayList<>();

        //Use custom adapter for performance so that we only load the include the number of files we need
        mAdapter = new LocalFileAdapter(mContext, mItemModels, 0);

        mAdapter.setOnItemClickListener(new LocalFileAdapter.OnItemClickListener() {
            @Override
            public void selectImg(int position) {
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putExtra("CURRENT_FOLDER_PATH", CURRENT_FOLDER_PATH);
                intent.putExtra("CURRENT_GALLERY_POSITION", position);
                startActivity(intent);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
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
        itemModel.mSize = file.length() / 1024 + " KB";

        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            //Remove T character
            String[] split = fileTime.toString().split("T");
            String datetime = split[0] + "\n" + split[1];
            //Remove Z character
            itemModel.mDate = datetime.substring(0, datetime.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mItemModels.add(itemModel);
        mAdapter.notifyDataSetChanged();

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);


    }


}

