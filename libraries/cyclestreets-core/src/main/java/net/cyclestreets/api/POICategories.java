package net.cyclestreets.api;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class POICategories implements Iterable<POICategory> {
  private final List<POICategory> categories = new ArrayList<>();
  private final Map<String, POICategory> categoryMap = new HashMap<>();
  
  public POICategories(Collection<POICategory> categories) {
    this.categories.addAll(categories);
    for (POICategory category : categories) {
      categoryMap.put(category.name(), category);
    }
  }
  
  public int count() { return categories.size(); }
  public POICategory get(int index) { return categories.get(index); }
  public POICategory get(final String name) { return categoryMap.get(name); }
  public Iterator<POICategory> iterator() { return categories.iterator(); }
  
  //////////////////////////////////////////////
  private static POICategories loaded_;
  public static final int IconSize = 32;
  
  public static POICategories get() {
    if (loaded_ == null)
      loaded_ = load();
    return loaded_;
  }
  
  public static POICategories load() {
    try {
      return ApiClient.getPOICategories(IconSize);
    } catch(Exception e) {
      // ah
    }
    return null;
  }
  
  public static boolean loaded() { return loaded_ != null; }

  public static void backgroundLoad() {
    new GetPOICategoriesTask().execute();
  }
  
  private static class GetPOICategoriesTask extends AsyncTask<Void,Void,POICategories> {
    protected POICategories doInBackground(Void... params) {
      try {
        return ApiClient.getPOICategories(POICategories.IconSize);
      } catch (final Exception ex) {
        // never mind, eh?
      }
      return null;
    }
    
    @Override
    protected void onPostExecute(final POICategories cats) {
      POICategories.loaded_ = cats;
    }
  }
}
