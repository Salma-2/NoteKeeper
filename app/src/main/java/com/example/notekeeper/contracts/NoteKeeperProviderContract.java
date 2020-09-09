package com.example.notekeeper.contracts;

import android.net.Uri;
import android.provider.BaseColumns;


public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {
    }


    private interface CourseIdColumn {
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    private interface NoteColumns {

        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    private interface CourseColumns {
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    public static final String AUTHORITY = "com.example.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse( "content://" + AUTHORITY );

    public final static class Courses implements CourseIdColumn, CourseColumns {
        public final static String PATH = "courses";
        public final static Uri CONTENT_URI = Uri.withAppendedPath( AUTHORITY_URI, PATH );
    }

    public final static class Notes implements NoteColumns, CourseIdColumn {
        public final static String PATH = "notes";
        public final static Uri CONTENT_URI = Uri.withAppendedPath( AUTHORITY_URI, PATH );
    }


}
