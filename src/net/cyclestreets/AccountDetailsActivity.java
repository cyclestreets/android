package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
		
		EXISTING_SIGNIN_DETAILS(null),;
		
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

		step_ = (CycleStreetsPreferences.accountOK()) ? RegisterStep.EXISTING_SIGNIN_DETAILS : RegisterStep.ACCOUNT;
		
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
			setText(registerDetails_, R.id.username, CycleStreetsPreferences.username());
			setText(registerDetails_, R.id.password, CycleStreetsPreferences.password());
			setText(registerDetails_, R.id.name, CycleStreetsPreferences.name());
			setText(registerDetails_, R.id.email, CycleStreetsPreferences.email());
			hookUpButton(registerDetails_, R.id.register_button);
			break;
		case SIGNIN_DETAILS:
		case EXISTING_SIGNIN_DETAILS:
			setContentView(signinDetails_);
			setText(signinDetails_, R.id.username, CycleStreetsPreferences.username());
			setText(signinDetails_, R.id.password, CycleStreetsPreferences.password());
			setText(signinDetails_, R.id.signin_message, signinMessage());
			hookUpButton(signinDetails_, R.id.signin_button);
			hookUpButton(signinDetails_, R.id.cleardetails_button);
			break;
		} // switch
	} // setupView
	
	private String signinMessage()
	{
		if(CycleStreetsPreferences.accountOK())
			return "You are already signed in.";
		return "Please enter your account username and password to sign in.";
	} // signinMessage
	
	private void hookUpButton(final View v, final int id)
	{
		final Button b = (Button)v.findViewById(id);
		if(b == null)
			return;
		b.setOnClickListener(this);		
	} // hookUpNext
	
	private void setText(final View v, final int id, final String value)
	{
		final TextView tv = (TextView)v.findViewById(id);
		if(tv == null)
			return;
		tv.setText(value);
	} // setText
	
	private String getText(final View v, final int id)
	{
		final TextView tv = (TextView)v.findViewById(id);
		return tv.getText().toString();
	} // getText
	
	@Override
	public void onClick(final View v) 
	{
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
			case R.id.signin_button:
				signin();
				return;
			case R.id.register_button:
				register();
				return;
		} // switch
		
		setupView();
	} // onClick
	
	private void confirmClear()
	{
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage("Are you sure you want to clear the stored account details?");
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
        		CycleStreetsPreferences.setUsernamePassword("", "", false);
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

	////////////////////////////////////////////////////////
	private void signin()
	{
		final SignInTask task = new SignInTask(this,
											   getText(signinDetails_, R.id.username),
											   getText(signinDetails_, R.id.password));
		task.execute();
	} // signin
	
	private void signinOK(final String username, final String password)
	{
		CycleStreetsPreferences.setUsernamePassword(username, password, true);
		
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage("You have successfully signed into CycleStreets.");
        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
                finish();
        	}
        });
        final AlertDialog ab = alertbox.create();
        ab.show();
	} // signinOK
	
	private void signinFailed(final String msg, final String username, final String password)
	{
		CycleStreetsPreferences.setUsernamePassword(username, password, false);

		final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage(msg);
        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
        	}
        });
        final AlertDialog ab = alertbox.create();
        ab.show();
	} // signinFailed
	
	private class SignInTask extends AsyncTask<Object, Void, String>
	{
		private final String username_;
		private final String password_;
		private final ProgressDialog progress_;
		
		SignInTask(final Context context,
	 			   final String username,
	 			   final String password) 
	    {
			username_ = username;
			password_ = password;
			
			progress_ = new ProgressDialog(context);
			progress_.setMessage(context.getString(R.string.signing_in));
			progress_.setIndeterminate(true);
			progress_.setCancelable(false);
	    } // SigninTask
		
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			progress_.show();
		} // onPreExecute
		
		protected String doInBackground(Object... params)
		{
			try {
				return ApiClient.signin(username_, 
						                password_);
			} // try
			catch(final Exception e) {
				return "Error: " + e.getMessage();
			} // catch
		} // doInBackground
		
		@Override
	    protected void onPostExecute(final String result) 
		{
	       	progress_.dismiss();
	       	
	       	// I don't usually condone doing this kind of thing
	       	// with XML
	       	if(result.indexOf("<id>") != -1)
	       	{
	       		signinOK(username_, password_);
	       		return;
	       	} // if ...

	       	final String msg = result.startsWith("Error:") ? result : "Could not sign into CycleStreets.  Please check your username and password.";
	       	signinFailed(msg, username_, password_);
		} // onPostExecute
	} // class SignInTask
	
	////////////////////////////////////////////////////////
	private void register()
	{
		final RegisterTask task = new RegisterTask(this,
												   getText(registerDetails_, R.id.username),
												   getText(registerDetails_, R.id.password),
												   getText(registerDetails_, R.id.name),
												   getText(registerDetails_, R.id.email));
		task.execute();
	} // signin
	
	private void registeredOK(final String username, 
							  final String password,
							  final String name,
							  final String email)
	{
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage("You have successfully signed into CycleStreets.");
        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
        	}
        });
        final AlertDialog ab = alertbox.create();
        ab.show();
        finish();
	} // signinOK
	
	private void registrationFailed(final String msg, 
									final String username, 
									final String password,
									final String name,
									final String email)
	{
		final AlertDialog.Builder alertbox = new AlertDialog.Builder(signinDetails_.getContext());
        alertbox.setTitle("CycleStreets");
        alertbox.setMessage(msg);
        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface arg0, int arg1) {
        	}
        });
        final AlertDialog ab = alertbox.create();
        ab.show();
	} // signinFailed
	
	private class RegisterTask extends AsyncTask<Object, Void, String>
	{
		private final String username_;
		private final String password_;
		private final String name_;
		private final String email_;
		private final ProgressDialog progress_;
		
		RegisterTask(final Context context,
					 final String username,
					 final String password,
					 final String name,
					 final String email) 
	    {
			username_ = username;
			password_ = password;
			name_ = name;
			email_ = email;
			
			progress_ = new ProgressDialog(context);
			progress_.setMessage(context.getString(R.string.signing_in));
			progress_.setIndeterminate(true);
			progress_.setCancelable(false);
	    } // RegisterTask
		
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			progress_.show();
		} // onPreExecute
		
		protected String doInBackground(Object... params)
		{
			try {
				return ApiClient.register(username_, 
						                  password_,
						                  name_,
						                  email_);
			} // try
			catch(final Exception e) {
				return "Error: " + e.getMessage();
			} // catch
		} // doInBackground
		
		@Override
	    protected void onPostExecute(final String result) 
		{
	       	progress_.dismiss();
	       	
	       	// I don't usually condone doing this kind of thing
	       	// with XML
	       	if(result.indexOf("<id>") != -1)
	       	{
	       		registeredOK(username_, password_, name_, email_);
	       		return;
	       	} // if ...

	       	final String msg = result.startsWith("Error:") ? result : "Could not sign into CycleStreets.  Please check your username and password.";
	       	registrationFailed(msg, username_, password_, name_, email_);
		} // onPostExecute
	} // class UploadPhotoTask
	
} // class AccountDetailsActivity
