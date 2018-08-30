package net.cyclestreets.api;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class PhotomapCategories
{
  private static PhotomapCategories loaded_;

  private final List<PhotomapCategory> categories = new ArrayList<>();
  private final List<PhotomapCategory> metaCategories = new ArrayList<>();

  public List<PhotomapCategory> categories() { return categories; }
  public List<PhotomapCategory> metaCategories() { return metaCategories; }

  public PhotomapCategories(List<PhotomapCategory> categories,
                            List<PhotomapCategory> metaCategories) {
    this.categories.addAll(categories);
    this.metaCategories.addAll(metaCategories);
  }

  public static PhotomapCategories get() {
    if (loaded_ == null)
      loaded_ = load();
    return loaded_;
  }

  public static boolean loaded() { return loaded_ != null; }

  public static PhotomapCategories load() {
    try {
      return ApiClient.INSTANCE.getPhotomapCategories();
    } catch (Exception e) {
      // ah
    }
    return null;
  }

  public static void backgroundLoad() {
    new GetPhotomapCategoriesTask().execute();
  }

  private static class GetPhotomapCategoriesTask extends AsyncTask<Void,Void,PhotomapCategories> {
    protected PhotomapCategories doInBackground(Void... params) {
      return PhotomapCategories.load();
    }

    @Override
    protected void onPostExecute(final PhotomapCategories cats) {
      PhotomapCategories.loaded_ = cats;
    }
  }
}
