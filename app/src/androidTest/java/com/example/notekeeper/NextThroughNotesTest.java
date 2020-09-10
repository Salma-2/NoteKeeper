package com.example.notekeeper;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.notekeeper.activities.MainActivity;
import com.example.notekeeper.models.NoteInfo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule =
            new ActivityTestRule<>( MainActivity.class );


    @Test
    public void NextThroughNotesTest() {
        NoteInfo note;
        onView( withId( R.id.drawer_layout ) ).perform( DrawerActions.open() );
        onView( withId( R.id.nav_view ) ).perform( NavigationViewActions.navigateTo( R.id.nav_notes ) );

        onView( withId( R.id.item_list ) ).perform( RecyclerViewActions.actionOnItemAtPosition( 0
                , click() ) );


        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        for (int index = 0; index < notes.size(); index++) {
            note = notes.get( index );

            onView( withId( R.id.spinner_courses ) ).check( matches( withSpinnerText( note.getCourse().getTitle() ) ) );
            onView( withId( R.id.text_note_title ) ).check( matches( withText( note.getTitle() ) ) );
            onView( withId( R.id.text_note_text ) ).check( matches( withText( note.getText() ) ) );

            if (index < notes.size() - 1)
                onView( allOf( withId( R.id.action_next ), isEnabled() ) ).perform( click() );

        }

        onView( withId( R.id.action_next ) ).check( matches( not( isEnabled() ) ) );
        pressBack();


    }

}