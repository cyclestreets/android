package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.api.RegistrationResult;
import net.cyclestreets.api.SigninResult;
import android.app.Activity;
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

		SIGNIN_DETAILS(ACCOUNT),
		
		EXISTING_SIGNIN_DETAILS(null);
		
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
			setText(registerDetails_, R.id.registration_message, registrationMessage());
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
			return String.format("You are already signed in as\n%s, %s", 
								 CycleStreetsPreferences.name(),
								 CycleStreetsPreferences.email());
		return "Please enter your account username and password to sign in.";
	} // signinMessage
	
	private String registrationMessage()
	{
		if(CycleStreetsPreferences.accountPending())
			return "You have already registered an account.  Please check your email for the verification email.";
		return "Registration is free and registered users can add photos. To start " + 
		       "the registration process please enter your details in the form below.";
	} // registrationMessage
	
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
		MessageBox.YesNo(signinDetails_, 
						 "Are you sure you want to clear the stored account details?",
						 new DialogInterface.OnClickListener() {
					        	public void onClick(DialogInterface arg0, int arg1) {
					        		CycleStreetsPreferences.clearUsernamePassword();
					        		setupView();
					        	}
					        });
	} // confirmClear

	////////////////////////////////////////////////////////
	private void MessageBox(final String message, final boolean finishOnOK)
	{
		MessageBox.OKAndFinish(this, message, finishOnOK);
	} // MessageBox
	
	////////////////////////////////////////////////////////
	private void signin()
	{
		final String username = getText(signinDetails_, R.id.username);
		final String password = getText(signinDetails_, R.id.password);
		
		if((username.length() == 0) || (password.length() == 0))
		{
			MessageBox("Please enter username and password.", false);
			return;
		} // if ...
		final SignInTask task = new SignInTask(this, username, password);
		task.execute();
	} // signin
		
	private class SignInTask extends AsyncTask<Object, Void, SigninResult>
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
		
		protected SigninResult doInBackground(Object... params)
		{
			try {
				return ApiClient.signin(username_, 
						                password_);
			} // try
			catch(final Exception e) {
				return new SigninResult("Error: " + e.getMessage());
			} // catch
		} // doInBackground
		
		@Override
	    protected void onPostExecute(final SigninResult result) 
		{
	       	progress_.dismiss();
	       	
			CycleStreetsPreferences.setUsernamePassword(username_, 
														password_,
														result.name(),
														result.email(),
														result.ok());
			setText(signinDetails_, R.id.signin_message, signinMessage());
			
			String msg = "You have successfully signed into CycleStreets.";
			if(!result.ok())
				msg = result.error().startsWith("Error:") ? result.error() : "Could not sign into CycleStreets.  Please check your username and password.";				
			MessageBox(msg, result.ok());
		} // onPostExecute
	} // class SignInTask
	
	////////////////////////////////////////////////////////
	private void register()
	{
		final String emailRegex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$";
		
		final String username = getText(registerDetails_, R.id.username);
		final String password = getText(registerDetails_, R.id.password);
		final String password2 = getText(registerDetails_, R.id.confirm_password);
		final String name = getText(registerDetails_, R.id.name);
		final String email = getText(registerDetails_, R.id.email);
			
		String oops = null;
		
		if(!email.toLowerCase().matches(emailRegex))
			oops = "The email address entered is not a properly formatted address.";
		if(!password.equals(password2))
			oops = "Password and confirmation password do not match.";
		if(username.length() < 5)
			oops = "Username must be at least five letters/numbers long.";
		
		if(oops != null)
		{
			MessageBox(oops, false);
			return;
		} // if(oops)
		
		final RegisterTask task = new RegisterTask(this,
												   username,
												   password,
												   name,
												   email);
		task.execute();
	} // register
	
	private class RegisterTask extends AsyncTask<Object, Void, RegistrationResult>
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
			progress_.setMessage(context.getString(R.string.registering));
			progress_.setIndeterminate(true);
			progress_.setCancelable(false);
	    } // RegisterTask
		
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			progress_.show();
		} // onPreExecute
		
		protected RegistrationResult doInBackground(Object... params)
		{
			try {
				return ApiClient.register(username_, 
						                  password_,
						                  name_,
						                  email_);
			} // try
			catch(final Exception e) {
				return new RegistrationResult(e.getMessage());
			} // catch
		} // doInBackground
		
		@Override
	    protected void onPostExecute(final RegistrationResult result) 
		{
	       	progress_.dismiss();
			CycleStreetsPreferences.setPendingUsernamePassword(username_, password_, name_, email_, result.ok());
			MessageBox(result.message(), result.ok());
		} // onPostExecute
	} // class RegisterTask	
} // class AccountDetailsActivity
