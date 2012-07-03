package net.cyclestreets;

import net.cyclestreets.util.MapPack;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class CycleStreets extends TabActivity 
						  implements OnTabChangeListener 
{
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

        // initialize objects
	    final Resources res = getResources();
	    final TabHost tabHost = getTabHost();
	    tabHost.setOnTabChangedListener(this);
	    TabHost.TabSpec spec;
	    
	    // Plan route
	    spec = tabHost.newTabSpec("Route Map").setIndicator("", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(new Intent(this, RouteMapActivity.class));
	    tabHost.addTab(spec);

	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("", res.getDrawable(R.drawable.ic_tab_itinerary));
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    tabHost.addTab(spec);

	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("", res.getDrawable(R.drawable.ic_tab_photomap));
	    spec.setContent(new Intent(this, PhotomapActivity.class));
	    tabHost.addTab(spec);

	    // Add photo
	    spec = tabHost.newTabSpec("Photo upload").setIndicator("", res.getDrawable(R.drawable.ic_tab_addphoto));
	    spec.setContent(new Intent(this, AddPhotoActivity.class));
	    tabHost.addTab(spec);

	    spec = tabHost.newTabSpec("More ...").setIndicator("", res.getDrawable(R.drawable.ic_tab_more));
	    spec.setContent(new Intent(this, MoreActivity.class));
	    tabHost.addTab(spec);
	    
	    for(int i = 0; i != 5; ++i)
	    {
	    	final ViewGroup.LayoutParams layout = tabHost.getTabWidget().getChildAt(i).getLayoutParams();
		    layout.height = (int)(layout.height*0.66);
		    tabHost.getTabWidget().getChildAt(i).setLayoutParams(layout);
	    } // for ...
	    
	    switchMapFile();
	    
	    // start with route tab
	    showMap();

	    showWhatsNew();
	} // onCreate
	
	private void switchMapFile()
	{
	  final String mappackage = getIntent().getStringExtra("mapfile");
	  if(mappackage == null)
	    return;
	  final MapPack pack = MapPack.findByPackage(mappackage);
	  if(pack == null)
	    return;
	  CycleStreetsPreferences.enableMapFile(pack.path());
	} // switchMapFile
	
	public void showMap()
	{
		getTabHost().setCurrentTab(0);
	} // showMap

	@Override
	public void onTabChanged(String tabId) 
	{
		setTitle("CycleStreets : " + tabId);
	} // onTabChanged

	public void showWhatsNew()
	{
    if(!isNew())
      return;
      
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString(VERSION_KEY, currentVersion());
    edit.commit();

    final LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View whatsnewView = layoutInflater.inflate(R.layout.whatsnew, null);
    final WebView htmlView = (WebView)whatsnewView.findViewById(R.id.html_view);
    htmlView.loadUrl("file:///android_asset/whatsnew.html");
        
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("What's New")
           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { }
            })
           .setView(whatsnewView)
           .show();
	} // showWhatsNew
	
	private boolean isNew()
	{
	  return !currentVersion().equals(previousVersion());
	} // isNew

	private String currentVersion()
	{
	  return ((CycleStreetsApp)getApplication()).version();
	} // currentVersion
	
	private String previousVersion()
	{
    return prefs().getString(VERSION_KEY, "unknown");
	} // previousVersion
	
  private SharedPreferences prefs()
  {
    return getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  } // prefs()
  
  static private String VERSION_KEY = "previous-version";
} // class CycleStreets

