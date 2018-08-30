package net.cyclestreets.util;

public class Logging {

  public static String getTag(Class clazz) {
    return clazz.getCanonicalName().replace("net.cyclestreets.", "");
  }

}
