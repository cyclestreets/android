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
  static public Rect drawBubble(final Canvas canvas,
                                final Paint brush,
                                final int offset,
                                final float cornerRadius,
                                final Point pos, 
                                final String text)
  {
    final String[] lines = text.split("\n");
    Rect bounds = new Rect();
    
    for(final String line : lines)
    {      
      final Rect lineBounds = new Rect();
      brush.getTextBounds(line, 0, line.length(), lineBounds);
      if(lineBounds.width() > bounds.width())
        bounds = lineBounds;
    } // for ...

    final FontMetricsInt fm = brush.getFontMetricsInt();
    
    int doubleOffset = (offset * 2);
    int width = bounds.width() + doubleOffset;
    int lineHeight = -fm.ascent + fm.descent;
    int boxHeight = (lineHeight * lines.length) + doubleOffset - fm.descent;
    
    bounds.left = pos.x - (width/2);
    bounds.right = bounds.left + width;
    bounds.top = pos.y - (boxHeight + (doubleOffset * 2));
    bounds.bottom = bounds.top + boxHeight;

    // draw the balloon
    canvas.drawRoundRect(new RectF(bounds), cornerRadius, cornerRadius, Brush.Grey);
    canvas.drawRoundRect(new RectF(bounds), cornerRadius, cornerRadius, Brush.BlackOutline);
    
    // put the words in
    int lineY = bounds.top + (-fm.ascent + offset);
    for(final String line : lines)
    {
      canvas.drawText(line, bounds.centerX(), lineY, brush);
      lineY += lineHeight;
    } // for ...
    
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
  } // drawBubble
  
	static public int measureTextInRect(final Canvas canvas,
	                                    final Paint brush,
	                                    final Rect r,
	                                    final String text)
	{
		return textInRect(false, canvas, brush, r, text);
	} // measureTextInRect

	static public int drawTextInRect(final Canvas canvas,
	                                 final Paint brush,
	                                 final Rect r,
	                                 final String text)
	{
		return textInRect(true, canvas, brush, r, text);
	} // drawTextInRect
	
	// got this from 
	// http://groups.google.com/group/android-developers/browse_thread/thread/820fb7ddbfd1ca99
	static private int textInRect(final boolean draw,
	                              final Canvas canvas, 
	                              final Paint brush, 
	                              final Rect r,
	                              final String text) 
  {
	  // initial text range and starting position
	  int start = 0;
	  final int end = text.length() - 1;
	  float x = r.left;
	  float y = r.top;
	  int allowedWidth = r.width();   // constrain text block within this width in pixels
	  if(allowedWidth < 30) {
	    return -1;  // you have got to be kidding me!  I can't work with this!  You deserve worse!
	  }

    // get the distance in pixels between two lines of text
	  int lineHeight = brush.getFontMetricsInt( null );

	  // emit one line at a time, as much as will fit, with word wrap on whitespace.
	  while(start < end) 
	  {
	    y += lineHeight;
	    
	    int charactersRemaining = end - start + 1;
	    int charactersToRenderThisPass = charactersRemaining;  // optimism!
	    int extraSkip = 0;
	    
	    // This 'while' is nothing to be proud of.
	    // This should probably be a binary search or more googling to
	    // find "character index at distance N pixels in string"
	    while(charactersToRenderThisPass > 0 && 
	        brush.measureText(text, start, start+charactersToRenderThisPass ) > allowedWidth ) 
    	{
	      // remaining text won't fit, cut one character from the end and check again
	      charactersToRenderThisPass--;
    	}

	    // charactersToRenderThisPass would definitely fit, but could be in the middle of a word
	    int thisManyWouldDefinitelyFit = charactersToRenderThisPass;
	    if( charactersToRenderThisPass < charactersRemaining ) 
	    {
	      while( charactersToRenderThisPass > 0 &&
	          !Character.isWhitespace( text.charAt( start+charactersToRenderThisPass-1) ) ) 
    		{
	        charactersToRenderThisPass--;   // good bye character that would have fit!
    		}
	    } // if ...

	    // Now wouldn't it be nice to be able to put in line breaks?
	    for(int i=0; i < charactersToRenderThisPass; i++ ) 
	    {
	      if(text.charAt(start+i) == '\n') 
	      {  // um, what's unicode for isLineBreak' or '\n'?
	         // cool, lets stop this line early
	        charactersToRenderThisPass = i;
	        extraSkip = 1;  // so we don't start next line with the lineBreak character
	        break;
	      }
    	} // for ...

	    if(charactersToRenderThisPass < 1 && (extraSkip == 0)) 
	    {
	      // no spaces found, must be a really long word.
	      // Panic and show as much as would fit, breaking the word in the middle	
	      charactersToRenderThisPass = thisManyWouldDefinitelyFit;
    	}

	    // Emit this line of characters and advance our offsets for the next line
	    if( charactersToRenderThisPass > 0 ) 
	    {
	      if(draw)
	        canvas.drawText( text, start, start+charactersToRenderThisPass, x, y, brush );
    	}
	    start += charactersToRenderThisPass + extraSkip;

	    // start had better advance each time through the while, or we've invented an infinite loop
	    if( (charactersToRenderThisPass + extraSkip) < 1 ) 
	    {
	      return (int)y; // better than freezing, I guess.  I am a coward.
    	}
    }
    	
	  // write google a letter asking why I couldn't find this as an	existing function
	  // after doing a LOT of googling.  Is my phone going to explode?
	  return (int)y;
  } // textInRect

	private Draw() { }
} // class Draw
