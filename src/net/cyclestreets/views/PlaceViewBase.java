package net.cyclestreets.views;

import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import net.cyclestreets.R;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.contacts.Contact;
import net.cyclestreets.contacts.Contacts;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.ListDialog;
import net.cyclestreets.util.MessageBox;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlaceViewBase extends LinearLayout
             implements OnClickListener, DialogInterface.OnClickListener  
{
  public interface OnResolveListener
  {
    public void onResolve(final GeoPlace place);
  } // interface ResolveListener
  
  ////////////////////////////////
  static private String CURRENT_LOCATION;
  static private String CHOOSE_LOCATION;
  static private String LOCATION_NOT_FOUND;
  static private String LOCATION_SEARCH;
  static private String CONTACTS;
  static private String NO_CONTACTS_WITH_ADDRESSES;
  static private String CONTACTS_LOADING;
  
  private final Context context_;
  private final PlaceAutoCompleteTextView textView_;
  private final ImageButton button_;
  private List<GeoPlace> allowedPlaces_;
  private List<String> options_;
  private List<Contact> contacts_;
  
  protected PlaceViewBase(final Context context, final int layout, final AttributeSet attrs)
  {
    super(context, attrs);
    context_ = context;
    
    setOrientation(HORIZONTAL);
    
    final LayoutInflater inflator = LayoutInflater.from(context);
    inflator.inflate(layout, this);

    textView_ = (PlaceAutoCompleteTextView)findViewById(R.id.placeBox);
    button_ = (ImageButton)findViewById(R.id.optionsBtn);
    
    button_.setOnClickListener(this);
    
    allowedPlaces_ = new ArrayList<GeoPlace>();

    loadStrings(context);
  } // PlaceViewBase
  
  private void loadStrings(final Context context)
  {
    if(CURRENT_LOCATION != null) 
      return;
    final Resources res = context.getResources();
    CURRENT_LOCATION = res.getString(R.string.placeview_current_location);
    CHOOSE_LOCATION = res.getString(R.string.placeview_choose_location);
    LOCATION_NOT_FOUND = res.getString(R.string.placeview_location_not_found);
    LOCATION_SEARCH = res.getString(R.string.placeview_location_search);
    CONTACTS = res.getString(R.string.placeview_contacts);
    NO_CONTACTS_WITH_ADDRESSES = res.getString(R.string.placeview_no_contacts_with_addresses);
    CONTACTS_LOADING = res.getString(R.string.placeview_contacts_loading);
  } // loadStrings
  
  ////////////////////////////////////
  public void allowCurrentLocation(final GeoPoint loc, final boolean hint) 
  { 
    if(loc == null)
      return;
    final GeoPlace gp = new GeoPlace(loc, CURRENT_LOCATION, "");
    allowedPlaces_.add(gp);
    if(hint)
      setPlaceHint(gp);
  } // allowCurrentLocation  
  
  public void allowLocation(final GeoPoint loc, final String label)
  { 
    if(loc == null)
      return;
    allowedPlaces_.add(new GeoPlace(loc, label, "")); 
  } // allowMapLocation

  public String getText() { return textView_.getText().toString(); }
  public void setText(final String text) { textView_.setText(text); }
  public String getHint() { return textView_.getHint().toString(); }
  public void setHint(final String text) { textView_.setHint(text); }

  public void geoPlace(final OnResolveListener listener) 
  { 
    if(textView_.geoPlace() != null)
    {
      listener.onResolve(textView_.geoPlace());
      return;
    } // if ...
    
    if(textView_.contact() != null)
      lookup(textView_.contact(), listener);
    else if(getText() != null)
      lookup(getText(), listener);      
  } // geoPlace 
  
  public void addHistory(final GeoPlace place) 
  { 
    for(final GeoPlace gp : allowedPlaces_)
      if(gp == place)
        return;      
    textView_.addHistory(place); 
  } // addHistory
  
  private BoundingBoxE6 bounds() { return textView_.bounds(); }
  public void setBounds(final BoundingBoxE6 bounds) { textView_.setBounds(bounds); }
  
  public void swap(final PlaceViewBase other)
  {
    final String to = other.getText();
    final String ho = other.getHint();
    final GeoPlace po = other.textView_.geoPlace();
    
    final String t = getText();
    final String h = getHint();
    final GeoPlace p = textView_.geoPlace();
    
    set(po, to, ho);
    other.set(p, t, h);
  } // other

  //////////////////////////////////////////
  private void set(final GeoPlace gp, final String t, final String h)
  {
    if(gp == null)
    {
      if(notEmpty(t))
        setText(t);
      else if(notEmpty(h))
        setHint(h);
      else
        setText("");
      return;
    } // if ...
    if(notEmpty(t))
      setPlace(gp);
    else
      setPlaceHint(gp);
  } // set
  private void setPlace(final GeoPlace geoPlace) { textView_.setGeoPlace(geoPlace); }
  private void setPlaceHint(final GeoPlace geoPlace) { textView_.setGeoPlaceHint(geoPlace); }
  private void setContact(final Contact contact) { textView_.setContact(contact); }
  
  private boolean notEmpty(final String s) { return s != null && s.length() != 0; }
  
  @Override
  public void onClick(final View v) 
  {
    options_ = new ArrayList<String>();
    for(final GeoPlace gp : allowedPlaces_)
      options_.add(gp.name());
    options_.add(CONTACTS);

    ListDialog.showListDialog(context_, 
                              CHOOSE_LOCATION, 
                              options_, 
                              this);
  } // onClick

  @Override
  public void onClick(final DialogInterface dialog, final int whichButton)
  {
    final String option = options_.get(whichButton);

    for(final GeoPlace gp : allowedPlaces_)
      if(gp.name().equals(option))
        setPlaceHint(gp);
    
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
      MessageBox.OK(this, NO_CONTACTS_WITH_ADDRESSES);
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
  private void lookup(final Object what, final OnResolveListener listener)
  {
    final AsyncContactLookup asc = new AsyncContactLookup(this, listener);
    asc.execute(what, bounds());
  } // lookup

  private void resolvedContacts(final GeoPlaces results,
                                final OnResolveListener listener)
  {
    if(results.isEmpty())
    {
      MessageBox.OK(this, LOCATION_NOT_FOUND);
      return;
    } // if ...
    
    if(results.size() == 1)
    {
      textView_.setGeoPlace(results.get(0));
      listener.onResolve(results.get(0));
      return;
    } // if ...

    ListDialog.showListDialog(context_, 
                              CHOOSE_LOCATION, 
                              results.asList(), 
                              new PlaceListener(results, listener));
  } // resolvedContacts
  
  private class PlaceListener implements DialogInterface.OnClickListener
  {
    private GeoPlaces results_;
    private OnResolveListener listener_;
    
    public PlaceListener(final GeoPlaces results, 
                         final OnResolveListener listener)
    {
      results_ = results;
      listener_ = listener;
    } // PlaceListener
    
    @Override
    public void onClick(final DialogInterface dialog, final int whichButton)
    {
      textView_.setGeoPlace(results_.get(whichButton));
      listener_.onResolve(results_.get(whichButton));
    } // onClick
  } // PlaceListener

  ///////////////////////////////////////////////////////////
  static private class AsyncContactLoad extends AsyncTask<Void, Void, List<Contact>>
  {
    final ProgressDialog progress_;
    final PlaceViewBase view_;

    public AsyncContactLoad(final PlaceViewBase view)
    {
      progress_ = Dialog.createProgressDialog(view.getContext(), CONTACTS_LOADING);
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
 
  ////////////////////////////////  
  static private class AsyncContactLookup extends AsyncTask<Object, Void, GeoPlaces>
  {
    final ProgressDialog progress_;
    final OnResolveListener listener_;
    final PlaceViewBase view_;
    
    public AsyncContactLookup(final PlaceViewBase view,
                              final OnResolveListener listener)
    {
      progress_ = Dialog.createProgressDialog(view.getContext(), LOCATION_SEARCH);
      view_ = view;
      listener_ = listener;
    } // AsyncLookup
    
    @Override
    protected void onPreExecute() {  progress_.show(); }
    
    @Override
    protected GeoPlaces doInBackground(Object... params) 
    {
      final BoundingBoxE6 bounds = (BoundingBoxE6)params[1];

      if(params[0] instanceof String)
        return doSearch((String)params[0], bounds);
      
      return doContactSearch((Contact)params[0], bounds);
    } // doInBackground

    @Override
    protected void onPostExecute(final GeoPlaces result) 
    { 
      progress_.dismiss(); 
      view_.resolvedContacts(result, listener_);
    } // onPostExecute
    
    private GeoPlaces doContactSearch(final Contact contact, 
                                      final BoundingBoxE6 bounds)
    {
      GeoPlaces r = doSearch(contact.address(), bounds);
      if(!r.isEmpty())
        return r;
      
      r = doSearch(contact.postcode(), bounds);
      if(!r.isEmpty())
        return r;
      
      r = doSearch(contact.city(), bounds);
      return r;
    } // doContactSearch
    
    private GeoPlaces doSearch(final String search, 
                    final BoundingBoxE6 bounds)
    {
      try {
        return GeoPlaces.search(search, bounds);        
      }
      catch(Exception e) {
        return GeoPlaces.EMPTY;
      } // catch
    } // doSearch
  } // AsyncContactLookup
} // class PlaceView

