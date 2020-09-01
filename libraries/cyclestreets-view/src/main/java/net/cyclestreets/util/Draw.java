package net.cyclestreets.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.FontMetricsInt;

public class Draw
{
  public static int titleSectionY;

  public static Rect drawBubble(final Canvas canvas,
                                final Paint textBrush,
                                final Paint urlBrush,
                                final int offset,
                                final float cornerRadius,
                                final Point pos,
                                final String text,
                                final String url,
                                final String title) {
    final String[] lines = text.split("\n");

    Rect bounds = new Rect();

    for (final String line : lines) {
        bounds = getWidestLine(textBrush, bounds, line);
    }
    if ((!url.isEmpty()) && (!title.isEmpty()))
      bounds = getWidestLine(urlBrush, bounds, title);

    final FontMetricsInt fm = textBrush.getFontMetricsInt();

    int doubleOffset = (offset * 2);
    int width = bounds.width() + doubleOffset;
    int lineHeight = -fm.ascent + fm.descent;
    int boxHeight;
    FontMetricsInt fmUrl = null;
    int ascent;

    if (!url.isEmpty()) {
      fmUrl = urlBrush.getFontMetricsInt();
      ascent = fmUrl.ascent;
      int urlLineHeight = -ascent + fmUrl.descent;
      // add .descent so there is space to make link stand out a little
      boxHeight = urlLineHeight + lineHeight * (lines.length - 1) + doubleOffset + fmUrl.descent;
    }
    else {
      ascent = fm.ascent;
      boxHeight = (lineHeight * lines.length) + doubleOffset;
    }

    bounds.left = pos.x - (width/2);
    bounds.right = bounds.left + width;
    // Subtract to go UP
    bounds.top = pos.y - (boxHeight + (doubleOffset * 2));
    // Add to go DOWN
    bounds.bottom = bounds.top + boxHeight;

    // draw the balloon
    canvas.drawRoundRect(new RectF(bounds), cornerRadius, cornerRadius, Brush.Grey);
    canvas.drawRoundRect(new RectF(bounds), cornerRadius, cornerRadius, Brush.BlackOutline);

    // put the words in
    int lineY;
    // Remember ascent is -ve... and we are now going down...
    lineY = bounds.top + (-ascent + offset);
    titleSectionY = bounds.top;

    for (String line : lines) {
      if (!url.isEmpty() && line.contains(title)) {
        canvas.drawText(line, bounds.centerX(), lineY, urlBrush);
        titleSectionY = lineY;
        // .descent gives a little extra space so link stands out more
        lineY += fmUrl.descent + lineHeight;
      }
      else {
        canvas.drawText(line, bounds.centerX(), lineY, textBrush);
        lineY += lineHeight;
      }
    }

    // draw the little triangle
    final Path path = new Path();
    path.moveTo(pos.x, pos.y - offset);
    path.lineTo(pos.x - offset, bounds.bottom-1);
    path.lineTo(pos.x + offset, bounds.bottom-1);
    path.lineTo(pos.x, pos.y - offset);
    path.close();
    canvas.drawPath(path, Brush.Grey);
    canvas.drawLine(pos.x, pos.y - offset, pos.x - offset, bounds.bottom, Brush.BlackOutline);
    canvas.drawLine(pos.x, pos.y - offset, pos.x + offset, bounds.bottom, Brush.BlackOutline);
    canvas.drawLine(pos.x - offset, bounds.bottom, pos.x + offset, bounds.bottom, Brush.Grey);

    return bounds;
  }

  private static Rect getWidestLine(final Paint brush, Rect bounds, String line) {
    final Rect lineBounds = new Rect();
    brush.getTextBounds(line, 0, line.length(), lineBounds);
    if (lineBounds.width() > bounds.width())
      return lineBounds;
    return bounds;
  }

  public static int measureTextInRect(final Canvas canvas,
                                      final Paint brush,
                                      final Rect r,
                                      final String text) {
    return textInRect(false, canvas, brush, r, text);
  }

  public static int drawTextInRect(final Canvas canvas,
                                   final Paint brush,
                                   final Rect r,
                                   final String text) {
    return textInRect(true, canvas, brush, r, text);
  }

  // got this from
  // http://groups.google.com/group/android-developers/browse_thread/thread/820fb7ddbfd1ca99
  private static int textInRect(final boolean draw,
                                final Canvas canvas,
                                final Paint brush,
                                final Rect r,
                                final String text) {
    // initial text range and starting position
    int start = 0;
    final int end = text.length() - 1;
    float x = r.left;
    float y = r.top;
    int allowedWidth = r.width();   // constrain text block within this width in pixels
    if (allowedWidth < 30) {
      return -1;  // you have got to be kidding me!  I can't work with this!  You deserve worse!
    }

    // get the distance in pixels between two lines of text
    int lineHeight = brush.getFontMetricsInt(null);

    // emit one line at a time, as much as will fit, with word wrap on whitespace.
    while (start < end) {
      y += lineHeight;

      int charactersRemaining = end - start + 1;
      int charactersToRenderThisPass = charactersRemaining;  // optimism!
      int extraSkip = 0;

      // This 'while' is nothing to be proud of.
      // This should probably be a binary search or more googling to
      // find "character index at distance N pixels in string"
      while (charactersToRenderThisPass > 0 &&
             brush.measureText(text, start, start+charactersToRenderThisPass) > allowedWidth) {
        // remaining text won't fit, cut one character from the end and check again
        charactersToRenderThisPass--;
      }

      // charactersToRenderThisPass would definitely fit, but could be in the middle of a word
      int thisManyWouldDefinitelyFit = charactersToRenderThisPass;
      if (charactersToRenderThisPass < charactersRemaining) {
        while (charactersToRenderThisPass > 0 &&
               !Character.isWhitespace(text.charAt(start+charactersToRenderThisPass-1))) {
          charactersToRenderThisPass--;   // good bye character that would have fit!
        }
      }

      // Now wouldn't it be nice to be able to put in line breaks?
      for (int i = 0; i < charactersToRenderThisPass; i++) {
        if (text.charAt(start+i) == '\n') {  // um, what's unicode for isLineBreak' or '\n'?
           // cool, lets stop this line early
          charactersToRenderThisPass = i;
          extraSkip = 1;  // so we don't start next line with the lineBreak character
          break;
        }
      }

      if (charactersToRenderThisPass < 1 && (extraSkip == 0)) {
        // no spaces found, must be a really long word.
        // Panic and show as much as would fit, breaking the word in the middle
        charactersToRenderThisPass = thisManyWouldDefinitelyFit;
      }

      // Emit this line of characters and advance our offsets for the next line
      if (charactersToRenderThisPass > 0) {
        if (draw)
          canvas.drawText(text, start, start+charactersToRenderThisPass, x, y, brush);
      }
      start += charactersToRenderThisPass + extraSkip;

      // start had better advance each time through the while, or we've invented an infinite loop
      if ((charactersToRenderThisPass + extraSkip) < 1) {
        return (int)y; // better than freezing, I guess.  I am a coward.
      }
    }

    // write google a letter asking why I couldn't find this as an existing function
    // after doing a LOT of googling.  Is my phone going to explode?
    return (int)y;
  }

  private Draw() { }
}
