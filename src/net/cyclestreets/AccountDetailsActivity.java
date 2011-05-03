package net.cyclestreets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AccountDetailsActivity extends Activity
									implements View.OnClickListener
{
	public enum RegisterStep
	{
		ACCOUNT(null),
		
		REGISTER_DETAILS(ACCOUNT),
		REGISTER(REGISTER_DETAILS),
		REGISTERED_OK(REGISTER),

		SIGNIN_DETAILS(ACCOUNT),
		SIGNIN(SIGNIN_DETAILS),
		SIGNEDIN_OK(SIGNIN),
		
		EXISTING_SIGNIN_DETAILS(null),
		EXISTING_SIGNIN(EXISTING_SIGNIN_DETAILS),
		EXISTING_SIGNEDIN_OK(EXISTING_SIGNIN);
		
		private RegisterStep(final RegisterStep p)
		{
			prev_ = p;
			if(prev_ != null)
				prev_.next_ = this;
		} // AddStep

		public RegisterStep prev() { return prev_; }
		public RegisterStep next() { return next_; }
		
		private RegisterStep prev_;
		private RegisterStep next_;
	} // RegisterStep	
	
	private RegisterStep step_;
	
	private View registerView_;
	private View registerDetails_;
	private View signinDetails_;
	
    @Override
    public void onCreate(final Bundle saved)
    {
        super.onCreate(saved);
        
		final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		registerView_ = inflater.inflate(R.layout.accountdetails, null);
		registerDetails_ = inflater.inflate(R.layout.accountregister, null);
		signinDetails_ = inflater.inflate(R.layout.accountsignin, null);

		step_ = (CycleStreetsPreferences.username() != null) ? RegisterStep.ACCOUNT : RegisterStep.EXISTING_SIGNIN;
		
		setupView();
    } // onCreate

	@Override 
	public void onBackPressed()
	{
		step_ = step_.prev();

		if(step_ != null)
			setupView();
		else
			super.onBackPressed();
	} // onBackPressed    
    
	private void setupView()
	{
		switch(step_)
		{
		case ACCOUNT:
			setContentView(registerView_);
			hookUpButton(registerView_, R.id.newaccount_button);
			hookUpButton(registerView_, R.id.existingaccount_button);
			break;
		case REGISTER_DETAILS:
			setContentView(registerDetails_);
			break;
		case SIGNIN_DETAILS:
		case EXISTING_SIGNIN_DETAILS:
			setContentView(signinDetails_);
			setText(R.id.username, CycleStreetsPreferences.username());
			setText(R.id.password, CycleStreetsPreferences.password());
			hookUpButton(signinDetails_, R.id.cleardetails_button);
			break;
		} // switch
	} // setupView
	
	private void hookUpButton(final View v, final int id)
	{
		final Button b = (Button)v.findViewById(id);
		if(b == null)
			return;
		b.setOnClickListener(this);		
	} // hookUpNext
	
	private void setText(final int id, final String value)
	{
		final TextView tv = (TextView)findViewById(id);
		if(tv == null)
			return;
		tv.setText(value);
	} // setText
	
	@Override
	public void onClick(final View v) 
	{
		Intent i = null;
		
		switch(v.getId())
		{
			case R.id.newaccount_button:
				step_ = RegisterStep.REGISTER_DETAILS;
				break;
			case R.id.existingaccount_button:
				step_ = RegisterStep.SIGNIN_DETAILS;
				break;
			case R.id.cleardetails_button:
				confirmClear();
				break;
//			case R.id.next:
//				nextStep();
//				break;
		} // switch
		
		if(i != null)
			startActivityForResult(i, v.getId());
		else
			setupView();
	} // onClick
	
	private void confirmClear()
	{
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage("Are you sure you want to clear the stored account details?");
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
        		// clear the details
        		step_ = step_.prev();
        		setupView();
        	}
        });
        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
        	// do something when the button is clicked
        	public void onClick(DialogInterface arg0, int arg1) {  }
        });
        final AlertDialog ab = alertbox.create();
        ab.show();
	} // confirmClear
} // class AccountDetailsActivity
