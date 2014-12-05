package net.cyclestreets.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.DisplayMetrics;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.view.R;

import org.mapsforge.android.maps.MapsforgeOSMDroidTileProvider;
import org.mapsforge.android.maps.MapsforgeOSMTileSource;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.util.ArrayList;
import java.util.List;

public class TileSource {
  public static String mapAttribution() {
    try {
      return source(CycleStreetsPreferences.mapstyle()).attribution();
    }
    catch(Exception e) {
      // sigh
    } // catch
    return source(DEFAULT_RENDERER).attribution();
  } // mapAttribution

  public static ITileSource mapRenderer(final Context context) {
    try {
      final Source source = source(CycleStreetsPreferences.mapstyle());
      final ITileSource renderer = source.renderer();

      if(renderer instanceof MapsforgeOSMTileSource) {
        final String mapFile = CycleStreetsPreferences.mapfile();
        final MapPack pack = MapPack.findByPackage(mapFile);
        if(pack.current())
          ((MapsforgeOSMTileSource)renderer).setMapFile(mapFile);
        else {
          MessageBox.YesNo(context,
              R.string.out_of_date_map_pack,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                  MapPack.searchGooglePlay(context);
                } // onClick
              });
          CycleStreetsPreferences.resetMapstyle();
          return source(DEFAULT_RENDERER).renderer();
        }
      }

