package uk.org.invisibility.cycloid;

public class CycloidConstants
{
	public static final String LOGTAG = "CYCLOID";
	
	// Default map type
	public static final String DEFAULT_MAPTYPE = "CycleMap";
	
	// Preference strings for application
    public static final String PREFS_APP_KEY = "uk.org.invisibility.cycloid";
    public static final String PREFS_APP_RENDERER = "renderer";
    public static final String PREFS_APP_SCROLL_X = "scrollX";
    public static final String PREFS_APP_SCROLL_Y = "scrollY";
    public static final String PREFS_APP_ZOOM_LEVEL = "zoomLevel";
    public static final String PREFS_APP_FOLLOW_LOCATION = "followLocation";

	// Preference strings for geocoding
    public static final String PREFS_GEO_KEY = "uk.org.invisibility.cycloid.geocode";
    public static final String PREFS_GEO_NAME_PREFIX = "name/";
    public static final String PREFS_GEO_NEAR_PREFIX = "near/";
    public static final String PREFS_GEO_LATITUDE_PREFIX = "lat/";
    public static final String PREFS_GEO_LONGITUDE_PREFIX = "lon/";
    
    // Magic string
    public static final String MY_LOCATION = "My Location";
    
    // Intent request codes
    public static final int GEO_REQUEST_FROM = 1;
    public static final int GEO_REQUEST_TO = 2;
    
    // Intent extras
    public static final String GEO_NEAR = "near";    
    public static final String GEO_LATITUDE = "latitude";
    public static final String GEO_LONGITUDE = "longitude";
    public static final String GEO_SEARCH = "search";   
    public static final String GEO_TYPE = "type";
}
