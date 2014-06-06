package com.gbt.appdetect.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.io.File;

/**
 * Created by kurt.yang on 2014/4/22.
 */
public class DownloadBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        String appPkgName;

        if (action.equals("android.intent.action.DOWNLOAD_COMPLETE")) {
            appPkgName = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getString("AppPkgName", null);
            if (!appPkgName.isEmpty()) {
                installApk(appPkgName, context);
                SharedPreferences.Editor editor = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();
            }
        } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            appPkgName = intent.getDataString();
            if (appPkgName.indexOf("package:com.gbt") == 0 || appPkgName.indexOf("package:com.gigabyte") == 0) {
                Bundle bundle = new Bundle();
                bundle.putString("action","PACKAGE_ADDED");
                bundle.putString("appPkgName",appPkgName.substring(8));
                Intent toIntent = new Intent();
                toIntent.putExtras(bundle);
                toIntent.setAction("com.gbt.appdetect.app.MainActivityDM");
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
}
