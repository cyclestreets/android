package net.cyclestreets.views.place;

import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.views.PlaceAutoCompleteTextView;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;

import net.cyclestreets.content.LocationDatabase;
import net.cyclestreets.content.SavedLocation;
import net.cyclestreets.view.R;
import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoPlaces;
import net.cyclestreets.contacts.Contact;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.MessageBox;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlaceViewBase extends LinearLayout
             implements OnClickListener, DialogInterface.OnClickListener
{
  public interface OnResolveListener  {
    void onResolve(final GeoPlace place);
  }

  ////////////////////////////////
  private static String CURRENT_LOCATION;
  private static String LOCATION_NOT_FOUND;
  private static String CONTACTS;
  private static String NO_CONTACTS_WITH_ADDRESSES;
  private static String SAVED_LOCATIONS;

  private final Context context_;
  private final PlaceAutoCompleteTextView textView;
  private final ImageButton button;
  private final List<GeoPlace> allowedPlaces = new ArrayList<>();
  private final List<SavedLocation> savedLocations;
  private List<String> options;
  private List<Contact> contacts;

  protected PlaceViewBase(final Context context, final int layout, final AttributeSet attrs) {
    super(context, attrs);
    context_ = context;

    LocationDatabase locDb = new LocationDatabase(context);
    savedLocations = locDb.savedLocations();

    setOrientation(HORIZONTAL);

    final LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(layout, this);

    textView = findViewById(R.id.placeBox);
    final String hint = attrs != null ? attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "hint") : null;
    if (hint != null)
      textView.setHint(hint);

    button = findViewById(R.id.optionsBtn);

    button.setOnClickListener(this);

    loadStrings(context);
  }

  private void loadStrings(final Context context) {
    if (CURRENT_LOCATION != null)
      return;
    final Resources res = context.getResources();
    CURRENT_LOCATION = res.getString(R.string.placeview_current_location);
    LOCATION_NOT_FOUND = res.getString(R.string.placeview_location_not_found);
    CONTACTS = res.getString(R.string.placeview_contacts);
    NO_CONTACTS_WITH_ADDRESSES = res.getString(R.string.placeview_no_contacts_with_addresses);
    SAVED_LOCATIONS = res.getString(R.string.placeview_saved_locations);
  }

  ////////////////////////////////////
  public void allowCurrentLocation(final IGeoPoint loc, final boolean hint) {
    if (loc == null)
      return;
    final GeoPlace gp = new GeoPlace(loc, CURRENT_LOCATION, "");
    allowedPlaces.add(gp);
    if (hint)
      setPlaceHint(gp);
  }

  public void allowLocation(final IGeoPoint loc, final String label) {
    if (loc == null)
      return;
    allowedPlaces.add(new GeoPlace(loc, label, ""));
  }

  public String getText() { return textView.getText().toString(); }
  public void setText(final String text) { textView.setText(text); }
  public String getHint() { return textView.getHint().toString(); }
  public void setHint(final String text) { textView.setHint(text); }

  public void geoPlace(final OnResolveListener listener) {
    if (textView.geoPlace() != null) {
      listener.onResolve(textView.geoPlace());
      return;
    }

    if (textView.contact() != null)
      lookup(textView.contact(), listener);
    else if (getText() != null)
      lookup(getText(), listener);
  }

  public void addHistory(final GeoPlace place) {
    for (final GeoPlace gp : allowedPlaces)
      if (gp == place)
        return;
    textView.addHistory(place);
  }

  private BoundingBox bounds() { return textView.bounds(); }
  public void setBounds(final BoundingBox bounds) { textView.setBounds(bounds); }

  private void setPlace(final GeoPlace geoPlace) { textView.setGeoPlace(geoPlace); }
  private void setPlaceHint(final GeoPlace geoPlace) { textView.setGeoPlaceHint(geoPlace); }
  private void setContact(final Contact contact) { textView.setContact(contact); }
  private void setSavedLocation(final SavedLocation location) {
    GeoPlace gp = new GeoPlace(location.where(), location.name(), null);
    setPlace(gp);
  }

  @Override
  public void onClick(final View v) {
    options = new ArrayList<>();
    for (final GeoPlace gp : allowedPlaces)
      options.add(gp.name());
    if (contactsAvailable())
      options.add(CONTACTS);
    if (savedLocationsAvailable())
      options.add(SAVED_LOCATIONS);

    Dialog.listViewDialog(context_,
        R.string.placeview_choose_location,
        options,
        this);
  }

  private boolean contactsAvailable() {
    final PackageManager pm = context_.getPackageManager();
    int hasPerm = pm.checkPermission(Manifest.permission.READ_CONTACTS,
                                     context_.getPackageName());
    return (hasPerm == PackageManager.PERMISSION_GRANTED);
  }

  private boolean savedLocationsAvailable() {
    return savedLocations.size() != 0;
  }

  @Override
  public void onClick(final DialogInterface dialog, final int whichButton) {
    final String option = options.get(whichButton);

    for (final GeoPlace gp : allowedPlaces)
      if (gp.name().equals(option))
        setPlaceHint(gp);

    if (CONTACTS.equals(option))
      pickContact();

    if (SAVED_LOCATIONS.equals(option))
      pickSavedLocation();
  }

  private void pickContact() {
    if (contacts == null) {
      loadContacts();
      return;
    }

    if (contacts.size() == 0) {
      MessageBox.OK(this, NO_CONTACTS_WITH_ADDRESSES);
      return;
    }

    Dialog.listViewDialog(context_,
                          R.string.placeview_contacts,
                          contacts,
                          new ContactsListener());
  }

  private class ContactsListener implements DialogInterface.OnClickListener  {
    @Override
    public void onClick(final DialogInterface dialog, final int whichButton) {
      final Contact c = contacts.get(whichButton);
      setContact(c);
    }
  }

  private void pickSavedLocation() {
    Dialog.listViewDialog(context_,
                          R.string.placeview_saved_locations,
                          savedLocations,
                          new SavedLocationListener());
  }

  private class SavedLocationListener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(final DialogInterface dialog, final int whichButton) {
      final SavedLocation l = savedLocations.get(whichButton);
      setSavedLocation(l);
    }
  }

  ///////////////////////////////////////////////////////////
  private void loadContacts() {
    final AsyncContactLoad acl = new AsyncContactLoad(this);
    acl.execute();
  }

  void onContactsLoaded(final List<Contact> contacts) {
    this.contacts = contacts;
    pickContact();
  }

  ///////////////////////////////////////////////////////////
  private void lookup(final Object what, final OnResolveListener listener) {
    final AsyncContactLookup asc = new AsyncContactLookup(this, listener);
    asc.execute(what, bounds());
  }

  void resolvedContacts(final GeoPlaces results,
                        final OnResolveListener listener) {
    if (results.isEmpty()) {
      MessageBox.OK(this, LOCATION_NOT_FOUND);
      return;
    }

    if (results.size() == 1) {
      textView.setGeoPlace(results.get(0));
      listener.onResolve(results.get(0));
      return;
    }

    Dialog.listViewDialog(context_,
                          R.string.placeview_choose_location,
                          results.asList(),
                          new PlaceListener(results, listener));
  }

  private class PlaceListener implements DialogInterface.OnClickListener  {
    private GeoPlaces results_;
    private OnResolveListener listener_;

    public PlaceListener(final GeoPlaces results,
                         final OnResolveListener listener) {
      results_ = results;
      listener_ = listener;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int whichButton) {
      textView.setGeoPlace(results_.get(whichButton));
      listener_.onResolve(results_.get(whichButton));
    }
  }
}
