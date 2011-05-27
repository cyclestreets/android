package uk.org.invisibility.cycloid;

public class CycloidConstants
{
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
    public static final String GEO_SEARCH = "search";   
    public static final String GEO_TYPE = "type";
}
