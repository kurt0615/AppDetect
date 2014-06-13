package com.gbt.appdetect.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kurt.yang on 2014/4/22.
 */
public class DownloadBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.i("action",action);

        long downloadId = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getLong("DownloadId", -1);

        if (action.equals("android.intent.action.DOWNLOAD_COMPLETE") && downloadId != -1) {
            String appPkgName = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getString("AppPkgName", null);
            if (appPkgName != null) {
                installApk(appPkgName, context);
                SharedPreferences.Editor editor = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();

                Bundle bundle = new Bundle();
                bundle.putString("action","DOWNLOAD_COMPLETE");
                bundle.putString("appPkgName",appPkgName);
                bundle.putLong("downloadId",downloadId);
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(context).sendBroadcast(toIntent);
            }
        } else if (action.equals("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED") && downloadId != -1) {
            Log.i("DOWNLOAD_NOTIFICATION_CLICKED","CLICKED");
            long[] downloadIds = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            Arrays.sort(downloadIds);
            int index = Arrays.binarySearch(downloadIds,downloadId);

            if(index > -1) {
                launchApp(context);
            }
        } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            String appPkgName = intent.getDataString();
            if (appPkgName.indexOf("package:com.gbt") == 0 || appPkgName.indexOf("package:com.gigabyte") == 0) {
                Bundle bundle = new Bundle();
                bundle.putString("action","PACKAGE_ADDED");
                bundle.putString("appPkgName",appPkgName.substring(8));
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(context).sendBroadcast(toIntent);
            }
        } else if (action.equals("android.intent.action.PACKAGE_FULLY_REMOVED")) {
            String appPkgName = intent.getDataString();
            if (appPkgName.indexOf("package:com.gbt") == 0 || appPkgName.indexOf("package:com.gigabyte") == 0) {
                Bundle bundle = new Bundle();
                bundle.putString("action","PACKAGE_FULLY_REMOVED");
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityThreadDM");
                LocalBroadcastManager.getInstance(context).sendBroadcast(toIntent);
            }
        }
    }

    private void installApk(String appPkgName, Context context) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.format("/GApps/%s.apk", appPkgName))), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private void launchApp(Context context)
    {
        Intent i = new Intent(context, MainActivityDM.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }
}
