package net.cyclestreets;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppStartupTest {

  @Rule
  public ActivityTestRule<CycleStreets> mActivityRule = new ActivityTestRule<>(CycleStreets.class);

  @Test
  public void testAppStartup() throws Exception {
    System.out.println("I've started up without crashing.");
    Thread.sleep(240000L);
  }
}
