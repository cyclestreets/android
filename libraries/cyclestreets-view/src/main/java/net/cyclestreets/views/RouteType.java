package net.cyclestreets.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.RoutePlans;
import net.cyclestreets.view.R;

public class RouteType extends LinearLayout {
  private final Spinner routeTypeSpinner_;
  private final String[] plans_;

  public RouteType(final Context context)

  {
    this(context, null);
  } // RouteType

  public RouteType(final Context context, final AttributeSet attrs) {
    super(context, attrs);

    setOrientation(HORIZONTAL);

    final LayoutInflater inflator = LayoutInflater.from(context);
    inflator.inflate(R.layout.journey_type, this);

    routeTypeSpinner_ = (Spinner)findViewById(R.id.routeTypeSpinner);

    plans_ = RoutePlans.allPlans();
    final String defaultType = CycleStreetsPreferences.routeType();
    routeTypeSpinner_.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, plans_));
    for(int i = 0; i != plans_.length; ++i)
      if (plans_[i].equals(defaultType))
        routeTypeSpinner_.setSelection(i);

  } // RouteType

  public String selectedType() {
    final int sel = routeTypeSpinner_.getSelectedItemPosition();
    final String routeType = plans_[sel];
    return routeType;
  } // selectedType
} // RouteType
