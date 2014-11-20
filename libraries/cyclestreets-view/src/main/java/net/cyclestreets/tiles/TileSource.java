package net.cyclestreets.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.MapFactory;
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
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TileSource {
  public static String mapAttribution() {
    try {
      return attributions_.get(CycleStreetsPreferences.mapstyle());
    }
    catch(Exception e) {
      // sigh
    } // catch
    return attributions_.get(DEFAULT_RENDERER);
  } // mapAttribution

  public static ITileSource mapRenderer(final Context context) {
    try {
      final ITileSource renderer = TileSourceFactory.getTileSource(CycleStreetsPreferences.mapstyle());

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
          return TileSourceFactory.getTileSource(DEFAULT_RENDERER);
        }
      }

      return renderer;
    } // try
    catch(Exception e) {
      // oh dear
    } // catch
    return TileSourceFactory.getTileSource(DEFAULT_RENDERER);
  } // mapRenderer

  public static void configurePreference(final ListPreference mapStyle) {
    if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
      CycleStreetsPreferences.setMapstyle(DEFAULT_RENDERER);

    final int styleCount = availableSources.size();
    final CharSequence[] entries = new CharSequence[styleCount];
    final CharSequence[] entryValues = new CharSequence[styleCount];

    for (int i = 0; i != styleCount; ++i) {
      entries[i] = availableSources.get(i).friendlyName;
      entryValues[i] = availableSources.get(i).tileSourceName;
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
                                   final ITileSource source,
                                   final String attribution,
                                   final boolean setAsDefault) {
    attributions_.put(source.name(), attribution != null ? attribution : DEFAULT_ATTRIBUTION);
    TileSourceFactory.addTileSource(source);

    if (setAsDefault) {
      DEFAULT_RENDERER = source.name();

      if (CycleStreetsPreferences.mapstyle().equals(CycleStreetsPreferences.NOT_SET))
        CycleStreetsPreferences.setMapstyle(source.name());
    } // if ...

    Source s = new Source(friendlyName, source.name());
    if (setAsDefault)
      availableSources.add(0, s);
    else
      availableSources.add(s);
  } // addTileSource

  public static ITileSource createStandardTileSource(final String name, final String... baseUrls) {
    return createStandardTileSource(name, ResourceProxy.string.unknown, baseUrls);
  } // createStandardTileSource

  public static ITileSource createStandardTileSource(final String name, final ResourceProxy.string aResourceId, final String... baseUrls) {
    return new XYTileSource(name,
        aResourceId, 0, 17, 256, ".png",
        baseUrls);
  } // createStandardTileSource


  private static String DEFAULT_RENDERER = CycleStreetsPreferences.MAPSTYLE_OCM;
  private static String DEFAULT_ATTRIBUTION = "\u00a9 OpenStreetMap contributors";
  private static final List<Source> availableSources = new ArrayList<>();
  private static final Map<String, String> attributions_ = new HashMap<>();

  static {


    final ITileSource OPENCYCLEMAP = createStandardTileSource(CycleStreetsPreferences.MAPSTYLE_OCM,
                                                              ResourceProxy.string.cyclemap,
                                                              "http://tile.cyclestreets.net/opencyclemap/");
    final ITileSource OPENSTREETMAP = createStandardTileSource(CycleStreetsPreferences.MAPSTYLE_OSM,
                                                               ResourceProxy.string.base,
                                                               "http://tile.cyclestreets.net/mapnik/");
    final ITileSource OSMAP = createStandardTileSource(CycleStreetsPreferences.MAPSTYLE_OS,
                                                       ResourceProxy.string.unknown,
                                                        "http://a.os.openstreetmap.org/sv/",
                                                        "http://b.os.openstreetmap.org/sv/",
                                                        "http://c.os.openstreetmap.org/sv/");
    final MapsforgeOSMTileSource MAPSFORGE = new MapsforgeOSMTileSource(CycleStreetsPreferences.MAPSTYLE_MAPSFORGE);

    addTileSource("OpenCycleMap (shows hills)", OPENCYCLEMAP, "\u00a9 OpenStreetMap contributors. Map images \u00a9 OpenCycleMap");
    addTileSource("OpenStreetMap default style", OPENSTREETMAP, DEFAULT_ATTRIBUTION);
    addTileSource("Ordnance Survey OpenData", OSMAP, "Contains Ordnance Survey Data \u00a9 Crown copyright and database right 2010");
    addTileSource("Offline Vector Maps", MAPSFORGE, DEFAULT_ATTRIBUTION);
  } // static

  public static MapTileProviderBase mapTileProvider(final Context context) {
    return new CycleMapTileProvider(context);
  } // MapTileProviderBase

  private static class CycleMapTileProvider extends MapTileProviderArray
      implements IMapTileProviderCallback {
    public CycleMapTileProvider(final Context context) {
      this(context,
          TileSourceFactory.getTileSource(DEFAULT_RENDERER),
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
    final String friendlyName;
    final String tileSourceName;

    public Source(final String friendlyName,
                  final String tileSourceName) {
      this.friendlyName = friendlyName;
      this.tileSourceName = tileSourceName;
    }
  } // TS
} // TileSource
