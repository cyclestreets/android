package net.cyclestreets.tiles;

import android.content.Context;

import net.cyclestreets.AppInfo;

import org.mapsforge.map.android.MapsforgeOSMDroidTileProvider;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.CycleStreetsTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import okhttp3.Interceptor;

class CycleMapTileProvider extends MapTileProviderArray implements IMapTileProviderCallback {

  public CycleMapTileProvider(final Context context,
                              final ITileSource defaultTileSource) {
    this(context, defaultTileSource, new SimpleRegisterReceiver(context));
  }

  private CycleMapTileProvider(final Context context,
                               final ITileSource tileSource,
                               final IRegisterReceiver registerReceiver) {
    super(tileSource, registerReceiver);

    Interceptor interceptor = new UserAgentInterceptor(AppInfo.version(context));

    final MapTileFilesystemProvider fileSystemProvider =
        new MapTileFilesystemProvider(registerReceiver, tileSource);
    mTileProviderList.add(fileSystemProvider);

    final NetworkAvailabliltyCheck networkCheck = new NetworkAvailabliltyCheck(context);

    final CycleStreetsTileDownloader downloaderProvider = new CycleStreetsTileDownloader.Builder()
            .withTileSource(tileSource)
            .withFilesystemCache(new TileWriter())
            .withNetworkAvailabilityCheck(networkCheck)
            .withInterceptor(interceptor)
            .build();
    mTileProviderList.add(downloaderProvider);

    final MapsforgeOSMDroidTileProvider mapsforgeProvider =
        new MapsforgeOSMDroidTileProvider(tileSource, networkCheck, interceptor);
    mTileProviderList.add(mapsforgeProvider);
  }
}
