package net.cyclestreets.views.overlay;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.DistanceFormatter;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.tiles.TileSource;
import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Logging;
import net.cyclestreets.view.R;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;

public class LiveRideJourney {

    private static final String TAG = Logging.getTag(LiveRideJourney.class);

    private final int offset;
    private final float radius;
    private Paint speedTextBrush;
    private Paint rjTextBrush;
    private final Paint smallTextBrush;
    private final Paint fillBrush;

    private int speedLineHeight;
    private int rjLineHeight;
    private final int smallLineHeight;

    private int speedWidth;
    private int totalSpeedWidth;
    private int remainingWidth;
    private int spaceWidth;
    private int etaTextWidth;
    private int distanceWidth;
    private int remTimeWidth;
    private int etaWidth;

    private final DistanceFormatter formatter;

    private String remainingText;
    private String etaText;

    private int height;
    private int heightNoText;
    private String strRemDistance;
    private String strRemTime;
    private String eta;
    private String amOrPm;
    private boolean showRemainingTime;
    private boolean showEta;
    private boolean showTime;
    private int titleVerticalPosition;
    private int remainingJourneyStartPos;
    private SimpleDateFormat etaTimeFormat;
    private SimpleDateFormat amPmTimeFormat;

    public LiveRideJourney(final Activity context) {

        offset = DrawingHelperKt.offset(context);
        radius = DrawingHelperKt.cornerRadius();
        smallTextBrush = Brush.createTextBrush(offset);
        smallTextBrush.setTextAlign(Paint.Align.LEFT);
        fillBrush = Brush.HighlightBrush(context);

        formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());

        final Rect bounds = new Rect();

        smallTextBrush.getTextBounds("0.0", 0, 3, bounds);
        smallLineHeight = bounds.height();

