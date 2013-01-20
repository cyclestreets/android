package net.cyclestreets.planned;

import net.cyclestreets.R;
import net.cyclestreets.content.RouteData;
import net.cyclestreets.planned.Route;

import android.content.Context;

public class FetchCycleStreetsRouteTask extends RoutingTask<Long>
{
  private final String routeType_;
  private final int speed_;
        
  FetchCycleStreetsRouteTask(final String routeType,
                             final int speed,  
                             final Route.Callback whoToTell,
                             final Context context) 
  {
    super(R.string.fetching_route, whoToTell, context);
    routeType_ = routeType;
    speed_ = speed;
  } // FetchCycleStreetsRouteTask
      
  @Override
  protected RouteData doInBackground(Long... params)
  {
    return fetchRoute(routeType_, params[0], speed_);
  } // doInBackground
} // class FetchCycleStreetsRouteTask
