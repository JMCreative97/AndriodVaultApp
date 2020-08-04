package com.example.javaproject.Files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.R;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ImageViewHolder> {

    private Context mContext;
    private List<FolderModel> mFolders;
    private OnItemClickListener onItemClickListener;
    private List<ImageViewHolder> mImageViewHolders;
    private List<Integer> mSelectedItems;
    private boolean selectMode;

    public interface OnItemClickListener {
        void onGetPos(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    public FolderAdapter(Context context, List<FolderModel> folders) {
        mContext = context;
        mFolders = folders;
        mImageViewHolders = new ArrayList<>();
        mSelectedItems = new ArrayList<>();
        selectMode = false;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.img_folder_item, parent, false);
        return new FolderAdapter.ImageViewHolder(v);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private TextView folder_title;
        private ImageView folder_img, folder_select_empty, folder_select_filled;


        public ImageViewHolder(View itemView) {
            super(itemView);

            folder_title = itemView.findViewById(R.id.folder_title);
            folder_img = itemView.findViewById(R.id.folder_img);
            folder_select_empty = itemView.findViewById(R.id.folder_select_empty);
            folder_select_filled = itemView.findViewById(R.id.folder_select_filled);

            folder_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();

                    if (!selectMode) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onGetPos(position);
                        } else {
                            selectModeOff();
                        }
                    }
                }
            });

            folder_img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (selectMode) selectMode = false;
                    else {
                        selectMode = true;
                        Toast.makeText(mContext, "Select mode enabled", Toast.LENGTH_SHORT).show();
                        for (ImageViewHolder holder : mImageViewHolders) {
                            holder.folder_select_empty.setVisibility(View.VISIBLE);
                        }
                    }
                    if (!selectMode) {
                        selectModeOff();
                        Toast.makeText(mContext, "Select mode disabled", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }

    public void selectModeOff() {
        for (ImageViewHolder holder : mImageViewHolders) {
            holder.folder_select_empty.setVisibility(View.GONE);
            holder.folder_select_filled.setVisibility(View.GONE);
        }
    }

    public void deselectAll() {
        for (ImageViewHolder holder : mImageViewHolders) {
            holder.folder_select_empty.setVisibility(View.VISIBLE);
            holder.folder_select_filled.setVisibility(View.GONE);
        }
    }

    public List<Integer> selectedItems() {
        return mSelectedItems;
    }

    public boolean selectModeStatus() {
        return selectMode;
    }


    @Override
    public void onBindViewHolder(@NonNull final FolderAdapter.ImageViewHolder holder, int position) {
        final FolderModel folder = mFolders.get(position);
        mImageViewHolders.add(holder);
        holder.folder_title.setText(folder.mTitle);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectMode) {

                    if (folder.mSelected) {
                        folder.mSelected = false;
                        mSelectedItems.remove(new Integer(position)); //Removes first occurance of int in list
                    } else {
                        folder.mSelected = true;
                        mSelectedItems.add(position);
                    }

                    holder.folder_select_empty.setVisibility(folder.mSelected == true ? View.GONE : View.VISIBLE);
                    holder.folder_select_filled.setVisibility(folder.mSelected == true ? View.VISIBLE : View.GONE);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mFolders.size() == 0) deselectAll();
        return mFolders.size();
    }
}
