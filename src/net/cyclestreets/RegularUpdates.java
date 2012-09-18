package net.cyclestreets;

import java.util.Timer;
import java.util.TimerTask;

import net.cyclestreets.api.Blog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

class RegularUpdates
{
  private static Timer updater_;

  static public void schedule(final Context context, final long delay, final long period) 
  {
    updater_ = new Timer();
    updater_.schedule(new RegularUpdatesTask(context), delay, period);
  } // schedule
  
  ////////////////////////////////////////////////
  static private class RegularUpdatesTask extends TimerTask
  {
    private final Context context_;
  
    private RegularUpdatesTask(final Context context)
    {
      context_ = context;
    } // RegularUpdates
  
    public void run() 
    {
      final Blog blog = Blog.load();
    
      // check for new blog entries
      if(blog == null)
        return;
      
      if(blog.mostRecent().equals(lastBlogUpdate()))
        return;
      
      setLastBlogUpdate(blog.mostRecent());
  
      notify(s(R.string.blog_cyclestreets_update), blog.mostRecentTitle(), s(R.string.blog_update), BlogActivity.class);
    } // run
  
    private <T> void notify(final String title, final String text, final String ticker, final Class<T> tap)
    {
      final NotificationManager nm = (NotificationManager)context_.getSystemService(Context.NOTIFICATION_SERVICE);
      final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
      notification.flags = Notification.FLAG_AUTO_CANCEL;
      final Intent notificationIntent = new Intent(context_, tap);
      final PendingIntent contentIntent = 
           PendingIntent.getActivity(context_, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
      notification.setLatestEventInfo(context_, title, text, contentIntent);
      nm.notify(1, notification);
    } // notify
    
    private String s(final int id)
    {
      return context_.getResources().getString(id);
    } // s
    
    private final String LAST_DATE = "lastDate";
    
    private String lastBlogUpdate()
    {
      final SharedPreferences prefs = prefs();
      return prefs.getString(LAST_DATE, null);
    } // lastBlogUpdate
    
    private void setLastBlogUpdate(final String update)
    {
      final SharedPreferences.Editor edit = prefs().edit();
      edit.putString(LAST_DATE, update);
      edit.commit();
    } // setLastBlogUpdate
    
    private SharedPreferences prefs()
    {
      return context_.getSharedPreferences("net.cyclestreets.app", Context.MODE_PRIVATE);
    } // prefs
  } // class RegularUpdatesTask
} // class RegularUpdates


