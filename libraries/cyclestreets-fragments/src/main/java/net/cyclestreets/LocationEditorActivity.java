package net.cyclestreets;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import net.cyclestreets.content.LocationDatabase;
import net.cyclestreets.content.SavedLocation;
import net.cyclestreets.fragments.R;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.ThereOverlay;

import org.osmdroid.api.IGeoPoint;

public class LocationEditorActivity extends ActionBarActivity
    implements ThereOverlay.LocationListener,
               View.OnClickListener,
               TextWatcher {
  private CycleMapView map_;
  private ThereOverlay there_;
  private Button save_;
  private Button cancel_;
  private EditText nameBox_;
  private LocationDatabase ldb_;
  private int localId_;
  private boolean firstTime_;


  @Override
  public void onCreate(final Bundle saved) {
    super.onCreate(saved);

    localId_ = getIntent().getIntExtra("localId", -1);
    ldb_ = new LocationDatabase(this);

    setContentView(R.layout.location_editor);
    setupMap();
    setupButtons();
    setupEditBox();

    firstTime_ = true;
  } // onCreate

  private void setupMap() {
    final RelativeLayout v = (RelativeLayout)(findViewById(R.id.mapholder));

    map_ = new CycleMapView(this, getClass().getName());

    there_ = new ThereOverlay(this);
    there_.setLocationListener(this);

    map_.overlayPushTop(there_);

    v.addView(map_, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    map_.enableAndFollowLocation();
    map_.onResume();
    there_.setMapView(map_);
  } // setupMap

  private void setupButtons() {
    save_ = (Button)findViewById(R.id.save);
    save_.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_save, 0);
    save_.setOnClickListener(this);
    save_.setEnabled(false);

    cancel_ = (Button)findViewById(R.id.cancel);
    cancel_.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
    cancel_.setOnClickListener(this);
  } // setupButtons

  private void setupEditBox() {
    nameBox_ = (EditText)findViewById(R.id.name);
    nameBox_.addTextChangedListener(this);
  } // setupEditBox

  private void setupLocation() {
    if (localId_ == -1) {
      map_.enableAndFollowLocation();
      return;
    }

    SavedLocation location = ldb_.savedLocation(localId_);
    nameBox_.setText(location.name());
    there_.noOverThere(location.where());
    map_.centreOn(location.where());

    checkAllowSave();
  } // setupLocation

  @Override
  public void onResume() {
    ldb_ = new LocationDatabase(this);
    map_.onResume();
    super.onResume();

    if (firstTime_) {
      setupLocation();
      firstTime_ = false;
    } // if ...
  } // onResume

  @Override
  public void onPause() {
    super.onPause();
    map_.onPause();
    ldb_.close();
  } // onPause

  @Override
  public void onSetLocation(IGeoPoint point) {
    checkAllowSave();
  } // onSetLocation

  @Override
  public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
  @Override
  public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

  @Override
  public void afterTextChanged(Editable editable) {
    checkAllowSave();
  } // afterTextChanged

  private void checkAllowSave() {
    boolean allow =  (there_.there() != null) && (nameBox_.getText().length() > 0);
    save_.setEnabled(allow);
  } // checkAllowSave

  @Override
  public void onClick(View view) {
    if (save_ == view)
      saveLocation();
    finish();
  } // onClick

  private void saveLocation() {
    if (localId_ == -1)
      ldb_.addLocation(nameBox_.getText().toString(), there_.there());
    else
      ldb_.updateLocation(localId_, nameBox_.getText().toString(), there_.there());
  } // saveLocation
} // LocationEditorFragment
