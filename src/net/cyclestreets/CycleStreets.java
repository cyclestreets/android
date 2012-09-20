package net.cyclestreets;

import java.util.HashMap;
import java.util.Map;

import net.cyclestreets.util.MapPack;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class CycleStreets extends FragmentActivity implements OnTabChangeListener, TabHost.TabContentFactory 
{
  private TabHost tabHost_;
  private final Map<String, TabInfo> tabs_ = new HashMap<String, TabInfo>();
  private TabInfo lastTab_;
  
	public void onCreate(final Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      tabHost_ = (TabHost)findViewById(android.R.id.tabhost);
      tabHost_.setup();
      tabHost_.setOnTabChangedListener(this);

      addTab("Photo upload", R.drawable.ic_tab_addphoto, PhotoUploadFragment.class);
      addTab("More ...", R.drawable.ic_tab_more, MoreFragment.class);

	    // initialize objects
/*	    
	    // Plan route
	    spec = tabHost.newTabSpec("Route Map").setIndicator("", res.getDrawable(R.drawable.ic_tab_planroute));
	    spec.setContent(new Intent(this, RouteMapActivity.class));
	    
	    // Itinerary
	    spec = tabHost.newTabSpec("Itinerary").setIndicator("", res.getDrawable(R.drawable.ic_tab_itinerary));
	    spec.setContent(new Intent(this, ItineraryActivity.class));
	    
	    // Photomap
	    spec = tabHost.newTabSpec("Photomap").setIndicator("", res.getDrawable(R.drawable.ic_tab_photomap));
	    spec.setContent(new Intent(this, PhotomapActivity.class));
	    
*/
	    
	    for(int i = 0; i != tabs_.size(); ++i)
	    {
	    	final ViewGroup.LayoutParams layout = tabHost_.getTabWidget().getChildAt(i).getLayoutParams();
		    layout.height = (int)(layout.height*0.66);
		    tabHost_.getTabWidget().getChildAt(i).setLayoutParams(layout);
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
		tabHost_.setCurrentTab(0);
	} // showMap

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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Tab handling
  static final class TabInfo 
  {
    private final String tag;
    private final Class<? extends Fragment> clss;
    private final Bundle args;
    private Fragment fragment;

    TabInfo(final String t, Class<? extends Fragment> fc, final Bundle a) 
    {
      tag = t;
      clss = fc;
      args = a;
    } // TabInfo
  }  // class TabInfo

  @Override
  public View createTabContent(String tag) 
  {
    final View v = new View(this);
    v.setMinimumWidth(0);
    v.setMinimumHeight(0);
    return v;
  } // createTabContent

  private void addTab(final String tabId, final int iconId, final Class<? extends Fragment> fragClass) 
  {
    final TabSpec tabSpec = tabHost_.newTabSpec(tabId);
    tabSpec.setIndicator("", getResources().getDrawable(iconId));
    tabSpec.setContent(this);

    final TabInfo info = new TabInfo(tabId, fragClass, null);

    // Check to see if we already have a fragment for this tab, probably
    // from a previously saved state.  If so, deactivate it, because our
    // initial state is that a tab isn't shown.
    info.fragment = getSupportFragmentManager().findFragmentByTag(tabId);
    if (info.fragment != null && !info.fragment.isDetached()) 
    {
      final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.detach(info.fragment);
      ft.commit();
    } // if

    tabs_.put(tabId, info);
    tabHost_.addTab(tabSpec);
  } // addTab

  @Override
  public void onTabChanged(String tabId) 
  {
    final TabInfo newTab = tabs_.get(tabId);
    if(lastTab_ == newTab) 
      return;

    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    if(lastTab_ != null && lastTab_.fragment != null) 
      ft.detach(lastTab_.fragment);

    if(newTab != null) 
    {
       if(newTab.fragment == null) 
       {
         newTab.fragment = Fragment.instantiate(this,
                                                newTab.clss.getName(), 
                                                newTab.args);
         ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
       }
       else 
       {
         ft.attach(newTab.fragment);
       }
    }

    lastTab_ = newTab;
    ft.commit();
    getSupportFragmentManager().executePendingTransactions();
    
    setTitle("CycleStreets : " + tabId);
  } // onTabChanged
} // class CycleStreets

