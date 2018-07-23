package net.cyclestreets.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class MapPack
{
  private static String MAPSFORGE_VERSION = "0.9.1";

  public static void searchGooglePlay(final Context context) {
    final Intent play = new Intent(Intent.ACTION_VIEW);
    play.setData(Uri.parse("market://search?q=net.cyclestreets"));
    context.startActivity(play);
  }

  public static List<MapPack> availableMapPacks() {
    final List<MapPack> packs = new ArrayList<>();

    final File obbDir = new File(Environment.getExternalStorageDirectory(), "Android/obb");
    if (!obbDir.exists())
      return packs;

    for (final File mapDir : obbDir.listFiles(new CycleStreetsMapFilter())) {
      final File map = findMapFile(mapDir, "main.");
      final Properties props = mapProperties(mapDir);
      final String name = props.getProperty("title");
      final String version = props.getProperty("version");
      if (map == null || name == null)
        continue;

      packs.add(new MapPack(name, version, map));
    }

    return packs;
  }

  public static MapPack findByPackage(final String packageName) {
    for (final MapPack pack : availableMapPacks())
      if (pack.path().contains(packageName))
        return pack;
    return null;
  }

  private static File findMapFile(final File mapDir, final String prefix) {
    for (final File c : mapDir.listFiles())
      if (c.getName().startsWith(prefix))
        return c;
    return null;
  }

  private static Properties mapProperties(final File mapDir) {
    final Properties details = new Properties();
    try {
      final File detailsFile = findMapFile(mapDir, "patch.");
      details.load(new FileInputStream(detailsFile));
    }
    catch (IOException | RuntimeException e) {
    }
    return details;
  }

  private static class CycleStreetsMapFilter implements FilenameFilter  {
    public boolean accept(final File dir, final String name) {
      return name.contains("net.cyclestreets.maps");
    }
  }

  //////////////////////////////////////////////////////
  private final String name_;
  private final String path_;
  private final String version_;

  private MapPack(final String n,
            final String v,
            final File p) {
    name_ = n;
    path_ = p.getAbsolutePath();
    version_ = v;
  }

  public String name() { return name_; }
  public String path() { return path_; }
  public boolean current() { return MAPSFORGE_VERSION.equals(version_); }
}
