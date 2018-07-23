package net.cyclestreets.api;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Filter;

import org.osmdroid.util.BoundingBox;

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
  private final BoundingBox bounds;
  private final SharedPreferences prefs;

  /*
   * Constructor when used with an AutoCompleteTextView
   */
  public GeoLiveAdapter(final Context context,
                        final BoundingBox bounds) {
    super(context);
    this.bounds = bounds;

    prefs = context.getSharedPreferences(PREFS_GEO_KEY, Application.MODE_PRIVATE);
    filter = new GeocodeFilter(prefs);
  }

  @Override
  public Filter getFilter() {
    return filter;
  }

  public BoundingBox bounds() { return bounds; }

  public GeoPlace exactMatch(final String p) {
    for (int i = 0; i != getCount(); ++i) {
      final GeoPlace gp = getItem(i);
      if (p.equals(gp.toString()))
        return gp;
    }
    return null;
  }

  /*
   * Add to geocoding history
   */
  public void addHistory(final GeoPlace p) {
    if (p.name().equals(MY_LOCATION))
      return;

    final String key = p.name().toLowerCase();

    final SharedPreferences.Editor edit = prefs.edit();
    edit.putString(PREFS_GEO_NAME_PREFIX + key, p.name());
    edit.putString(PREFS_GEO_NEAR_PREFIX + key, p.near());
    edit.putInt(PREFS_GEO_LATITUDE_PREFIX + key, (int)(p.coord().getLatitude() * 1e6));
    edit.putInt(PREFS_GEO_LONGITUDE_PREFIX + key, (int)(p.coord().getLongitude() * 1e6));
    edit.apply();
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  private class GeocodeFilter extends Filter  {
    private SharedPreferences prefs;

    public GeocodeFilter(final SharedPreferences prefs) {
      this.prefs = prefs;
    }

    @Override
    protected FilterResults performFiltering(CharSequence cs) {
      final List<GeoPlace> list = new ArrayList<>();

      if (cs != null) {
        // Add history hits first
        filterPrefs(list, cs);

        // Only geocode if more than two characters
        if (cs.length() > 2)
          list.addAll(geoCode(cs.toString(), bounds).asList());
      }
      else  {
        // Add all prefs
        filterPrefs(list, "");
      }

      final FilterResults results = new FilterResults();
      results.values = list;
      results.count = list.size();
      return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence cs, FilterResults fr) {
      clear();

      if (fr != null && fr.values != null)
        addAll((List<GeoPlace>)fr.values);

      notifyDataSetChanged();
    }

    /*
     * Add any matching entries from prefs
     */
    private void filterPrefs(final List<GeoPlace> list,
                             final CharSequence cs) {
      if (prefs == null)
        return;

      final String match = (PREFS_GEO_NAME_PREFIX + cs).toLowerCase();
      final Set<String> sortedKeys = new TreeSet<>(prefs.getAll().keySet());

      for (final String s: sortedKeys) {
        if (!s.startsWith(match))
          continue;

        final String key = prefs.getString(s, "").toLowerCase();

        list.add(new GeoPlace(prefs.getInt(PREFS_GEO_LATITUDE_PREFIX + key, 0),
                              prefs.getInt(PREFS_GEO_LONGITUDE_PREFIX + key, 0),
                              prefs.getString(PREFS_GEO_NAME_PREFIX + key, ""),
                              prefs.getString(PREFS_GEO_NEAR_PREFIX + key, "")));
      }
    }
  }
}
