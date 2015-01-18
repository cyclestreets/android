package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.api.json.JsonReader;
import net.cyclestreets.util.Base64;
import net.cyclestreets.util.Bitmaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class POICategories implements Iterable<POICategory> {
  private final List<POICategory> cats_;
  
  private POICategories()
  {
    cats_ = new ArrayList<>();
  } // POICategories
  
  private void add(final POICategory cat) { cats_.add(cat); }
  
  public int count() { return cats_.size(); }
  public POICategory get(int index) { return cats_.get(index); }
  public POICategory get(final String name) {
    for(final POICategory c : cats_)
      if(c.name().equals(name))
        return c;
    return null;
  } // get
  public Iterator<POICategory> iterator() { return cats_.iterator(); }
  
  ////////////////////////////////////////////////////
  public static Factory<POICategories> factory(final Context context) {
    return new POICategoriesFactory(context);
  } // factory

  private static class POICategoriesFactory extends Factory.JsonProcessor<POICategories> {
    private final Context context_;
    private POICategories cats_;

    public POICategoriesFactory(final Context context) {
      context_ = context;
      cats_ = new POICategories();
    } // POICategoriesFactory2

    protected POICategories readJson(final JsonReader reader) throws IOException {
      reader.beginObject();

      while (reader.hasNext()) {
        final String name = reader.nextName();
        if ("types".equals(name))
          readCategories(reader);
        else
          reader.skipValue();
      } // while

      reader.endObject();

      return cats_;
    } // readJson

    private void readCategories(final JsonReader reader) throws IOException {
      reader.beginObject();

      while (reader.hasNext()) {
        reader.nextName();
        readCategory(reader);
      } // while ...

      reader.endObject();
    } // readCategories

    private void readCategory(final JsonReader reader) throws IOException {
      reader.beginObject();

      String id = null;
      String desc = null;
      String icon = null;

      while (reader.hasNext()) {
        final String name = reader.nextName();
        if ("id".equals(name))
          id = reader.nextString();
        else if ("name".equals(name))
          desc = reader.nextString();
        else if ("icon".equals(name))
          icon = reader.nextString();
        else
          reader.skipValue();
      } // while ...

      cats_.add(new POICategory(id, desc, poiIcon(icon)));

      reader.endObject();
    } // readCategory

    private Drawable poiIcon(final String iconAsBase64) {
      final Bitmap bmp = decodeIcon(iconAsBase64);
      return new BitmapDrawable(context_.getResources(), bmp);
    } // poiIcon

    private static Bitmap decodeIcon(final String iconAsBase64) {
      try {
        byte[] bytes = Base64.decode(iconAsBase64);
        return Bitmaps.loadStream(new ByteArrayInputStream(bytes));
      } // try
      catch(Exception e) {
        // never mind for the moment
      } // catch
      return null;
    } // decodeIcon
  } // POICategoriesFactory

  //////////////////////////////////////////////
  private static POICategories loaded_;
  private static int iconSize_;
  
  public static POICategories get() {
    if(loaded_ == null)
      load();
    return loaded_;
  } // get
  
  public static POICategories load(final int requestedIconSize) {
    try {
      return ApiClient.getPOICategories(requestedIconSize);      
    } catch(Exception e) {
      // ah
    }
    return null;
  } // get
  
  public static boolean loaded() { return loaded_ != null; }
  
  public static void load() {
    try {
      iconSize_ = CycleStreetsPreferences.iconSize();
      loaded_ = ApiClient.getPOICategories(iconSize_);      
    } catch(Exception e) {
      // ah
    }
  } // load
  
  public static void reload() {
    if(iconSize_ == CycleStreetsPreferences.iconSize())
      return;
    iconSize_ = 0;
    loaded_ = null;
  } // reload
  
  public static void backgroundLoad() {
    new GetPOICategoriesTask().execute();
  } // backgroundLoad
  
  private static class GetPOICategoriesTask extends AsyncTask<Void,Void,POICategories> {
    private int iconSize_;
    
    protected POICategories doInBackground(Void... params) {
      try {
        iconSize_ = CycleStreetsPreferences.iconSize();
        return ApiClient.getPOICategories(iconSize_);
      } catch (final Exception ex) {
        // never mind, eh?
      }
      return null;
    } // doInBackground
    
    @Override
    protected void onPostExecute(final POICategories cats) {
      POICategories.iconSize_ = iconSize_;
      POICategories.loaded_ = cats;
    } // onPostExecute
  } // GetPOICategoriesTask
} // class POICategories
