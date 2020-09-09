package com.example.notekeeper.contracts;

import android.net.Uri;
import android.provider.BaseColumns;


public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {
    }

    public static final String AUTHORITY = "com.example.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse( "content://" + AUTHORITY );

    public final static class Courses{
        public final static String PATH ="courses";

    }



}
