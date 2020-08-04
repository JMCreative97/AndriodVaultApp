package com.example.javaproject.Misc;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class SharedPreferencesManager {

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        initializeSharedPreference(context);
    }

    private void initializeSharedPreference(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStringPref(String id, String string) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, string);
        editor.commit();
    }

    public void setIntPref(String id, Integer integer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(id, integer);
        editor.commit();
    }

    public void setBoolPref(String id, Boolean bool) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(id, bool);
        editor.commit();
    }

    public String getStringPref(String id) {
        String stringTemp = sharedPreferences.getString(id, null);
        return stringTemp;
    }

    public Integer getIntPref(String id) {
        Integer intTemp = sharedPreferences.getInt(id, 0);
        return intTemp;
    }

    public Boolean getBoolPref(String id) {
        Boolean boolTemp = sharedPreferences.getBoolean(id, false);
        return boolTemp;
    }

    public void sharedPrefReset(Context context) {
        SharedPreferences.Editor editor = sharedPreferences.edit().clear();
        editor.commit();
    }
}
