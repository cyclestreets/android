package net.cyclestreets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MoreActivity extends Activity implements View.OnClickListener
{
	private static final int DIALOG_ABOUT_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more);
		
		setButtonListener(R.id.settings_button);
		setButtonListener(R.id.about_button);
	} // onCreate
			
	private void setButtonListener(final int id)
	{
		final Button b = (Button)findViewById(id);
		b.setOnClickListener(this);
	} // setButtonListener

	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
			case R.id.settings_button:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.about_button:
				showDialog(DIALOG_ABOUT_ID);
				break;
		} // switch
	} // onClick
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog;

		switch (id)
		{
			case DIALOG_ABOUT_ID:
	        	dialog = new AlertDialog.Builder(MoreActivity.this)
	            .setIcon(R.drawable.icon)
	            .setTitle(R.string.app_name)
	            .setMessage(R.string.about_message)
	            .setPositiveButton
	            (
	        		"OK",
	        		new DialogInterface.OnClickListener()
		            {
		                @Override
		                public void onClick(DialogInterface dialog, int whichButton) {}
		            }
	        	).create();
	        	break;

	        default:
	            dialog = null;
	            break;
		}
	    
		return dialog;
	} // onCreateDialog
} // class MoreActivity
