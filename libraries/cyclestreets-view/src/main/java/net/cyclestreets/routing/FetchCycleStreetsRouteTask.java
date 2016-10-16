package net.cyclestreets.routing;

import net.cyclestreets.view.R;
import net.cyclestreets.content.RouteData;
import android.content.Context;

public class FetchCycleStreetsRouteTask extends RoutingTask<Long>
{
  private final String routeType_;
  private final int speed_;

  FetchCycleStreetsRouteTask(final String routeType,
                             final int speed,
                             final Context context)
  {
    super(R.string.route_fetching_existing, context);
    routeType_ = routeType;
    speed_ = speed;
  } // FetchCycleStreetsRouteTask

  @Override
  protected RouteData doInBackground(Long... params)
  {
    return fetchRoute(routeType_, params[0], speed_);
  } // doInBackground
} // class FetchCycleStreetsRouteTask
