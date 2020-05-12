package net.cyclestreets.views;

import java.util.Set;
import java.util.HashSet;

import net.cyclestreets.api.GeoPlace;
import net.cyclestreets.api.GeoLiveAdapter;
import net.cyclestreets.contacts.Contact;

import org.osmdroid.util.BoundingBox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlaceAutoCompleteTextView extends AppCompatAutoCompleteTextView
                 implements OnClickListener, OnItemClickListener
{
  private GeoLiveAdapter adapter_;
  private GeoPlace place_;
  private Contact contact_;
  private Set<GeoPlace> localHistory_ = new HashSet<>();

  public PlaceAutoCompleteTextView(final Context context) {
    super(context);
    init();
  }

  public PlaceAutoCompleteTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PlaceAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setThreshold(0);
    setOnClickListener(this);
    setOnItemClickListener(this);
  }

  /////////////////////////////////////
  public BoundingBox bounds() {  return adapter_.bounds(); }
  public void setBounds(final BoundingBox bounds) {
    adapter_ = new GeoLiveAdapter(getContext(), bounds);
    setAdapter(adapter_);
  }

  public GeoPlace geoPlace() {
    if (place_ == null) {
      final String t = getEditableText().toString();
      for (final GeoPlace gp : localHistory_)
        if (t.equals(gp.toString()))
          place_ = gp;
    }
    return place_;
  }
  public void setGeoPlace(final GeoPlace place) {
    // set text first because we clear place_ in the callback
    // then set place_
    setText(place.toString());
    place_ = place;
    localHistory_.add(place);
  }
  public void setGeoPlaceHint(final GeoPlace place) {
    setText("");
    setHint(place.toString());
    place_ = place;
    localHistory_.add(place);
  }

  public Contact contact() { return contact_; }
  public void setContact(final Contact contact) {
    setText(contact.address());
    contact_ = contact;
  }

  public void addHistory(final GeoPlace place) {
    if (adapter_ == null)
      return;
    adapter_.addHistory(place);
  }

  /////////////////////////////////////
  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
    if (adapter_ != null && position < adapter_.getCount())
      setGeoPlace(adapter_.getItem(position));
  }

  @Override
  public boolean enoughToFilter() { return true; }

  @Override
  public void onFilterComplete(int count) {
    if (hasFocus() && hasWindowFocus() && (place_ == null))
      doShowDropDown();
    else
      dismissDropDown();
  }

  @Override
  public void onClick(View v) {
    performFiltering(null, KeyEvent.KEYCODE_FOCUS);
    doShowDropDown();
  }

  private void doShowDropDown() {
    setDropDownWidth((int)(getWidth() * 0.9));
    showDropDown();
  }

  @Override
  public void onEditorAction(int actionCode) {
    super.onEditorAction(actionCode);
    dismissDropDown();
  }

  @Override
  public void onTextChanged(final CharSequence s,
                            int start,
                            int before,
                            int after) {
    place_ = null;
    contact_ = null;
    setHint("");
    super.onTextChanged(s, start, before, after);
  }
}
