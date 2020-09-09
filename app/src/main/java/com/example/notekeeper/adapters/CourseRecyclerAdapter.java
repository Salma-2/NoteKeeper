package com.example.notekeeper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.R;
import com.example.notekeeper.models.CourseInfo;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.example.notekeeper.R.id.text_course;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder> {
    private final Context mContext;
    private final List<CourseInfo> mCourses;
    private final LayoutInflater mLayoutInflater;


    public CourseRecyclerAdapter(Context context, List<CourseInfo> course) {
        mContext = context;
        mCourses = course;
//        to create view from layout resource , we need to use LayoutInflater
        mLayoutInflater = LayoutInflater.from( mContext );
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
        View itemView = mLayoutInflater.inflate( R.layout.item_course_list, parent, false );
        return new ViewHolder( itemView );
    }

    /*
     * Receive view from ViewHolder and position of the data
     * set data in the ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseInfo course = mCourses.get( position );
        holder.mTextCourse.setText( course.getTitle() );
        holder.mCurrentPosition = position;

    }

    /**
     * @return total number of data items
     */
    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    //Viewholder --> Reference le view
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public int mCurrentPosition;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            mTextCourse = (TextView) itemView.findViewById( text_course );


            itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make( v,mCourses.get( mCurrentPosition ).getTitle(),
                            Snackbar.LENGTH_LONG ).show();
                }
            } );
        }

    }
}
