package com.xero.ca.script;

import android.app.Activity;
import android.content.Context;

import com.tendcloud.tenddata.TCAgent;

import java.util.Map;

@ScriptObject
public class AnalyticsPlatform {
    private static AnalyticsPlatform sInstance = new AnalyticsPlatform();

    public static AnalyticsPlatform getInstance() {
        return sInstance;
    }

    public void onError(Context context, Throwable throwable) {
        TCAgent.onError(context, throwable);
    }

    public void onPageStart(Context context, String pageName) {
        TCAgent.onPageStart(context, pageName);
    }

    public void onPageEnd(Context context, String pageName) {
        TCAgent.onPageEnd(context, pageName);
    }

    public void onEvent(Context context, String eventId, String eventLabel) {
        TCAgent.onEvent(context, eventId, eventLabel);
    }

    public void onEvent(Context context, String eventId, String eventLabel, Map kv) {
        TCAgent.onEvent(context, eventId, eventLabel, kv);
    }

    public void setGlobalKV(String key, String value) {
        TCAgent.setGlobalKV(key, value);
    }

    public void removeGlobalKV(String key) {
        TCAgent.removeGlobalKV(key);
    }
}
