package com.example.notekeeper;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    public static final String ORIGINAL_COURSE_ID = "androidx.lifecycle.ViewModel" +
            ".ORIGINAL_COURSE_ID";
    public static final String ORIGINAL_COURSE_TITLE = "androidx.lifecycle.ViewModel" +
            ".ORIGINAL_COURSE_TITLE";
    public static final String ORIGINAL_COURSE_TEXT = "androidx.lifecycle.ViewModel" +
            ".ORIGINAL_COURSE_TEXT";
    public String mOriginalCourseId;
    public String mOriginalCourseTitle;
    public String mMOriginalCourseText;
    public boolean isNewlyCreated = true;


    public void saveState(Bundle outState) {
        outState.putString( ORIGINAL_COURSE_ID, mOriginalCourseId );
        outState.putString( ORIGINAL_COURSE_TITLE, mOriginalCourseTitle );
        outState.putString( ORIGINAL_COURSE_TEXT, mMOriginalCourseText );
    }

    public void restoreState(Bundle inState) {
        mOriginalCourseId = inState.getString( ORIGINAL_COURSE_ID );
        mOriginalCourseTitle = inState.getString( ORIGINAL_COURSE_TITLE );
        mMOriginalCourseText = inState.getString( ORIGINAL_COURSE_TEXT );
    }
}
