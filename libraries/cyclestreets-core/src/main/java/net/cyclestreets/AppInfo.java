package net.cyclestreets;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import net.cyclestreets.core.BuildConfig;

public class AppInfo {
  private static String version_ = null;

  public static String version(final Context context) {
    if (version_ == null)
      version_ = String.format("%s/%s/%s", context.getPackageName(),
                               versionName(context), BuildConfig.BUILD_IDENTIFIER);
    return version_;
  }

  private static String versionName(final Context context) {
    try {
      final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    }
    catch (PackageManager.NameNotFoundException nnfe) {
      // like this is going to happen
      return UNKNOWN;
    }
  }

  private static final String UNKNOWN = "unknown";
}
