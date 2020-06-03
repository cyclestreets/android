package net.cyclestreets.views.overlay;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.DistanceFormatter;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import net.cyclestreets.util.TurnIcons;
import net.cyclestreets.view.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Locale;

public class LiveRideOverlay extends Overlay
{
  public interface Locator {
    Location lastLocation();
  }

  private final Locator locator_;
  private final int offset_;
  private final float radius_;
  private final Paint largeTextBrush_;
  private final Paint midTextBrush_;
  private final Paint smallTextBrush_;

  private final Paint fillBrush_;
  private final int speedWidth_;
  private final int kmWidth_;
  private final int largeLineHeight_;
  private final int midLineHeight_;
  private final int smallLineHeight_;

  private final DistanceFormatter formatter_;
  private String remainingText;
  private int remainingWidth;
  private String ETAText;
  private int height;
  private int heightNoText;
  private Integer distanceUntilTurn;
  private boolean showETA;
  private boolean showRemainingTime;
  private boolean showTime;
  private int titleVerticalPosition;
  private SimpleDateFormat ETAtimeFormat;
  private SimpleDateFormat AMPMtimeFormat;

  public LiveRideOverlay(final Activity context, final Locator locator) {
    super();

    locator_ = locator;
    offset_ = DrawingHelperKt.offset(context);
    radius_ = DrawingHelperKt.cornerRadius();
    largeTextBrush_ = Brush.createTextBrush(offset_ * 4);
    largeTextBrush_.setTextAlign(Align.LEFT);
    midTextBrush_ = Brush.createTextBrush((int)(offset_ * 1.8));
    midTextBrush_.setTextAlign(Align.LEFT);
    smallTextBrush_ = Brush.createTextBrush(offset_);
    smallTextBrush_.setTextAlign(Align.LEFT);
    fillBrush_ = Brush.HighlightBrush(context);

    formatter_ = DistanceFormatter.formatter(CycleStreetsPreferences.units());

    speedWidth_ = (int)largeTextBrush_.measureText("0.0");
    kmWidth_ = (int)midTextBrush_.measureText(formatter_.speedUnit());

    final Rect bounds = new Rect();
    largeTextBrush_.getTextBounds("0.0", 0, 3, bounds); // Measure the text
    largeLineHeight_ = bounds.height();
    midTextBrush_.getTextBounds("0.0", 0, 3, bounds);
    midLineHeight_ = bounds.height();
    smallTextBrush_.getTextBounds("0.0", 0, 3, bounds);
    smallLineHeight_ = bounds.height();

    remainingJourneySetup(context);

  }

  private void remainingJourneySetup(final Activity context) {
    remainingText = context.getString(R.string.remaining); //"Remaining";
    ETAText = " " + context.getString(R.string.slash_ETA); //" / ETA";
    remainingWidth = (int)smallTextBrush_.measureText(remainingText + ":");
    showETA = CycleStreetsPreferences.showETA();
    showRemainingTime = CycleStreetsPreferences.showRemainingTime();
    showTime = showETA || showRemainingTime;

    //int aDistance = Route.journey().totalDistance();

    if (showTime) {
      titleVerticalPosition = offset_ + midLineHeight_ * 2;
      height = smallLineHeight_ + midLineHeight_ * 2 + offset_ * 4;
      // After 1st seg, text will be removed from bottom box and height reduced
      heightNoText = height - smallLineHeight_;
    }
    else {
      titleVerticalPosition = (largeLineHeight_ - smallLineHeight_ - midLineHeight_ - offset_) / 2 + midLineHeight_ + offset_;
      height = largeLineHeight_ + offset_*2;
      // If time not shown, no need to reduce height of bottom box after 1st seg
      heightNoText = height;
    }
    TileSource.setAttributionUpShift(height);  // Use this height to shift map attribution in ControllerOverlay
    ///////////////////// todo For testing only :
    /* Locale locale = new Locale("en-gb");
    Locale.setDefault(locale);
    Configuration config = context.getBaseContext().getResources().getConfiguration();
    config.locale = locale; */
    ///////////////////////////
    String patternETA;
    String patternAMPM;
    if (android.text.format.DateFormat.is24HourFormat(context)) {
      patternETA = "HH:mm";
      patternAMPM = "";
    }
    else {
      patternETA = "h:mm";
      patternAMPM = "a";
    }
    ETAtimeFormat = new SimpleDateFormat(patternETA);
    AMPMtimeFormat = new SimpleDateFormat(patternAMPM);
  }

