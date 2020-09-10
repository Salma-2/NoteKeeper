package com.example.notekeeper.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.DataManager;
import com.example.notekeeper.NoteActivityViewModel;
import com.example.notekeeper.NoteReminderNotification;
import com.example.notekeeper.R;
import com.example.notekeeper.database.NoteKeeperOpenHelper;
import com.example.notekeeper.models.CourseInfo;
import com.example.notekeeper.models.NoteInfo;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static String NOTE_ID = "com.example.notekeeper.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int NOTE_LOADER = 0;
    public static final int COURSE_LOADER = 1;
    private NoteInfo mNote =
            new NoteInfo( DataManager.getInstance().getCourses().get( 0 ), "", "" );
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTitleNote;
    private EditText mTextNote;
    private int mNotePosition;
    private boolean mIsNoteCanceled;
    private NoteActivityViewModel mViewModel;
    private int mNoteId;
    private NoteKeeperOpenHelper mOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdIndex;
    private int mNoteTitleIndex;
    private int mNoteTextIndex;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mNotesQueryFinishid;
    private boolean mCourseQueryFinishid;
    private Uri mNoteUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_note );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );


        mOpenHelper = new NoteKeeperOpenHelper( this );

        mSpinnerCourses = findViewById( R.id.spinner_courses );
//        //list of courses--> get data
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //set data in adapter
        mAdapterCourses = new SimpleCursorAdapter( this,
                android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},
                0 );
        //set data in spinner
        mAdapterCourses.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerCourses.setAdapter( mAdapterCourses );
//         loadCourseData();
        LoaderManager.getInstance( this ).initLoader( COURSE_LOADER, null, this );

        mTitleNote = findViewById( R.id.text_note_title );
        mTextNote = findViewById( R.id.text_note_text );


        //get reference to ViewModelProvider "same steps every time"
        ViewModelProvider viewModelProvider = new ViewModelProvider( getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance( getApplication() ) );
        mViewModel = viewModelProvider.get( NoteActivityViewModel.class );

        if (mViewModel.isNewlyCreated && savedInstanceState != null) {
            mViewModel.restoreState( savedInstanceState );
        }

        mViewModel.isNewlyCreated = false;


        readDisplayStateValues();
        if (savedInstanceState == null)
            storeOriginalValues();
        else {
            restoreOriginalValues();
        }

        if (!mIsNewNote) {
//            loadNoteData();
            LoaderManager.getInstance( this ).initLoader( NOTE_LOADER, null, this );
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry._ID};

        Cursor cursor = db.query( CourseInfoEntry.TABLE_NAME,
                courseColumns, null, null, null, null,
                CourseInfoEntry.COLUMN_COURSE_TITLE );
        mAdapterCourses.changeCursor( cursor );
    }

    private void loadNoteData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

