package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.notekeeper.contracts.NoteKeeperDatabaseContract;
import com.example.notekeeper.contracts.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.contracts.NoteKeeperProviderContract;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;


public class NoteKeeperProvider extends ContentProvider {
    private NoteKeeperOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;

    static {
        sUriMatcher.addURI( AUTHORITY, Courses.PATH, COURSES );
        sUriMatcher.addURI( AUTHORITY, Notes.PATH, NOTES );
        sUriMatcher.addURI( AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED );
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri rowUri = null;
        long rowId = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        int uriMatcher = sUriMatcher.match( uri );
        switch (uriMatcher) {
            case COURSES:
                rowId= db.insert( CourseInfoEntry.TABLE_NAME, null, values );
                rowUri = ContentUris.withAppendedId( Courses.CONTENT_URI, rowId );
                break;
            case NOTES:
                rowId = db.insert( NoteInfoEntry.TABLE_NAME, null, values );
                //content://com.example.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId( Notes.CONTENT_URI, rowId );
                break;
            case NOTES_EXPANDED:
                //this is read-only table
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper( getContext() );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        int matcher = sUriMatcher.match( uri );
        switch (matcher) {
            case COURSES:
                cursor = db.query( CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder );
                break;
            case NOTES:
                cursor = db.query( NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder );
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery( db, projection, selection,
                        selectionArgs, sortOrder );
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = projection;
        for (int i = 0; i < projection.length; i++) {
            columns[i] =
                    projection[i].equals( BaseColumns._ID ) || projection[i].equals( CourseInfoEntry.COLUMN_COURSE_ID ) ?
                            NoteInfoEntry.getQName( projection[i] ) : projection[i];
        }

        // note_info JOIN course_info ON note_info.course_id = course_info.course_id
        String tableWithJoin =
                NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                        NoteInfoEntry.getQName( NoteInfoEntry.COLUMN_COURSE_ID ) + " = " + CourseInfoEntry.getQName( CourseInfoEntry.COLUMN_COURSE_ID );
        Cursor cursor = db.query( tableWithJoin, columns, selection, selectionArgs, null, null,
                sortOrder );
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
