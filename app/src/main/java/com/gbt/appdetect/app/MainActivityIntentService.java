package com.gbt.appdetect.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
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

public class MainActivityIntentService extends Activity {
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

    private Boolean checkInstall(String appPkgName) {
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

    private void updateProgress(int max, final int progress, boolean done) {
        if (barProgressDialog == null || !barProgressDialog.isShowing()) {
            barProgressDialog = new ProgressDialog(this);

            barProgressDialog.setTitle("Downloading Image ...");

            barProgressDialog.setMessage("Download in progress ...");

            barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);

            barProgressDialog.setProgress(0);
            barProgressDialog.setProgressNumberFormat(null);

            barProgressDialog.setMax(max);

            barProgressDialog.setCancelable(false);

            barProgressDialog.show();

        } else {
            barProgressDialog.setProgress(progress);
        }
    }

    private void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/GApps/vote.apk")), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public void doDownload() {
       if (isExternalStorageWritable()) {

            File file = new File(Environment.getExternalStorageDirectory() + "/GApps");
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file, "vote.apk");

            Intent intent = new Intent(this, DownloadService.class);
            intent.putExtra("sUrl", "https://dl.dropboxusercontent.com/u/2787615/vote.apk");
            intent.putExtra("receiver", new DownloadReceiver(new Handler()));
            intent.putExtra("file", file);

            startService(intent);
        }
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 888) {

                if (resultData.getBoolean("done", false)) {
                    barProgressDialog.dismiss();
                    installApk();
                } else {
                    updateProgress(resultData.getInt("max"), resultData.getInt("progress"), resultData.getBoolean("done"));
                }
            }
        }
    }

    public boolean isExternalStorageWritable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
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

            if(convertView == null){
                view =  LayoutInflater.from(ctxt).inflate(R.layout.item,null);
                holder = new ViewHolder();
                holder.title = (TextView) view.findViewById(R.id.title);
                holder.btn = (Button) view.findViewById(R.id.btn);
                holder.progressBar = (ProgressBar)view.findViewById(R.id.pb);
                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }

            Map map = this.dataSource.get(position);

            final String appPkgName = map.get("pkg").toString();
            holder.title.setText(map.get("title").toString());

            if (checkInstall(appPkgName)) {
                holder.btn.setText("開啟");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivityIntentService.this.launchApp(appPkgName);
                    }
                });
            } else {
                holder.btn.setText("安裝");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivityIntentService.this.doDownload();
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

