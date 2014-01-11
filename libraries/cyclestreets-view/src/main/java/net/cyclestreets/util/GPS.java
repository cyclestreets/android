package net.cyclestreets.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

public final class GPS
{
  static private Boolean hasGps_;
  
  static public boolean deviceHasGPS(final Context context)
  {
    if(hasGps_ == null) {    
      final PackageManager pm = context.getPackageManager();
      hasGps_ = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }
    return hasGps_;
  } // deviceHasGPS
  
  static public boolean isOn(final Context context)
  {
    final LocationManager service = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
  } // isOn
  
  static public void showSettings(final Context context)
  {
    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    context.startActivity(intent);
  } // showSettings
} // GPS
