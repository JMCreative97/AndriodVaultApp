package com.example.javaproject.FirstLaunchActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javaproject.Authentication.RegisterActivity;
import com.example.javaproject.Misc.SharedPreferencesManager;
import com.example.javaproject.R;

public class SetCryptography extends AppCompatActivity {

    private ImageView AES_down, TripleDes_down, BlowFish_down, SHA_down, PBKDF2_down, AES_up, TripleDes_up, BlowFish_up, SHA_up, PBKDF2_up;
    private TextView AES_description, TripleDes_description, BlowFish_description, SHA_description, PBKDF2_description;
    private RadioButton AES_radio_button, TripleDes_radio_button, BlowFish_radio_button, SHA_radio_button, PBKDF2_radio_button;
    private Button Continue;
    private String CURRENT_ENC;
    private String CURRENT_HASH;

    private SharedPreferencesManager mSharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_cryptography);

        mSharedPreferencesManager = new SharedPreferencesManager(this);

        AES_up = findViewById(R.id.AES_drop_up);
        AES_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideDesription(AES_description, AES_up, AES_down);
            }
        });
        TripleDes_up = findViewById(R.id.DES_drop_up);
        TripleDes_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideDesription(TripleDes_description, TripleDes_up, TripleDes_down);
            }
        });
        BlowFish_up = findViewById(R.id.BLOWFISH_drop_up);
        BlowFish_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideDesription(BlowFish_description, BlowFish_up, BlowFish_down);
            }
        });
        SHA_up = findViewById(R.id.SHA_drop_up);
        SHA_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideDesription(SHA_description, SHA_up, SHA_down);
            }
        });
        PBKDF2_up = findViewById(R.id.PBKDF2_drop_up);
        PBKDF2_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideDesription(PBKDF2_description, PBKDF2_up, PBKDF2_down);
            }
        });

        AES_down = findViewById(R.id.AES_drop_down);
        AES_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDesription(AES_description, AES_up, AES_down);
            }
        });
        TripleDes_down = findViewById(R.id.DES_drop_down);
        TripleDes_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDesription(TripleDes_description, TripleDes_up, TripleDes_down);
            }
        });
        BlowFish_down = findViewById(R.id.BLOWFISH_drop_down);
        BlowFish_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDesription(BlowFish_description, BlowFish_up, BlowFish_down);
            }
        });
        SHA_down = findViewById(R.id.SHA_drop_down);
        SHA_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDesription(SHA_description, SHA_up, SHA_down);
            }
        });
        PBKDF2_down = findViewById(R.id.PBKDF2_drop_down);
        PBKDF2_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDesription(PBKDF2_description, PBKDF2_up, PBKDF2_down);
            }
        });


        AES_description = findViewById(R.id.AES_description);
        TripleDes_description = findViewById(R.id.TripleDES_description);
        BlowFish_description = findViewById(R.id.BlowFish_description);
        SHA_description = findViewById(R.id.SHA_description);
        PBKDF2_description = findViewById(R.id.PBKDF2_description);

        AES_radio_button = findViewById(R.id.radio_AES);
        AES_radio_button.setChecked(true);
        AES_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRadioButton(AES_radio_button, "AES", 1);
            }
        });
        TripleDes_radio_button = findViewById(R.id.radio_TripleDES);
        TripleDes_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRadioButton(TripleDes_radio_button, "DESede", 1);
            }
        });
        BlowFish_radio_button = findViewById(R.id.radio_BlowFish);
        BlowFish_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRadioButton(BlowFish_radio_button, "Blowfish", 1);
            }
        });
        SHA_radio_button = findViewById(R.id.radio_SHA);
        SHA_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRadioButton(SHA_radio_button, "SHA", 2);
            }
        });
        PBKDF2_radio_button = findViewById(R.id.radio_PBKDF2);
        PBKDF2_radio_button.setChecked(true);
        PBKDF2_radio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRadioButton(PBKDF2_radio_button, "PBKDF2", 2);
            }
        });

        Continue = findViewById(R.id.Continue);
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSharedPreferencesManager.setStringPref("ENCRYPTION_ALGORITHM", CURRENT_ENC == null ? "AES" : CURRENT_ENC);
                mSharedPreferencesManager.setStringPref("HASH_ALGORITHM", CURRENT_HASH == null ? "PBKDF2" : CURRENT_HASH);
                mSharedPreferencesManager.setBoolPref("CompletedLaunchActivity", true);
                Intent intent = new Intent(SetCryptography.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void selectRadioButton(RadioButton button, String id, int mode) {

        RadioButton[] encButtons = {
                AES_radio_button,
                TripleDes_radio_button,
                BlowFish_radio_button,
        };

        if (mode == 1) {

            for (RadioButton radioButton : encButtons) {
                radioButton.setChecked(false);
            }

            CURRENT_ENC = id;
        }

        RadioButton[] hashButtons = {
                SHA_radio_button,
                PBKDF2_radio_button
        };

        if (mode == 2) {
            for (RadioButton b : hashButtons) {
                b.setChecked(false);
            }

            CURRENT_HASH = id;
        }

        button.setChecked(true);
    }

    //Descriptions
    private void OpenDesription(TextView description, ImageView up, ImageView down) {
        down.setVisibility(View.GONE);
        up.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
    }

    private void HideDesription(TextView description, ImageView up, ImageView down) {
        down.setVisibility(View.VISIBLE);
        up.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
    }

}
