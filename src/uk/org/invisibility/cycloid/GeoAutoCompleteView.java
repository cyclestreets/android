package uk.org.invisibility.cycloid;

import net.cyclestreets.api.GeoPlace;

import org.osmdroid.util.BoundingBoxE6;

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
public class GeoAutoCompleteView extends AutoCompleteTextView 
								 implements OnClickListener
{
	private GeoAdapter adapter_;
	
	public GeoAutoCompleteView(final Context context)
	{
		super(context);
		setThreshold(0);
		setOnClickListener(this);
	} // GeoAutoCompleteView

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setThreshold(0);
		setOnClickListener(this);		
	} // GeoAutoCompleteView

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		setThreshold(0);
		setOnClickListener(this);
	} // GeoAutoCompleteView
	
	/////////////////////////////////////
	public void setBounds(BoundingBoxE6 bounds)
	{
        adapter_ = new GeoAdapter(getContext(), this, bounds);
    	setAdapter(adapter_);    	
	} // setBounds
	
	public GeoPlace getSelected()
	{
		return adapter_ != null ? adapter_.getSelected() : null;
	} // getSelected
	
	public void addHistory(final GeoPlace place)
	{
		if(adapter_ == null)
			return;
		adapter_.addHistory(place);
	} // addHistory
	
	/////////////////////////////////////
	@Override
	public boolean enoughToFilter() { return true; }
	
	@Override
	public void onFilterComplete(int count)
	{
        if (hasFocus() && hasWindowFocus())
            showDropDown();
        else
            dismissDropDown();
    } // onFilterComplete

	@Override
	public void onClick(View v)
	{
		performFiltering(null, KeyEvent.KEYCODE_FOCUS);
		showDropDown();
	} // onClick

	@Override
	public void onEditorAction(int actionCode)
	{
		super.onEditorAction(actionCode);
        dismissDropDown();
	} // onEditorAction
} // GeoAutoCompleteView
