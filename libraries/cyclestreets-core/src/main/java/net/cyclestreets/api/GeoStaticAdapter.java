package net.cyclestreets.api;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.util.BoundingBox;

public class GeoStaticAdapter extends GeoAdapter
{
  public interface OnPopulatedListener  {
    void onPopulated();
  }

  private final OnPopulatedListener listener_;

  public GeoStaticAdapter(final Context context,
              final String search,
                final BoundingBox bounds,
                final OnPopulatedListener listener) {
    super(context);

    listener_ = listener;

    asyncGeoCode(search, bounds);
  }

  private void populate(final GeoPlaces places) {
    addAll(places.asList());

    if (listener_ != null)
      listener_.onPopulated();
  }

  private void asyncGeoCode(final String search,
                            final BoundingBox bounds) {
    final AsyncGeoCoder coder = new AsyncGeoCoder(this);
    coder.execute(search, bounds);
  }

  private static class AsyncGeoCoder extends AsyncTask<Object, Void, GeoPlaces>  {
    private GeoStaticAdapter owner_;

    public AsyncGeoCoder(final GeoStaticAdapter adapter) {
      owner_ = adapter;
    }

    @Override
    protected GeoPlaces doInBackground(Object... params) {
      final String search = (String)params[0];
      final BoundingBox box = (BoundingBox)params[1];
      return owner_.geoCode(search, box);
    }

    @Override
    protected void onPostExecute(final GeoPlaces photos) {
      owner_.populate(photos);
    }
  }
}
