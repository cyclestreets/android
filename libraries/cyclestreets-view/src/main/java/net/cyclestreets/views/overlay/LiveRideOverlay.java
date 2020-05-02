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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.location.Location;

import java.text.SimpleDateFormat;

public class LiveRideOverlay extends Overlay
{
  public interface Locator {
    Location lastLocation();
  }

  private final Locator locator_;
  private final int offset_;
  private final float radius_;
  private Paint speedTextBrush_;
  private Paint rjTextBrush_;
  private final Paint midTextBrush_;
  private final Paint smallTextBrush_;
  private final Paint fillBrush_;

  private int speedLineHeight_;
  private int rjLineHeight_;
  private final int smallLineHeight_;

  private int speedWidth_;
  private int totalSpeedWidth;
  private int remainingWidth;
  private int spaceWidth;
  private int ETATextWidth;
  private int distanceWidth;
  private int remTimeWidth;
  private int ETAWidth;
  private int AMorPMwidth;

  private final DistanceFormatter formatter_;

  private String remainingText;
  private String ETAText;

  private int height;
  private int heightNoText;
  private String strRemDistance;
  private String strRemTime;
  private String ETA;
  private String AMorPM;
  private Integer distanceUntilTurn;
  private boolean showRemainingTime;
  private boolean showETA;
  private boolean showTime;
  private int titleVerticalPosition;
  private int remainingJourneyStartPos;
  private SimpleDateFormat ETAtimeFormat;
  private SimpleDateFormat AMPMtimeFormat;

  public LiveRideOverlay(final Activity context, final Locator locator) {
    super();

    locator_ = locator;
    offset_ = DrawingHelperKt.offset(context);
    radius_ = DrawingHelperKt.cornerRadius();
    midTextBrush_ = Brush.createTextBrush((int)(offset_ * 2));
    midTextBrush_.setTextAlign(Align.LEFT);
    smallTextBrush_ = Brush.createTextBrush(offset_);
    smallTextBrush_.setTextAlign(Align.LEFT);
    fillBrush_ = Brush.HighlightBrush(context);

    formatter_ = DistanceFormatter.formatter(CycleStreetsPreferences.units());

    final Rect bounds = new Rect();

    smallTextBrush_.getTextBounds("0.0", 0, 3, bounds);
    smallLineHeight_ = bounds.height();

    remainingJourneySetup(context);

  }

