package uk.org.invisibility.cycloid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;

/*
 * Specialised AutoCompleteTextView which supports displaying filter
 * results when no characters have been entered but the view has
 * been clicked on 
 */
public class GeoAutoCompleteView extends AutoCompleteTextView implements OnClickListener
{
	public GeoAutoCompleteView(final Context context)
	{
		super(context);
		this.setThreshold(0);
		this.setOnClickListener(this);
	}

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		this.setThreshold(0);
		this.setOnClickListener(this);		
	}

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		this.setThreshold(0);
		this.setOnClickListener(this);
	}
	
	@Override
	public boolean enoughToFilter() { return true; }
	
	@Override
	public void onFilterComplete(int count)
	{
        if (hasFocus() && hasWindowFocus())
            showDropDown();
        else
            dismissDropDown();
    }

	@Override
	public void onClick(View v)
	{
		performFiltering(null, KeyEvent.KEYCODE_FOCUS);
		showDropDown();
	}

	@Override
	public void onEditorAction(int actionCode)
	{
		super.onEditorAction(actionCode);
        dismissDropDown();
	}

}
