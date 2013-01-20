package net.cyclestreets;

import net.cyclestreets.CycleStreetsConstants;
import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.R;
import net.cyclestreets.RouteMapActivity;
import net.cyclestreets.util.EditTextHistory;
import net.cyclestreets.util.RouteTypeMapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

public class RouteNumberActivity extends Activity 
						   implements View.OnClickListener
{
	private AutoCompleteTextView numberText_;
	private RadioGroup routeTypeGroup;
	private Button routeGo;
	private EditTextHistory history_;
	
	@Override
	public void onCreate(Bundle saved)
	{
	  super.onCreate(saved);

	  setContentView(R.layout.routenumber);
	  getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);       
	  getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	  getWindow().setBackgroundDrawableResource(R.drawable.empty);
	       
	  numberText_ = (AutoCompleteTextView)findViewById(R.id.routeNumber);
	  history_ = new EditTextHistory(this, "RouteNumber");
	  numberText_.setAdapter(history_);
	  
	  routeGo = (Button) findViewById(R.id.routeGo);
	  routeGo.setOnClickListener(this);
	  
	  routeTypeGroup = (RadioGroup) findViewById(R.id.routeTypeGroup);
	  routeTypeGroup.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));  	
  } // RouteActivity

	private void findRoute(long routeNumber)
	{
		final Intent intent = new Intent(this, RouteMapActivity.class);
		intent.putExtra(CycleStreetsConstants.EXTRA_ROUTE_NUMBER, routeNumber);
		final String routeType = RouteTypeMapper.nameFromId(routeTypeGroup.getCheckedRadioButtonId());
		intent.putExtra(CycleStreetsConstants.EXTRA_ROUTE_TYPE, routeType);
		setResult(RESULT_OK, intent);
		finish();
	} // findRoute

	@Override
	public void onClick(final View view)
	{
	  final String entered = numberText_.getText().toString();
	  if(entered.length() == 0)
	    return;
	  try {
	    history_.addHistory(entered);
	    long number = Long.parseLong(entered);
	    findRoute(number);
	  } //try
	  catch(final NumberFormatException e) {
	    // let's just swallow this, because hopefully it won't happen
	  } // catch
	} // onClick
} // RouteActivity
