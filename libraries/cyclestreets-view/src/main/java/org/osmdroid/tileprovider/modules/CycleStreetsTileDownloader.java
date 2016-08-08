package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import net.cyclestreets.tiles.UpsizingTileSource;

import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.*;

/**
 * The {@link CycleStreetsTileDownloader} loads tiles from an HTTP server. It saves downloaded tiles to an
 * IFilesystemCache if available.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 *
 */
public class CycleStreetsTileDownloader extends MapTileModuleProviderBase {
  private interface IOnlineTileSource {
    String getTileURLString(MapTile aTile);
    Drawable getDrawable(InputStream aTileInputStream) throws LowMemoryException;
    ITileSource unwrap();
  }
  private static class OnlineTileSourceWrapper implements IOnlineTileSource {
    private OnlineTileSourceBase otsb_;
    OnlineTileSourceWrapper(OnlineTileSourceBase otsb) { otsb_ = otsb; }
    public String getTileURLString(MapTile aTile) { return otsb_.getTileURLString(aTile); }
    public Drawable getDrawable(InputStream aTileInputStream) throws LowMemoryException {
      return otsb_.getDrawable(aTileInputStream);
    }
    public ITileSource unwrap() { return otsb_; }
  }
  private static class UpsizingTileSourceWrapper implements IOnlineTileSource {
    private UpsizingTileSource uts_;
    UpsizingTileSourceWrapper(UpsizingTileSource uts) { uts_ = uts; }
    public String getTileURLString(MapTile aTile) { return uts_.getTileURLString(aTile); }
    public Drawable getDrawable(InputStream aTileInputStream) throws LowMemoryException {
      return uts_.getDrawable(aTileInputStream);
    }
    public ITileSource unwrap() { return uts_; }
  }

  // ===========================================================
  // Constants
  // ===========================================================

  private static final Logger logger = LoggerFactory.getLogger("CycleStreetsTileDownloader");

  // ===========================================================
  // Fields
  // ===========================================================

  private final IFilesystemCache mFilesystemCache;

  private final AtomicReference<IOnlineTileSource> mTileSource = new AtomicReference<>();

  private final INetworkAvailablityCheck mNetworkAvailablityCheck;

  private final OkHttpClient client;

  // ===========================================================
  // Constructors
  // ===========================================================

  private CycleStreetsTileDownloader(Builder builder) {
    super(builder.threadPoolSize, builder.pendingQueueSize);

    mFilesystemCache = builder.filesystemCache;
    mNetworkAvailablityCheck = builder.networkAvailabilityCheck;
    setTileSource(builder.tileSource);

    client = new OkHttpClient.Builder().addInterceptor(builder.interceptor).build();
  }

  public static class Builder {
    ITileSource tileSource;
    IFilesystemCache filesystemCache;
    INetworkAvailablityCheck networkAvailabilityCheck;
    int threadPoolSize = 4;
    int pendingQueueSize = TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE;
    private Interceptor interceptor;

    public Builder withTileSource(ITileSource tileSource) {
      this.tileSource = tileSource;
      return this;
    }

    public Builder withFilesystemCache(IFilesystemCache filesystemCache) {
      this.filesystemCache = filesystemCache;
      return this;
    }

    public Builder withNetworkAvailabilityCheck(INetworkAvailablityCheck networkAvailabilityCheck) {
      this.networkAvailabilityCheck = networkAvailabilityCheck;
      return this;
    }

    public Builder withInterceptor(Interceptor interceptor) {
      this.interceptor = interceptor;
      return this;
    }

    public CycleStreetsTileDownloader build() {
      return new CycleStreetsTileDownloader(this);
    }
  }

  // ===========================================================
  // Getter & Setter
  // ===========================================================

  public ITileSource getTileSource() {
    IOnlineTileSource ots = mTileSource.get();
    return ots != null ? ots.unwrap() : null;
  } // getTileSource

  // ===========================================================
  // Methods from SuperClass/Interfaces
  // ===========================================================

  @Override
  public boolean getUsesDataConnection() {
    return true;
  }

