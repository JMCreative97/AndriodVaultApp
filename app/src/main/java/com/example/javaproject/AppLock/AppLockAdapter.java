package com.example.javaproject.AppLock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;

import java.util.List;

public class AppLockAdapter extends RecyclerView.Adapter<AppLockAdapter.ImageViewHolder> {
    private Context mContext;
    private List<AppModel> mApps;
    private OnItemClickListener mListener;
    private SharedPreferencesManager sharedPreferencesManager;
    private AppService appService;

    public interface OnItemClickListener {
        void switchImg(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public AppLockAdapter(Context context, List<AppModel> apps) {
        mContext = context;
        mApps = apps;
        sharedPreferencesManager = new SharedPreferencesManager(context);
        appService = new AppService();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.app_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        AppModel app = mApps.get(position);
        holder.icon.setImageDrawable(app.icon);
        holder.name.setText(app.name);


        sharedPreferencesManager.setBoolPref(app.packName, false);

        holder.appSwitch.setChecked(sharedPreferencesManager.getBoolPref(app.packName) ? true : false);


        holder.appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean lock = false;
                //If app pref is false, then lock, otherwise unlock
                lock = sharedPreferencesManager.getBoolPref(app.packName) ? false : true;

                sharedPreferencesManager.setBoolPref(app.packName, lock);


                System.out.println(app.packName + " STATUS " + sharedPreferencesManager.getBoolPref(app.packName));

            }
        });
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView name;
        private Switch appSwitch;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            appSwitch = itemView.findViewById(R.id.app_switch);

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
}
