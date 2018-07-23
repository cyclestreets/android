package net.cyclestreets;

import android.app.AlertDialog;
import android.content.Context;
import android.webkit.WebView;

import net.cyclestreets.util.MessageBox;

public class Welcome {
  public static void welcome(final Context context) {
    launch(context,
        "CycleStreets",
        "file:///android_asset/welcome.html");
  }

  public static void whatsNew(final Context context) {
    launch(context,
        "CycleStreets - What's New",
        "file:///android_asset/whatsnew.html");
  }

  private static void launch(
      final Context context,
      final String title,
      final String assetUrl) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(title);
    builder.setPositiveButton("OK", MessageBox.NoAction);

    final WebView webView = new WebView(context);
    webView.loadUrl(assetUrl);
    builder.setView(webView);

    final AlertDialog ad = builder.create();
    ad.show();
    ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(android.R.style.TextAppearance_Large);
  }
}