//        String courseId = "android_intents";
//        String titleStart = "dynamic";
//
//
//        String selection =
//                NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
//                        + NoteInfoEntry.COLUMN_NOTE_TITLE +
//                        " LIKE ?";
//        String[] selectionArgs = {courseId, titleStart + "%"};

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString( mNoteId )};
        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
        };

        mNoteCursor = db.query( NoteInfoEntry.TABLE_NAME,
                noteColumns,
                selection,
                selectionArgs,
                null, null, null );

        mCourseIdIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        mNoteTitleIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mNoteTextIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TEXT );
        mNoteCursor.moveToNext();
        displayNote();


    }


    @Override
    protected void onDestroy() {
        mOpenHelper.close();
        super.onDestroy();
    }

    private void displayNote() {

        String courseId = mNoteCursor.getString( mCourseIdIndex );
        String noteTitle = mNoteCursor.getString( mNoteTitleIndex );
        String noteText = mNoteCursor.getString( mNoteTextIndex );
        int courseIndex = getIndexOfCourseId( courseId );
        mSpinnerCourses.setSelection( courseIndex );
        mTitleNote.setText( noteTitle );
        mTextNote.setText( noteText );
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex( CourseInfoEntry.COLUMN_COURSE_ID );
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();
        while (more) {
            if (cursor.getString( courseIdPos ).equals( courseId )) {
                break;
            }
            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        //mNote = intent.getParcelableExtra( NOTE_POSITION );
        mNoteId = intent.getIntExtra( NOTE_ID, ID_NOT_SET );

        //set true if mNote == null else set false
        // mIsNewNote = mNote == null;

        //set mIsNewNote true if id = -1
        mIsNewNote = mNoteId == ID_NOT_SET;

        if (mIsNewNote) {
            createNewNote();
        }
        //if not new note , get info of this note
//            mNote = DataManager.getInstance().getNotes().get( mNoteId );


    }


    //insert in db
    private void createNewNote() {
        final ContentValues values = new ContentValues();
        values.put( Notes.COLUMN_COURSE_ID, "" );
        values.put( Notes.COLUMN_NOTE_TITLE, "" );
        values.put( Notes.COLUMN_NOTE_TEXT, "" );
        mNoteUri = getContentResolver().insert( Notes.CONTENT_URI, values );


//                AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//                mNoteId = (int) db.insert( NoteInfoEntry.TABLE_NAME, null, values );
//
//                return null;
//            }
//        };
//        task.execute();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_note, menu );
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsNoteCanceled = true;
            finish();
            return true;
        } else if (id == R.id.action_next) {
            moveNext();
        }

        switch (id){
            case R.id.action_send_email:
                sendEmail();
                break;
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_next:
                moveNext();
                break;
            case R.id.action_set_reminder:
                showReminderNotification();


        }

        return super.onOptionsItemSelected( item );
    }

    private void showReminderNotification() {
        String noteTitle = mTitleNote.getText().toString();
        String noteText = mTextNote.getText().toString();
        int noteId= (int)ContentUris.parseId( mNoteUri );
        NoteReminderNotification.notify( this,noteTitle,noteText,noteId );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem( R.id.action_next );
        int lastElement = DataManager.getInstance().getNotes().size() - 1;

        item.setEnabled( mNoteId < lastElement );
        return super.onPrepareOptionsMenu( menu );
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get( mNoteId );
        storeOriginalValues();
        displayNote();
        invalidateOptionsMenu();
    }


    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String courseName = course.getTitle();
        String subject = mTitleNote.getText().toString();
        String body = mTextNote.getText().toString();
        String text =
                "Checkout what I learned in Pluralsight course \"" + courseName + "\"\n" + body;

        Intent intent = new Intent( Intent.ACTION_SEND );
        intent.setType( "message/rfc822" );

        intent.putExtra( Intent.EXTRA_SUBJECT, subject );
        intent.putExtra( Intent.EXTRA_TEXT, text );

        startActivity( intent );
    }

    @Override
    protected void onPause() {
        if (mIsNoteCanceled) {
            if (mIsNewNote) {
                deleteNotefromDatabase();
            } else {
                restoreOriginalValues();
            }
        } else {
            saveNote();
        }
        super.onPause();
    }

    //delete from db
    private void deleteNotefromDatabase() {
        final String selection = NoteInfoEntry._ID + "=?";
        final String[] selectionArgs = {String.valueOf( mNoteId )};
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.delete( NoteInfoEntry.TABLE_NAME, selection, selectionArgs );
                return null;
            }
        };
        task.execute();
    }


    private void restoreOriginalValues() {
        mNote.setCourse( DataManager.getInstance().getCourse( mViewModel.mOriginalCourseId ) );
        mNote.setTitle( mViewModel.mOriginalCourseTitle );
        mNote.setText( mViewModel.mMOriginalCourseText );
    }

    private void storeOriginalValues() {
        if (mIsNewNote)
            return;
        mViewModel.mOriginalCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalCourseTitle = mNote.getTitle();
        mViewModel.mMOriginalCourseText = mNote.getText();
    }

    private void saveNote() {
        String courseId = getCourseId();
        String noteTitle = mTitleNote.getText().toString();
        String noteText = mTextNote.getText().toString();
        saveNoteToDatabase( courseId, noteTitle, noteText );
    }

    //update db
    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        final String selection = NoteInfoEntry._ID + "=?";
        final String[] selectionArgs = {String.valueOf( mNoteId )};
        final ContentValues values = new ContentValues();
        values.put( NoteInfoEntry.COLUMN_COURSE_ID, courseId );
        values.put( NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle );
        values.put( NoteInfoEntry.COLUMN_NOTE_TEXT, noteText );

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.update( NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs );
                return null;
            }
        };
        task.execute();
    }

    private String getCourseId() {
        Cursor cursor = mAdapterCourses.getCursor();
        int position = mSpinnerCourses.getSelectedItemPosition();
        cursor.moveToPosition( position );
        int courseIdPos = cursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        return cursor.getString( courseIdPos );
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState( outState );
        if (outState != null) {
            mViewModel.saveState( outState );
        }
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == NOTE_LOADER)
            loader = createLoaderNotes();
        else if (id == COURSE_LOADER) {
            loader = createLoaderCourse();
        }

        return loader;
    }

    private CursorLoader createLoaderCourse() {
        mCourseQueryFinishid = false;
        CursorLoader loader;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_ID,
                Courses.COLUMN_COURSE_TITLE,
                Courses._ID};
        loader = new CursorLoader( this, uri, courseColumns, null, null,
                Courses.COLUMN_COURSE_TITLE );
        return loader;
//        return new CursorLoader( this ) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//                String[] courseColumns = {
//                        CourseInfoEntry.COLUMN_COURSE_ID,
//                        CourseInfoEntry.COLUMN_COURSE_TITLE,
//                        CourseInfoEntry._ID};
//
//                return db.query( CourseInfoEntry.TABLE_NAME,
//                        courseColumns, null, null, null, null,
//                        CourseInfoEntry.COLUMN_COURSE_TITLE );
//            }
//        };
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinishid = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNoteUri = ContentUris.withAppendedId( Notes.CONTENT_URI, mNoteId );
        return new CursorLoader( this, mNoteUri, noteColumns, null, null, null );

//        return new CursorLoader( this ) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//                String selection = NoteInfoEntry._ID + " = ?";
//                String[] selectionArgs = {Integer.toString( mNoteId )};
//                String[] noteColumns = {
//                        NoteInfoEntry.COLUMN_COURSE_ID,
//                        NoteInfoEntry.COLUMN_NOTE_TITLE,
//                        NoteInfoEntry.COLUMN_NOTE_TEXT,
//                };
//                //return cursor
//                return db.query( NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
//                        null, null, null );
//            }
//        };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == NOTE_LOADER) {
            loadFinishidNotes( data );
        }
        if (loader.getId() == COURSE_LOADER) {
            mAdapterCourses.changeCursor( data );
            mCourseQueryFinishid = true;
            displayNotesWhenQueriesFinished();
        }

    }

    private void loadFinishidNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        mNoteTitleIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mNoteTextIndex = mNoteCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TEXT );
        mNoteCursor.moveToNext();
        mNotesQueryFinishid = true;
        displayNotesWhenQueriesFinished();
    }

    private void displayNotesWhenQueriesFinished() {
        if (mNotesQueryFinishid && mCourseQueryFinishid)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == NOTE_LOADER) {
            if (mNoteCursor != null)
                mNoteCursor.close();
        } else if (loader.getId() == COURSE_LOADER) {
            mAdapterCourses.changeCursor( null );
        }
    }
}