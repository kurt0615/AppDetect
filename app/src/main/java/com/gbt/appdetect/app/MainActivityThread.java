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

public class MainActivityThread extends Activity {
    private ListView listView;
    private ListAdapter simpleAdapter;
    private List<Map<String, Object>> items;

    private ProgressDialog barProgressDialog;
    private ProgressBar progressBar;
    //private Handler updateBarHandler;

    private static List installQueue = new ArrayList();
    private static Map<String,ProgressBar> mProgressBars = new HashMap<String, ProgressBar>();

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

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(this.getClass().getName()));
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


        /*item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item); item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);
        item = new HashMap<String, Object>();
        item.put("title", "投票");
        item.put("pkg", "com.gigabyte.vote");
        items.add(item);*/

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


    //kurt add
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle resultData = intent.getExtras();
            ProgressBar progressBar = mProgressBars.get(resultData.getString("progressBarId"));
            if (resultData.getBoolean("done", false)) {
                //installApk();
                String progressBarId = resultData.getString("progressBarId");
                installQueue.add(progressBarId);
                mProgressBars.remove(progressBarId);
                progressBar.setVisibility(View.GONE);
            } else {
                updateProgress(resultData.getInt("max"), resultData.getInt("progress"), resultData.getBoolean("done"), progressBar);
            }
        }
    };

    private void updateProgress(int max, int progress, boolean done, ProgressBar progressBar) {
        if(progressBar.getVisibility() == View.GONE){
            progressBar.setMax(max);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setProgress(progress);
        }
    }


    private void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/GApps/vote.apk")), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public void doDownload(final String progressBarId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                OutputStream fileOutputStream = null;
                try {
                    if (isExternalStorageWritable()) {

                        File outputFileDir = new File(Environment.getExternalStorageDirectory() + "/GApps");
                        if (!outputFileDir.exists()) {
                            outputFileDir.mkdirs();
                        }
                        outputFileDir = new File(outputFileDir, "vote.apk");

                        fileOutputStream = new FileOutputStream(outputFileDir);

                    } else {
                        //internal storage
                        /*fileOutputStream = openFileOutput("abc.apk", Context.MODE_PRIVATE);*/
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //https://secure-appldnld.apple.com/iTunes11/031-02949.20140515.ZP3er/iTunes64Setup.exe
                //https://dl.dropboxusercontent.com/u/2787615/vote.apk
                RequestUtil.doDownload("https://secure-appldnld.apple.com/iTunes11/031-02949.20140515.ZP3er/iTunes64Setup.exe", MainActivityThread.this.getApplicationContext(), fileOutputStream, progressBarId);
                Looper.loop();
            }
        }).start();
    }

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

            String appPkgName = map.get("pkg").toString();
            holder.title.setText(map.get("title").toString());

            if (checkInstall(appPkgName)) {
                holder.btn.setText("已安裝");
            } else {
                holder.btn.setText("安裝");

            }
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UUID uuid = UUID.randomUUID();
                    String progressBarId = uuid.toString();
                    mProgressBars.put(progressBarId,holder.progressBar);
                    MainActivityThread.this.doDownload(progressBarId);
                }
            });

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

