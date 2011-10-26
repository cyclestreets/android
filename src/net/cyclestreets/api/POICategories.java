package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.Base64;
import net.cyclestreets.util.Bitmaps;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class POICategories implements Iterable<POICategory>
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
  
  static public Factory<POICategories> factory(final Context context) { 
    return new POICategoriesFactory(context);
  } // factory
  
  static private class POICategoriesFactory extends Factory<POICategories>
  {    
    private final Context context_;
    private POICategories cats_;
    private String key_;
    private String name_;
    private String shortName_;
    private String icon_;
    
    public POICategoriesFactory(final Context context) 
    {
      context_ = context;
    } // POICategoriesFactory
    
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
          icon_ = null;
        }
      });
      item.setEndElementListener(new EndElementListener(){
          public void end() {
            final Drawable icon = new BitmapDrawable(context_.getResources(), decodeIcon(icon_));
            cats_.add(new POICategory(key_, shortName_, name_, icon));
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
      item.getChild("icon").setEndTextElementListener(new EndTextElementListener(){
        public void end(String body) {
          icon_ = body;
        }
      });

      return root.getContentHandler();
    } // contentHandler

    @Override
    protected POICategories get()
    {
      return cats_;
    } // get
    
    static private Bitmap decodeIcon(final String iconAsBase64)
    {
      try {
        byte[] bytes = Base64.decode(iconAsBase64);
        return Bitmaps.loadStream(new ByteArrayInputStream(bytes));
      } // try
      catch(Exception e) {
        // never mind for the moment
      } // catch
      return null;
    } // decodeIcon
  } // POICategories

  //////////////////////////////////////////////
  static private POICategories loaded_;
  static private int iconSize_;
  
  static public POICategories get() 
  {
    if(loaded_ == null)
      load();
    return loaded_;
  } // get
  
  static public boolean loaded() { return loaded_ != null; }
  
  static public void load()
  {
    try {
      iconSize_ = CycleStreetsPreferences.iconSize();
      loaded_ = ApiClient.getPOICategories(iconSize_);      
    }
    catch(Exception e) {
      // ah
    }
  } // load
  
  static public void reload()
  {
    if(iconSize_ == CycleStreetsPreferences.iconSize())
      return;
    iconSize_ = 0;
    loaded_ = null;
  } // reload
  
  static public void backgroundLoad()
  {
    new GetPOICategoriesTask().execute();
  } // backgroundLoad
  
  static private class GetPOICategoriesTask extends AsyncTask<Void,Void,POICategories>
  {
    private int iconSize_;
    
    protected POICategories doInBackground(Void... params) 
    {
      try {
        iconSize_ = CycleStreetsPreferences.iconSize();
        return ApiClient.getPOICategories(iconSize_);
      }
      catch (final Exception ex) {
        // never mind, eh?
      }
      return null;
    } // doInBackground
    
    @Override
    protected void onPostExecute(final POICategories cats) 
    {
      POICategories.iconSize_ = iconSize_;
      POICategories.loaded_ = cats;
    } // onPostExecute
  } // GetPOICategoriesTask
} // class POICategories
