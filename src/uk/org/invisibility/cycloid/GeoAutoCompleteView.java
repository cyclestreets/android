package uk.org.invisibility.cycloid;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoLiveAdapter;

import org.osmdroid.util.BoundingBoxE6;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * Specialised AutoCompleteTextView which supports displaying filter
 * results when no characters have been entered but the view has
 * been clicked on 
 */
public class GeoAutoCompleteView extends AutoCompleteTextView 
								 implements OnClickListener, OnItemClickListener
{
	private GeoLiveAdapter adapter_;
	private GeoPlace place_;
	
	public GeoAutoCompleteView(final Context context)
	{
		super(context);
		init();
	} // GeoAutoCompleteView

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init();

	} // GeoAutoCompleteView

	public GeoAutoCompleteView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	} // GeoAutoCompleteView
	
	private void init() 
	{
		setThreshold(0);
		setOnClickListener(this);
		setOnItemClickListener(this);
	} // init
	
	/////////////////////////////////////
	public BoundingBoxE6 bounds()
	{
		return adapter_.bounds();
	} // bounds
	
	public void setBounds(final BoundingBoxE6 bounds)
	{
        adapter_ = new GeoLiveAdapter(getContext(), bounds);
    	setAdapter(adapter_);  
	} // setBounds
	
	public GeoPlace geoPlace()
	{
		return place_;
	} // geoPlace
	
	public void setGeoPlace(final GeoPlace place)
	{
		// set text first because we clear place_ in the callback
		// then set place_
		setText(place.toString());
		place_ = place;
	} // setGeoPlace
	
	public void addHistory(final GeoPlace place)
	{
		if(adapter_ == null)
			return;
		adapter_.addHistory(place);
	} // addHistory
	
	/////////////////////////////////////
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
	{
		/*
		 * Called when a GeoPlace is selected from the drop down. Store the
		 * selected item and the text used at the time it was selected.
		 */
		setGeoPlace(adapter_.getItem(position));
	} // GeoPlace
	

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

	@Override
	public void onTextChanged(final CharSequence s, 
						      int start, 
							  int before,
							  int after) 
	{ 
		place_ = null;
		super.onTextChanged(s, start, before, after);
	} // onTextChanged
} // GeoAutoCompleteView
