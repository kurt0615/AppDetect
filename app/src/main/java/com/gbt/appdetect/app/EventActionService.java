package com.gbt.appdetect.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;

public class EventActionService extends IntentService {

    public static final String DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
    public static final String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    public static final String PACKAGE_FULLY_REMOVED = "android.intent.action.PACKAGE_FULLY_REMOVED";

    public EventActionService() {
        super("EventActionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final Bundle bundle = intent.getExtras();
            final String action = intent.getAction();

            if (DOWNLOAD_COMPLETE.equals(action)) {

                bundle.putString("action","DOWNLOAD_COMPLETE");
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);

                String appPkgName = bundle.getString("appPkgName");
                String downloadId = bundle.getString("downloadId");

                removeDownloadInfo(downloadId);

                installApk(appPkgName, getApplicationContext());

            } else if (PACKAGE_ADDED.equals(action)) {

                String appPkgName = bundle.getString("appPkgName");
                removeApkFile(appPkgName);

                bundle.putString("action","PACKAGE_ADDED");
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);

            } else if (PACKAGE_FULLY_REMOVED.equals(action)) {

                bundle.putString("action","PACKAGE_FULLY_REMOVED");
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);
            }
        }

        EventBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void installApk(String appPkgName, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.format("/GApps/%s.apk", appPkgName))), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void removeDownloadInfo(String downloadId) {
        SharedPreferences.Editor editor = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
        editor.remove(downloadId);
        editor.commit();
    }

    private void removeApkFile(String appPkgName){
        File file = new File(Environment.getExternalStorageDirectory() + "/GApps");
        file = new File(file, String.format("%s.apk", appPkgName));
        if (file.exists()) {
            file.delete();
        }
    }
}
