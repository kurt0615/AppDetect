package com.gbt.appdetect.app;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String sURL = intent.getStringExtra("sUrl");
        File file = (File) intent.getSerializableExtra("file");
        String progressBarId = intent.getStringExtra("progressBarId");
        OutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        HttpURLConnection urlConn = null;

        try {

            URL url = new URL(sURL);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            urlConn.setRequestMethod("POST");

            int fileLength = urlConn.getContentLength();

            InputStream input = new BufferedInputStream(urlConn.getInputStream());

            byte data[] = new byte[1024];
            long progress = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                progress += count;
                Bundle resultData = new Bundle();
                resultData.putInt("max", (fileLength / 1024));
                resultData.putInt("progress", (int) (progress / 1024));
                receiver.send(888, resultData);

                fileOutputStream.write(data, 0, count);
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
            Bundle resultData = new Bundle();
            resultData.putBoolean("done", true);
            receiver.send(888, resultData);
        }
    }
}
