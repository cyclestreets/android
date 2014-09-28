package net.cyclestreets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import net.cyclestreets.routing.Route;
import net.cyclestreets.util.MapPack;

public class MainSupport {
  public static boolean switchMapFile(final Intent launchIntent) {
    final String mappackage = launchIntent.getStringExtra("mapfile");
    if(mappackage == null)
      return false;
    final MapPack pack = MapPack.findByPackage(mappackage);
    if(pack == null)
      return false;
    CycleStreetsPreferences.enableMapFile(pack.path());
    return true;
  } // switchMapFile

  public static boolean loadRoute(final Intent launchIntent,
                                  final Context context) {
    final Uri launchUri = launchIntent.getData();
    if (launchUri == null)
      return false;

    final int itinerary = findItinerary(launchUri);
    if (itinerary == -1)
      return false;

    Route.FetchRoute(CycleStreetsPreferences.routeType(),
        itinerary,
        CycleStreetsPreferences.speed(),
        context);
    return true;
  } // loadRoute

  private static int findItinerary(final Uri launchUri) {
    try {
      final String itinerary = extractItinerary(launchUri);
      return Integer.parseInt(itinerary);
    } catch(Exception whatever) {
      return -1;
    } // catch
  } // findItinerary

  private static String extractItinerary(final Uri launchUri) {
    final String host = launchUri.getHost();

    if ("cycle.st".equals(host))
      return launchUri.getPath().substring(2);

    if ("m.cyclestreets.net".equals(host)) {
      final String frag = launchUri.getFragment();
      return frag.substring(0, frag.indexOf('/'));
    }

    final String path = launchUri.getPath().substring(8);
    return path.replace("/", "");
  } // extractItinerary

  private MainSupport() { }
} // MainSupport
