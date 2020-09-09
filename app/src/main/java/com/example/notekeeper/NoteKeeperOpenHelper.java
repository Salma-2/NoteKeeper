package com.example.notekeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeper.db";
    /*
    version changes from 1 to 2
    version 1 --> has only tables
    version 2 --> has tables and indexes
    */
    public static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(@Nullable Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( CourseInfoEntry.SQL_CREATE_TABLE );
        db.execSQL( NoteInfoEntry.SQL_CREATE_TABLE );
        db.execSQL( NoteInfoEntry.SQL_CREATE_INDEX1 );
        db.execSQL( CourseInfoEntry.SQL_CREATE_INDEX1 );

        DatabaseDataWorker worker = new DatabaseDataWorker( db );
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {

            db.execSQL( CourseInfoEntry.SQL_CREATE_INDEX1 );
            db.execSQL( NoteInfoEntry.SQL_CREATE_INDEX1 );
        }
    }
}
