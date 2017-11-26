package arc.com.arctrails;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Caleigh
 * All activity tests for MenuActivity
 */
@RunWith(AndroidJUnit4.class)
public class MenuActivityTests {
    @Rule
    public ActivityTestRule<MenuActivity> mActivityRule =
            new ActivityTestRule<>(MenuActivity.class);
    @Test
    public void testTrailName() {
        // This is yelling at me when I try it :(
        onView(withId(R.id.TrailNameField)).perform(typeText("hollo"),closeSoftKeyboard());
    }
}