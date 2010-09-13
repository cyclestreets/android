package net.cyclestreets;

public interface CycleStreetsConstants {
    // API Key
    public final String API_KEY = "120175c44303728f";

    // Intent constants
    public static final int ACTIVITY_GET_ENDPOINTS = 1;
	public static final String EXTRA_PHOTO = "net.cyclestreets.extra.PHOTO";
	public static final String EXTRA_PLACE_FROM = "net.cyclestreets.extra.PLACE_FROM";
	public static final String EXTRA_PLACE_TO = "net.cyclestreets.extra.PLACE_TO";
	public static final String EXTRA_ROUTE = "net.cyclestreets.extra.ROUTE";

	// Route types 
    public final static String PLAN_BALANCED = "balanced";
    public final static String PLAN_FASTEST = "fastest";
    public final static String PLAN_QUIETEST = "quietest";
    public final static String PLAN_SHORTEST = "shortest";       

}
