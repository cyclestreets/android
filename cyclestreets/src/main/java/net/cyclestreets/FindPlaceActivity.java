package net.cyclestreets;

import net.cyclestreets.api.GeoPlace;

import net.cyclestreets.util.GeoIntent;
import net.cyclestreets.views.PlaceView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class FindPlaceActivity extends Activity 
implements View.OnClickListener, PlaceView.OnResolveListener
{
  private PlaceView place_;

  @Override
  public void onCreate(final Bundle saved)
  {
    super.onCreate(saved);

    setContentView(R.layout.findplace);
    getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    getWindow().setBackgroundDrawableResource(R.drawable.empty);
    
    place_ = (PlaceView)findViewById(R.id.place);
    place_.setBounds(GeoIntent.getBoundingBox(getIntent()));
    
    final Button findButton = (Button)findViewById(R.id.find_place);
    findButton.setOnClickListener(this);
  } // onCreate

  private void placeSelected(final GeoPlace place)
  {
    if (place == null || place.coord() == null)
      return;

    place_.addHistory(place);

    final Intent intent = new Intent(this, RouteMapFragment.class);
    GeoIntent.setGeoPoint(intent, place.coord());
    setResult(RESULT_OK, intent);
    finish();
  } // placeSelected

  @Override
  public void onClick(final View view)
  {
    final String from = place_.getText();
    if(from.length() == 0)
    {
      Toast.makeText(this, R.string.lbl_choose_place, Toast.LENGTH_LONG).show();
      return;
    } // if ...

    place_.geoPlace(this);
  } // onClick

  @Override
  public void onResolve(final GeoPlace place)   
  {
    placeSelected(place);
  } // onResolve
} // class FindPlaceActivity
