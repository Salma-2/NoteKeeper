package com.example.notekeeper.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.notekeeper.BuildConfig;
import com.example.notekeeper.DataManager;
import com.example.notekeeper.R;
import com.example.notekeeper.adapters.CourseRecyclerAdapter;
import com.example.notekeeper.adapters.NoteRecyclerAdapter;
import com.example.notekeeper.database.NoteKeeperOpenHelper;
import com.example.notekeeper.models.CourseInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.contracts.NoteKeeperProviderContract.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final int NOTE_LOADER = 0;
    private AppBarConfiguration mAppBarConfiguration;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerNotes;
    private LinearLayoutManager mNotesLayoutManager;
    private NavigationView mNavigationView;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private FloatingActionButton mFab;
    private SharedPreferences mPref;
    private NoteKeeperOpenHelper mOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        enableStrictMode();


        mOpenHelper = new NoteKeeperOpenHelper( this );

        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mFab = findViewById( R.id.fab );
        mFab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( MainActivity.this, NoteActivity.class ) );
            }
        } );


        //strings for accessibility
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar,
                R.string.open_nav, R.string.close_nav );

        drawer.setDrawerListener( toggle );
        toggle.syncState();

        mNavigationView = findViewById( R.id.nav_view );
        mNavigationView.setNavigationItemSelectedListener( this );

        initializeDisplayContent();
    }

    private void enableStrictMode() {
        if(BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();
            StrictMode.setThreadPolicy( policy );
        }
    }

    @Override
    protected void onDestroy() {
        mOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mAdapterNotes.notifyDataSetChanged();
//        mNoteRecyclerAdapter.notifyDataSetChanged();
//        loadNotes();
        LoaderManager.getInstance( this ).restartLoader( NOTE_LOADER, null, this );

        updateHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
        };
        final String noteOrderBy =
                NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query( NoteInfoEntry.TABLE_NAME,
                noteColumns,
                null, null, null, null, noteOrderBy );
        mNoteRecyclerAdapter.changeCursor( noteCursor );
    }

    private void updateHeader() {

        View headerView = mNavigationView.getHeaderView( 0 );
        TextView textName = headerView.findViewById( R.id.user_name );
        TextView textEmail = headerView.findViewById( R.id.user_email );

        mPref = PreferenceManager.getDefaultSharedPreferences( this );
        String userName = mPref.getString(
                "user_display_name",
                getString( R.string.pref_user_display_name_title ) );
        String userEmail = mPref.getString(
                "user_email_address",
                getString( R.string.pref_user_email_address_value ) );

        textName.setText( userName );
        textEmail.setText( userEmail );


    }

    private void initializeDisplayContent() {
//        final ListView listNotes = findViewById(R.id.list_notes);
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        mAdapterNotes = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, notes);
//
//        listNotes.setAdapter(mAdapterNotes);
//
//        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
////                NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
//                intent.putExtra(NoteActivity.NOTE_POSITION, position);
//                startActivity(intent);
//            }
//        });

        DataManager.getInstance().loadFromDatabase( mOpenHelper );

        mRecyclerNotes = findViewById( R.id.item_list );

        mCoursesLayoutManager = new GridLayoutManager( this,
                getResources().getInteger( R.integer.course_grid_span ) );
        mNotesLayoutManager = new LinearLayoutManager( this );

        final List<CourseInfo> courses = DataManager.getInstance().getCourses();

        mCourseRecyclerAdapter = new CourseRecyclerAdapter( this, courses );

        mNoteRecyclerAdapter = new NoteRecyclerAdapter( this, null );

        displayNotes();
    }

    private void displayNotes() {
        mRecyclerNotes.setLayoutManager( mNotesLayoutManager );
        mRecyclerNotes.setAdapter( mNoteRecyclerAdapter );

        selectNavigationMenuItem( R.id.nav_notes );

    }

    private void selectNavigationMenuItem(int id) {
        Menu menu = mNavigationView.getMenu();
        menu.findItem( id ).setChecked( true );
    }

    private void displayCourses() {

        mRecyclerNotes.setLayoutManager( mCoursesLayoutManager );
        mRecyclerNotes.setAdapter( mCourseRecyclerAdapter );

        selectNavigationMenuItem( R.id.nav_courses );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity( new Intent( this, SettingsActivity.class ) );
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
//            handleSelect( R.string.nav_share_message );
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelect( R.string.nav_send_message );
        }
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    private void handleShare() {
        View view = findViewById( R.id.item_list );

        Snackbar.make( view, "Share To " + mPref.getString( "user_fav_social", "Not set" ),
                Snackbar.LENGTH_SHORT ).show();
    }

    private void handleSelect(int messageId) {
        View view = findViewById( R.id.item_list );
        Snackbar.make( view, messageId, Snackbar.LENGTH_SHORT ).show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == NOTE_LOADER)
            loader = createLoaderNotes();
        return loader;
    }

    private CursorLoader createLoaderNotes() {
        CursorLoader loader = null;
        final String[] noteColumns = {
                Notes._ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_COURSE_TITLE,
        };
        final String noteOrderBy =
                Notes.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;

        loader = new CursorLoader( this, Notes.CONTENT_EXPANDED_URI,
                noteColumns, null, null,
                noteOrderBy );
        return loader;
//        return new CursorLoader( this ) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
//                final String[] noteColumns = {
//                        NoteInfoEntry.getQName( NoteInfoEntry._ID ),
//                        NoteInfoEntry.COLUMN_NOTE_TITLE,
//                        CourseInfoEntry.COLUMN_COURSE_TITLE,
//                };
//                final String noteOrderBy =
//                        CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
//
//                // note_info JOIN course_info ON note_info.course_id = course_info.course_id
//                final String tablesWithJoin =
//                        NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON "
//                                + NoteInfoEntry.getQName( NoteInfoEntry.COLUMN_COURSE_ID )
//                                + " = "
//                                + CourseInfoEntry.getQName( CourseInfoEntry.COLUMN_COURSE_ID );
//
//                return db.query( tablesWithJoin,
//                        noteColumns,
//                        null, null, null, null, noteOrderBy );
//            }
//        };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == NOTE_LOADER) {
            mNoteRecyclerAdapter.changeCursor( data );
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == NOTE_LOADER) {
            mNoteRecyclerAdapter.changeCursor( null );
        }

    }
}





