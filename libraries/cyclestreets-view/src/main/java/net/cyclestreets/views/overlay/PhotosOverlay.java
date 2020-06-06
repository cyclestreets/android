package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import net.cyclestreets.DisplayPhoto;
import net.cyclestreets.api.Photo;
import net.cyclestreets.api.Photos;
import net.cyclestreets.photos.IndividualPhoto;
import net.cyclestreets.views.CycleMapView;

public class PhotosOverlay extends LiveItemOverlay<PhotosOverlay.PhotoItem> implements PauseResumeListener, IndividualPhoto.Listener {
  public static class PhotoItem extends OverlayItem {
    private final Photo photo_;
    private final PhotoMarkers photoMarkers_;

    public PhotoItem(final Photo photo, final PhotoMarkers photoMarkers) {
      super(photo.id() + "", photo.caption(), photo.position());
      photo_ = photo;
      photoMarkers_ = photoMarkers;
    }

    public Photo photo() { return photo_; }

    // Markers
    @Override
    public Drawable getMarker(int stateBitset) {
      return photoMarkers_.getMarker(photo_);
    }

    // Equality testing
    @Override
    public int hashCode() { return ((photo_ == null) ? 0 : photo_.id()); }

    /*
     * PhotoItems are equal if underlying Photos have the same id
     */
    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final PhotoItem other = (PhotoItem) obj;
      if (photo_ == null)
        return (other.photo_ == null);

      return (photo_.id() == other.photo_.id());
    }

    @Override
    public String toString() {
      return "PhotoItem [photo=" + photo_ + "]";
    }
  }

  /////////////////////////////////////////////////////
  private final Context context_;
  private final PhotoMarkers photoMarkers_;

  public PhotosOverlay(final CycleMapView mapView) {
    super(mapView,
          true);

    context_ = mapView.getContext();
    photoMarkers_ = new PhotoMarkers(context_.getResources());
  }

  /////////////////////////////////////////////////////
  @Override
  public void onResume(SharedPreferences prefs) {
    IndividualPhoto.INSTANCE.registerListener(this);
  }

  @Override
  public void onPause(SharedPreferences.Editor prefs) {
    IndividualPhoto.INSTANCE.unregisterListener(this);
  }

  ///////////////////////////////////////////////////
  @Override
  public void onPhotoLoaded(@NonNull Photo photo) {
    centreOn(photo.position());
    DisplayPhoto.launch(photo, context_);
  }

  ///////////////////////////////////////////////////
  @Override
  protected boolean onItemSingleTap(final PhotoItem item) {
    showPhoto(item);
    return true;
  }

  @Override
  protected boolean onItemDoubleTap(final PhotoItem item) {
    showPhoto(item);
    return true;
  }

  private void showPhoto(final PhotoItem item) {
    DisplayPhoto.launch(item.photo(), context_);
  }

  ///////////////////////////////////////////////////
  protected boolean fetchItemsInBackground(final IGeoPoint mapCentre,
                                           final int zoom,
                                           final BoundingBox boundingBox) {
    GetPhotosTask.fetch(this, boundingBox);
    return true;
  }

  /////////////////////////////////////////////////////
  /////////////////////////////////////////////////////
  private static class GetPhotosTask extends AsyncTask<Object,Void,Photos> {
    static void fetch(final PhotosOverlay overlay,
                      final Object... params) {
      new GetPhotosTask(overlay).execute(params);
    }

    //////////////////////////////////////////////////////
    private final PhotosOverlay overlay_;

    private  GetPhotosTask(final PhotosOverlay overlay) {
      overlay_ = overlay;
    }

    protected Photos doInBackground(Object... params) {
      final BoundingBox boundingBox = (BoundingBox)params[0];

      try {
        return Photos.load(boundingBox);
      } catch (final Exception ex) {
        // never mind, eh?
      }
      return null;
    }

    @Override
    protected void onPostExecute(final Photos photos) {
      final List<PhotosOverlay.PhotoItem> items = new ArrayList<>();

      if (photos != null)
        for (final Photo photo: photos)
          items.add(new PhotosOverlay.PhotoItem(photo, overlay_.photoMarkers_));

      overlay_.setItems(items);
    }
  }
}