        remainingJourneySetup(context);
    }

    private void remainingJourneySetup(final Activity context) {
        remainingText = context.getString(R.string.remaining); //"Remaining";
        etaText = " " + context.getString(R.string.slash_ETA); //" / ETA";
        etaTextWidth = (int) smallTextBrush.measureText(etaText);
        eta = "";
        remainingWidth = (int) smallTextBrush.measureText(remainingText + ":");
        showEta = CycleStreetsPreferences.showEta();
        showRemainingTime = CycleStreetsPreferences.showRemainingTime();
        showTime = showEta || showRemainingTime;

        String patternEta;
        String patternAmPm;
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            patternEta = "HH:mm";
            patternAmPm = "";
        }
        else {
            patternEta = "h:mm";
            patternAmPm = "a";
        }
        etaTimeFormat = new SimpleDateFormat(patternEta);
        amPmTimeFormat = new SimpleDateFormat(patternAmPm);
        amOrPm = "";
    }

    public void drawJourneyInfo(final Canvas canvas, Integer distanceUntilTurn, Location location) {

        try {
            drawBottomBox(canvas, distanceUntilTurn, location);
        } catch (Exception e) {
            Log.d(TAG, "Error while drawing journey info box in LiveRide", e);
        }
    }

    // Speed and Remaining journey
    private void drawBottomBox(final Canvas canvas, Integer distanceUntilTurn, Location location) {
        final Rect box = canvas.getClipBounds();

        strRemDistance = getRemDistance(distanceUntilTurn);

        if (showRemainingTime || showEta) {
            int remTime = getRemTime(distanceUntilTurn);

            if (showEta) {
                calculateEta(remTime);
            }

            strRemTime = showRemainingTime ? formatRemTime(remTime) : "";
        }

        // Check for overlap between speed and remaining journey and adjust text size if necessary
        adjustTextSize(box);
        final int activeSegIndex = Route.journey().activeSegmentIndex();

        box.right = box.width();

        // After the first segment, "Remaining" text won't be displayed, so adjust box height
        if ((activeSegIndex > 1) && (height != heightNoText)) {
            height = heightNoText;
            TileSource.setAttributionUpShift(height);
        }
        box.top = box.bottom - height;
        DrawingHelperKt.drawRoundRect(canvas, box, radius, fillBrush);
        box.left += offset;

        final String speed = (location != null) ? formatter.speed(location.getSpeed()) : "0.0";

        // Vertically centre the speed:
        box.bottom -= (height - speedLineHeight) / 2;
        canvas.drawText(speed, box.left, box.bottom, speedTextBrush);
        box.left += speedWidth;
        canvas.drawText(formatter.speedUnit(), box.left, box.bottom, rjTextBrush);

        drawRemainingJourney(canvas, box);
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
            box.bottom -= (height - offset - rjLineHeight * 2) / 2 + offset + rjLineHeight;
        }
        else {
            // Display "Remaining" text
            box.bottom = box.top + height - titleVerticalPosition;
            canvas.drawText(remainingText, box.left, box.bottom, smallTextBrush);
            box.left += remainingWidth;
            // Display "/ ETA"
            if (showEta) {
                canvas.drawText(etaText, box.left, box.bottom, smallTextBrush);
                box.left += etaTextWidth;
                canvas.drawText(":", box.left, box.bottom, smallTextBrush);
                box.left -= remainingWidth + etaTextWidth;
            }
            else {
                canvas.drawText(":", box.left, box.bottom, smallTextBrush);
                box.left -= remainingWidth;
            }
            // Down to next line for distance:
            box.bottom += offset + rjLineHeight;
        }

        canvas.drawText(strRemDistance, box.left, box.bottom, rjTextBrush);
        // Down another line for time:
        box.bottom += offset + rjLineHeight;

        if (showRemainingTime) {
            canvas.drawText(strRemTime + " ", box.left, box.bottom, rjTextBrush);
            box.left += spaceWidth;
        }
        if (showEta) {
            box.left += remTimeWidth;
            canvas.drawText(eta, box.left, box.bottom, rjTextBrush);
            box.left += (int) rjTextBrush.measureText(eta);
            canvas.drawText(amOrPm, box.left, box.bottom, smallTextBrush);
        }
    }

    // Note that at the beginning of the journey, before the active segment has been determined,
    // the Remaining Distance and Time can appear as blank / zero, which can look a bit weird

    private String getRemDistance(Integer distanceUntilTurn) {
        int remDistance;

        if (distanceUntilTurn == null)
            return "";

        remDistance = Route.journey().remainingDistance(distanceUntilTurn);
        return formatter.distance(remDistance);
    }

    private int getRemTime(Integer distanceUntilTurn) {
        int remTimeSecs;

        if (distanceUntilTurn == null)
            return 0;

        remTimeSecs = Route.journey().remainingTime(distanceUntilTurn);

        return remTimeSecs;
    }

    private String formatRemTime(int remTime) {

        return Segment.formatTime(remTime, true)
                .replace("minute", "min")
                .replace("mins", "min")
                .replace("hour", "hr");
    }

    private void calculateEta(int remTime) {
        long millisETA = System.currentTimeMillis() + (remTime * 1000);
        eta = etaTimeFormat.format(millisETA);
        amOrPm = amPmTimeFormat.format(millisETA);
    }

    private void adjustTextSize(Rect box) {
        float factor = 2.0f;
        // Set initial text size then, if it overlaps with speed, progressively reduce size until no overlap
        do {
            rjTextBrush = Brush.createTextBrush((int)(offset * factor));
            speedTextBrush = Brush.createTextBrush((int)(offset * (factor + 2)));
            // Calculate widths of each component of box
            calculateWidths();
            int titleLineWidth = remainingWidth + etaTextWidth;
            int timeLineWidth = remTimeWidth + spaceWidth + etaWidth;
            // Calc starting position for remaining journey from max line width
            if ((height == heightNoText) && showTime) { // (no title shown)
                remainingJourneyStartPos = box.right - Math.max(distanceWidth, timeLineWidth)- offset;
            }
            else {  // (title shown)
                remainingJourneyStartPos = box.right - Math.max(Math.max(titleLineWidth, distanceWidth), timeLineWidth)- offset;
            }
            // Reduce text size
            factor -= 0.1;
            // Repeat while there is an overlap, but not beyond the small text size (factor = 1)
        } while ((remainingJourneyStartPos < totalSpeedWidth) && (factor >= 1));

        // Adjust text height and box height
        adjustHeights();
    }

    private void calculateWidths() {
        int amOrPmWidth;
        if (showEta) {
            amOrPmWidth = (int) smallTextBrush.measureText(amOrPm);
            etaWidth = (int) rjTextBrush.measureText(eta) + amOrPmWidth;
        }

        distanceWidth = (int) rjTextBrush.measureText(strRemDistance);
        remTimeWidth = (int) rjTextBrush.measureText(strRemTime);
        spaceWidth = (int) rjTextBrush.measureText(" ");

        int kmWidth_ = (int) rjTextBrush.measureText(formatter.speedUnit());
        speedWidth = (int) speedTextBrush.measureText("0.0");
        totalSpeedWidth = speedWidth + kmWidth_ + offset * 2;
    }

    private void adjustHeights() {
        // Measure the text height
        Rect bounds = new Rect();
        rjTextBrush.getTextBounds("0.0", 0, 3, bounds);
        rjLineHeight = bounds.height();
        speedTextBrush.getTextBounds("0.0", 0, 3, bounds);
        speedLineHeight = bounds.height();

        rjTextBrush.setTextAlign(Paint.Align.LEFT);
        speedTextBrush.setTextAlign(Paint.Align.LEFT);

        if (showTime) {
            titleVerticalPosition = offset * 3 + rjLineHeight * 2;
            height = smallLineHeight + rjLineHeight * 2 + offset * 4;
            // After 1st seg, text will be removed from bottom box and height reduced
            heightNoText = height - smallLineHeight;
        }
        else {
            height = speedLineHeight + offset * 2;
            // Vertically centre title and distance
            titleVerticalPosition = (height - smallLineHeight - rjLineHeight - offset) / 2 + rjLineHeight + offset;
            // If time not shown, no need to reduce height of bottom box after 1st seg
            heightNoText = height;
        }
        TileSource.setAttributionUpShift(height);  // Use this height to shift map attribution in ControllerOverlay
    }
}
