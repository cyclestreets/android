package net.cyclestreets.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.RouteTypeMapper;
import net.cyclestreets.view.R;

public class RouteType extends LinearLayout {
  private final RadioGroup routeTypeGroup_;

  public RouteType(final Context context)
  {
    this(context, null);
  } // RouteType

  public RouteType(final Context context, final AttributeSet attrs) {
    super(context, attrs);

    setOrientation(HORIZONTAL);

    final LayoutInflater inflator = LayoutInflater.from(context);
    inflator.inflate(R.layout.journey_type, this);

    routeTypeGroup_ = (RadioGroup)findViewById(R.id.routeTypeGroup);
    routeTypeGroup_.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));
  } // RouteType

  public String selectedType() {
    final String routeType = RouteTypeMapper.nameFromId(routeTypeGroup_.getCheckedRadioButtonId());
    return routeType;
  } // selectedType
} // RouteType
