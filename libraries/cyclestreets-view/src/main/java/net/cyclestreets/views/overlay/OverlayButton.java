package net.cyclestreets.views.overlay;

import net.cyclestreets.util.Brush;
import net.cyclestreets.util.Draw;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

class OverlayButton {
  private final Bitmap img_;
  private final Bitmap altImg_;
  private final Rect pos_;
  private final float radius_;
  private boolean enabled_;
  private boolean pressed_;
  private boolean alt_;

  private String label_;
  private Paint labelBrush_;

  private boolean bottomAlign_;
  private boolean centreAlign_;
  private boolean rightAlign_;

  public OverlayButton(final Drawable image, final int left, final int top, final float curveRadius) {
    this(image, null, left, top, curveRadius);
  } // OverlayButton

  public OverlayButton(final Drawable image,
                       final Drawable altImage,
                       final int left,
                       final int top,
                       final float curveRadius) {
    img_ = ((BitmapDrawable)image).getBitmap();
    altImg_ = (altImage != null) ? ((BitmapDrawable)altImage).getBitmap() : img_;
    pos_ = new Rect(left,
                    top,
                    left + image.getIntrinsicWidth(),
                    top + image.getIntrinsicHeight());
    radius_ = curveRadius;
    enabled_ = true;
    pressed_ = false;
    alt_ = false;
    rightAlign_ = false;
  } // OverlayButton

  public OverlayButton(final Drawable image,
                       final String label,
                       final int left,
                       final int top,
                       final int width,
                       final float curveRadius) {
    img_ = ((BitmapDrawable)image).getBitmap();
    altImg_ = img_;
    pos_ = new Rect(left,
                    top,
                    left + width,
                    top + image.getIntrinsicHeight());
    radius_ = curveRadius;
    enabled_ = true;
    pressed_ = false;
    alt_ = false;
    rightAlign_ = false;

    label_ = label;
    labelBrush_ = Brush.createTextBrush(pos_.height()/7, 127, 127, 127);
    labelBrush_.setTextAlign(Align.CENTER);
  } // OverlayButton

  public void enable(final boolean on) { enabled_ = on; }
  public boolean enabled() { return enabled_; }
  public void pressed(final boolean on) { pressed_ = on; }
  public boolean pressed() { return pressed_; }
  public void alternate(final boolean on) { alt_ = on; }
  public boolean alternate() { return alt_; }

  public OverlayButton bottomAlign() { bottomAlign_ = true; return this; }
  public OverlayButton centreAlign() { centreAlign_ = true; return this; }
  public OverlayButton rightAlign() { rightAlign_ = true; return this; }

  public int right() { return pos_.right;  }
  public int bottom() { return pos_.bottom; }
  public int width() { return pos_.width(); }
  public int height() { return pos_.height(); }

  public void draw(final Canvas canvas) {
    drawButton(canvas);
  } // draw

  public void drawButton(final Canvas canvas) {
    final Rect coords = drawCoords(canvas);

    drawOutLine(canvas, coords);
    DrawingHelper.drawRoundRect(canvas, coords, radius_, enabled_ ? Brush.White : Brush.LightGrey);

    if(enabled_ && pressed_) {
      final Rect inner = new Rect(coords);
      shrinkAndDrawInner(canvas, inner, Brush.LightGrey);
      shrinkAndDrawInner(canvas, inner, Brush.White);
    } // if ...

    DrawingHelper.drawBitmap(canvas, bitmap(), coords);

    if(label_ == null)
      return;

    coords.left += img_.getWidth();
    coords.offset(coords.width()/2, 0);
    int y = Draw.measureTextInRect(canvas, labelBrush_, coords, label_);
    int height = y - coords.top;
    coords.top += (coords.height() - height) / 2;
    Draw.drawTextInRect(canvas, labelBrush_, coords, label_);
  } // drawButton

  private Bitmap bitmap() { return !alt_ ? img_ : altImg_; }

  private Rect drawCoords(final Canvas canvas) {
    final Rect screen = canvas.getClipBounds();

    if((rightAlign_) || (bottomAlign_))
      reflectPosition(screen);

    screen.offset(pos_.left, pos_.top);
    screen.right = screen.left + pos_.width();
    screen.bottom = screen.top + pos_.height();

    return screen;
  } // drawCoords

  private void drawOutLine(final Canvas canvas,
                           final Rect button) {
    final Rect outline = new Rect(button);
    --outline.left;
    --outline.top;
    ++outline.right;
    ++outline.bottom;
    DrawingHelper.drawRoundRect(canvas, outline, radius_, Brush.LightGrey);
  } // drawOutLine

  private void shrinkAndDrawInner(final Canvas canvas,
                                  final Rect rect,
                                  final Paint brush) {
    rect.left += 4;
    rect.top += 4;
    rect.right -= 4;
    rect.bottom -= 4;
    DrawingHelper.drawRoundRect(canvas, rect, radius_, brush);
  } // shrinkAndDrawInner

  public boolean hit(final MotionEvent event) {
    int x = (int)event.getX();
    int y = (int)event.getY();

    return pos_.contains(x, y);
  } // contains

  private void reflectPosition(final Rect screen) {
    if(rightAlign_) {
      int width = pos_.width();
      pos_.left = (screen.width() - width) - pos_.left;
      pos_.right = pos_.left + width;
      rightAlign_ = false;
    } // if ...

    if(centreAlign_) {
      int width = pos_.width();
      pos_.left = (screen.width() / 2)  - (width / 2);
      pos_.right = pos_.left + width;
      centreAlign_ = false;
    } // if ...

    if(bottomAlign_) {
      int height = pos_.height();
      pos_.top = (screen.height() - height) - pos_.top;
      pos_.bottom = pos_.top + height;
      bottomAlign_ = false;
    } // if ...
  } // reflectPosition
} // class OverlayButton
