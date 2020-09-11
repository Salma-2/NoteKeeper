package com.example.notekeeper.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_ID = "com.example.notekeeper.extra.NOTE_ID";
    public static final String EXTRA_NOTE_TITLE = "com.example.notekeeper.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.example.notekeeper.extra.NOTE_TEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra( EXTRA_NOTE_ID, 0 );
        String noteTitle = intent.getStringExtra( EXTRA_NOTE_TITLE );
        String noteText = intent.getStringExtra( EXTRA_NOTE_TEXT );

        NoteReminderNotification.notify( context, noteTitle, noteText, noteId );
    }
}
