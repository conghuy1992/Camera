package com.conghuy.example.classs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by maidinh on 05-Oct-17.
 */

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String PREF_NAME = "CH_Camera";
    // shared pref mode
    private int PRIVATE_MODE = 0;
    private String FLASH = "FLASH";

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void ClearData() {
        editor.clear();
    }

    public void setFlash(boolean s) {
        editor.putBoolean(FLASH, s);
        editor.commit();
    }

    public boolean isFlash() {
        return pref.getBoolean(FLASH, false);
    }
}
