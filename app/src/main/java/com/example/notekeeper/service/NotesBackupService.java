package com.example.notekeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.example.notekeeper.NoteBackup;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NotesBackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "com.example.notekeeper.extra.COURSE_ID";

    public NotesBackupService() {
        super( "NotesBackupService" );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra( EXTRA_COURSE_ID );
            NoteBackup.doBackup( this, backupCourseId);
        }
    }


}
