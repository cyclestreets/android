package net.cyclestreets.api;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;

import net.cyclestreets.api.json.JsonObjectHandler;
import net.cyclestreets.api.json.JsonRootHandler;
import net.cyclestreets.api.json.JsonRootObjectHandler;
import net.cyclestreets.api.json.JsonStringHandler;
import net.cyclestreets.util.Base64;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    } // POICategoriesFactory

    @Override
    protected POICategories get() { return cats_; }

    @Override
    protected JsonRootHandler rootHandler() {
      final JsonRootHandler root = new JsonRootObjectHandler();

      final JsonObjectHandler categoryHandler = root.getObject("types").getObject(JsonObjectHandler.ANY_OBJECT);

      final Map<String, String> details = new HashMap<>();
      final JsonStringHandler.Listener stringListener = new JsonStringHandler.Listener() {
        @Override
        public void string(String name, String value) {
          details.put(name, value);
        }
      }; // JsonStringHandler.Listener

      categoryHandler.getString("id").setListener(stringListener);
      categoryHandler.getString("name").setListener(stringListener);
      categoryHandler.getString("icon").setListener(stringListener);

      categoryHandler.setBeginObjectListener(new JsonObjectHandler.BeginListener() {
        @Override
        public void begin(String name) {
          details.clear();
        }
      });
      categoryHandler.setEndObjectListener(new JsonObjectHandler.EndListener() {
        @Override
        public void end() {
          cats_.add(new POICategory(details.get("id"),
                                    details.get("name"),
                                    poiIcon(details.get("icon"))));
        }
      });

      return root;
    } // rootHandler

    private Drawable poiIcon(final String iconAsBase64) {
      final Bitmap bmp = decodeIcon(iconAsBase64);
      return new BitmapDrawable(context_.getResources(), bmp);
    } // poiIcon

    private static Bitmap decodeIcon(final String iconAsBase64) {
      try {
        byte[] bytes = Base64.decode(iconAsBase64);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
      } // try
      catch(Exception e) {
        // never mind for the moment
      } // catch
      return null;
    } // decodeIcon
  } // POICategoriesFactory

  //////////////////////////////////////////////
  private static POICategories loaded_;
  public static final int IconSize = 32;
  
  public static POICategories get() {
    if(loaded_ == null)
      loaded_ = load();
    return loaded_;
  } // get
  
  public static POICategories load() {
    try {
      return ApiClient.getPOICategories(IconSize);
    } catch(Exception e) {
      // ah
    }
    return null;
  } // get
  
  public static boolean loaded() { return loaded_ != null; }

  public static void backgroundLoad() {
    new GetPOICategoriesTask().execute();
  } // backgroundLoad
  
  private static class GetPOICategoriesTask extends AsyncTask<Void,Void,POICategories> {
    protected POICategories doInBackground(Void... params) {
      try {
        return ApiClient.getPOICategories(POICategories.IconSize);
      } catch (final Exception ex) {
        // never mind, eh?
      }
      return null;
    } // doInBackground
    
    @Override
    protected void onPostExecute(final POICategories cats) {
      POICategories.loaded_ = cats;
    } // onPostExecute
  } // GetPOICategoriesTask
} // class POICategories
