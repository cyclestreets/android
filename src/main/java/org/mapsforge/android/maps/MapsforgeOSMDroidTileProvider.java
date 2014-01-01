package org.mapsforge.android.maps;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class MapsforgeOSMDroidTileProvider extends MapTileModuleProviderBase
{
  private MapsforgeOSMTileSource tileSource_;
  private final OnlineTileSourceBase fallbackTileSource_;
  private final NetworkAvailabliltyCheck networkCheck_;
  
  public MapsforgeOSMDroidTileProvider(final ITileSource fallbackSource,
                       final NetworkAvailabliltyCheck networkCheck)
  {
    super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
    tileSource_ = null;
    fallbackTileSource_ = fallbackSource instanceof OnlineTileSourceBase ? (OnlineTileSourceBase)fallbackSource : null;
    networkCheck_ = networkCheck;
  } // MapsforgeOSMDroidTileProvider
  
  @Override
  protected String getName() { return "Mapsforge"; } 

  @Override
  protected String getThreadGroupName() { return "mapsforge"; }

  @Override
  public boolean getUsesDataConnection() { return false; }

  @Override
  public int getMaximumZoomLevel()
  {
    return (tileSource_ != null ? tileSource_.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
  } // getMaximumZoomLevel

  @Override
  public int getMinimumZoomLevel()
  {
    return (tileSource_ != null ? tileSource_.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
  } // getMinimumZoomLevel

  @Override
  protected Runnable getTileLoader()
  {
    return new TileLoader();
  } // getTileLoader

  @Override
  public void setTileSource(final ITileSource tileSource)
  {
    tileSource_ = (tileSource instanceof MapsforgeOSMTileSource) ?
                    (MapsforgeOSMTileSource)tileSource :
                    null;
  } // setTileSource
  
  /////////////////////////////////////////////////
  private class TileLoader extends MapTileModuleProviderBase.TileLoader 
  {
    @Override
    public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException 
    {
      Drawable tile = drawMapsforgeTile(aState);
      if(tile == null) 
        tile = downloadTile(aState);
      return tile;
    } // loadFile
    
    private Drawable drawMapsforgeTile(final MapTileRequestState aState)
    {
      if(tileSource_ == null) 
        return null;

      try {
        final MapTile tile = aState.getMapTile();
        return tileSource_.getDrawable(tile.getX(), tile.getY(), tile.getZoomLevel());
      }
      catch(Exception e) {
        return null;
      }
    } // drawMapsforgeTile
    
    private Drawable downloadTile(final MapTileRequestState aState) throws CantContinueException
    {
      if(fallbackTileSource_ == null)
        return null;

      final MapTile tile = aState.getMapTile();

      try {
        if (!isNetworkAvailable()) 
          return null;

        final String tileUrl = fallbackTileSource_.getTileURLString(tile);
        
        final InputStream in = fetchTileFromUrl(tileUrl);
        if(in == null)
          return null;
        
        final byte[] data = loadTileByteArray(in);
    
        final Drawable result = fallbackTileSource_.getDrawable(new ByteArrayInputStream(data));  
        return result;
      } catch (final UnknownHostException e) {
        throw new CantContinueException(e);
      } catch (final LowMemoryException e) {
        throw new CantContinueException(e);
      } catch (final Exception e) {
        return null;
      }
    } // downloadTile
    
    private boolean isNetworkAvailable()
    {
      return (networkCheck_ == null || networkCheck_.getNetworkAvailable()); 
    } // networkAvailable
    
    private InputStream fetchTileFromUrl(final String tileURLString) throws ClientProtocolException, IOException
    {
      if (TextUtils.isEmpty(tileURLString)) 
        return null;

      final HttpClient client = new DefaultHttpClient();
      final HttpUriRequest head = new HttpGet(tileURLString);
      final HttpResponse response = client.execute(head);

      final org.apache.http.StatusLine line = response.getStatusLine();
      if (line.getStatusCode() != 200) 
        return null;

       final HttpEntity entity = response.getEntity();
       return (entity != null) ? entity.getContent() : null;
    } // fetchTileFromUrl
    
    private byte[] loadTileByteArray(final InputStream in) throws IOException
    {
      final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
      final OutputStream out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
      try {
        StreamUtils.copy(in, out);
        out.flush();
        return dataStream.toByteArray();
      }
      finally {
        StreamUtils.closeStream(in);
        StreamUtils.closeStream(out);
      } // finally
    } // loadTileByteArray
  } // TileLoader
} // MapsForgeOSMDroidTileProvider
