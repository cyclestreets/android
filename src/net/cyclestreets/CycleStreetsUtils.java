package net.cyclestreets;

public class CycleStreetsUtils {
	public static final int DEFAULT_MAXLEN = 100;
	
	public static String truncate(String s, int maxlen) {
		if (s.length() <= maxlen) {
			return s;
		}
		return s.substring(0, maxlen-3) + "...";
	}
	
	public static String truncate(String s) {
		return truncate(s, DEFAULT_MAXLEN);
	}
}
