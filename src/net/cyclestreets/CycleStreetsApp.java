package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.planned.Route;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class CycleStreetsApp extends Application 
{
	@Override
	public void onCreate()
	{
	  CycleStreetsPreferences.initialise(this);
	    
	  Route.initialise(this);
	  ApiClient.loadSslCertificates(this);
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
