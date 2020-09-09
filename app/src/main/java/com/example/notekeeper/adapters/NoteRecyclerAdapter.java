package com.example.notekeeper.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.NoteActivity;
import com.example.notekeeper.R;

import static com.example.notekeeper.contracts.NoteKeeperDatabaseContract.*;
import static com.example.notekeeper.R.id.text_course;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private String mCourseId;
    private String mNoteTitle;
    private int mNotePos;
    private int mCourseIdIndex;
    private int mNoteTitleIndex;
    private int mNoteIdIndex;
    private String mCourseTitle;
    private int mCourseTitleIndex;



    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
//        to create view from layout resource , we need to use LayoutInflater
        mLayoutInflater = LayoutInflater.from( mContext );
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (mCursor == null)
            return;
        mCourseTitleIndex = mCursor.getColumnIndex( CourseInfoEntry.COLUMN_COURSE_TITLE );
//        mCourseIdIndex = mCursor.getColumnIndex( NoteInfoEntry.COLUMN_COURSE_ID );
        mNoteTitleIndex = mCursor.getColumnIndex( NoteInfoEntry.COLUMN_NOTE_TITLE );
        mNoteIdIndex = mCursor.getColumnIndex( NoteInfoEntry._ID );
    }

    public void changeCursor(Cursor cursor){
        if(mCursor != null){
            //3mlt le de comment 3shan lma 3mlt loader fel main activity , lma bd5ol onResume
            // bygeb error (attempt to re-open an already-closed object)
            //onLoaderReset --> by2fl el cursor lw7do
            //mCursor.close();
        }
        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }
    @NonNull
    @Override

    //create views
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
          parent --> ViewGrouped that will be inflated within
          false --> don't want to have this newly inflated view automatically
          attached to its parent , instead we're going to do this through the adapter
          and recyclerView cooperation
          itemView--> points to the root of the view that's created when
          when item_note_list is inflated
         */
        View itemView = mLayoutInflater.inflate( R.layout.item_note_list, parent, false );
        return new ViewHolder( itemView );
    }

    /*
     * Receive view from ViewHolder and position of the data
     * set data in the ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition( position );
        loadNoteData();
        holder.mTextCourse.setText( mCourseTitle);
        holder.mTextTitle.setText( mNoteTitle );
        holder.mCurrentId = mNotePos;
    }

    private void loadNoteData() {

        mCourseTitle = mCursor.getString( mCourseTitleIndex );
//        mCourseId = mCursor.getString( mCourseIdIndex );
        mNoteTitle = mCursor.getString( mNoteTitleIndex );
        mNotePos = mCursor.getInt(mNoteIdIndex);
    }


    /**
     * @return total number of data items
     */
    @Override
    public int getItemCount() {
        return mCursor == null ? 0: mCursor.getCount();
    }

    //Viewholder --> Reference le view
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurrentId;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            mTextCourse = (TextView) itemView.findViewById( text_course );
            mTextTitle = (TextView) itemView.findViewById( R.id.text_title );


            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent( mContext, NoteActivity.class );
                    intent.putExtra( NoteActivity.NOTE_ID, mCurrentId );
                    mContext.startActivity( intent );
                }
            } );
        }

    }
}
