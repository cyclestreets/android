package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class Photos implements Iterable<Photo>
{
  private List<Photo> photos_;
  
  protected Photos()
  {
    photos_ = new ArrayList<Photo>();
  } // Photos
  
  private void add(final Photo photo)
  {
    photos_.add(photo);
  } // add
  
  @Override
  public Iterator<Photo> iterator()
  {
    return photos_.iterator();
  } // iterator
  
  /////////////////////////////////////////////////////////////
  static public Photos load(final IGeoPoint centre,
                            final int zoom, 
                            final BoundingBoxE6 boundingBox) 
     throws Exception 
  {
    return load(centre.getLongitudeE6() / 1E6, 
                centre.getLatitudeE6() / 1E6, 
                zoom,
                boundingBox.getLonEastE6() / 1E6,
                boundingBox.getLonWestE6() / 1E6,
                boundingBox.getLatNorthE6() / 1E6,
                boundingBox.getLatSouthE6() / 1E6);
  } // load
  
  static private Photos load(final double clong,
                             final double clat,
                             final int zoom,
                             final double e,
                             final double w,
                             final double n,
                             final double s)
      throws Exception
  {
    return ApiClient.getPhotos(clong, clat, zoom, e, w, n, s);
  } // load

  ////////////////////////////////////////////////////
  static public Factory<Photos> factory() { 
    return new PhotosFactory();
  } // factory
  
  static private class PhotosFactory extends Factory<Photos>
  {    
    private Photos photos_;

    public PhotosFactory() 
    {
    } // PhotosFactory
    
    @Override
    protected ContentHandler contentHandler()
    {
      photos_ = new Photos();
      
      final RootElement root = new RootElement("markers");
      final Element item = root.getChild("marker");
      item.setStartElementListener(new StartElementListener() {
        @Override
        public void start(final Attributes attributes)
        {
          final String id = attributes.getValue("id");
          final String feature = attributes.getValue("feature");
          final String caption = attributes.getValue("caption");
          final String url = attributes.getValue("shortlink");
          final String thumbnailUrl = attributes.getValue("thumbnailUrl");
          final String lat = attributes.getValue("latitude");
          final String lon = attributes.getValue("longitude");
          
          try { 
            final Photo newPhoto = new Photo(Integer.parseInt(id),
                                             Integer.parseInt(feature),
                                             caption,
                                             url,
                                             thumbnailUrl,
                                             new GeoPoint(Double.parseDouble(lat),
                                                          Double.parseDouble(lon)));
            photos_.add(newPhoto);
          } // try
          catch(Exception e) {
            // never mind
          } // catch
        } // start
      });

      return root.getContentHandler();
    } // contentHandler

    @Override
    protected Photos get()
    {
      return photos_;
    } // get
  } // class PhotosFactory
} // class Photos
