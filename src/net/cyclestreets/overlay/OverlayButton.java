package net.cyclestreets.overlay;

import net.cyclestreets.util.Brush;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

class OverlayButton
{
	private final Drawable img_;
	private final Rect pos_;
	private final float radius_;
	
	public OverlayButton(final Drawable image, final int left, final int top, final float curveRadius)
	{
		img_ = image;
        pos_ = new Rect(left, 
        				top, 
        				left + image.getIntrinsicWidth(), 
        				top + image.getIntrinsicHeight());
        radius_ = curveRadius;
	} // OverlayButton
	
	public int right() { return pos_.right;	}
	public int height() { return pos_.height(); }
	
	public void draw(final Canvas canvas)
	{
        final Rect screen = canvas.getClipBounds();
        screen.offset(pos_.left, pos_.top);
        screen.right = screen.left + pos_.width();
        screen.bottom = screen.top + pos_.height();
	        
		canvas.drawRoundRect(new RectF(screen), radius_, radius_, Brush.White);

        img_.setBounds(screen);
        img_.draw(canvas);
	} // drawButton
	
	public boolean hit(final MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		return pos_.contains(x, y);
	} // contains
} // class OverlayButton
