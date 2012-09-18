package net.cyclestreets;


import net.cyclestreets.api.ApiClient;
import net.cyclestreets.planned.Route;
import android.app.Application;
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
	 
	  RegularUpdates.schedule(this, oneMinute, aDay);
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
  
} // CycleStreetsApp
