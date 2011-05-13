package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.planned.Route;
import android.app.Application;

public class CycleStreetsApp extends Application 
{
	@Override
	public void onCreate()
	{
	    CycleStreetsPreferences.initialise(this);
	    
	    Route.initialise(this);
	    ApiClient.loadSslCertificates(this);
	} // onCreate
} // CycleStreetsApp
