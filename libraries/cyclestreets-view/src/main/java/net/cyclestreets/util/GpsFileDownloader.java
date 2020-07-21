package net.cyclestreets.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import net.cyclestreets.routing.Route;

public class GpsFileDownloader {

  public static final String GPX_FILE_EXTENSION = ".gpx";
  public static final String ITINERARY_TOKEN = "%Itinerary%";
  public static final String ROUTE_PLAN_TOKEN = "%Route_Plan%";

  // Sample gpx file URL: https://cyclestreets.net/journey/52759941/cyclestreets52759941balanced.gpx*/
  public static final String GPS_FILE_URL_PATH_FORMAT =
      "https://cyclestreets.net/journey/" + ITINERARY_TOKEN + "/cyclestreets" +
          ITINERARY_TOKEN + ROUTE_PLAN_TOKEN + GPX_FILE_EXTENSION;

  public void downloadGPSFile(Context ctx) {
    Uri gpsFileUrl = getGPSFileUrl();
    DownloadManager.Request request = new DownloadManager.Request(gpsFileUrl);
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getDestinationFilePath());
    DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
    downloadManager.enqueue(request);
  }

  private Uri getGPSFileUrl() {
    String itinerary = Integer.toString(Route.journey().itinerary());
    String routePlan = Route.journey().plan();
    String gpsFileUrl = GPS_FILE_URL_PATH_FORMAT.replaceAll(ITINERARY_TOKEN, itinerary);
    gpsFileUrl = gpsFileUrl.replaceAll(ROUTE_PLAN_TOKEN, routePlan);
    return Uri.parse(gpsFileUrl);
  }

  private String getDestinationFilePath() {
    String itinerary = Integer.toString(Route.journey().itinerary());
    String routePlan = Route.journey().plan();
    return "cyclestreets" + itinerary + routePlan + GPX_FILE_EXTENSION;
  }
}
