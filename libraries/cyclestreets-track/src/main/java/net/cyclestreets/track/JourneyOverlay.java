package net.cyclestreets.track;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

import net.cyclestreets.views.CycleMapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class JourneyOverlay extends Overlay {
  public static JourneyOverlay CompletedJourneyOverlay(final Context context,
                                                       final CycleMapView mapView,
                                                       final TripData tripData) {
    return new JourneyOverlay(context, mapView, tripData);
  } // CompletedJourneyOverlay

  public static JourneyOverlay InProgressJourneyOverlay(final Context context,
                                                        final CycleMapView mapView,
                                                        final TripData tripData) {
    JourneyOverlay jo = new JourneyOverlay(context, mapView, tripData);
    jo.inProgress();
    return jo;
  } // InProgressJourneyOverlay

  static private int ROUTE_COLOUR = 0x80ff00ff;

  private final CycleMapView mapView_;
  private boolean initial_ = true;

  private TripData trip_;
  private final Paint rideBrush_;
  private Path ridePath_;
  private int zoomLevel_ = -1;
  private IGeoPoint mapCentre_;
  private final BitmapDrawable greenWisp_;
  private final BitmapDrawable redWisp_;
  private final Matrix canvasTransform_ = new Matrix();
  private final float[] transformValues_ = new float[9];
  private final Matrix bitmapTransform_ = new Matrix();
  private final Paint bitmapPaint_ = new Paint();

  private boolean inProgress_ = false;

  private JourneyOverlay(final Context context,
                         final CycleMapView mapView,
                         final TripData tripData) {
    super(context);

    mapView_ = mapView;
    trip_ = tripData;

    rideBrush_ = createBrush(ROUTE_COLOUR);

    final Resources res = context.getResources();
    greenWisp_ = (BitmapDrawable)res.getDrawable(R.drawable.greep_wisp);
    redWisp_ = (BitmapDrawable)res.getDrawable(R.drawable.red_wisp);
  } // JourneyOverlay

  private void inProgress() {
    inProgress_ = true;
  } // inProgress

  public void update(final TripData trip) {
    trip_ = trip;
    mapView_.invalidate();
  } // update

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    if (shadow)
      return;

    if(!trip_.dataAvailable())
      return;

    final IGeoPoint centre = mapView.getMapCenter();

    if(zoomLevel_ != mapView.getZoomLevel() ||
       !centre.equals(mapCentre_)) {
      ridePath_ = null;
      zoomLevel_ = mapView.getProjection().getZoomLevel();
      mapCentre_ = centre;
    } // if ...

    if(ridePath_ == null || inProgress_)
      ridePath_ = journeyPath(mapView.getProjection());

    canvas.drawPath(ridePath_, rideBrush_);
    drawMarker(canvas, mapView.getProjection(), trip_.startLocation(), greenWisp_);
    if(!inProgress_)
      drawMarker(canvas, mapView.getProjection(), trip_.endLocation(), redWisp_);

    if (initial_ && !inProgress_) {
      mapView_.zoomToBoundingBox(trip_.boundingBox());
      initial_ = false;
    } // if ...
  } // draw

  private Path journeyPath(final IProjection projection) {
    Path ridePath = newPath();

    Point screenPoint = new Point();

    boolean first = true;
    for(final GeoPoint gp : trip_.journey()) {
      screenPoint = projection.toPixels(gp, screenPoint);

      if(first) {
        ridePath.moveTo(screenPoint.x, screenPoint.y);
        first = false;
      } else
        ridePath.lineTo(screenPoint.x, screenPoint.y);
    } // for ...

    return ridePath;
  } // drawJourney

  private void drawMarker(final Canvas canvas,
                          final IProjection projection,
                          final GeoPoint location,
                          final BitmapDrawable marker) {
    Point screenPoint = new Point();
    projection.toPixels(location, screenPoint);

    canvas.getMatrix(canvasTransform_);
    canvasTransform_.getValues(transformValues_);

    final int halfWidth = marker.getIntrinsicWidth()/2;
    final int halfHeight = marker.getIntrinsicHeight()/2;
    bitmapTransform_.setTranslate(-halfWidth, -halfHeight);
    bitmapTransform_.postScale(1/transformValues_[Matrix.MSCALE_X], 1/transformValues_[Matrix.MSCALE_Y]);
    bitmapTransform_.postTranslate(screenPoint.x, screenPoint.y);
    canvas.drawBitmap(marker.getBitmap(), bitmapTransform_, bitmapPaint_);
  } // drawMarker

  private Paint createBrush(int colour) {
    final Paint brush = new Paint();

    brush.setColor(colour);
    brush.setStrokeWidth(2.0f);
    brush.setStyle(Paint.Style.STROKE);
    brush.setStrokeWidth(10.0f);

    return brush;
  } // createBrush

  private Path newPath() {
    final Path path = new Path();
    path.rewind();
    return path;
  } // newPath

} // JourneyOverlay

