package com.example.javaproject.Misc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.javaproject.Authentication.LoginActivity;
import com.example.javaproject.FirstLaunchActivities.LaunchActivity;
import com.example.javaproject.NoteActivities.NotesActivity;
import com.example.javaproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {


    private DataStore dataStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferencesManager sharedPreferencesManager;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);


            sharedPreferencesManager = new SharedPreferencesManager(this.getActivity());

            //Authentication
            SwitchPreferenceCompat Authentication_sign_in_lock = findPreference("Authentication_sign_in_lock");
            if (Authentication_sign_in_lock != null) {
                Authentication_sign_in_lock.setChecked(sharedPreferencesManager.getBoolPref("AUTHENTICATION_SIGN_IN_LOCK") ? true : false);
            }
            Authentication_sign_in_lock.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        Authentication_sign_in_lock.setChecked((boolean) newValue);
                        sharedPreferencesManager.setBoolPref("AUTHENTICATION_SIGN_IN_LOCK", (boolean) newValue);
                        System.out.println(sharedPreferencesManager.getBoolPref("AUTHENTICATION_SIGN_IN_LOCK"));
                    }
                    return true;
                }
            });

            EditTextPreference Authentication_sign_in_lock_attempts = findPreference("Authentication_sign_in_lock_attempts");
            if (Authentication_sign_in_lock_attempts != null) {
                Authentication_sign_in_lock_attempts.setSummary((sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_ATTEMPTS") == 0 ? 3 + " attemps" : sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_ATTEMPTS") + " seconds"));
            }
            Authentication_sign_in_lock_attempts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        int newVal = Integer.parseInt((String) newValue);
                        try {
                            if (0 < newVal && (int) newVal <= 60) {
                                sharedPreferencesManager.setIntPref("AUTHENTICATION_SIGN_IN_LOCK_ATTEMPTS", newVal);
                                Authentication_sign_in_lock_attempts.setSummary(newVal + " attempts");
                            } else {
                                Toast.makeText(getContext(), "Please enter a number between 1 - 60", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            System.out.println("ERR : " + e);
                        }
                    }
                    return true;
                }
            });


            EditTextPreference Authentication_sign_in_lock_timer = findPreference("Authentication_sign_in_lock_timer");
            if (Authentication_sign_in_lock_timer != null) {
                Authentication_sign_in_lock_timer.setSummary((sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_TIMER") == 0 ? 10 + " seconds" : sharedPreferencesManager.getIntPref("AUTHENTICATION_SIGN_IN_LOCK_TIMER") + " seconds"));
            }
            Authentication_sign_in_lock_timer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Authentication_sign_in_lock_timer.setText("");
                    if (newValue != null) {
                        int newVal = Integer.parseInt((String) newValue);
                        try {
                            if (0 < newVal && (int) newVal <= 60) {
                                sharedPreferencesManager.setIntPref("AUTHENTICATION_SIGN_IN_LOCK_TIMER", newVal);
                                Authentication_sign_in_lock_timer.setSummary(newVal + " seconds");
                            } else {
                                Toast.makeText(getContext(), "Please enter a number between 1 - 60", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            System.out.println("ERR : " + e);
                        }
                    }
                    return true;
                }
            });

            SwitchPreferenceCompat Authentication_intruder_snap = findPreference("Authentication_intruder_snap");
            if (Authentication_intruder_snap != null) {
                Authentication_intruder_snap.setChecked(sharedPreferencesManager.getBoolPref("AUTHENTICATION_INTRUDER_SNAP") ? true : false);
            }
            Authentication_intruder_snap.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        Authentication_intruder_snap.setChecked((boolean) newValue);
                        sharedPreferencesManager.setBoolPref("AUTHENTICATION_INTRUDER_SNAP", (boolean) newValue);
                        System.out.println(sharedPreferencesManager.getBoolPref("AUTHENTICATION_INTRUDER_SNAP"));

                    }
                    return true;
                }
            });

            //TODO
            //  FIREBASE LOGIN PASSWORD DIALOG

            //User
            EditTextPreference User_change_email = findPreference("User_change_email");
            if (User_change_email != null) {
                User_change_email.setSummary((sharedPreferencesManager.getStringPref("Email")));
            }

            User_change_email.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String email = (String) newValue;
                    if (!TextUtils.isEmpty(email)) passwordDialog(1, email, null);
                    preference.setDefaultValue("");
                    return true;
                }
            });


            EditTextPreference User_change_password = findPreference("User_change_password");
            if (User_change_password != null) {

            }
            User_change_password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String password = (String) newValue;
                    if (!TextUtils.isEmpty(password)) passwordDialog(2, null, password);
                    preference.setDefaultValue("");
                    return true;
                }
            });

            CheckBoxPreference User_delete_account = findPreference("User_delete_account");
            if (User_delete_account != null) {

            }
            User_delete_account.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    passwordDialog(3, null, null);
                    return true;
                }
            });

        }

        private void deleteFiles(){
            File images = new File(getContext().getFilesDir(), "eImages");
            if(images.isDirectory()){
                File[] f = images.listFiles();
                for(File dir : f){
                    if(dir.isDirectory()){
                        File[] dirFiles = dir.listFiles();
                        for(File d : dirFiles){
                            d.delete();
                        }
                        dir.delete();
                    }
                }
                images.delete();
            }
            File notes = new File(getContext().getFilesDir(), "eNotes");
            File credentials = new File(getContext().getFilesDir(), "credentials");
            File intruderSnaps = new File(getContext().getFilesDir(), "IntruderSnaps");
            File[] files = { notes, credentials, intruderSnaps};
            for(File f : files){
                if(f.isDirectory()){
                    File[] l = f.listFiles();
                    for(File d : l){
                        d.delete();
                    }
                    f.delete();
                }
            }
        }

        private void passwordDialog(int mode, String email, String newPassword) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Please enter password");

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            EditText password = new EditText(getActivity());
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            password.setHint("Password");

            layout.addView(password);

            alert.setView(layout);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!TextUtils.isEmpty(password.getText().toString())) {
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.signInWithEmailAndPassword(sharedPreferencesManager.getStringPref("Email"), password.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
                                        switch (mode) {
                                            case 1:
                                                //Change email
                                                firebaseAuth.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Successfully updated email", Toast.LENGTH_SHORT).show();
                                                        sharedPreferencesManager.setStringPref("Email", email);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Failed updating email " + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            case 2:
                                                //Update password
                                                firebaseAuth.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Successfully updated password", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Failed updating password " + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            case 3:
                                                //Delete account
                                                firebaseAuth.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Successfully deleted account", Toast.LENGTH_SHORT).show();

                                                        deleteFiles();
                                                        sharedPreferencesManager.sharedPrefReset(getContext());
                                                        Intent intent = new Intent(getActivity(), LaunchActivity.class);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    }

                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "Failed deleting account " + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            default:
                                                break;
                                        }

                                        dialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });

            alert.show();
        }
    }

    public class DataStore extends PreferenceDataStore {

        private SharedPreferencesManager sharedPreferencesManager;

        @Override
        public void putString(String key, @Nullable String value) {
            super.putString(key, value);
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            sharedPreferencesManager.setStringPref(key, value);
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            return sharedPreferencesManager.getStringPref(key);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            super.putBoolean(key, value);
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            sharedPreferencesManager.setBoolPref(key, value);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            return sharedPreferencesManager.getBoolPref(key);
        }

        @Override
        public void putInt(String key, int value) {
            super.putInt(key, value);
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            sharedPreferencesManager.setIntPref(key, value);
        }

        @Override
        public int getInt(String key, int defValue) {
            sharedPreferencesManager = new SharedPreferencesManager(SettingsActivity.this);
            return sharedPreferencesManager.getIntPref(key);
        }
    }
}