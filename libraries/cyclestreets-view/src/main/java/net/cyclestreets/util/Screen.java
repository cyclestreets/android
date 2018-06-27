package net.cyclestreets.util;

import android.content.Context;
import android.util.DisplayMetrics;

public final class Screen {
  public static boolean isHighDensity(final Context context) {
    final int density = displayMetrics(context).densityDpi;
    return density > DisplayMetrics.DENSITY_HIGH;
  }

  public static boolean isSmall(final Context context) {
    final DisplayMetrics dm = displayMetrics(context);
    final int pixelWidth = dm.widthPixels;
    final double xdpi = dm.xdpi;

    final double width = pixelWidth / xdpi;
    return (width < 3.0);
  }

  private static DisplayMetrics displayMetrics(final Context context) {
    return context.getResources().getDisplayMetrics();
  }

  private Screen() { }
}
