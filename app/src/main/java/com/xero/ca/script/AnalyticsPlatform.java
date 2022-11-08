package com.xero.ca.script;

import android.app.Activity;
import android.content.Context;

import com.tendcloud.tenddata.TalkingDataSDK;

import java.util.HashMap;
import java.util.Map;

@ScriptObject
public class AnalyticsPlatform {
    private static final AnalyticsPlatform sInstance = new AnalyticsPlatform();

    public static AnalyticsPlatform getInstance() {
        return sInstance;
    }

    public void onError(Context context, Throwable throwable) {
        TalkingDataSDK.onError(context, throwable);
    }

    public void onPageStart(Context context, String pageName) {
        TalkingDataSDK.onPageBegin(context, pageName);
    }

    public void onPageEnd(Context context, String pageName) {
        TalkingDataSDK.onPageEnd(context, pageName);
    }

    public void onEvent(Context context, String eventId, Double eventValue) {
        TalkingDataSDK.onEvent(context, eventId, eventValue, null);
    }

    public void onEvent(Context context, String eventId, Double eventValue, Map<String, Object> kv) {
        TalkingDataSDK.onEvent(context, eventId, eventValue, kv);
    }

    public void setGlobalKV(String key, String value) {
        TalkingDataSDK.setGlobalKV(key, value);
    }

    public void removeGlobalKV(String key) {
        TalkingDataSDK.removeGlobalKV(key);
    }

    public static void reportAdError(Activity activity, int errorCode, String errorMessage) {
        Map<String, Object> kv = new HashMap<>();
        kv.put("error", errorCode + " " + errorMessage);
        TalkingDataSDK.onEvent(activity, "AD_ERROR", 0, kv);
    }
}