      return renderer;
    } // try
    catch(Exception e) {
      // oh dear
    } // catch
    return source(DEFAULT_RENDERER).renderer();
  } // mapRenderer

  public static void configurePreference(final ListPreference mapStyle) {
    if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
      CycleStreetsPreferences.setMapstyle(DEFAULT_RENDERER);

    final int styleCount = availableSources_.size();
    final CharSequence[] entries = new CharSequence[styleCount];
    final CharSequence[] entryValues = new CharSequence[styleCount];

    for (int i = 0; i != styleCount; ++i) {
      entries[i] = availableSources_.get(i).friendlyName();
      entryValues[i] = availableSources_.get(i).tileSourceName();
    } // for ...

    mapStyle.setEntries(entries);
    mapStyle.setEntryValues(entryValues);
  } // configurePreferences

  public static void addTileSource(final String friendlyName,
                                   final ITileSource source,
                                   final String attribution) {
    addTileSource(friendlyName, source, attribution, false);
  } // addTileSource
  public static void addTileSource(final String friendlyName,
                                   final ITileSource tileSource,
                                   final String attribution,
                                   final boolean setAsDefault) {
    final Source source =
        new Source(friendlyName,
                   attribution != null ? attribution : DEFAULT_ATTRIBUTION,
                   tileSource);

    if (setAsDefault) {
      DEFAULT_RENDERER = tileSource.name();

      if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
        CycleStreetsPreferences.setMapstyle(tileSource.name());
    } // if ...

    if (setAsDefault)
      addDefaultSource(source);
    else
      addSource(source);
  } // addTileSource

  public static ITileSource createStandardTileSource(final String name, final String... baseUrls) {
    return createStandardTileSource(name, ResourceProxy.string.unknown, baseUrls);
  } // createStandardTileSource

  public static ITileSource createStandardTileSource(final String name,
                                                     final ResourceProxy.string aResourceId,
                                                     final String... baseUrls) {
    return createXYTileSource(name, aResourceId, 256, ".png", baseUrls);
  } // createStandardTileSource

  public static ITileSource createDensityAwareTileSource(final DisplayMetrics metrics,
                                                         final String name,
                                                         final String... baseUrls) {
    return createDensityAwareTileSource(metrics, name, ResourceProxy.string.unknown, baseUrls);
  } // createDensityAwareTileSource

  public static ITileSource createDensityAwareTileSource(final DisplayMetrics metrics,
                                                         final String name,
                                                         final ResourceProxy.string aResourceId,
                                                         final String... baseUrls) {
    final boolean highDensity = isHighDensity(metrics);
    final int tileSize = highDensity ? 512 : 256;
    final String tileSuffix = highDensity ? "@2x.png" : ".png";
    return createXYTileSource(name, aResourceId, tileSize, tileSuffix, baseUrls);
  } // createDensityAwareTileSource

  private static ITileSource createXYTileSource(final String name,
                                                final ResourceProxy.string aResourceId,
                                                final int tileSize,
                                                final String extension,
                                                final String... baseUrls) {
    return new XYTileSource(name,
                            aResourceId,
                            0,
                            17,
                            tileSize,
                            extension,
                            baseUrls);
  } // createXYTileSource

  private static boolean isHighDensity(final DisplayMetrics metrics) {
    final int density = metrics.densityDpi;
    return density >= DisplayMetrics.DENSITY_HIGH;
  } // highDensity

  private static String DEFAULT_RENDERER = CycleStreetsPreferences.MAPSTYLE_OCM;
  private static String DEFAULT_ATTRIBUTION = "\u00a9 OpenStreetMap contributors";
  private static boolean builtInsAdded_ = false;
  private static final List<Source> availableSources_ = new ArrayList<>();

  private static Iterable<Source> allSources() { return availableSources_; }
  private static void addDefaultSource(final Source source) { availableSources_.add(0, source); }
  private static void addSource(final Source source) { availableSources_.add(source); }
  private static Source source(final String tileSourceName) {
    for(Source s : allSources())
      if (s.tileSourceName().equals(tileSourceName))
        return s;
    return null;
  } // source

  private static void addBuiltInSources(final Context context) {
    if (builtInsAdded_)
      return;

    final DisplayMetrics display = context.getResources().getDisplayMetrics();

    final ITileSource OPENCYCLEMAP =
        createDensityAwareTileSource(display,
                                     CycleStreetsPreferences.MAPSTYLE_OCM,
                                     ResourceProxy.string.cyclemap,
                                     "http://tile.cyclestreets.net/opencyclemap/");
    final ITileSource OPENSTREETMAP =
        createDensityAwareTileSource(display,
                                     CycleStreetsPreferences.MAPSTYLE_OSM,
                                     ResourceProxy.string.base,
                                     "http://tile.cyclestreets.net/mapnik/");

    final boolean highDensity = isHighDensity(display);

    ITileSource OSMAP = createStandardTileSource(CycleStreetsPreferences.MAPSTYLE_OS,
                                                 ResourceProxy.string.unknown,
                                                 "http://a.os.openstreetmap.org/sv/",
                                                 "http://b.os.openstreetmap.org/sv/",
                                                 "http://c.os.openstreetmap.org/sv/");
    if (highDensity)
      OSMAP = new UpsizingTileSource(OSMAP);

    final MapsforgeOSMTileSource MAPSFORGE = new MapsforgeOSMTileSource(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE, highDensity);


    addTileSource("OpenCycleMap (shows hills)", OPENCYCLEMAP, "\u00a9 OpenStreetMap contributors. Map images \u00a9 OpenCycleMap");
    addTileSource("OpenStreetMap default style", OPENSTREETMAP, DEFAULT_ATTRIBUTION);
    addTileSource("Ordnance Survey OpenData", OSMAP, "Contains Ordnance Survey Data \u00a9 Crown copyright and database right 2010");
    addTileSource("Offline Vector Maps", MAPSFORGE, DEFAULT_ATTRIBUTION);

    builtInsAdded_ = true;
  } // addBuiltInSources

  public static MapTileProviderBase mapTileProvider(final Context context) {
    addBuiltInSources(context);

    return new CycleMapTileProvider(context);
  } // MapTileProviderBase

  private static class CycleMapTileProvider extends MapTileProviderArray
      implements IMapTileProviderCallback {
    public CycleMapTileProvider(final Context context) {
      this(context,
          TileSource.source(DEFAULT_RENDERER).renderer(),
          new SimpleRegisterReceiver(context));
    } // CycleMapTileProvider

    private CycleMapTileProvider(final Context context,
                                 final ITileSource tileSource,
                                 final IRegisterReceiver registerReceiver) {
      super(tileSource, registerReceiver);

      final MapTileFilesystemProvider fileSystemProvider =
          new MapTileFilesystemProvider(registerReceiver, tileSource);
      mTileProviderList.add(fileSystemProvider);

      final NetworkAvailabliltyCheck networkCheck = new NetworkAvailabliltyCheck(context);

      final MapTileDownloader downloaderProvider =
          new MapTileDownloader(tileSource,
              new TileWriter(),
              networkCheck);
      mTileProviderList.add(downloaderProvider);

      final MapsforgeOSMDroidTileProvider mapsforgeProvider =
          new MapsforgeOSMDroidTileProvider(tileSource, networkCheck);
      mTileProviderList.add(mapsforgeProvider);
    } // CycleMapTileProvider
  } // CycleMapTileProvider

  private static class Source {
    private final String friendlyName_;
    private final String attribution_;
    private final ITileSource tileSource_;

    public Source(final String friendlyName,
                  final String attribution,
                  final ITileSource tileSource) {
      friendlyName_ = friendlyName;
      attribution_ = attribution;
      tileSource_ = tileSource;
    } // Source

    public String friendlyName() { return friendlyName_; }
    public String attribution() { return attribution_; }
    public String tileSourceName() { return tileSource_.name(); }
    public ITileSource renderer() { return tileSource_; }
  } // TS
} // TileSource
