package net.cyclestreets;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import net.cyclestreets.routing.Route;
import net.cyclestreets.util.MapPack;

public class MainSupport {
  public static boolean switchMapFile(final Intent launchIntent) {
    final String mappackage = launchIntent.getStringExtra("mapfile");
    if (mappackage == null)
      return false;
    final MapPack pack = MapPack.findByPackage(mappackage);
    if (pack == null)
      return false;
    CycleStreetsPreferences.enableMapFile(pack.path());
    return true;
  }

  public static boolean handleLaunchIntent(final Intent launchIntent,
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
  }

  private static int findItinerary(final Uri launchUri) {
    try {
      final String itinerary = extractItinerary(launchUri);
      return Integer.parseInt(itinerary);
    } catch (Exception whatever) {
      return -1;
    }
  }

  private static String extractItinerary(final Uri launchUri) {
    final String host = launchUri.getHost();

    if ("cycle.st".equals(host)) {
      // e.g. http://cycle.st/j61207326
      return launchUri.getPath().substring(2);
    }

    if ("m.cyclestreets.net".equals(host)) {
      // e.g. https://m.cyclestreets.net/journey/#57201887/balanced
      final String frag = launchUri.getFragment();
      return frag.substring(0, frag.indexOf('/'));
    }

    // e.g. http://(www.)cyclestreets.net/journey/61207326(/#balanced)
    final String path = launchUri.getPath().substring(8);
    return path.replace("/", "");
  }

  private MainSupport() { }

  // Helper classes
  private enum LaunchIntentType {
    JOURNEY, LOCATION;

    public LaunchIntent withId(int id) {
      return new LaunchIntent(this, id);
    }
  }

  private static class LaunchIntent {
    private final LaunchIntentType type;
    private final int id;

    private LaunchIntent(LaunchIntentType type, int id) {
      this.type = type;
      this.id = id;
    }
  }
}
