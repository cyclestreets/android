package net.cyclestreets.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import net.cyclestreets.R;
import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.contacts.Contact;
import net.cyclestreets.contacts.Contacts;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.ListDialog;
import net.cyclestreets.util.MessageBox;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlaceView extends LinearLayout
					   implements OnClickListener, DialogInterface.OnClickListener	
{
	public interface OnResolveListener
	{
		public void onResolve(final GeoPlace place);
	} // interface ResolveListener
	
	////////////////////////////////
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

	public void geoPlace(final OnResolveListener listener) 
	{ 
		if(textView_.geoPlace() != null)
			listener.onResolve(textView_.geoPlace());
		
		if(textView_.contact() != null)
			lookup(textView_.contact(), listener);
		else if(getText() != null)
			lookup(getText(), listener);			
	} // geoPlace 
	
	private void lookup(final Object what, final OnResolveListener listener)
	{
		final AsyncContactLookup asc = new AsyncContactLookup(this, listener);
		asc.execute(what, bounds());
	} // lookup
	/*
	
	private GeoPlace lookupContact()
	{
		List<GeoPlace> gps = (contact_ != null) 
								? ContactLookup.lookup(contact_, bounds(), getContext()) 
								: ContactLookup.lookup(getText().toString(), bounds(), getContext());
		if(gps.size() == 1)
			return gps.get(0);
		return null;
	} // lookupContact
	

	 */

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
		{
			loadContacts();
			return;
		} // if ...

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
	
	///////////////////////////////////////////////////////////
	private void loadContacts()
	{
		final AsyncContactLoad acl = new AsyncContactLoad(this);
		acl.execute();
	} // loadContacts
	
	private void onContactsLoaded(final List<Contact> contacts)
	{
		contacts_ = contacts;
		pickContact();
	} // onContactsLoaded
	
	///////////////////////////////////////////////////////////
	private void resolvedContacts(final List<GeoPlace> results,
								  final OnResolveListener listener)
	{
		if(results.size() == 1)
			listener.onResolve(results.get(0));

		ListDialog.showListDialog(context_, 
				  "Choose location", 
				  results, 
				  new PlaceListener(results, listener));
	} // resolvedContacts
	
	private class PlaceListener implements DialogInterface.OnClickListener
	{
		private List<GeoPlace> results_;
		private OnResolveListener listener_;
		
		public PlaceListener(final List<GeoPlace> results, 
				             final OnResolveListener listener)
		{
			results_ = results;
			listener_ = listener;
		} // PlaceListener
		
		@Override
		public void onClick(final DialogInterface dialog, final int whichButton)
		{
			listener_.onResolve(results_.get(whichButton));
		} // onClick
	} // PlaceListener

	///////////////////////////////////////////////////////////
	static private class AsyncContactLoad extends AsyncTask<Void, Void, List<Contact>>
	{
		final ProgressDialog progress_;
		final PlaceView view_;

		public AsyncContactLoad(final PlaceView view)
		{
			progress_ = Dialog.createProgressDialog(view.getContext(), "Loading contacts");
			view_ = view;
		} // AsyncContactLoad
		
		@Override 
		protected void onPreExecute() { progress_.show(); }
		
		@Override
		protected List<Contact> doInBackground(Void... params)
		{
			return Contacts.load(view_.getContext());
		} // doInBackground
		
		@Override
		protected void onPostExecute(final List<Contact> results) 
		{
			progress_.dismiss();
			view_.onContactsLoaded(results);
		} // onPostExecute
	} // class AsyncContactLoad
	
	static private class AsyncContactLookup extends AsyncTask<Object, Void, List<GeoPlace>>
	{
		final ProgressDialog progress_;
		final OnResolveListener listener_;
		final PlaceView view_;
		
		public AsyncContactLookup(final PlaceView view,
								  final OnResolveListener listener)
		{
			progress_ = Dialog.createProgressDialog(view.getContext(), "Searching for location");
			view_ = view;
			listener_ = listener;
		} // AsyncLookup
		
		@Override
		protected void onPreExecute() {	progress_.show(); }
		
		@Override
		protected List<GeoPlace> doInBackground(Object... params) 
		{
			final BoundingBoxE6 bounds = (BoundingBoxE6)params[1];

			if(params[0] instanceof String)
				return doSearch((String)params[0], bounds);
			
			return doContactSearch((Contact)params[0], bounds);
		} // doInBackground

		@Override
		protected void onPostExecute(final List<GeoPlace> result) 
		{ 
			progress_.dismiss(); 
			view_.resolvedContacts(result, listener_);
		} // onPostExecute
		
		private List<GeoPlace> doContactSearch(final Contact contact, 
											   final BoundingBoxE6 bounds)
		{
			List<GeoPlace> r = doSearch(contact.address(), bounds);
			if(!r.isEmpty())
				return r;
			
			r = doSearch(contact.postcode(), bounds);
			if(!r.isEmpty())
				return r;
			
			r = doSearch(contact.city(), bounds);
			return r;
		} // doContactSearch
		
		@SuppressWarnings("unchecked")
		private List<GeoPlace> doSearch(final String search, 
										final BoundingBoxE6 bounds)
		{
			try {
				return ApiClient.geoCoder(search, bounds).places;				
			}
			catch(Exception e) {
				return (List<GeoPlace>)Collections.EMPTY_LIST;
			} // catch
		} // doSearch
	} // AsyncContactLookup
} // class PlaceView

