package com.example.myapplication;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(MainActivity.TAG, "onStartJob params " + params);
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(MainActivity.TAG, "onStopJob params " + params);
        return false;  // 返回false表示停止后不再重试执行
    }
}
