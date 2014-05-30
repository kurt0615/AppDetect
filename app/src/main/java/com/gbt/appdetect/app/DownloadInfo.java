package com.gbt.appdetect.app;


import android.widget.ProgressBar;

public class DownloadInfo {

    private volatile Integer mProgress;
    private volatile ProgressBar mProgressBar;

    public DownloadInfo() {
        mProgress = 0;
        mProgressBar = null;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }
    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }


    public Integer getProgress() {
        return mProgress;
    }

    public void setProgress(Integer progress) {
        this.mProgress = progress;
    }
}