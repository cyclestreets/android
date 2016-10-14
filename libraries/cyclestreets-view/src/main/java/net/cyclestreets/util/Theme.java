package net.cyclestreets.util;

import android.content.Context;
import android.content.res.TypedArray;

public final class Theme {
  public static int lowlightColor(final Context context) { return color(context, android.R.attr.textColorSecondary); }
  public static int highlightColor(final Context context) {  return color(context, android.R.attr.textColorHighlight); }
  public static int backgroundColor(final Context context) { return color(context, android.R.attr.colorBackground); }

  private static int color(final Context context, int colorRef) {
    TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{colorRef});
    int c = array.getColor(0, 0xFF00FF);
    array.recycle();
    return c;
  } // color

  private Theme() { }
} // Theme
