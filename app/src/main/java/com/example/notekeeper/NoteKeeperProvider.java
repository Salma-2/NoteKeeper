package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.notekeeper.contracts.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.database.NoteKeeperOpenHelper;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;


public class NoteKeeperProvider extends ContentProvider {
    public static final String MIME_VENDOR_TYPE = "vnd." + AUTHORITY;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;
    private static final int COURSES_ROW = 4;
    private static final int NOTES_EXPANDED_ROW = 5;

    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, Courses.PATH + "/#", COURSES_ROW);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED + "/#", NOTES_EXPANDED_ROW);
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
        String mimeType = null;
        int matcher = sUriMatcher.match( uri );

        switch (matcher) {
            case COURSES:
                //vnd.android.cursor.dir/vnd.com.example.notekeeper.provider.courses
                mimeType =
                        ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + "." + Courses.PATH;
                break;
            case NOTES:
                mimeType =
                        ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + "." + Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType =
                        ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + "." + Notes.PATH_EXPANDED;
                break;
            case NOTES_ROW:
                mimeType =
                        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + "." + Notes.PATH;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri rowUri = null;
        long rowId = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        int uriMatcher = sUriMatcher.match( uri );
        switch (uriMatcher) {
            case COURSES:
                rowId = db.insert( CourseInfoEntry.TABLE_NAME, null, values );
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
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId( uri );
                String rowSelection = NoteInfoEntry._ID + " = ? ";
                String[] rowSelectionArgs = new String[]{Long.toString( rowId )};
                //null for sort--> query for 1 specific row
                cursor = db.query( NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs,
                        null, null, null );
                break;
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
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }
}
