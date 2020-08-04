package com.example.javaproject.FirstLaunchActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.javaproject.R;

public class LaunchActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private Button skip, next, start;
    private int[] layouts;
    private TextView[] dots;
    private int current_dot = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        viewPager = findViewById(R.id.viewPager);
        linearLayout = (LinearLayout) findViewById(R.id.layoutDots);
        skip = findViewById(R.id.intro_btn_skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(4);
                addDots(4);
            }
        });
        next = findViewById(R.id.intro_btn_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(current_dot == 4)) {
                    viewPager.setCurrentItem(current_dot += 1);
                }
            }
        });

        start = findViewById(R.id.intro_btn_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchActivity.this, SetCryptography.class);
                startActivity(intent);
            }
        });

        layouts = new int[]{R.layout.intro_slide_format};

        SliderAdapter sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);
        addDots(0);
        viewPager.addOnPageChangeListener(onPageChangeListener);


    }

    public void addDots(int position) {

        dots = new TextView[5];

        linearLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.myGrey));

            linearLayout.addView(dots[i]);
        }

        dots[position].setTextColor(getResources().getColor(R.color.myBlue));

    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 4) {
                linearLayout.removeAllViews();
                next.setVisibility(View.GONE);
                skip.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                current_dot = position;
            } else {
                addDots(position);
                next.setVisibility(View.VISIBLE);
                skip.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                current_dot = position;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
