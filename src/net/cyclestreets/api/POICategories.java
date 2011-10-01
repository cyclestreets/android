package net.cyclestreets.api;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class POICategories
{
  private final List<POICategory> cats_;
  
  private POICategories()
  {
    cats_ = new ArrayList<POICategory>();
  } // POICategories
  
  private void add(final POICategory cat) { cats_.add(cat); }
  
  public int count() { return cats_.size(); }
  public POICategory get(int index) { return cats_.get(index); }
  public Iterator<POICategory> iterator() { return cats_.iterator(); }
  
  
  static public Factory<POICategories> factory() { 
    return new POICategoriesFactory();
  }
  
  static private class POICategoriesFactory extends Factory<POICategories>
  {
    private String key_;
    private String name_;
    private String shortName_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      cats_ = new POICategories();
      
      final RootElement root = new RootElement("poitypes");
      final Element item = root.getChild("poitypes").getChild("poitype");
      item.setStartElementListener(new StartElementListener() {
        @Override
        public void start(Attributes attributes)
        {
          key_ = null;
          name_ = null;
          shortName_ = null;
        }
      });
      item.setEndElementListener(new EndElementListener(){
          public void end() {
            cats_.add(new POICategory(key_, shortName_, name_));
          }
      });
      item.getChild("key").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            key_ = body;
          }
      });
      item.getChild("name").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            name_ = body;
          }
      });
      item.getChild("shortname").setEndTextElementListener(new EndTextElementListener(){
          public void end(String body) {
            shortName_ = body;
          }
      });

      return root.getContentHandler();
    } // contentHandler

    @Override
    protected POICategories get()
    {
      return cats_;
    } // get
    
    private POICategories cats_;
  } // POICategories
} // class POICategories
