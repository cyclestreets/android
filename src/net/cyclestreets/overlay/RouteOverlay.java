package net.cyclestreets.overlay;

import java.util.ArrayList;

import net.cyclestreets.R;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.MotionEvent;


public class RouteOverlay extends OpenStreetMapViewItemizedOverlay<OpenStreetMapViewOverlayItem> {
	public interface Callback {
		void onRouteNow(final GeoPoint from, final GeoPoint to);
	}
	
	private final Callback callback_;
	private final Resources res_;
    
	public RouteOverlay(final Context context,
				        final Callback callback) {
		super(context, 
			  new ArrayList<OpenStreetMapViewOverlayItem>(),
			  context.getResources().getDrawable(R.drawable.icon),
			  new Point(10,10),
			  null,
			  new DefaultResourceProxyImpl(context));
		callback_ = callback;
		res_ = context.getResources();
	} // RouteOverlay
	
	public void setRoute(final GeoPoint start, final GeoPoint end)
	{
		mItemList.clear();
		setStart(start);
		setEnd(end);
	} // setRoute
	
	public void setStart(final GeoPoint point)
	{
		addMarker(point, "start", R.drawable.green_wisp_36x30);
	} // setStart
	
	public void setEnd(final GeoPoint point)
	{
		addMarker(point, "finish", R.drawable.red_wisp_36x30);
	} // setEnd
	
	private void addMarker(final GeoPoint point, final String label, final int iconId)
	{
		final OpenStreetMapViewOverlayItem marker = new OpenStreetMapViewOverlayItem(label, label, point);
		marker.setMarker(res_.getDrawable(iconId));
		marker.setMarkerHotspot(new Point(0,30));
		mItemList.add(marker);
	} // addMarker
	
    @Override
    public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView) {
    	if(mItemList.size() >= 2)
    		return super.onSingleTapUp(event, mapView);
    	final GeoPoint p = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY());
    	if(mItemList.size() == 0)
    		setStart(p);
    	else {
    		setEnd(p);
    		if(callback_ != null)
    			callback_.onRouteNow(mItemList.get(0).getPoint(), 
    								 mItemList.get(1).getPoint());
    	}
    	mapView.invalidate();
    	return true;
    }
} // class RouteOverlay
