package net.cyclestreets.views.overlay;

import java.util.Iterator;

import net.cyclestreets.views.CycleMapView;

import org.osmdroid.views.overlay.Overlay;

public class OverlayIterator<T> implements Iterator<T>
{
  private Iterator<Overlay> iter_;
  private Class<T> targetClass_;
  private T current_;

  public OverlayIterator(final CycleMapView mapView, final Class<T> cl) {
    targetClass_ = cl;
    iter_ = mapView.getOverlays().iterator();
    current_ = advance();
  }

  public boolean hasNext() {
    return current_ != null;
  }

  public T next() {
    T c = current_;
    current_ = advance();
    return c;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  ////////////////////////////
    @SuppressWarnings("unchecked")
  private T advance() {
    T n = null;

    while ((n == null) && iter_.hasNext()) {
      Overlay o = iter_.next();
      if (targetClass_.isInstance(o))
        n = (T)o;
    }

    return n;
  }
}
