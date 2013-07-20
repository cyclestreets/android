package net.cyclestreets.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import net.cyclestreets.util.Collections;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.os.AsyncTask;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

public class PhotomapCategories 
{
  static private PhotomapCategories loaded_;

  private List<PhotomapCategory> categories_ = new ArrayList<PhotomapCategory>();
  private List<PhotomapCategory> metaCategories_ = new ArrayList<PhotomapCategory>();

  public List<PhotomapCategory> categories() { return categories_; }
  public List<PhotomapCategory> metaCategories() { return metaCategories_; }
  
  void addCategory(final String tag, 
                   final String name, 
                   final String description,
                   final long ordering)
  {
    categories_.add(new PhotomapCategory(tag, name, description, ordering));
  } // addCategory
  
  void addMetaCategory(final String tag, 
                       final String name, 
                       final String description,
                       final long ordering)
  {
    metaCategories_.add(new PhotomapCategory(tag, name, description, ordering));
  } // addMetaCategory

  //////////////////////////////////////////////////////////////
  static private class Listener 
  {
    private String tag_;
    private String name_;
    private String description_;
    private long ordering_;
    
    private final Map<String, EndTextElementListener> endlisteners_;
    
    public Listener() 
    {
      endlisteners_ = Collections.map("tag", tagListener()).
                                  map("name", nameListener()).
                                  map("description", descriptionListener()).
                                  map("ordering", orderingListener());
    } // Listener

    public StartElementListener start() {
      return new StartElementListener() {
        @Override
        public void start(Attributes attributes)
        {
          tag_ = null;
          name_ = null;
          description_ = null;
          ordering_ = -1;
        }
      };
    } // start
    
    public Map<String, EndTextElementListener> endListeners()
    {
      return endlisteners_;
    } // endListener
    
    public EndTextElementListener tagListener() {
      return new EndTextElementListener() {
        @Override
        public void end(String body) { tag_ = body; }
      };         
    } // tag
    public EndTextElementListener nameListener() {
      return new EndTextElementListener() {
        @Override
        public void end(String body) { name_ = body; }
      };         
    } // name
    public EndTextElementListener descriptionListener() {
      return new EndTextElementListener() {
        @Override
        public void end(String body) { description_ = body; }
      };         
    } // description
    public EndTextElementListener orderingListener() {
      return new EndTextElementListener() {
        @Override
        public void end(String body) { ordering_ = Long.parseLong(body); }
      };         
    } // ordering
    
    public String tag() { return tag_; }
    public String name() { return name_; }
    public String description() { return description_; }
    public long ordering() { return ordering_; }
  } // class Listener

  static private class PhotomapCategoriesFactory extends Factory<PhotomapCategories>
  {    
    private PhotomapCategories cats_;
    
    @Override
    protected ContentHandler contentHandler()
    {
      cats_ = new PhotomapCategories();
      
      final RootElement root = new RootElement("photomapcategories");
      final Element cat = root.getChild("categories").getChild("category");
      final Element metaCat = root.getChild("metacategories").getChild("metacategory");
      
      final Listener listener = new Listener();
      
      cat.setStartElementListener(listener.start());
      metaCat.setStartElementListener(listener.start());
      
      for(final String n : listener.endListeners().keySet())
      {
        final EndTextElementListener l = listener.endListeners().get(n);
        cat.getChild(n).setEndTextElementListener(l);
        metaCat.getChild(n).setEndTextElementListener(l);
      } // for ...
      
      cat.setEndElementListener(new EndElementListener(){
          public void end() {
            cats_.addCategory(listener.tag(), 
                              listener.name(), 
                              listener.description(), 
                              listener.ordering());
          } // end
      });
      metaCat.setEndElementListener(new EndElementListener(){
        public void end() {
          cats_.addMetaCategory(listener.tag(), 
                                listener.name(), 
                                listener.description(), 
                                listener.ordering());
        } // end
      });

      return root.getContentHandler();
    } // contentHandler

    @Override
    protected PhotomapCategories get()
    {
      return cats_;
    } // get
  } // class PhotomapCategoriesFactory
  
  //////////////////////////////////////////////
  static public Factory<PhotomapCategories> factory() { 
    return new PhotomapCategoriesFactory();
  } // factory

  static public PhotomapCategories get() 
  {
    if(loaded_ == null)
      loaded_ = load();
    return loaded_;
  } // get
  
  static public boolean loaded() { return loaded_ != null; }
  
  static public PhotomapCategories load()
  {
    try {
      return ApiClient.getPhotomapCategories();      
    } // try
    catch(Exception e) {
      // ah
    } // catch
    return null;
  } // load
  
  static public void backgroundLoad()
  {
    new GetPhotomapCategoriesTask().execute();
  } // backgroundLoad
  
  static private class GetPhotomapCategoriesTask extends AsyncTask<Void,Void,PhotomapCategories>
  {
    protected PhotomapCategories doInBackground(Void... params) 
    {
      return PhotomapCategories.load();
    } // doInBackground
    
    @Override
    protected void onPostExecute(final PhotomapCategories cats) 
    {
      PhotomapCategories.loaded_ = cats;
    } // onPostExecute
  } // GetPhotomapCategoriesTask

} // class PhotomapCategories
