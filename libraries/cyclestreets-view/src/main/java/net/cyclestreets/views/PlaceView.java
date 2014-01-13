package net.cyclestreets.views;

import net.cyclestreets.view.R;
import android.content.Context;
import android.util.AttributeSet;

public class PlaceView extends PlaceViewBase
{
  public PlaceView(final Context context)
  {
    this(context, null);
  } // PlaceView

  public PlaceView(final Context context, final AttributeSet attrs)
  {
    super(context, R.layout.placetextview, attrs);
  } // PlaceView
} // class PlaceView
