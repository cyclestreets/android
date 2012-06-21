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
  final public String name;
  final public String path;

  static public void searchGooglePlay(final Context context)
  {
    final Intent play = new Intent(Intent.ACTION_VIEW);
    play.setData(Uri.parse("market://search?q=net.cyclestreets"));
    context.startActivity(play);
  } // searchGooglePlay
  
  static public List<MapPack> availableMapPacks()
  {
    final List<MapPack> packs = new ArrayList<MapPack>();
    
    final File obbDir = new File(Environment.getExternalStorageDirectory(), "Android/obb");
    if(!obbDir.exists())
      return packs;
    
    for(final File mapDir : obbDir.listFiles(new CycleStreetsMapFilter()))
    {
      final File map = findMapFile(mapDir, "main.");
      final String name = mapName(mapDir);
      if(map == null || name == null)
        continue;

      packs.add(new MapPack(name, map));
    } // for
    
    return packs;
  } // availableMapPacks

  private MapPack(final String n, final File p) 
  { 
    name = n;
    path = p.getAbsolutePath();
  } // MapPack

  static private File findMapFile(final File mapDir, final String prefix)
  {
    for(final File c : mapDir.listFiles())
      if(c.getName().startsWith(prefix))
        return c;
    return null;
  } // findMapFile
  
  static private String mapName(final File mapDir)
  {
    try {
      final File detailsFile = findMapFile(mapDir, "patch.");
      final Properties details = new Properties();
      details.load(new FileInputStream(detailsFile));
      return details.getProperty("title");
    } // try
    catch(IOException e) {
      return null;
    } // catch
  } // mapName
  
  static private class CycleStreetsMapFilter implements FilenameFilter
  {
    public boolean accept(final File dir, final String name)
    {
      return name.contains("net.cyclestreets.maps");
    } // accept
  } // class CycleStreetsMapFilter
} // class MapPack
