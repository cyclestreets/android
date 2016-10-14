package net.cyclestreets.api;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Filter;

import org.osmdroid.util.BoundingBoxE6;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GeoLiveAdapter extends GeoAdapter 
{
  private static final String PREFS_GEO_KEY = "net.cyclestreets.api.GeoLiveAdapter";
  private static final String PREFS_GEO_NAME_PREFIX = "name/";
  private static final String PREFS_GEO_NEAR_PREFIX = "near/";
  private static final String PREFS_GEO_LATITUDE_PREFIX = "lat/";
  private static final String PREFS_GEO_LONGITUDE_PREFIX = "lon/";

  // Magic string
  public static final String MY_LOCATION = "My Location";
	
  private final GeocodeFilter filter;
  private final BoundingBoxE6 bounds_;
  private final SharedPreferences prefs;

  /*
   * Constructor when used with an AutoCompleteTextView
   */
  public GeoLiveAdapter(final Context context,
                        final BoundingBoxE6 bounds)
  {
    super(context);
    bounds_ = bounds;
    
    prefs = context.getSharedPreferences(PREFS_GEO_KEY, Application.MODE_PRIVATE);
    filter = new GeocodeFilter(prefs);
  } // GeoAdapter

  @Override
  public Filter getFilter()
  {
    return filter;
  } // getFilter
	
  public BoundingBoxE6 bounds() { return bounds_; }
  
  public GeoPlace exactMatch(final String p)
  {
    for(int i = 0; i != getCount(); ++i)
    {
      final GeoPlace gp = getItem(i);
      if(p.equals(gp.toString()))
        return gp;
    } // for ...
    return null;
  } // exactMatch
	
  /*
   * Add to geocoding history
   */
  public void addHistory(final GeoPlace p)
  {
    if (p.name().equals(MY_LOCATION))
      return;
		
    final String key = p.name().toLowerCase();
    
    final SharedPreferences.Editor edit = prefs.edit();
    edit.putString(PREFS_GEO_NAME_PREFIX + key, p.name());
    edit.putString(PREFS_GEO_NEAR_PREFIX + key, p.near());
    edit.putInt(PREFS_GEO_LATITUDE_PREFIX + key, p.coord().getLatitudeE6());
    edit.putInt(PREFS_GEO_LONGITUDE_PREFIX + key, p.coord().getLongitudeE6());
    edit.commit();
  } // addHistory
	
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  private class GeocodeFilter extends Filter
  {
    private SharedPreferences prefs_;
    
    public GeocodeFilter(final SharedPreferences prefs)
    {
      prefs_ = prefs;
    } // GeocodeFilter
		
    @Override
    protected FilterResults performFiltering(CharSequence cs)
    {
      final List<GeoPlace> list = new ArrayList<>();
      
      if (cs != null)
      {
        // Add history hits first
        filterPrefs(list, cs);
        
        // Only geocode if more than two characters
        if (cs.length() > 2)
          list.addAll(geoCode(cs.toString(), bounds_).asList());
      }
      else
      {
        // Add all prefs
        filterPrefs(list, "");
      }
      
      final FilterResults results = new FilterResults();
      results.values = list;
      results.count = list.size();
      return results;
    } // performFiltering
		
    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence cs, FilterResults fr)
    {
      clear();

      if (fr != null && fr.values != null)
        addAll((List<GeoPlace>)fr.values);
      
      notifyDataSetChanged();
    } // publishResults
    
    /*
     * Add any matching entries from prefs
     */
    private void filterPrefs(final List<GeoPlace> list, 
                             final CharSequence cs)
    {
      if (prefs_ == null)
        return;
			
      final String match = (PREFS_GEO_NAME_PREFIX + cs).toLowerCase();
      final Set<String> sortedKeys = new TreeSet<>(prefs.getAll().keySet());
		
      for (final String s: sortedKeys)
      {		
        if (!s.startsWith(match))
          continue;
        
        final String key = prefs.getString(s, "").toLowerCase();
        
        list.add(new GeoPlace(prefs.getInt(PREFS_GEO_LATITUDE_PREFIX + key, 0),
                              prefs.getInt(PREFS_GEO_LONGITUDE_PREFIX + key, 0),
                              prefs.getString(PREFS_GEO_NAME_PREFIX + key, ""),
                              prefs.getString(PREFS_GEO_NEAR_PREFIX + key, "")));
      } // for ...
    } // filterPrefs
  } // class GeocodeFilter
} // class GeoAdapter
