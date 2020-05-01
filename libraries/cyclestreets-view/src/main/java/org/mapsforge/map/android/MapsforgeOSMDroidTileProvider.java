package org.mapsforge.map.android;

import java.io.ByteArrayInputStream;
import java.net.UnknownHostException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.CantContinueException;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.*;

public class MapsforgeOSMDroidTileProvider extends MapTileModuleProviderBase
{
  private MapsforgeOSMTileSource tileSource;
  private final OnlineTileSourceBase fallbackTileSource;
  private final NetworkAvailabliltyCheck networkCheck;
  private final OkHttpClient client;

  public MapsforgeOSMDroidTileProvider(final ITileSource fallbackSource,
                                       final NetworkAvailabliltyCheck networkCheck,
                                       final Interceptor interceptor) {
    super(Configuration.getInstance().getTileDownloadThreads(),
          Configuration.getInstance().getTileDownloadMaxQueueSize());
    tileSource = null;
    fallbackTileSource = fallbackSource instanceof OnlineTileSourceBase ? (OnlineTileSourceBase)fallbackSource : null;
    this.networkCheck = networkCheck;

    client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
  }

  @Override
  protected String getName() { return "Mapsforge"; }

  @Override
  protected String getThreadGroupName() { return "mapsforge"; }

  @Override
  public boolean getUsesDataConnection() { return false; }

  @Override
  public int getMaximumZoomLevel() {
    return (tileSource != null ? tileSource.getMaximumZoomLevel() : 20);
  }

  @Override
  public int getMinimumZoomLevel() {
    return (tileSource != null ? tileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
  }

  @Override
  public MapTileModuleProviderBase.TileLoader getTileLoader() {
    return new TileLoader();
  }

  @Override
  public void setTileSource(final ITileSource tileSource) {
    this.tileSource = (tileSource instanceof MapsforgeOSMTileSource) ?
        (MapsforgeOSMTileSource)tileSource :
        null;
  }

  /////////////////////////////////////////////////
  private class TileLoader extends MapTileModuleProviderBase.TileLoader  {

    @Override
    public Drawable loadTile(long pMapTileIndex) throws CantContinueException {
      if (tileSource == null)
        return null;

      Drawable tile = drawMapsforgeTile(pMapTileIndex);
      if (tile == null)
        tile = downloadTile(pMapTileIndex);
      return tile;
    }

    private Drawable drawMapsforgeTile(final long pTileIndex) {
      // mapsforge goes wonky at zoom <= 7
      if (MapTileIndex.getZoom(pTileIndex) <= 7)
        return null;

      try {
        return tileSource.getDrawable(MapTileIndex.getX(pTileIndex),
                                      MapTileIndex.getY(pTileIndex),
                                      MapTileIndex.getZoom(pTileIndex));
      }
      catch (Exception e) {
        return null;
      }
    }

    private Drawable downloadTile(final long pTileIndex) throws CantContinueException {
      if (fallbackTileSource == null)
        return null;

      try {
        if (!isNetworkAvailable())
          return null;

        final String tileUrl = fallbackTileSource.getTileURLString(pTileIndex);
        if (TextUtils.isEmpty(tileUrl))
          return null;

        Request request = new Request.Builder()
                .url(tileUrl)
                .build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful())
          return null;

        final byte[] data = response.body().bytes();
        if (data.length == 0)
          return null;

        return fallbackTileSource.getDrawable(new ByteArrayInputStream(data));
      } catch (final UnknownHostException | LowMemoryException e) {
        throw new CantContinueException(e);
      } catch (final Exception e) {
        return null;
      }
    }

    private boolean isNetworkAvailable() {
      return (networkCheck == null || networkCheck.getNetworkAvailable());
    }
  }
}
