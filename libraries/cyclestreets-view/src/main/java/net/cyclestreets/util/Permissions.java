package net.cyclestreets.util;

import android.app.Activity;
import android.content.pm.PackageManager;

public class Permissions {
  public static boolean verify(final Activity activity, final String permission) {
    if (!hasPermssion(activity, permission))
      activity.requestPermissions(new String[]{ permission }, 1);
    return hasPermssion(activity, permission);
  } // verify

  private static boolean hasPermssion(final Activity activity, final String permission) {
    return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  } // hasPermission
} // class Permissions
