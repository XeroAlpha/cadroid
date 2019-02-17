package com.xero.ca;

import android.content.Context;
import android.content.SharedPreferences;

import com.xero.ca.script.ScriptObject;

public class Preference {
    public static final String SETTING_HIDE_SPLASH = "hideSplash";
    public static final String SETTING_HIDE_NOTIFICATION = "hideNotification";
    public static final String SETTING_START_ON_BOOT = "bootStart";
    public static final String PREFERENCE_NAME = "user_settings";

    private static Preference sInstance = null;

    private SharedPreferences mPreferences;

    private Preference(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static Preference getInstance(Context context) {
        if (sInstance != null) return sInstance;
        sInstance = new Preference(context);
        return sInstance;
    }

    @ScriptObject
    public boolean getHideSplash() {
        return mPreferences.getBoolean(SETTING_HIDE_SPLASH, false);
    }

    @ScriptObject
    public void setHideSplash(boolean v) {
        mPreferences.edit().putBoolean(SETTING_HIDE_SPLASH, v).apply();
    }

    @ScriptObject
    public boolean getHideNotification() {
        return mPreferences.getBoolean(SETTING_HIDE_NOTIFICATION, false);
    }

    @ScriptObject
    public void setHideNotification(boolean v) {
        mPreferences.edit().putBoolean(SETTING_HIDE_NOTIFICATION, v).apply();
    }

    @ScriptObject
    public boolean getBootStart() {
        return mPreferences.getBoolean(SETTING_START_ON_BOOT, false);
    }

    @ScriptObject
    public void setBootStart(boolean v) {
        mPreferences.edit().putBoolean(SETTING_START_ON_BOOT, v).apply();
    }

    public String getDebugSource() {
        return mPreferences.getString("debugSource", "");
    }
}