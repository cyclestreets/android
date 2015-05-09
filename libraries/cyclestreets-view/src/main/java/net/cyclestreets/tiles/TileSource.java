package net.cyclestreets.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.MapPack;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Screen;
import net.cyclestreets.view.R;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mapsforge.android.maps.MapsforgeOSMTileSource;
import org.osmdroid.ResourceProxy;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.http.IHttpClientFactory;
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

  public static ITileSource createDensityAwareTileSource(final Context context,
                                                         final String name,
                                                         final String... baseUrls) {
    return createDensityAwareTileSource(context, name, ResourceProxy.string.unknown, baseUrls);
  } // createDensityAwareTileSource

  public static ITileSource createDensityAwareTileSource(final Context context,
                                                         final String name,
                                                         final ResourceProxy.string aResourceId,
                                                         final String... baseUrls) {
    final boolean highDensity = Screen.isHighDensity(context);
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

  public static void preloadStandardTileSources(final Context context) {
    addBuiltInSources(context);
  } // preloadStandardTileSources

  private static void addBuiltInSources(final Context context) {
    if (builtInsAdded_)
      return;

    final ITileSource OPENCYCLEMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OCM,
                                     ResourceProxy.string.cyclemap,
                                     "http://tile.cyclestreets.net/opencyclemap/");
    final ITileSource OPENSTREETMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OSM,
                                     ResourceProxy.string.unknown,
                                     "http://tile.cyclestreets.net/mapnik/");

    final ITileSource OSMAP =
        createDensityAwareTileSource(context,
                                     CycleStreetsPreferences.MAPSTYLE_OS,
                                     ResourceProxy.string.unknown,
                                     "http://tile.cyclestreets.net/osopendata/");

    final MapsforgeOSMTileSource MAPSFORGE = new MapsforgeOSMTileSource(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE, Screen.isHighDensity(context));

    addTileSource("OpenCycleMap (shows hills)", OPENCYCLEMAP, "\u00a9 OpenStreetMap contributors. Map images \u00a9 Thunderforest");
    addTileSource("OpenStreetMap default style", OPENSTREETMAP, DEFAULT_ATTRIBUTION);
    addTileSource("Ordnance Survey OpenData", OSMAP, "Contains Ordnance Survey Data \u00a9 Crown copyright and database right 2010");
    addTileSource("Offline Vector Maps", MAPSFORGE, DEFAULT_ATTRIBUTION);

    builtInsAdded_ = true;
  } // addBuiltInSources

  public static MapTileProviderBase mapTileProvider(final Context context) {
    addBuiltInSources(context);

    final IHttpClientFactory httpFactory = new IHttpClientFactory() {
      @Override
      public HttpClient createHttpClient() {
        final DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Cyclestreets Android TileSource");
        return client;
      }
    };
    HttpClientFactory.setFactoryInstance(httpFactory);

    return new CycleMapTileProvider(context, TileSource.source(TileSource.DEFAULT_RENDERER).renderer());
  } // MapTileProviderBase

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
