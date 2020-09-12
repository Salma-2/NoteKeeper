package com.example.notekeeper.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.CourseEventBroadcastHelper;
import com.example.notekeeper.DataManager;
import com.example.notekeeper.ModuleStatusView;
import com.example.notekeeper.NoteActivityViewModel;
import com.example.notekeeper.notifications.NoteReminderReceiver;
import com.example.notekeeper.R;
import com.example.notekeeper.database.NoteKeeperOpenHelper;
import com.example.notekeeper.models.CourseInfo;
import com.example.notekeeper.models.NoteInfo;
import com.google.android.material.snackbar.Snackbar;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static String NOTE_ID = "com.example.notekeeper.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final String NOTE_URI = "com.jwhh.jim.notekeeper.NOTE_URI";
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    public final static String TAG = "NoteActivity";
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
    private ModuleStatusView mModuleStatusView;


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
        mModuleStatusView = (ModuleStatusView) findViewById( R.id.module_status );
        loadModuleStatusValues();


        //get reference to ViewModelProvider "same steps every time"
//        ViewModelProvider viewModelProvider = new ViewModelProvider( getViewModelStore(),
//                ViewModelProvider.AndroidViewModelFactory.getInstance( getApplication() ) );
//        mViewModel = viewModelProvider.get( NoteActivityViewModel.class );
//
//        if (mViewModel.isNewlyCreated && savedInstanceState != null) {
//            mViewModel.restoreState( savedInstanceState );
//        }
//
//        mViewModel.isNewlyCreated = false;


        readDisplayStateValues();
        if (savedInstanceState == null)
            saveOriginalValues();
        else {
            restoreOriginalValues(savedInstanceState);
        }

        if (!mIsNewNote) {
//            loadNoteData();
            LoaderManager.getInstance( this ).initLoader( NOTE_LOADER, null, this );
        }
    }

    private void loadModuleStatusValues() {
        int totalNumberOfModules = 11;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];
        for (int x= 0;x<completedNumberOfModules;x++){
            moduleStatus[x] = true;
        }
        mModuleStatusView.setModuleStatus( moduleStatus );
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

        CourseEventBroadcastHelper.sendEventBroadcast( this, courseId, "Editing Note" );
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
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress( progressValue );
            }


            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar) findViewById( R.id.progress_bar );
                mProgressBar.setProgress( 1 );
                mProgressBar.setVisibility( View.VISIBLE );
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {

                Log.d( TAG, "Call to doInBackground - thread: " + Thread.currentThread().getId() );
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert( Notes.CONTENT_URI, values );
                simulateLongRunningWork();
                publishProgress( 2 );
//                    mProgressBar.setProgress( 2 );

                simulateLongRunningWork();
                publishProgress( 3 );
//                mProgressBar.setProgress( 3 );


                return rowUri;
            }

            @Override
            protected void onPostExecute(Uri uri) {
                Log.d( TAG, "Call to PostExecute - thread: " + Thread.currentThread().getId() );
                displaySnackbar( uri.toString() );
                mNoteUri = uri;
                mProgressBar.setVisibility( View.GONE );
            }
        };


        Log.d( TAG, "Call to Exectue - thread: " + Thread.currentThread().getId() );
        task.execute( values );

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


    private void simulateLongRunningWork() {
        try {
            Thread.sleep( 2000 );
        } catch (Exception ex) {
        }
    }

    private void displaySnackbar(String message) {
        View view = findViewById( R.id.spinner_courses );
        Snackbar.make( view, message, Snackbar.LENGTH_LONG ).show();
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

        switch (id) {
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
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    private void showReminderNotification() {
        String noteTitle = mTitleNote.getText().toString();
        String noteText = mTextNote.getText().toString();
        int noteId = (int) ContentUris.parseId( mNoteUri );

        Intent intent = new Intent( this, NoteReminderReceiver.class );
        intent.putExtra( NoteReminderReceiver.EXTRA_NOTE_ID, noteId );
        intent.putExtra( NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle );
        intent.putExtra( NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT );

        AlarmManager alarmManager = (AlarmManager) this.getSystemService( ALARM_SERVICE );
        int currentTimeInMilliSeconds = (int) SystemClock.elapsedRealtime();
        int ONE_HOUR = 60 * 60 * 1000;
        int TEN_SECONDS = 10 * 1000;
        int alarmTime = TEN_SECONDS + currentTimeInMilliSeconds;
        alarmManager.set( AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent );


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
        saveOriginalValues();
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
               storePreviousNoteValues();
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


    private void restoreOriginalValues(Bundle savedInstanceState) {
//        mNote.setCourse( DataManager.getInstance().getCourse( mViewModel.mOriginalCourseId ) );
//        mNote.setTitle( mViewModel.mOriginalCourseTitle );
//        mNote.setText( mViewModel.mMOriginalCourseText );

        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
        String noteUri = savedInstanceState.getString( NOTE_URI );
        mNoteUri = Uri.parse( noteUri );
    }

    private void saveOriginalValues() {
        if (mIsNewNote)
            return;
//        mViewModel.mOriginalCourseId = mNote.getCourse().getCourseId();
//        mViewModel.mOriginalCourseTitle = mNote.getTitle();
//        mViewModel.mMOriginalCourseText = mNote.getText();

        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
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

        getContentResolver().update( mNoteUri, values, selection, selectionArgs );

//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//                db.update( NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs );
//                return null;
//            }
//        };
//        task.execute();
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
        outState.putString( ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId );
        outState.putString( ORIGINAL_NOTE_TITLE, mOriginalNoteTitle );
        outState.putString( ORIGINAL_NOTE_TEXT, mOriginalNoteText );
        outState.putString( NOTE_URI,mNoteUri.toString() );

//        if (outState != null) {
//            mViewModel.saveState( outState );
//        }
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
        mNoteCursor.moveToFirst();
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