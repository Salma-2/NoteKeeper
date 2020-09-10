package com.example.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.notekeeper.contracts.NoteKeeperProviderContract;

import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;

public class NoteBackup {
    public final static String TAG = NoteBackup.class.getSimpleName();
    public final static String ALL_COURSES = "ALL_COURSES";


    public static void doBackup(Context context, String backupCourseId) {
        String[] projection = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        String selcetion = null;
        String[] selectionArgs = null;

        if (!backupCourseId.equals( ALL_COURSES )) {
            selcetion = Notes.COLUMN_COURSE_ID + " = ?";
            selectionArgs = new String[]{backupCourseId};
        }
        Cursor cursor = context.getContentResolver().query( Notes.CONTENT_URI, projection,
                selcetion, selectionArgs, null );


        int count = cursor.getCount();
        int courseIdIndex = cursor.getColumnIndex( Notes.COLUMN_COURSE_ID );
        int noteTitleIndex = cursor.getColumnIndex( Notes.COLUMN_NOTE_TITLE );
        int noteTextIndex = cursor.getColumnIndex( Notes.COLUMN_NOTE_TEXT );
        cursor.moveToFirst();

        Log.i(TAG, ">>>***   BACKUP START - Thread: " + Thread.currentThread().getId() + "   ***<<<");
        while (cursor.moveToNext()) {
            String courseId = cursor.getString( courseIdIndex );
            String noteTitle = cursor.getString( noteTitleIndex );
            String noteText = cursor.getString( noteTextIndex );
            if (noteTitle != null) {
                Log.i(TAG, ">>>Backing Up Note<<< " + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }
        Log.i(TAG, ">>>***   BACKUP COMPLETE   ***<<<");
        cursor.close();

    }

    private static void simulateLongRunningWork() {
        try {
            Thread.sleep( 1000 );
        } catch (Exception ex) {
        }
    }

}
