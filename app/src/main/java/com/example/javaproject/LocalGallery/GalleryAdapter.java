package com.example.javaproject.LocalGallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.example.javaproject.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {
    private Context mContext;
    private List<GalleryModel> mImg;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void switchImg(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public GalleryAdapter(Context context, List<GalleryModel> imgs, Integer display) {
        mContext = context;
        mImg = imgs;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        GalleryModel img = mImg.get(position);
        String[] split = img.mTitle.split("[.]");

        if (split[split.length - 1].equals("mp3") || split[split.length - 1].equals("mp4") || split[split.length - 1].equals("mkv") || split[split.length - 1].equals("webm")) {
            holder.mFrameLayout.setVisibility(View.VISIBLE);
            holder.mImageView.setVisibility(View.GONE);
            holder.mVideoView.seekTo(100);
            holder.mVideoView.setVideoPath(img.mPath);

        } else if ((split[split.length - 1].equals("pdf"))) {
            holder.mFrameLayout.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.VISIBLE);
            Drawable myDrawable = mContext.getDrawable(R.drawable.pdf_icon);
            holder.mImageView.setImageDrawable(myDrawable);
        }

        //PNG, BMP, WEBP, JPEG and GIF

        else if (split[split.length - 1].equals("png") || split[split.length - 1].equals("bmp") || split[split.length - 1].equals("webp") || split[split.length - 1].equals("jpg") || split[split.length - 1].equals("gif")) {
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.mFrameLayout.setVisibility(View.GONE);
            Bitmap bitmap = BitmapFactory.decodeFile(img.mPath);
            holder.mImageView.setImageBitmap(bitmap);

        }

//       else if(split[split.length-1].equals("png") || split[split.length-1].equals("bmp") || split[split.length-1].equals("webp") || split[split.length-1].equals("jpg") ||split[split.length-1].equals("gif")){
//        if (split[split.length-1].equals("mp3") || split[split.length-1].equals("mp4") || split[split.length-1].equals("mkv") || split[split.length-1].equals("webm")) {
//            holder.mFrameLayout.setVisibility(View.VISIBLE);
//            holder.mImageView.setVisibility(View.GONE);
//            holder.mVideoView.seekTo(100);
//            holder.mVideoView.setVideoPath(img.mPath);
//        }else {
//            holder.mImageView.setVisibility(View.VISIBLE);
//            holder.mFrameLayout.setVisibility(View.GONE);
//            Bitmap bitmap = BitmapFactory.decodeFile(img.mPath);
//            holder.mImageView.setImageBitmap(bitmap);
//        }
    }

    @Override
    public int getItemCount() {
        return mImg.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private VideoView mVideoView;
        private FrameLayout mFrameLayout;
        private TextView mTitle, mDate;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.gallery_item_img);
            mVideoView = itemView.findViewById(R.id.gallery_item_vid);
            mFrameLayout = itemView.findViewById(R.id.gallery_item_vid_fl);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.switchImg(position);
                        }
                    }
                }
            });
        }
    }

    public void updateAdapterList(int position) {
        mImg.remove(position);
        notifyDataSetChanged();
    }
}
