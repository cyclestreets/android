package net.cyclestreets.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class GeoPlaces implements Iterable<GeoPlace>
{
  private List<GeoPlace> places_;
  
  private GeoPlaces()
  {
    places_ = new ArrayList<>();
  } // GeoPlaces
  
  private void add(final GeoPlace place) { places_.add(place); }
  
  @Override
  public Iterator<GeoPlace> iterator() { return places_.iterator(); }
  
  public boolean isEmpty() { return places_.isEmpty(); }
  
  public int size() { return places_.size(); }
  public GeoPlace get(int index) { return places_.get(index); }
  
  public List<GeoPlace> asList() { return places_; }
  
  static public GeoPlaces EMPTY = new GeoPlaces();
  
  ///////////////////////////////////////////////
  static public GeoPlaces search(final String searchTerm,
                                 final BoundingBoxE6 bounds)
    throws Exception
  {
    return search(searchTerm,
                  bounds.getLatNorthE6() / 1E6,
                  bounds.getLatSouthE6() / 1E6,
                  bounds.getLonEastE6() / 1E6,
                  bounds.getLonWestE6() / 1E6);
  } // search

  static public GeoPlaces search(final String searchTerm,
                                 double n,
                                 double s,
                                 double e,
                                 double w)
    throws Exception
  {
    return ApiClient.geoCoder(searchTerm, n, s, e, w);
  } // search
  
  ////////////////////////////////////////////////////
  static public Factory<GeoPlaces> factory() { 
    return new GeoPlacesFactory();
  } // factory
  
  static private class GeoPlacesFactory extends Factory.XmlReader<GeoPlaces>
  {    
    private GeoPlaces places_;
    private String name_;
    private String near_;
    private String lat_;
    private String lon_;
    
    public GeoPlacesFactory() 
    {
    } // GeoPlacesFactory
    
    @Override
    protected ContentHandler contentHandler()
    {
      places_ = new GeoPlaces();
      
      final RootElement root = new RootElement("sayt");
      final Element item = root.getChild("results").getChild("result");
      item.setStartElementListener(new StartElementListener() {
        @Override
        public void start(Attributes attributes)
        {
          name_ = null;
          near_ = null;
          lat_ = null;
          lon_ = null;
        }
      });
      item.setEndElementListener(new EndElementListener(){
          public void end() {
            final GeoPoint coord = new GeoPoint(Double.parseDouble(lat_),
                                                Double.parseDouble(lon_));
            places_.add(new GeoPlace(coord, name_, near_));
          }
      });
      item.getChild("name").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            name_ = body;
          }
      });
      item.getChild("near").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            near_ = body;
          }
      });
      item.getChild("latitude").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            lat_ = body;
          }
      });
      item.getChild("longitude").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            lon_ = body;
          }
      });

      return root.getContentHandler();
    } // contentHandler

    @Override
    protected GeoPlaces get()
    {
      return places_;
    } // get
  } // POICategoriesFactory
} // class GeoPlaces
