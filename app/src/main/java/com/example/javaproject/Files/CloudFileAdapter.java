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

public class CloudFileAdapter extends RecyclerView.Adapter<CloudFileAdapter.ImageViewHolder> {
    private Context mContext;
    private List<CloudFileModel> mUploads;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onGetPos(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    public CloudFileAdapter(Context context, List<CloudFileModel> uploads) {
        mContext = context;
        mUploads = uploads;

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        View v = LayoutInflater.from(mContext).inflate(R.layout.files_row_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        CloudFileModel img = mUploads.get(position);
        String[] split = img.mTitle.split("[.]");
        if (split[split.length - 1].equals("mp3") || split[split.length - 1].equals("mp4") || split[split.length - 1].equals("mkv") || split[split.length - 1].equals("webm")) {
            holder.mTitle.setText(img.mTitle);
            holder.mSize.setText(img.mSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.video_icon);
            holder.mImage.setImageDrawable(myDrawable);

        } else if ((split[split.length - 1].equals("pdf"))) {
            holder.mTitle.setText(img.mTitle);
            holder.mSize.setText(img.mSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.pdf_icon);
            holder.mImage.setImageDrawable(myDrawable);
        } else if (split[split.length - 1].equals("png") || split[split.length - 1].equals("bmp") || split[split.length - 1].equals("webp") || split[split.length - 1].equals("jpg") || split[split.length - 1].equals("gif")) {
            holder.mTitle.setText(img.mTitle);
            holder.mSize.setText(img.mSize + " KB");
            Drawable myDrawable = mContext.getDrawable(R.drawable.img_icon);
            holder.mImage.setImageDrawable(myDrawable);
        } else {
            holder.mTitle.setText(img.mTitle);
            Drawable myDrawable = mContext.getDrawable(R.drawable.basic_icon);
            holder.mImage.setImageDrawable(myDrawable);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        //public ImageView imageView;
        private TextView mTitle, mSize;
        private ImageView mImage;

        public ImageViewHolder(View itemView) {
            super(itemView);

            mImage = itemView.findViewById(R.id.files_item_img);
            mTitle = itemView.findViewById(R.id.files_item_title);
            mSize = itemView.findViewById(R.id.files_item_size);
            //imageView = itemView.findViewById(R.id.files_item_img);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onGetPos(position);
                        }
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mUploads.size();
    }
}