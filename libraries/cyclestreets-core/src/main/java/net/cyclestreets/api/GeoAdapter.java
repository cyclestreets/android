package net.cyclestreets.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.cyclestreets.core.R;

import org.osmdroid.util.BoundingBoxE6;

import java.util.List;

public class GeoAdapter extends ArrayAdapter<GeoPlace>
{
  static private final int AdapterViewId = R.layout.geo_item_2line;

  private final LayoutInflater inflater;

  protected GeoAdapter(final Context context)
  {
    super(context, AdapterViewId);
    inflater = LayoutInflater.from(context);
  } // GeoAdapter

  @Override
  public View getView(int position,
            final View convertView,
            final ViewGroup parent)
  {
    final View row = inflater.inflate(AdapterViewId, parent, false);
    final GeoPlace p = getItem(position);

    setText(row, android.R.id.text1, p.name());
    setText(row, android.R.id.text2, p.near());

    return row;
  } // getView

  private void setText(final View parent, final int id, final String text)
  {
    ((TextView)parent.findViewById(id)).setText(text);
  } // setText

  protected GeoPlaces geoCode(final String search,
                              final BoundingBoxE6 bounds)
  {
    try {
      return GeoPlaces.search(search, bounds);
    }
    catch(Exception e) {
      return GeoPlaces.EMPTY;
    } // catch
  } // geoCode

  protected void addAll(final List<GeoPlace> list)
  {
    for(final GeoPlace p : list)
      add(p);
  } // addAll
} // class GeoAdapter
