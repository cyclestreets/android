package net.cyclestreets;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@Ignore("Needs an emulator running - not practical in Travis CI at the moment")
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
