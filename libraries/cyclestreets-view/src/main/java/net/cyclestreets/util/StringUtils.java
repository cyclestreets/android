package net.cyclestreets.util;

public class StringUtils {
  public static String initCap(String s) {
    if (s == null)
      return "";
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  private StringUtils() { }
} // class StringUtils
