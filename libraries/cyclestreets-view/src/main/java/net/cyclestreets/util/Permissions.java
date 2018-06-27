package net.cyclestreets.util;

import android.app.Activity;
import android.content.pm.PackageManager;

public class Permissions {
  public static boolean verify(final Activity activity, final String permission) {
    if (!hasPermission(activity, permission))
      activity.requestPermissions(new String[]{ permission }, 1);
    return hasPermission(activity, permission);
  }

  private static boolean hasPermission(final Activity activity, final String permission) {
    return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }
}
