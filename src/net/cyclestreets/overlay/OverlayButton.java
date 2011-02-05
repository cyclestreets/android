package net.cyclestreets.overlay;

import net.cyclestreets.util.Brush;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

class OverlayButton
{
	private final Drawable img_;
	private final Rect pos_;
	private final float radius_;
	private boolean enabled_;
	private boolean pressed_;

	private boolean bottomAlign_;
	private boolean rightAlign_;
	
	public OverlayButton(final Drawable image, final int left, final int top, final float curveRadius)
	{
		img_ = image;
        pos_ = new Rect(left, 
        				top, 
        				left + image.getIntrinsicWidth(), 
        				top + image.getIntrinsicHeight());
        radius_ = curveRadius;
        enabled_ = true;
        pressed_ = false;
        rightAlign_ = false;
	} // OverlayButton
	
	public void enable(final boolean on) { enabled_ = on; }
	public boolean enabled() { return enabled_; }
	public void pressed(final boolean on) { pressed_ = on; }
	
	public OverlayButton bottomAlign() { bottomAlign_ = true; return this; }
	public OverlayButton rightAlign() { rightAlign_ = true; return this; }
	
	public int right() { return pos_.right;	}
	public int height() { return pos_.height(); }
	
	public void draw(final Canvas canvas)
	{
        final Rect screen = canvas.getClipBounds();
        
        if((rightAlign_) || (bottomAlign_))
        	reflectPosition(screen);
        
        screen.offset(pos_.left, pos_.top);
        screen.right = screen.left + pos_.width();
        screen.bottom = screen.top + pos_.height();
	    
        OverlayHelper.drawRoundRect(canvas, screen, radius_, enabled_ ? Brush.White : Brush.LightGrey);
		
		if(enabled_ && pressed_)
		{
			shrinkAndDrawInner(canvas, screen, Brush.LightGrey);
			shrinkAndDrawInner(canvas, screen, Brush.White);
		} // if ...

        img_.setBounds(screen);
        img_.draw(canvas);
	} // drawButton
	
	private void reflectPosition(final Rect screen) 
	{
		if(rightAlign_) 
		{
			int width = pos_.width();
			pos_.left = (screen.width() - width) - pos_.left;
			pos_.right = pos_.left + width;
			rightAlign_ = false;
		} // if ...
		if(bottomAlign_)
		{
			int height = pos_.height();
			pos_.top = (screen.height() - height) - pos_.top;
			pos_.bottom = pos_.top + height;
			bottomAlign_ = false;
		} // if ...
	} // reflectPosition
	
	private void shrinkAndDrawInner(final Canvas canvas, final Rect rect, final Paint brush)
	{
		rect.left += 4;
		rect.top += 4;
		rect.right -= 4;
		rect.bottom -= 4;
        OverlayHelper.drawRoundRect(canvas, rect, radius_, brush);	 
	} // shrinkAndDrawInner
	
	public boolean hit(final MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		return pos_.contains(x, y);
	} // contains
} // class OverlayButton
