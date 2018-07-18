package net.cyclestreets.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.cyclestreets.core.R;

import org.osmdroid.util.BoundingBox;

import java.util.List;

public class GeoAdapter extends ArrayAdapter<GeoPlace>
{
  private static final int AdapterViewId = R.layout.geo_item_2line;

  private final LayoutInflater inflater;

  protected GeoAdapter(final Context context) {
    super(context, AdapterViewId);
    inflater = LayoutInflater.from(context);
  }

  @Override
  public View getView(int position,
            final View convertView,
            final ViewGroup parent) {
    final View row = inflater.inflate(AdapterViewId, parent, false);
    final GeoPlace p = getItem(position);

    setText(row, android.R.id.text1, p.name());
    setText(row, android.R.id.text2, p.near());

    return row;
  }

  private void setText(final View parent, final int id, final String text) {
    ((TextView)parent.findViewById(id)).setText(text);
  }

  protected GeoPlaces geoCode(final String search,
                              final BoundingBox bounds) {
    try {
      return GeoPlaces.search(search, bounds);
    }
    catch (Exception e) {
      return GeoPlaces.EMPTY;
    }
  }

  protected void addAll(final List<GeoPlace> list) {
    for (final GeoPlace p : list)
      add(p);
  }
}
