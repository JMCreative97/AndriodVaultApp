package com.example.javaproject.Files;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.R;

import java.util.List;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.ImageViewHolder> {
    private Context mContext;
    private List<String> mTitles;
    private List<String> mFileSize;
    private List<String> mUploadingStatus;
    private String mUPLOAD_TO;


    public UploadAdapter(Context context, List<String> titles, List<String> fileSize, List<String> uploadingStatus, String UPLOAD_TO) {
        mContext = context;
        mTitles = titles;
        mFileSize = fileSize;
        mUploadingStatus = uploadingStatus;
        mUPLOAD_TO = UPLOAD_TO;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.file_upload_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String title = mTitles.get(position);
        String uploadStatus = mUploadingStatus.get(position);
        String uploadSize = mFileSize.get(position);

        String[] split = title.split("[.]");

        if (split[split.length - 1].equals("mp3") || split[split.length - 1].equals("mp4") || split[split.length - 1].equals("mkv") || split[split.length - 1].equals("webm")) {
            holder.mTitle.setText(title);
            holder.mSize.setText(uploadSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.video_icon);
            holder.mImage.setImageDrawable(myDrawable);

            //            holder.mPDFView.setVisibility(View.GONE);
//            holder.mImageView.setVisibility(View.GONE);
//            holder.mFrameLayout.setVisibility(View.VISIBLE);
//            holder.mVideoView.seekTo(100);
//            holder.mVideoView.setVideoPath(img.mPath);

        } else if ((split[split.length - 1].equals("pdf"))) {
            holder.mTitle.setText(title);
            holder.mSize.setText(uploadSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.pdf_icon);
            holder.mImage.setImageDrawable(myDrawable);
//            holder.mFrameLayout.setVisibility(View.GONE);
//            holder.mImageView.setVisibility(View.GONE);
//            holder.mPDFView.setVisibility(View.VISIBLE);
//            holder.mPDFView.fromFile(new File(img.mPath));
        }

        //PNG, BMP, WEBP, JPEG and GIF

        else if (split[split.length - 1].equals("png") || split[split.length - 1].equals("bmp") || split[split.length - 1].equals("webp") || split[split.length - 1].equals("jpg") || split[split.length - 1].equals("gif")) {
            holder.mTitle.setText(title);
            holder.mSize.setText(uploadSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.img_icon);
            holder.mImage.setImageDrawable(myDrawable);
            //            holder.mPDFView.setVisibility(View.GONE);
//            holder.mFrameLayout.setVisibility(View.GONE);
//            holder.mImageView.setVisibility(View.VISIBLE);
//            Bitmap bitmap = BitmapFactory.decodeFile(img.mPath);
//            holder.mImageView.setImageBitmap(bitmap);
        } else {
            holder.mTitle.setText(title);
            Drawable myDrawable = mContext.getDrawable(R.drawable.basic_icon);
            holder.mImage.setImageDrawable(myDrawable);
        }


        if (mUPLOAD_TO.equals("cloud")) {
            if (uploadStatus.equals("Uploading..")) {
                holder.status.setText(uploadStatus);
                holder.iStatus.setImageResource(R.drawable.ic_cloud_queue_black);
            } else {
                holder.status.setText(uploadStatus);
                holder.iStatus.setImageResource(R.drawable.ic_cloud_done);
            }
        } else {
            if (uploadStatus.equals("Uploading..")) {
                holder.status.setText(uploadStatus);
                holder.iStatus.setImageResource(R.drawable.ic_check_box_outline);
            } else {
                holder.status.setText(uploadStatus);
                holder.iStatus.setImageResource(R.drawable.ic_check_box_filled);
            }
        }


    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mSize, status;
        public ImageView mImage, iStatus;


        public ImageViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.cloud_upload_item_title);
            status = itemView.findViewById(R.id.cloud_upload_item_status);
            mSize = itemView.findViewById(R.id.cloud_upload_item_size);
            mImage = itemView.findViewById(R.id.upload_img);
            iStatus = itemView.findViewById(R.id.cloud_upload_completed);

        }
    }


    @Override
    public int getItemCount() {
        return mTitles.size();
    }


}