package com.gbt.appdetect.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kurt.yang on 2014/4/22.
 */
public class EventBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.i("action",action);

        // long downloadId = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getLong("DownloadId", -1);

        if (action.equals("android.intent.action.DOWNLOAD_COMPLETE")) {

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
            String appPkgName = null;
            if(downloadId != -1){
                appPkgName = context.getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getString(String.valueOf(downloadId),"");
            }

            if (!appPkgName.equals("")) {
                Bundle bundle = new Bundle();
                bundle.putString("appPkgName",appPkgName);
                bundle.putString("downloadId",String.valueOf(downloadId));

                intent.putExtras(bundle);

                ComponentName comp = new ComponentName(context,EventActionService.class.getName());
                startWakefulService(context,intent.setComponent(comp));
            }
        } else if (action.equals("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED")) {
//            Log.i("DOWNLOAD_NOTIFICATION_CLICKED","CLICKED");
//            long[] downloadIds = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
//            Arrays.sort(downloadIds);
//            int index = Arrays.binarySearch(downloadIds,downloadId);
//
//            if(index > -1) {
//                launchApp(context);
//            }
        } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            String appPkgName = intent.getDataString();
            if (appPkgName.indexOf("package:com.gbt") == 0 || appPkgName.indexOf("package:com.gigabyte") == 0) {
                Bundle bundle = new Bundle();
                bundle.putString("appPkgName",appPkgName.substring(8));
                intent.putExtras(bundle);


                ComponentName comp = new ComponentName(context,EventActionService.class.getName());
                startWakefulService(context,intent.setComponent(comp));
            }
        } else if (action.equals("android.intent.action.PACKAGE_FULLY_REMOVED")) {
            String appPkgName = intent.getDataString();
            if (appPkgName.indexOf("package:com.gbt") == 0 || appPkgName.indexOf("package:com.gigabyte") == 0) {
                ComponentName comp = new ComponentName(context,EventActionService.class.getName());
                startWakefulService(context,intent.setComponent(comp));
            }
        }
    }
//    private void launchApp(Context context)
//    {
//        Intent i = new Intent(context, MainActivityDM.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(i);
//    }
}
