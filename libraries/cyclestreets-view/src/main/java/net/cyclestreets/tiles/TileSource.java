package net.cyclestreets.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.preference.ListPreference;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Screen;
import net.cyclestreets.view.R;
import net.cyclestreets.views.CycleMapView;

import org.mapsforge.map.android.MapsforgeOSMTileSource;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.util.ArrayList;
import java.util.List;

public class TileSource {
  public static String mapAttribution() {
    try {
      return source(CycleStreetsPreferences.mapstyle()).attribution();
    }
    catch (Exception e) {
      // sigh
    }
    return source(DEFAULT_RENDERER).attribution();
  }

  public static ITileSource mapRenderer(final Context context) {
    try {
      final Source source = source(CycleStreetsPreferences.mapstyle());
      final ITileSource renderer = source.renderer();

      if (renderer instanceof MapsforgeOSMTileSource) {
        final String mapFile = CycleStreetsPreferences.mapfile();
        final MapPack pack = MapPack.findByPackage(mapFile);
        if (pack.current())
          ((MapsforgeOSMTileSource)renderer).setMapFile(mapFile);
        else {
          MessageBox.YesNo(context,
              R.string.tiles_map_pack_out_of_date,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                  MapPack.searchGooglePlay(context);
                }
              });
          CycleStreetsPreferences.resetMapstyle();
          return source(DEFAULT_RENDERER).renderer();
        }
      }

      return renderer;
    }
    catch (Exception e) {
      // oh dear
    }
    return source(DEFAULT_RENDERER).renderer();
  }

  public static void configurePreference(final ListPreference mapStyle) {
    if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
      CycleStreetsPreferences.setMapstyle(DEFAULT_RENDERER);

    final int styleCount = availableSources_.size();
    final CharSequence[] entries = new CharSequence[styleCount];
    final CharSequence[] entryValues = new CharSequence[styleCount];

    for (int i = 0; i != styleCount; ++i) {
      entries[i] = availableSources_.get(i).friendlyName();
      entryValues[i] = availableSources_.get(i).tileSourceName();
    }

    mapStyle.setEntries(entries);
    mapStyle.setEntryValues(entryValues);
  }

  public static void addTileSource(final String friendlyName,
                                   final ITileSource source) {
    addTileSource(friendlyName, source, false);
  }
  public static void addTileSource(final String friendlyName,
                                   final ITileSource tileSource,
                                   final boolean setAsDefault) {
    final Source source =
        new Source(friendlyName, tileSource);

    if (setAsDefault) {
      DEFAULT_RENDERER = tileSource.name();

      if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
        CycleStreetsPreferences.setMapstyle(tileSource.name());
    }

    if (setAsDefault)
      addDefaultSource(source);
    else
      addSource(source);
  }

  public static ITileSource createDensityAwareTileSource(final Context context,
                                                         final String name,
                                                         final String attribution,
                                                         final String... baseUrls) {
    final boolean highDensity = Screen.isHighDensity(context);
    final int tileSize = highDensity ? 512 : 256;
    final String tileSuffix = highDensity ? "@2x.png" : ".png";
    return createXYTileSource(name, attribution, tileSize, tileSuffix, baseUrls);
  }

  private static ITileSource createXYTileSource(final String name,
                                                final String attribution,
                                                final int tileSize,
                                                final String extension,
                                                final String[] baseUrls) {
    return new XYTileSource(name,
                            0,
                            CycleMapView.MAX_ZOOM_LEVEL,
                            tileSize,
                            extension,
                            baseUrls,
                            attribution);
  }

  private static String DEFAULT_RENDERER = CycleStreetsPreferences.MAPSTYLE_OCM;
  private static String DEFAULT_ATTRIBUTION = "\u00a9 OpenStreetMap contributors";
  private static boolean builtInsAdded_ = false;
  private static final List<Source> availableSources_ = new ArrayList<>();

  private static Iterable<Source> allSources() { return availableSources_; }
  private static void addDefaultSource(final Source source) { availableSources_.add(0, source); }
  private static void addSource(final Source source) { availableSources_.add(source); }
  private static Source source(final String tileSourceName) {
    for (Source s : allSources())
      if (s.tileSourceName().equals(tileSourceName))
        return s;
    return null;
  }

  public static void preloadStandardTileSources(final Context context) {
    addBuiltInSources(context);
  }

  private static void addBuiltInSources(final Context context) {
    if (builtInsAdded_)
      return;

    final ITileSource OPENCYCLEMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OCM,
                                     "\u00a9 OpenStreetMap contributors. Map images \u00a9 Thunderforest",
                                     "https://a.tile.cyclestreets.net/opencyclemap/",
                                     "https://b.tile.cyclestreets.net/opencyclemap/",
                                     "https://c.tile.cyclestreets.net/opencyclemap/");
    final ITileSource OPENSTREETMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OSM,
                                     DEFAULT_ATTRIBUTION,
                                     "https://a.tile.cyclestreets.net/mapnik/",
                                     "https://b.tile.cyclestreets.net/mapnik/",
                                     "https://c.tile.cyclestreets.net/mapnik/");

    final ITileSource OSMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OS,
                                     "Contains Ordnance Survey Data \u00a9 Crown copyright and database right 2010",
                                     "https://a.tile.cyclestreets.net/osopendata/",
                                     "https://b.tile.cyclestreets.net/osopendata/",
                                     "https://c.tile.cyclestreets.net/osopendata/");

    final MapsforgeOSMTileSource MAPSFORGE =
            new MapsforgeOSMTileSource(context,
                                       CycleStreetsPreferences.MAPSTYLE_MAPSFORGE,
                                       DEFAULT_ATTRIBUTION,
                                       Screen.isHighDensity(context));

    addTileSource("OpenCycleMap (shows hills)", OPENCYCLEMAP);
    addTileSource("OpenStreetMap default style", OPENSTREETMAP);
    addTileSource("Ordnance Survey OpenData", OSMAP);
    addTileSource("Offline Vector Maps", MAPSFORGE);

    builtInsAdded_ = true;
  }

  public static MapTileProviderBase mapTileProvider(final Context context) {
    addBuiltInSources(context);
    return new CycleMapTileProvider(context, TileSource.source(TileSource.DEFAULT_RENDERER).renderer());
  }

  private static class Source {
    private final String friendlyName_;
    private final ITileSource tileSource_;

    public Source(final String friendlyName,
                  final ITileSource tileSource) {
      friendlyName_ = friendlyName;
      tileSource_ = tileSource;
    }

    public String friendlyName() { return friendlyName_; }
    public String attribution() { return tileSource_.getCopyrightNotice(); }
    public String tileSourceName() { return tileSource_.name(); }
    public ITileSource renderer() { return tileSource_; }
  }
}
