package net.cyclestreets.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

public final class GPS
{
  private static Boolean hasGps_;

  public static boolean deviceHasGPS(final Context context) {
    if (hasGps_ == null) {
      final PackageManager pm = context.getPackageManager();
      hasGps_ = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }
    return hasGps_;
  }

  public static boolean isOn(final Context context) {
    final LocationManager service = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }

  public static void showSettings(final Context context) {
    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    context.startActivity(intent);
  }
}
