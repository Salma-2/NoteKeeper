package com.example.notekeeper.service;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.example.notekeeper.NoteUploader;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NoteUploaderJobService extends JobService {
    public static final String EXTRA_DATA_URI = "com.example.notekeeper.extra.COURSE_ID";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters, Void,Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                JobParameters jobParameters = backgroundParams[0];
                String stringDataUri = jobParameters.getExtras().getString( EXTRA_DATA_URI );
                Uri dataUri = Uri.parse( stringDataUri );


                mNoteUploader.doUpload( dataUri );
                if(!mNoteUploader.isCanceled())
                jobFinished( jobParameters, false );

                return null;
            }
        };


        mNoteUploader = new NoteUploader( this );
        task.execute( params );




        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        mNoteUploader.cancel();
        //reschedule
        return true;
    }
}
