package net.cyclestreets.views;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import net.cyclestreets.R;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.contacts.Contact;
import net.cyclestreets.contacts.ContactsSearch;
import net.cyclestreets.util.ListDialog;
import net.cyclestreets.util.MessageBox;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlaceView extends LinearLayout
					   implements OnClickListener, DialogInterface.OnClickListener	
{
	static private final String CURRENT_LOCATION = "Current Location";
	static private final String CONTACTS = "Contacts";
	static private final String MAP_POINT ="Point on map";
	
	final private Context context_;
	final private PlaceAutoCompleteTextView textView_;
	final private ImageButton button_;
	private GeoPlace currentLocation_;
	private GeoPlace mapPoint_;
	private List<String> options_;
	private List<Contact> contacts_;
	
	public PlaceView(final Context context) 
	{
		this(context, null);
	} // PlaceView
	
	public PlaceView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		context_ = context;
		
		setOrientation(HORIZONTAL);
		
		final LayoutInflater inflator = LayoutInflater.from(context);
		inflator.inflate(R.layout.placetextview, this);

		textView_ = (PlaceAutoCompleteTextView)findViewById(R.id.placeBox);
		button_ = (ImageButton)findViewById(R.id.optionsBtn);
		
		button_.setOnClickListener(this);
	} // PlaceView
	
	public void allowCurrentLocation(final GeoPoint loc) 
	{ 
		currentLocation_ = new GeoPlace(loc, CURRENT_LOCATION, ""); 
	} // allowCurrentLocation  // setBounds
	public void allowMapLocation(final GeoPoint loc) 
	{ 
		mapPoint_ = new GeoPlace(loc, MAP_POINT, ""); 
	} // allowMapLocation

	public String getText() { return textView_.getText().toString(); }

	public GeoPlace geoPlace() { return textView_.geoPlace(); } 

	public void addHistory(final GeoPlace place)
	{
	} // addHistory
	
	public BoundingBoxE6 bounds() { return textView_.bounds(); }
	public void setBounds(final BoundingBoxE6 bounds) { textView_.setBounds(bounds); }

	//////////////////////////////////////////
	private void setPlace(final GeoPlace geoPoint, final String label)
	{
		textView_.setGeoPlace(geoPoint);
		textView_.setText(label);
	} // setPlace
	
	private void setContact(final Contact contact)
	{
		textView_.setContact(contact);
	} // setContact
	
	@Override
	public void onClick(final View v) 
	{
		options_ = new ArrayList<String>();
		if(currentLocation_ != null)
			options_.add(CURRENT_LOCATION);
		options_.add(CONTACTS);
		if(mapPoint_ != null)
			options_.add(MAP_POINT);

		ListDialog.showListDialog(context_, 
		  			              "Choose location", 
								  options_, 
								  this);
	} // onClick

	@Override
	public void onClick(final DialogInterface dialog, final int whichButton)
	{
		final String option = options_.get(whichButton);
		
		if(CURRENT_LOCATION.equals(option))
			setPlace(currentLocation_, CURRENT_LOCATION);
		if(MAP_POINT.equals(option))
			setPlace(mapPoint_, MAP_POINT);
		
		if(CONTACTS.equals(option))
			pickContact();
	} // onClick
	
	private void pickContact()
	{
		if(contacts_ == null)
			contacts_ = ContactsSearch.contactsList(context_);
		if(contacts_.size() == 0)
		{
			MessageBox.OK(this, "None of your contacts have addresses.");
			return;
		} // if ...
		
		ListDialog.showListDialog(context_, 
				  "Contacts", 
				  contacts_, 
				  new ContactsListener());
	} // pickContact
	
	private class ContactsListener implements DialogInterface.OnClickListener	
	{
		@Override
		public void onClick(final DialogInterface dialog, final int whichButton) 
		{
			final Contact c = contacts_.get(whichButton);
			setContact(c);
		} // onClick
	} // class ContactsListener
} // class PlaceView
