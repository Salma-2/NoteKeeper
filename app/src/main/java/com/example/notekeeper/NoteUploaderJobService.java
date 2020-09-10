package com.example.notekeeper;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NoteUploaderJobService extends JobService {
    public static final String EXTRA_COURSE_ID = "com.example.notekeeper.extra.COURSE_ID";
    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
