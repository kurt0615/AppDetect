package com.gbt.appdetect.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivityThreadDM extends Activity {
    private final int INSTALL_COMPLETE = 1;

    private ListView listView;
    private ListAdapter simpleAdapter;
    private List<Map<String, Object>> items;
    private static Map<String,DownloadInfo> downloadQueue = new HashMap<String,DownloadInfo>();

    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String action = bundle.getString("action");
            if(action.equals("DOWNLOAD_COMPLETE")){

                /*final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(bundle.getLong("downloadId"));
                final Cursor cursor = manager.query(q);
                if (cursor != null){
                    cursor.moveToFirst();
                    final int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED) {

                    }
                }*/

                downloadQueue.remove(bundle.get("appPkgName"));
                //simpleAdapter.notifyDataSetChanged();
            } else if(action.equals("PACKAGE_ADDED")){
                simpleAdapter.notifyDataSetChanged();
            } else if(action.equals("PACKAGE_FULLY_REMOVED")){
                simpleAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        this.genDetectItems();

        simpleAdapter = new ListAdapter(this,
                items, R.layout.item, new String[]{"title"},
                new int[]{R.id.title});
        listView.setAdapter(simpleAdapter);

        //debug時寫法
        removeDownloadInfo();
    }

    @Override
    protected void onResume() {

        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver,new IntentFilter(this.getClass().getName()));

       /* long downloadId = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getLong("DownloadId", -1);
        if (downloadId != -1){
            updateProgress(downloadId);
        }

        if(simpleAdapter != null){
            simpleAdapter.notifyDataSetChanged();
        }*/
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onPause();
    }


    private void genDetectItems() {
        items = new ArrayList<Map<String, Object>>();

        Map<String, Object> item = new HashMap<String, Object>();
        item.put("title", "行事曆");
        item.put("pkg", "com.gigabyte.calendars");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "郵件");
        item.put("pkg", "com.gigabyte.practice");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);


        item = new HashMap<String, Object>();
        item.put("title", "投票1");
        item.put("pkg", "com.gigabyte.vote1");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票2");
        item.put("pkg", "com.gigabyte.vote2");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票3");
        item.put("pkg", "com.gigabyte.vote3");
        items.add(item); item = new HashMap<String, Object>();
        item.put("title", "投票4");
        item.put("pkg", "com.gigabyte.vote4");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票5");
        item.put("pkg", "com.gigabyte.vote5");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票6");
        item.put("pkg", "com.gigabyte.vote6");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票7");
        item.put("pkg", "com.gigabyte.vote7");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票8");
        item.put("pkg", "com.gigabyte.vote8");
        items.add(item);

    }

    private Boolean checkAppisInstall(String appPkgName) {
        PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = (PackageInfo) packageInfoList.get(i);
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {

                if (appPkgName.equals(pak.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkApkIsDownloaded(String appPkgName){
        if (isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStorageDirectory() + String.format("/GApps/%s.apk",appPkgName));
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    private void updateProgress(int max, int progress, boolean done, ProgressBar progressBar) {
        if(progressBar.getVisibility() == View.GONE){
            progressBar.setMax(max);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setProgress(progress);
        }

        simpleAdapter.notifyDataSetChanged();
    }


    private void installApk(String appPkgName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.format("/GApps/%s.apk",appPkgName))), "application/vnd.android.package-archive");
        startActivityForResult(intent, INSTALL_COMPLETE);
    }

    public void doDownload(String appPkgName) {

        //https://dl.dropboxusercontent.com/u/2787615/vote.apk
        //https://secure-appldnld.apple.com/iTunes11/031-02993.20140528.Pu4r5/iTunes64Setup.exe
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(String.format("https://dl.dropboxusercontent.com/u/2787615/vote.apk")));
        //request.setDescription("AppName");
        //request.setTitle("下載中");
        request.allowScanningByMediaScanner();

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        if (isExternalStorageWritable()) {

            File file = new File(Environment.getExternalStorageDirectory() + "/GApps");
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(file, String.format("%s.apk",appPkgName));
            if(file.exists()){
                file.delete();
            }

            request.setDestinationUri(Uri.fromFile(file));
        }

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        final long downloadId = manager.enqueue(request);

        DownloadInfo di = new DownloadInfo(downloadId,manager);

        addDownloadInfo(downloadId, appPkgName);

        new Thread(di).start();
        downloadQueue.put(appPkgName,di);
        simpleAdapter.notifyDataSetChanged();
    }

    public boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
    }


    private void addDownloadInfo(long downloadId, String appPkgName) {
        SharedPreferences.Editor editor = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
        editor.putLong("DownloadId", downloadId);
        editor.putString("AppPkgName", appPkgName);
        editor.commit();
    }

    private void removeDownloadInfo() {
        SharedPreferences.Editor editor = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }


    class ListAdapter extends SimpleAdapter {
        private Context ctxt;
        private List<? extends Map<String, ?>> dataSource;

        public ListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.ctxt = context;
            this.dataSource = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Map map = this.dataSource.get(position);

            final String appPkgName = map.get("pkg").toString();

            final ViewHolder holder;

            if(convertView == null){

                convertView =  LayoutInflater.from(ctxt).inflate(R.layout.item,null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.btn = (Button) convertView.findViewById(R.id.btn);
                holder.progressBar = (ProgressBar)convertView.findViewById(R.id.pb);
                holder.dil = new DownloadInfo.ProgressListener(){
                    @Override
                    public void onProgressChanged(final int progress, final int status, final int reason) {
                       runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("status",String.valueOf(status));
                                Log.i("reason",String.valueOf(reason));


                                if(DownloadManager.STATUS_SUCCESSFUL == status){
                                    holder.btn.setText("安裝");
                                    holder.btn.setEnabled(true);
                                    holder.progressBar.setVisibility(View.GONE);
                                }else{

                                    holder.progressBar.setProgress(progress);

                                    if(DownloadManager.PAUSED_WAITING_FOR_NETWORK == reason) {
                                        holder.btn.setText("等待網路");
                                        holder.btn.setEnabled(false);
                                    }else{
                                        holder.btn.setText("下載中");
                                        holder.btn.setEnabled(false);
                                    }
                                }
                            }
                        });
                    }
                };

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(map.get("title").toString());



            //將畫面外的listener給刪掉
            if(holder.di != null && holder.dil != null){
                holder.di.removeListener(holder.dil);
            }

            //新增在畫面上的listener
            holder.di = downloadQueue.get(appPkgName);
            if(holder.di != null && holder.dil != null){
                holder.di.addListener(holder.dil);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if(holder.progressBar.getVisibility() == View.GONE){
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }
                        holder.progressBar.setProgress(holder.di.getProgress());                    }
                });
            }else{
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkAppisInstall(appPkgName)) {
                            holder.btn.setText("開啟");
                            holder.btn.setEnabled(true);
                            holder.btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // MainActivityThreadDM.this.launchApp(appPkgName);
                                }
                            });
                        } else if(checkApkIsDownloaded(appPkgName)){
                            holder.btn.setText("安裝");
                            holder.btn.setEnabled(true);
                            holder.btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivityThreadDM.this.installApk(appPkgName);
                                }
                            });
                        } else {
                            holder.btn.setText("下載並安裝");
                            holder.btn.setEnabled(true);
                            holder.btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivityThreadDM.this.doDownload(appPkgName);
                                }
                            });
                        }
                        holder.progressBar.setVisibility(View.GONE);
                    }
                });
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView title;
        Button btn;
        ProgressBar progressBar;
        DownloadInfo di;
        DownloadInfo.ProgressListener dil;
    }


}

