package net.cyclestreets.views;

import java.util.List;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoLiveAdapter;
import net.cyclestreets.contacts.Contact;
import net.cyclestreets.contacts.ContactLookup;

import org.osmdroid.util.BoundingBoxE6;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlaceAutoCompleteTextView extends AutoCompleteTextView 
								 implements OnClickListener, OnItemClickListener
{
	private GeoLiveAdapter adapter_;
	private GeoPlace place_;
	private Contact contact_;
	
	public PlaceAutoCompleteTextView(final Context context)
	{
		super(context);
		init();
	} // PlaceAutoCompleteTextView

	public PlaceAutoCompleteTextView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		init();
	} // PlaceAutoCompleteTextView

	public PlaceAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	} // PlaceAutoCompleteTextView
	
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
		if(place_ != null)
			return place_;

		return lookupContact();
	} // geoPlace
	
	public void setGeoPlace(final GeoPlace place)
	{
		// set text first because we clear place_ in the callback
		// then set place_
		setText(place.toString());
		place_ = place;
	} // setGeoPlace
	
	public void setContact(final Contact contact)
	{
		setText(contact.address());
		contact_ = contact;
	} // setContact
	
	public void addHistory(final GeoPlace place)
	{
		if(adapter_ == null)
			return;
		adapter_.addHistory(place);
	} // addHistory
	
	private GeoPlace lookupContact()
	{
		List<GeoPlace> gps = (contact_ != null) 
								? ContactLookup.lookup(contact_, bounds(), getContext()) 
								: ContactLookup.lookup(getText().toString(), bounds(), getContext());
		if(gps.size() == 1)
			return gps.get(0);
		return null;
	} // lookupContact
	
	/////////////////////////////////////
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
	{
		if(adapter_ == null)
			return;
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
		contact_ = null;
		super.onTextChanged(s, start, before, after);
	} // onTextChanged
} // PlaceAutoCompleteTextView