  @Override
  protected String getName() {
    return "Online Tile Download Provider";
  }

  @Override
  protected String getThreadGroupName() {
    return "downloader";
  }

  @Override
  protected Runnable getTileLoader() {
    return new TileLoader();
  }

  @Override
  public int getMaximumZoomLevel() {
    IOnlineTileSource tileSource = mTileSource.get();
    return (tileSource != null ? tileSource.unwrap().getMaximumZoomLevel() : 20);
  }

  @Override
  public int getMinimumZoomLevel() {
    IOnlineTileSource tileSource = mTileSource.get();
    return (tileSource != null ? tileSource.unwrap().getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
  }

  @Override
  public void setTileSource(final ITileSource tileSource) {
    // We are only interested in OnlineTileSourceBase tile sources
    if (tileSource instanceof OnlineTileSourceBase) {
      mTileSource.set(new OnlineTileSourceWrapper((OnlineTileSourceBase)tileSource));
    } else if (tileSource instanceof UpsizingTileSource) {
      mTileSource.set(new UpsizingTileSourceWrapper((UpsizingTileSource)tileSource));
    } else {
      // Otherwise shut down the tile downloader
      mTileSource.set(null);
    }
  }

  // ===========================================================
  // Inner and Anonymous Classes
  // ===========================================================
  protected class TileLoader extends MapTileModuleProviderBase.TileLoader {
    @Override
    public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {
      IOnlineTileSource tileSource = mTileSource.get();
      if (tileSource == null)
        return null;

      final MapTile tile = aState.getMapTile();

      try {
        if (mNetworkAvailablityCheck != null && !mNetworkAvailablityCheck.getNetworkAvailable()) {
          if (DEBUGMODE) {
            logger.debug("Skipping " + getName() + " due to NetworkAvailabliltyCheck.");
          }
          return null;
        }

        final String tileURLString = tileSource.getTileURLString(tile);

        if (DEBUGMODE) {
          logger.debug("Downloading Maptile from url: " + tileURLString);
        }

        if (TextUtils.isEmpty(tileURLString)) {
          return null;
        }

        Request request = new Request.Builder()
                .url(tileURLString)
                .build();
        Response response = client.newCall(request).execute();

        // Check to see if we got success
        if (!response.isSuccessful()) {
          logger.warn("Problem downloading MapTile: " + tile + " HTTP response: " + response.code());
          return null;
        }

        final byte[] data = response.body().bytes();
        if (data.length == 0) {
          logger.warn("No content downloading MapTile: " + tile);
          return null;
        }

        final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

        // Save the data to the filesystem cache
        if (mFilesystemCache != null) {
          mFilesystemCache.saveFile(tileSource.unwrap(), tile, byteStream);
          byteStream.reset();
        }
        final Drawable result = tileSource.getDrawable(byteStream);

        return result;
      } catch (final UnknownHostException e) {
        // no network connection so empty the queue
        logger.warn("UnknownHostException downloading MapTile: " + tile + " : " + e);
        throw new CantContinueException(e);
      } catch (final LowMemoryException e) {
        // low memory so empty the queue
        logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
        throw new CantContinueException(e);
      } catch (final FileNotFoundException e) {
        logger.warn("Tile not found: " + tile + " : " + e);
      } catch (final IOException e) {
        logger.warn("IOException downloading MapTile: " + tile + " : " + e);
      } catch (final Throwable e) {
        logger.error("Error downloading MapTile: " + tile, e);
      }

      return null;
    }

    @Override
    protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
      removeTileFromQueues(pState.getMapTile());
      // don't return the tile because we'll wait for the fs provider to ask for it
      // this prevent flickering when a load of delayed downloads complete for tiles
      // that we might not even be interested in any more
      pState.getCallback().mapTileRequestCompleted(pState, null);
      // We want to return the Bitmap to the BitmapPool if applicable
      if (pDrawable instanceof ReusableBitmapDrawable)
        BitmapPool.getInstance().returnDrawableToPool((ReusableBitmapDrawable) pDrawable);
    }
  }
}