  private void remainingJourneySetup(final Activity context) {
    remainingText = context.getString(R.string.remaining); //"Remaining";
    ETAText = " " + context.getString(R.string.slash_ETA); //" / ETA";
    ETATextWidth = (int)smallTextBrush_.measureText(ETAText);
    ETA = "";
    remainingWidth = (int)smallTextBrush_.measureText(remainingText + ":");
    showETA = CycleStreetsPreferences.showETA();
    showRemainingTime = CycleStreetsPreferences.showRemainingTime();
    showTime = showETA || showRemainingTime;

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
    AMorPM = "";
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
    final Rect box = canvas.getClipBounds();
    long millisETA;

    strRemDistance = getRemDistance();
    int remTime = getRemTime();

    if (showETA) {
        millisETA = System.currentTimeMillis() + (remTime * 1000);
        ETA = ETAtimeFormat.format(millisETA);
        AMorPM = AMPMtimeFormat.format(millisETA);
    }

    strRemTime = showRemainingTime ? formatRemTime(remTime): "";
    final String speed = speed();
    //Check for overlap between speed and remaining journey and adjust text size if necessary
    adjustTextSize(box);
    final int activeSegIndex = Route.journey().activeSegmentIndex();

    box.right = box.width();

    // After the first segment, "Remaining" text won't be displayed, so adjust box height
    if ((activeSegIndex > 1) && (height != heightNoText)) {
        height = heightNoText;
        TileSource.setAttributionUpShift(height);
    }
    box.top = box.bottom - height;

    DrawingHelperKt.drawRoundRect(canvas, box, radius_, fillBrush_);

    box.left += offset_;
    // Vertically centre the speed:
    box.bottom -= (height - speedLineHeight_) / 2;
    canvas.drawText(speed, box.left, box.bottom, speedTextBrush_);

    box.left += speedWidth_;
    canvas.drawText(formatter_.speedUnit(), box.left, box.bottom, rjTextBrush_);

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
    // Remaining journey consists of up to 3 lines:
    // 1) Title (removed after first segment if remaining time / ETA is shown, to save space)
    // 2) remaining distance
    // 3) remaining time / ETA (depending on settings)
    box.left = remainingJourneyStartPos;
    if ((height == heightNoText) && showTime) {
      box.bottom = box.top + height;
      // "Remaining" text no longer displayed after 1st seg, so need to vertically centre distance and time
      box.bottom -= (height - offset_ - rjLineHeight_*2)/2 + offset_ + rjLineHeight_;
    }
    else {
      // Display "Remaining" text
      box.bottom = box.top + height - titleVerticalPosition;
      canvas.drawText(remainingText, box.left, box.bottom, smallTextBrush_);
      box.left += remainingWidth;
      // Display "/ ETA"
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
      //Down to next line for distance:
      box.bottom += offset_ + rjLineHeight_;
    }

    canvas.drawText(strRemDistance, box.left, box.bottom, rjTextBrush_);
    // Down another line for time:
    box.bottom += offset_ + rjLineHeight_;

    if (showRemainingTime) {
      canvas.drawText(strRemTime + " ", box.left, box.bottom, rjTextBrush_);
      box.left += spaceWidth;
    }
    if (showETA) {
      box.left += remTimeWidth;
      canvas.drawText(ETA, box.left, box.bottom, rjTextBrush_);
      box.left += (int)rjTextBrush_.measureText(ETA);
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

  private void adjustTextSize(Rect box) {
    float factor = 2.0f;
    // Set initial text size then, if it overlaps with speed, progressively reduce size until no overlap
    do {
      rjTextBrush_ = Brush.createTextBrush((int)(offset_ * factor));
      speedTextBrush_ = Brush.createTextBrush((int)(offset_ * (factor + 2)));
      // Calculate widths of each component of box
      calculateWidths();
      int titleLineWidth = remainingWidth + ETATextWidth;
      int timeLineWidth = remTimeWidth + spaceWidth + ETAWidth;
      // Calc starting position for remaining journey from max line width
      if ((height == heightNoText) && showTime) { // (no title shown)
        remainingJourneyStartPos = box.right - Math.max(distanceWidth, timeLineWidth)- offset_;
      }
      else {  // (title shown)
        remainingJourneyStartPos = box.right - Math.max(Math.max(titleLineWidth, distanceWidth), timeLineWidth)- offset_;
      }
      // Reduce text size
      factor -= 0.1;
      // Repeat while there is an overlap, but not beyond the small text size (factor = 1)
    } while ((remainingJourneyStartPos < totalSpeedWidth) && (factor >= 1));

    // Adjust text height and box height
    adjustHeights();
  }

  private void calculateWidths() {
      if (showETA) {
        AMorPMwidth = (int)smallTextBrush_.measureText(AMorPM);
        ETAWidth = (int)rjTextBrush_.measureText(ETA) + AMorPMwidth;
      }

      distanceWidth = (int)rjTextBrush_.measureText(strRemDistance);
      remTimeWidth = (int)rjTextBrush_.measureText(strRemTime);
      spaceWidth = (int)rjTextBrush_.measureText(" ");

      int kmWidth_ = (int) rjTextBrush_.measureText(formatter_.speedUnit());
      speedWidth_ = (int) speedTextBrush_.measureText("0.0");
      totalSpeedWidth = speedWidth_ + kmWidth_ + offset_*2;
  }

  private void adjustHeights() {
      // Measure the text height
      Rect bounds = new Rect();
      rjTextBrush_.getTextBounds("0.0", 0, 3, bounds);
      rjLineHeight_ = bounds.height();
      speedTextBrush_.getTextBounds("0.0", 0, 3, bounds);
      speedLineHeight_ = bounds.height();

      rjTextBrush_.setTextAlign(Align.LEFT);
      speedTextBrush_.setTextAlign(Align.LEFT);

      if (showTime) {
          titleVerticalPosition = offset_*3 + rjLineHeight_ * 2;
          height = smallLineHeight_ + rjLineHeight_ * 2 + offset_ * 4;
          // After 1st seg, text will be removed from bottom box and height reduced
          heightNoText = height - smallLineHeight_;
      }
      else {
          height = speedLineHeight_ + offset_*2;
          // Vertically centre title and distance
          titleVerticalPosition = (height - smallLineHeight_ - rjLineHeight_ - offset_) / 2 + rjLineHeight_ + offset_;
          // If time not shown, no need to reduce height of bottom box after 1st seg
          heightNoText = height;
      }
      TileSource.setAttributionUpShift(height);  // Use this height to shift map attribution in ControllerOverlay
  }
}