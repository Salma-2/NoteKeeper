package com.example.notekeeper;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.notekeeper.activities.NoteListActivity;
import com.example.notekeeper.models.CourseInfo;
import com.example.notekeeper.models.NoteInfo;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;
    @BeforeClass
    public static void classSetUp(){
        sDataManager= DataManager.getInstance();
    }
    //junit will know if we marked it @rule
    @Rule
    //where the test starts
    public ActivityTestRule<NoteListActivity> mNoteListActivityTestRule=
            new ActivityTestRule<>(NoteListActivity.class );

    @Test
    public void createNewNote(){
        final CourseInfo course = sDataManager.getCourse( "java_lang" );
        final String noteTitle = "Test note title";
        final String noteText= " This is the body text of text note";
//        ViewInteraction fabNewNote = onView( withId( R.id.fab ) );
//        fabNewNote.perform( click() );
        onView( withId( R.id.fab ) ).perform( click() );

        onView( withId( R.id.spinner_courses ) ).perform( click() );
        onData( allOf( instanceOf( CourseInfo.class ),equalTo( course ) )).perform( click() );
        onView( withId( R.id.spinner_courses ) ).check( matches( withSpinnerText(
                containsString( course.getTitle() ) ) ) );

        onView( withId( R.id.text_note_title )).perform( typeText( noteTitle) ).
                check(matches( withText( noteTitle ) ) );

        onView( withId( R.id.text_note_text )).perform( typeText( noteText ),
                closeSoftKeyboard() );
        onView( withId( R.id.text_note_text) ).check( matches( withText( noteText ) ) );

        pressBack();

        int noteIndex = sDataManager.getNotes().size()-1;
        NoteInfo note = sDataManager.getNotes().get( noteIndex );
        assertEquals( course,note.getCourse() );
        assertEquals( noteTitle,note.getTitle() );
        assertEquals( noteText,note.getText() );


    }
}