  @Override
  public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    final Matrix unscaled = mapView.getProjection().getInvertedScaleRotateCanvasMatrix();

    canvas.save();
    canvas.concat(unscaled);

    try {
      distanceUntilTurn = distanceUntilTurn();
      drawNextTurn(canvas);
      drawBottomBox(canvas);
    } catch (Exception e) {
    }

    canvas.restore();
  }

  private void drawNextTurn(final Canvas canvas) {
    final Rect box = canvas.getClipBounds();
    int eighth = box.width() / 8;

    box.right = box.left + (eighth * 2);
    box.bottom = box.top + (eighth * 2);

    drawThenShrink(canvas, box, fillBrush_);
    drawThenShrink(canvas, box, Brush.White);

    final Segment nextSeg = Route.journey().nextSegment();
    final Drawable turnIcon = TurnIcons.icon(nextSeg.turn());
    turnIcon.setBounds(box);
    turnIcon.draw(canvas);

    if (Route.journey().atStart())
      return;

    final String distanceTo;
    if (distanceUntilTurn == null)
      distanceTo = "";
    else
      distanceTo = formatter_.distance(distanceUntilTurn);

    final String nextStreet = nextSeg.street();

    final Rect distanceToBox = canvas.getClipBounds();
    distanceToBox.left = box.right + (offset_*2);
    distanceToBox.bottom = distanceToBox.top + offset_;
    int bottom = Draw.measureTextInRect(canvas, midTextBrush_, distanceToBox, distanceTo);
    distanceToBox.bottom = bottom + offset_;

    final Rect nextBox = new Rect(distanceToBox);
    nextBox.top = distanceToBox.bottom;
    nextBox.bottom = nextBox.top + offset_;
    bottom = Draw.measureTextInRect(canvas, smallTextBrush_, nextBox, nextStreet);
    nextBox.bottom = bottom + offset_;

    final Rect wrapperBox = new Rect(distanceToBox);
    wrapperBox.bottom = nextBox.bottom;

    DrawingHelperKt.drawRoundRect(canvas, wrapperBox, radius_, fillBrush_);
    Draw.drawTextInRect(canvas, midTextBrush_, distanceToBox, distanceTo);
    Draw.drawTextInRect(canvas, smallTextBrush_, nextBox, nextStreet);

    turnIcon.draw(canvas);
  }

  private void drawThenShrink(final Canvas canvas, final Rect box, final Paint brush) {
    DrawingHelperKt.drawRoundRect(canvas, box, radius_, brush);

    box.left += offset_;
    box.right -= offset_;
    box.top += offset_;
    box.bottom -= offset_;
  }

  // Speed and Remaining journey
  private void drawBottomBox(final Canvas canvas) {
    final String speed = speed();
    final Rect box = canvas.getClipBounds();
    final int activeSegIndex = Route.journey().activeSegmentIndex();

    box.right = box.width();

    // After the first segment, don't display "Remaining" text
    if ((activeSegIndex > 1) && (height != heightNoText)) {
      height = heightNoText;
      TileSource.setAttributionUpShift(height);
    }
    box.top = box.bottom - height;

    DrawingHelperKt.drawRoundRect(canvas, box, radius_, fillBrush_);

    box.left += offset_;
    // Vertically centre the speed:
    box.bottom -= (height - largeLineHeight_) / 2;

    canvas.drawText(speed, box.left, box.bottom, largeTextBrush_);

    box.left += speedWidth_;
    canvas.drawText(formatter_.speedUnit(), box.left, box.bottom, midTextBrush_);

    drawRemainingJourney(canvas, box);

  }

  private Location lastLocation() {
    if (!Route.routeAvailable())
      return null;

    if (locator_ == null)
      return null;

    final Location location = locator_.lastLocation();
    if (location == null)
      return null;

    return location;
  }

  private String speed() {
    final Location location = lastLocation();
    if (location == null)
      return "0.0";

    return formatter_.speed(location.getSpeed());
  }

  private Integer distanceUntilTurn() {
    final GeoPoint whereIam = whereIAm();
    if (whereIam != null) {
      // Get distance to end of active segment
      final Segment activeSeg = Route.journey().activeSegment();
      return activeSeg.distanceFromEnd(whereIam);
    }
    return null;
  }

  private GeoPoint whereIAm() {
    final Location location = lastLocation();
    if (location == null)
      return null;
    return new GeoPoint(location);
  }

  private void drawRemainingJourney(Canvas canvas, Rect box) {
    String strRemDistance;
    String strRemTime;
    String ETA = "";
    int ETAWidth = 0;
    int ETATextWidth = 0;
    String AMorPM = "";
    long millisETA;

    strRemDistance = getRemDistance();
    int remTime = getRemTime();

    if (showETA) {
      //Instant now = Instant.now();   API 26+ only
      millisETA = System.currentTimeMillis() + (remTime * 1000);
      ETA = ETAtimeFormat.format(millisETA);
      AMorPM = AMPMtimeFormat.format(millisETA);
      ETATextWidth = (int)smallTextBrush_.measureText(ETAText);
      ETAWidth = (int)midTextBrush_.measureText(ETA) + (int)smallTextBrush_.measureText(AMorPM);
    }

    strRemTime = showRemainingTime ? formatRemTime(remTime): "";

    final int distanceWidth = (int)midTextBrush_.measureText(strRemDistance);
    final int remTimeWidth = (int)midTextBrush_.measureText(strRemTime);
    final int spaceWidth = (int)midTextBrush_.measureText(" ");

    if ((height == heightNoText) && showTime) {
      // Up 1 line (stop displaying the "Remaining" text after 1st seg)
      box.bottom -= offset_*2 + midLineHeight_*2;
      // Move over to the right
      box.left = box.right - Math.max(distanceWidth, (remTimeWidth + spaceWidth + ETAWidth))- offset_;
    }
    else {
      //Up 2 lines (display "Remaining" text)
      box.bottom -= titleVerticalPosition;
      // Move over to the right
      box.left = box.right - Math.max(Math.max(remainingWidth + ETATextWidth, distanceWidth), (remTimeWidth + spaceWidth + ETAWidth))- offset_;
      if (box.left < speedWidth_ + kmWidth_ + offset_)
        remainingText = remainingText + "x";  // todo for testing only
      canvas.drawText(remainingText, box.left, box.bottom, smallTextBrush_);
      box.left += remainingWidth;
      if (showETA) {
        canvas.drawText(ETAText, box.left, box.bottom, smallTextBrush_);
        box.left += ETATextWidth;
        canvas.drawText(":", box.left, box.bottom, smallTextBrush_);
        box.left -= remainingWidth + ETATextWidth;
      }
      else {
        canvas.drawText(":", box.left, box.bottom, smallTextBrush_);
        box.left -= remainingWidth;
      }
    }
    //Down to next line:
    box.bottom += offset_ + midLineHeight_;
    canvas.drawText(strRemDistance, box.left, box.bottom, midTextBrush_);
    // Down another line:
    box.bottom += offset_ + midLineHeight_;

    if (showRemainingTime) {
      canvas.drawText(strRemTime + " ", box.left, box.bottom, midTextBrush_);
      box.left += spaceWidth;
    }
    if (showETA) {
      box.left += remTimeWidth;
      canvas.drawText(ETA, box.left, box.bottom, midTextBrush_);
      box.left += (int)midTextBrush_.measureText(ETA);
      canvas.drawText(AMorPM, box.left, box.bottom, smallTextBrush_);
    }
  }

  private String getRemDistance() {
    int remDistance;

    if (distanceUntilTurn == null)
      return "";

    remDistance = Route.journey().remainingDistance(distanceUntilTurn);
    return formatter_.distance(remDistance);
  }

  private int getRemTime() {
    int remTimeSecs;
    if (distanceUntilTurn == null)
      return 0;

    remTimeSecs = Route.journey().remainingTime(distanceUntilTurn);

    return remTimeSecs;
  }

  private String formatRemTime(int remTime) {
    String strRemTimeFormatted;
    strRemTimeFormatted = Segment.formatTime(remTime, true).replace("minute", "min");
    strRemTimeFormatted = strRemTimeFormatted.replace("mins", "min");
    strRemTimeFormatted = strRemTimeFormatted.replace("hour", "hr");
    return strRemTimeFormatted;
  }

}