package net.cyclestreets;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.cyclestreets.api.Blog;

import java.util.Timer;
import java.util.TimerTask;

public class BlogFragment
    extends WebPageFragment {
  private static final String CycleStreetsBlogUrl = "http://www.cyclestreets.net/blog/";

  public BlogFragment() {
    super(CycleStreetsBlogUrl);
  } // BlogFragment

  public View onCreateView(
      final LayoutInflater inflater,
      final ViewGroup container,
      final Bundle saved) {
    readUpdate(getActivity());
    return super.onCreateView(inflater, container, saved);
  } // onCreateView

  //////////////////////////////////////////////////////////////////////////////////
  public static MainNavDrawerActivity.PageTitle blogTitle(
      final Context context) {
    final Context appContext = context.getApplicationContext();
    start(appContext);

    final String blogTitle = appContext.getString(R.string.cyclestreets_blog);
    final String updateBlogTitle = appContext.getString(R.string.cyclestreets_blog_updates);

    return new MainNavDrawerActivity.PageTitle() {
      @Override
      public String title() {
        return updateAvailable(appContext) ? updateBlogTitle : blogTitle;
      } // title
    };
  } // blogTitle

  //////////////////////////////////////////////////////////////////////////////////
  private final static long oneMinute = 1000*60;
  private final static long aDay = 1000*60*60*24; // in milliseconds
  private final static long initialDelay = oneMinute;
  private final static long repeatPeriod = aDay;

  private static Timer updater_;

  private static void start(
      final Context context) {
    if (updater_ != null)
      return;
    updater_ = new Timer();
    updater_.schedule(new CheckBlogTask(context.getApplicationContext()), initialDelay, repeatPeriod);
  } // schedule

  private static boolean updateAvailable(
      final Context context) {
    return prefs(context).getBoolean(UPDATE_AVAILABLE, false);
  } // updateAvailable

  private static void readUpdate(
      final Context context) {
    final SharedPreferences.Editor edit = prefs(context).edit();
    edit.putBoolean(UPDATE_AVAILABLE, false);
    edit.commit();
  } // readUpdate

  private static final String LAST_DATE = "lastDate";
  private static final String UPDATE_AVAILABLE = "updateAvailable";

  private static String lastBlogUpdate(
      final Context context) {
    final SharedPreferences prefs = prefs(context);
    return prefs.getString(LAST_DATE, null);
  } // lastBlogUpdate

  private static void setLastBlogUpdate(
      final Context context,
      final String update) {
    final SharedPreferences.Editor edit = prefs(context).edit();
    edit.putString(LAST_DATE, update);
    edit.putBoolean(UPDATE_AVAILABLE, true);
    edit.commit();
  } // setLastBlogUpdate

  private static SharedPreferences prefs(
      final Context context) {
    return context.getSharedPreferences("net.cyclestreets.blog", Context.MODE_PRIVATE);
  } // prefs

  ////////////////////////////////////////////////
  private static class CheckBlogTask extends TimerTask {
    private final Context context_;

    private CheckBlogTask(
        final Context context) {
      context_ = context;
    } // CheckBlogTask

    public void run() {
      final Blog blog = Blog.load();

      // check for new blog entries
      if(blog.isNull())
        return;

      if(blog.mostRecent().equals(lastBlogUpdate(context_)))
        return;

      setLastBlogUpdate(context_, blog.mostRecent());
    } // run
  } // class CheckBlogTask
} // BlogFragment
