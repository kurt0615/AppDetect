package com.gbt.appdetect.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestUtil {

    public static void doDownload(String sURL, Context applicationContext, OutputStream fileOutputStream, String progressBarId) {
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(sURL);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            urlConn.setRequestMethod("POST");

            int fileLength = urlConn.getContentLength();

            InputStream input = new BufferedInputStream(urlConn.getInputStream());//urlConn.getInputStream();

            byte data[] = new byte[1024];
            long progress = 0;
            int count;

            while ((count = input.read(data)) != -1) {

                progress += count;

                Intent intent = new Intent();
                Bundle resultData = new Bundle();
                intent.setAction("com.gbt.appdetect.app.MainActivityThread");
                resultData.putInt("max", (int) (fileLength / 1024));
                resultData.putInt("progress", (int) (progress / 1024));//(int) (progress * 100 / fileLength)
                resultData.putString("progressBarId", progressBarId);
                intent.putExtras(resultData);
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);

                fileOutputStream.write(data, 0, count);
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
            Intent intent = new Intent();
            intent.setAction("com.gbt.appdetect.app.MainActivityThread");
            Bundle resultData = new Bundle();
            resultData.putBoolean("done", true);
            resultData.putString("progressBarId", progressBarId);
            intent.putExtras(resultData);
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
        }
    }
}
