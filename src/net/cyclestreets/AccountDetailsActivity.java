package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AccountDetailsActivity extends Activity 
{
    @Override
    public void onCreate(final Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.accountdetails);
        
        setText(R.id.username, CycleStreetsPreferences.username());
        setText(R.id.password, CycleStreetsPreferences.password());
    } // onCreate
    
	private void setText(final int id, final String value)
	{
		final TextView tv = (TextView)findViewById(id);
		tv.setText(value);
	} // setText


} // class AccountDetailsActivity
