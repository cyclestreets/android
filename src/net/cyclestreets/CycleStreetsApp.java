package net.cyclestreets;

import java.util.Timer;
import java.util.TimerTask;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.Blog;
import net.cyclestreets.planned.Route;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey="dHBMQkk3aldWaW8tYlA0eVMzQ0ltQ2c6MQ",             
                mode = ReportingInteractionMode.NOTIFICATION,
                resNotifTickerText = R.string.crash_notif_ticker_text,
                resNotifTitle = R.string.crash_notif_title,
                resNotifText = R.string.crash_notif_text,
                resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
                resDialogText = R.string.crash_dialog_text,
                resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
                resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
                resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
                )
public class CycleStreetsApp extends Application 
{
  private static Timer updater_;
  private final static int oneMinute = 1000*60;
  private final static int aDay = 1000*60*60*24; // in milliseconds
  
	@Override
	public void onCreate()
	{
	  ACRA.init(this);
	  super.onCreate();
	  
	  CycleStreetsPreferences.initialise(this);
	    
	  Route.initialise(this);
	  ApiClient.initialise(this);
	      
    updater_ = new Timer();
    updater_.schedule(new RegularUpdates(this), oneMinute, aDay);
	} // onCreate
	
	public String version()
	{
	  return String.format("Version : %s/%s", getPackageName(), versionName());
	} // version
	
  private String versionName() 
  {
    try {
      final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      return info.versionName;
    } // try
    catch(PackageManager.NameNotFoundException nnfe) {
      // like this is going to happen    
      return "Unknown";
    } // catch
  } // versionName
  
  static private class RegularUpdates extends TimerTask
  {
    private final Context context_;
    
    RegularUpdates(final Context context)
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
  } // RegularUpdates


} // CycleStreetsApp
