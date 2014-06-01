package com.gbt.appdetect.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivityDM extends Activity {

    private int INSTALL_COMPLETE = 1;
    private ListView listView;
    private ListAdapter simpleAdapter;
    private List<Map<String, Object>> items;
    private ProgressDialog barProgressDialog;

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

        final long downloadId = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getLong("DownloadId", -1);
        final String appPkgName = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).getString("AppPkgName",null);

        if (downloadId != -1) {
            updateProgress(downloadId,appPkgName);
        }
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
    }

    private Boolean checkAppisInstall(String appPkgName) {
        PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = (PackageInfo) packageInfoList.get(i);
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                // customs applications
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

    private void installApk(String appPkgName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), String.format("/GApps/%s.apk",appPkgName))), "application/vnd.android.package-archive");
        startActivityForResult(intent, INSTALL_COMPLETE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == INSTALL_COMPLETE){
            simpleAdapter.notifyDataSetChanged();
        }
    }

    public void launchApp(String packageName)
    {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent != null){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else{
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id="+packageName));
            startActivity(intent);
        }
    }

    private void updateProgress(final long downloadId, final String appPkgName) {

        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;
                int downloadStatus = -1;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = manager.query(q);

                    if (cursor.getCount() > 0) {

                        cursor.moveToFirst();

                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED) {
                            downloading = false;
                        }

                        final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                if (barProgressDialog == null || !barProgressDialog.isShowing()) {

                                    barProgressDialog = new ProgressDialog(MainActivityDM.this);

                                    barProgressDialog.setTitle("下載中");

                                    //barProgressDialog.setMessage("下載中");

                                    barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);

                                    barProgressDialog.setProgress(dl_progress);

                                    barProgressDialog.setProgressNumberFormat(null);

                                    barProgressDialog.setCancelable(false);

                                    barProgressDialog.show();

                                } else {
                                    barProgressDialog.setProgress(dl_progress);
                                }
                            }
                        });
                    }
                    cursor.close();
                }

                removeDownloadId(downloadId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (barProgressDialog != null){
                            barProgressDialog.dismiss();
                            barProgressDialog = null;
                        }
                        simpleAdapter.notifyDataSetChanged();
                    }
                });

                if(appPkgName != null){
                    installApk(appPkgName);
                }
            }
        }).start();
    }

    public void doDownload(String appPkgName) {
        //https://dl.dropboxusercontent.com/u/2787615/vote.apk
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(String.format("https://dl.dropboxusercontent.com/u/2787615/vote.apk",appPkgName)));
        //request.setDescription("AppName");
        //request.setTitle("下載中");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

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
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "i64Setup.exe");
        }


        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        final long downloadId = manager.enqueue(request);

        addDownloadId(downloadId, appPkgName);

        updateProgress(downloadId, appPkgName);
    }

    private void addDownloadId(long downloadId, String appPkgName) {
        SharedPreferences.Editor editor = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
        editor.putLong("DownloadId", downloadId);
        editor.putString("AppPkgName", appPkgName);
        editor.commit();
    }

    private void removeDownloadId(long downloadId) {
        SharedPreferences.Editor editor = getSharedPreferences("DownloadInfo", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    /*private String statusMessage(Cursor c) {
        String msg = "???";

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg = "Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Download complete!";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }*/

    public boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
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
            View view = convertView;
            final ViewHolder holder;

            if (convertView == null) {
                view = LayoutInflater.from(ctxt).inflate(R.layout.item, null);
                holder = new ViewHolder();
                holder.title = (TextView) view.findViewById(R.id.title);
                holder.btn = (Button) view.findViewById(R.id.btn);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.pb);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Map map = this.dataSource.get(position);

            final String appPkgName = map.get("pkg").toString();
            holder.title.setText(map.get("title").toString());

            if (checkAppisInstall(appPkgName)) {
                holder.btn.setText("開啟");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivityDM.this.launchApp(appPkgName);
                    }
                });
            } else if(checkApkIsDownloaded(appPkgName)){
                holder.btn.setText("安裝");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivityDM.this.installApk(appPkgName);
                    }
                });
            } else {
                holder.btn.setText("下載並安裝");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivityDM.this.doDownload(appPkgName);
                    }
                });
            }

            return view;
        }
    }

    private static class ViewHolder {
        TextView title;
        Button btn;
        ProgressBar progressBar;
        DownloadInfo info;
    }

}
