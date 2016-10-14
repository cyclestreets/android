package net.cyclestreets.views.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import net.cyclestreets.util.Brush;
import net.cyclestreets.views.CycleMapView;

public abstract class LiveItemOverlay<T extends OverlayItem> 
          extends ItemizedOverlay<T>
          implements MapListener
{
	/////////////////////////////////////////////////////
	/////////////////////////////////////////////////////
	private final CycleMapView mapView_;
	private int zoomLevel_;
	private boolean loading_;
	
	private final int offset_;
	private final float radius_;
	private final Paint textBrush_;
	private final boolean showLoading_;
	
	static private final String LOADING = "Loading ...";
	
	public LiveItemOverlay(final Context context,
							           final CycleMapView mapView,
							           final boolean showLoading)
	{
		super(context, 
		      mapView,
		      new ArrayList<T>());
		
		mapView_ = mapView;
		zoomLevel_ = mapView_.getZoomLevel();
		loading_ = false;
		showLoading_ = showLoading;
		
		offset_ = DrawingHelper.offset(context);
		radius_ = DrawingHelper.cornerRadius(context);
		textBrush_ = Brush.createTextBrush(offset_);

		mapView_.setMapListener(new DelayedMapListener(this));
	} // PhotoItemOverlay
	
	protected Paint textBrush() { return textBrush_; }
	protected int offset() { return offset_; }
	protected float cornerRadius() { return radius_; }

	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
	{
		super.draw(canvas, mapView, shadow);
		
		if((!loading_) || (!showLoading_))
			return;
		
		final Rect bounds = new Rect();
		textBrush().getTextBounds(LOADING, 0, LOADING.length(), bounds);

		int width = bounds.width() + (offset() * 2);
		final Rect screen = canvas.getClipBounds();
		screen.left = screen.centerX() - (width/2); 
		screen.top += offset()* 2;
		screen.right = screen.left + width;
		screen.bottom = screen.top + bounds.height() + (offset() * 2);

    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();
    canvas.save();
    canvas.concat(unscaled);

    DrawingHelper.drawRoundRect(canvas, screen, cornerRadius(), Brush.Grey);
		canvas.drawText(LOADING, screen.centerX(), screen.centerY() + bounds.bottom, textBrush());

    canvas.restore();
	} // drawButtons
	
	@Override
	public boolean onScroll(final ScrollEvent event) 
	{
		refreshItems();
		return true;
	} // onScroll
	
	@Override
	public boolean onZoom(final ZoomEvent event) 
	{
		if(event.getZoomLevel() < zoomLevel_)
			items().clear();
		zoomLevel_ = event.getZoomLevel();
		refreshItems();
		return true;
	} // onZoom

	protected void redraw()
	{
	  mapView_.postInvalidate();
	} // redraw
	
	protected void refreshItems() 
	{		
		final IGeoPoint centre = mapView_.getMapCenter();
    final int zoom = mapView_.getZoomLevel();
    final BoundingBoxE6 bounds = mapView_.getBoundingBox();
		
		if(!fetchItemsInBackground(centre, zoom, bounds))
		  return;

		loading_ = true;
		redraw();
	} // refreshPhotos
	
	protected abstract boolean fetchItemsInBackground(final IGeoPoint mapCentre,
	                                                  final int zoom,
	                                                  final BoundingBoxE6 boundingBox);
	
	protected void setItems(final List<T> items)
	{
		for(final T item : items)
			if(!items().contains(item))
				items().add(item);
		if(items().size() > 500)  // arbitrary figure
			items().remove(items().subList(0, 100));
		loading_ = false;
    redraw();
	} // setItems
} // class CycleStreetsItemOverlay
