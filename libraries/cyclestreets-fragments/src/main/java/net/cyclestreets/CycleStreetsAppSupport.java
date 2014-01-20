package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.routing.Route;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class CycleStreetsAppSupport
{
  private final static int oneMinute = 1000*60;
  private final static int halfADay = 1000*60*60*12; // in milliseconds

  static public void initialise(final Context context)
  {
    CycleStreetsPreferences.initialise(context);

    Route.initialise(context);
    ApiClient.initialise(context);

    RegularUpdates.schedule(context, oneMinute, halfADay);
  } // onCreate

  static public String version(final Context context)
  {
    return String.format("Version : %s/%s", context.getPackageName(), versionName(context));
  } // version

  static private String versionName(final Context context)
  {
    try {
      final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    } // try
    catch(PackageManager.NameNotFoundException nnfe) {
      // like this is going to happen
      return "Unknown";
    } // catch
  } // versionName

  private CycleStreetsAppSupport() { }
} // CycleStreetsAppSupport
