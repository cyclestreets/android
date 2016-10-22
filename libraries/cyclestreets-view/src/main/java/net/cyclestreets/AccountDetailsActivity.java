package net.cyclestreets;

import net.cyclestreets.api.Result;
import net.cyclestreets.view.R;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.api.Registration;
import net.cyclestreets.api.Signin;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.LoginFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AccountDetailsActivity extends Activity
                  implements View.OnClickListener, TextWatcher
{
  public enum RegisterStep
  {
    ACCOUNT(null),
    
    REGISTER_DETAILS(ACCOUNT),

    SIGNIN_DETAILS(ACCOUNT),
    
    EXISTING_SIGNIN_DETAILS(null);
    
    RegisterStep(final RegisterStep p)
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
  private Button signinButton_;
  
  @Override
  public void onCreate(final Bundle saved)
  {
    super.onCreate(saved);
    
    final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    final InputFilter[] usernameFilters = new InputFilter[]{ new WhitespaceInputFilter() };
    
    registerView_ = inflater.inflate(R.layout.accountdetails, null);
    registerDetails_ = inflater.inflate(R.layout.accountregister, null);
    textView(registerDetails_, R.id.username).setFilters(usernameFilters);
    signinDetails_ = inflater.inflate(R.layout.accountsignin, null);
    signinButton_ = (Button)signinDetails_.findViewById(R.id.signin_button);
    TextView usernameTV = textView(signinDetails_, R.id.username);
    usernameTV.addTextChangedListener(this);
    usernameTV.setFilters(usernameFilters); 
    textView(signinDetails_, R.id.password).addTextChangedListener(this);
    signinButton_.setEnabled(false);

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
  
  private String signinMessage() {
    if (CycleStreetsPreferences.accountOK())
      return getString(R.string.account_already_signed_in_format,
                       CycleStreetsPreferences.name(),
                       CycleStreetsPreferences.email());
    return getString(R.string.account_signin_message);
  }
  
  private String registrationMessage() {
    if (CycleStreetsPreferences.accountPending())
      return getString(R.string.account_pending);
    return getString(R.string.account_registration_is_free_long);
  }
  
  private void hookUpButton(final View v, final int id)
  {
    final Button b = (Button)v.findViewById(id);
    if(b == null)
      return;
    b.setOnClickListener(this);    
  } // hookUpNext
  
  private TextView textView(final View v, final int id)
  {
    return (TextView)v.findViewById(id);
  } // textView
  
  private void setText(final View v, final int id, final String value)
  {
    final TextView tv = textView(v, id);
    if(tv == null)
      return;
    tv.setText(value);
  } // setText
  
  private String getText(final View v, final int id)
  {
    final TextView tv = textView(v, id);
    return tv.getText().toString();
  } // getText
  
  @Override
  public void onClick(final View v) 
  {
    final int clicked  = v.getId();

    if(R.id.newaccount_button == clicked)
        step_ = RegisterStep.REGISTER_DETAILS;
    if(R.id.existingaccount_button == clicked)
        step_ = RegisterStep.SIGNIN_DETAILS;
    if(R.id.cleardetails_button == clicked)
        confirmClear();
    if(R.id.signin_button == clicked) {
        signin();
        return;
    }
    if(R.id.register_button == clicked) {
        register();
        return;
    }
    
    setupView();
  } // onClick
  ///////////////////////////////////////////////////////

  @Override
  public void afterTextChanged(Editable arg0) { }
  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) 
  {
    final String username = getText(signinDetails_, R.id.username);
    final String password = getText(signinDetails_, R.id.password);
    signinButton_.setEnabled((username.length() != 0) && (password.length() != 0));
  } // onTextChanged

  ///////////////////////////////////////////////////////
  private void confirmClear()
  {
    MessageBox.YesNo(signinDetails_, 
             getString(R.string.account_clear_details_confirm),
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
    MessageBox.OKAndFinish(signinDetails_, message, this, finishOnOK);
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
    
  private class SignInTask extends AsyncTask<Object, Void, Signin.Result>
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
      
      progress_ = Dialog.createProgressDialog(context, R.string.account_signing_in);
    } // SigninTask
    
    @Override
    protected void onPreExecute() 
    {
      super.onPreExecute();
      progress_.show();
    } // onPreExecute
    
    protected Signin.Result doInBackground(Object... params)
    {
      return Signin.signin(username_, password_);
    } // doInBackground
    
    @Override
    protected void onPostExecute(final Signin.Result result) 
    {
      progress_.dismiss();
           
      CycleStreetsPreferences.setUsernamePassword(username_, 
                            password_,
                            result.name(),
                            result.email(),
                            result.ok());
      setText(signinDetails_, R.id.signin_message, signinMessage());
      
      MessageBox(result.message(), result.ok());
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
    
    if (!email.toLowerCase().matches(emailRegex))
      oops = getString(R.string.account_email_format);
    if (!password.equals(password2))
      oops = getString(R.string.account_password_mismatch);
    if (username.length() < 5)
      oops = getString(R.string.account_username_too_short);
    
    if (oops != null) {
      MessageBox(oops, false);
      return;
    }
    
    final RegisterTask task = new RegisterTask(this,
                                               username,
                                               password,
                                               name,
                                               email);
    task.execute();
  } // register
  
  private class RegisterTask extends AsyncTask<Object, Void, Result>
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
                 final String email) {
      username_ = username;
      password_ = password;
      name_ = name;
      email_ = email;
      
      progress_ = Dialog.createProgressDialog(context, R.string.account_registering);
    } // RegisterTask
    
    @Override
    protected void onPreExecute() 
    {
      super.onPreExecute();
      progress_.show();
    } // onPreExecute
    
    protected Result doInBackground(Object... params)
    {
      return Registration.register(username_, 
                                   password_,
                                   name_,
                                   email_);
    } // doInBackground
    
    @Override
    protected void onPostExecute(final Result result)
    {
      progress_.dismiss();
      CycleStreetsPreferences.setPendingUsernamePassword(username_, password_, name_, email_, result.ok());
      MessageBox(result.message(), result.ok());
    } // onPostExecute
  } // class RegisterTask  
  
  private class WhitespaceInputFilter extends LoginFilter.UsernameFilterGeneric {
    public WhitespaceInputFilter() {
      super(false);
    }

    @Override
    public boolean isAllowed(char c) {
      return !Character.isWhitespace(c);
    }
  } // class WhitespaceInputFilter
} // class AccountDetailsActivity
