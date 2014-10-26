package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.routing.Route;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class CycleStreetsAppSupport
{
  static public void initialise(final Context context, final int prefsDefault)
  {
    CycleStreetsPreferences.initialise(context, prefsDefault);

    Route.initialise(context);
    ApiClient.initialise(context);
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
