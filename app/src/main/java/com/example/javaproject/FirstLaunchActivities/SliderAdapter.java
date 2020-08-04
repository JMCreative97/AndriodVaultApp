package com.example.javaproject.FirstLaunchActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.javaproject.R;

public class SliderAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflator;
    private Button btn_skip, btn_next;

    public Integer[] slide_imgs = new Integer[]{
            R.drawable.app_logo,
            R.drawable.trusted_icon,
            R.drawable.cloud_backup_icon,
            R.drawable.pin_pad_icon,
            R.drawable.app_logo,
    };

    public String[] slide_headings = new String[]{
            "Welcome to SimpleSafe",
            "Advanced Security",
            "Cloud backup",
            "Pin Protected",
            "Get Started"};
    public String[] slide_descriptions = new String[]{
            "A vault app to protect your data",
            "We only use the most renowned and robust algorithms to protect your data",
            "We offer free cloud backup for all users, this is readily available when you register.",
            "Registering is as easy as giving your email and a pin, thats all it takes. Further security measures can be implemented within the app.",
            "Tap finnish to get started!"};


    public SliderAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        //object.g
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mLayoutInflator = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflator.inflate(R.layout.intro_slide_format, container, false);

        ImageView slide_img = view.findViewById(R.id.intro_slide_img);
        TextView slide_header = view.findViewById(R.id.slider_header);
        TextView slide_description = view.findViewById(R.id.slider_description);


        slide_img.setImageDrawable(mContext.getResources().getDrawable(slide_imgs[position]));
        slide_header.setText(slide_headings[position]);
        slide_description.setText(slide_descriptions[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((LinearLayout) object);
    }
}
