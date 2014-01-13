package net.cyclestreets.views;

import net.cyclestreets.view.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class PlaceViewWithCancel extends PlaceViewBase
{
  final private ImageButton cancel_;

  public PlaceViewWithCancel(final Context context)
  {
    this(context, null);
  } // PlaceView

  public PlaceViewWithCancel(final Context context, final AttributeSet attrs)
  {
    super(context, R.layout.placetextviewcancel, attrs);

    cancel_ = (ImageButton)findViewById(R.id.cancelBtn);
  } // PlaceView

  public void enableCancel(final boolean enabled) { cancel_.setEnabled(enabled); }
  public void setCancelOnClick(final OnClickListener listener) { cancel_.setOnClickListener(listener); }
} // class PlaceView
