package com.example.javaproject.NoteActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ImageViewHolder> {
    private Context mContext;
    private List<NoteModel> mNotes;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void openNote(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public NoteAdapter(Context context, List<NoteModel> notes) {
        mContext = context;
        mNotes = notes;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        NoteModel note = mNotes.get(position);
        holder.mTitle.setText(note.mTitle);
        holder.mDate.setText(note.mDate);
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle, mDate;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.note_title);
            mDate = itemView.findViewById(R.id.note_date);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.openNote(position);
                        }
                    }
                }

            });
        }
    }
}
