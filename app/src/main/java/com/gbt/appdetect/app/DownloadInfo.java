package com.gbt.appdetect.app;

import android.app.DownloadManager;
import android.database.Cursor;
import android.util.Log;

import java.util.Vector;

public class DownloadInfo implements Runnable {

    public interface ProgressListener {
        void onProgressChanged(final int progress, final int status, final int reason);
    }

    private Vector<ProgressListener> progressListenerList;

    public void addListener(ProgressListener l) {
        progressListenerList.add(l);
    }

    public void removeListener(ProgressListener l){
        progressListenerList.remove(l);
    }

    private long downloadId;

    private DownloadManager manager;

    private int progress;
    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        //progress > 100 ? 100 : progress;
        this.progress = progress;
        if (!progressListenerList.isEmpty()) {
            for (ProgressListener listener : progressListenerList)
                listener.onProgressChanged(this.progress, this.status, this.reason);
        }
    }

    private int reason;
    public void setReason(int reason) {
        this.reason = reason;
    }

    private int status;
    public void setStatus(int status) {
        this.status = status;
    }


    public DownloadInfo(long downloadId, DownloadManager manager){
        this.downloadId = downloadId;
        this.manager = manager;
        progressListenerList = new Vector<ProgressListener>();
    }

    @Override
    public void run() {

        boolean downloading = true;
        int reason = -1;
        int dl_progress = -1;
        int downloadStatus = -1;

        while (downloading) {

            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);

            final Cursor cursor = manager.query(q);

            if (cursor != null) {

                cursor.moveToFirst();

                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                }

                reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                setStatus(downloadStatus);
                setReason(reason);
                setProgress(dl_progress);

            }
            cursor.close();
        }
    }
}