package net.cyclestreets.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class MessageBox 
{
	static private final DialogInterface.OnClickListener NoAction = 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {  }
    		};
    		
	static public void YesNo(final View parent,
							 final String msg,
							 final DialogInterface.OnClickListener yesAction)
	{
		YesNo(parent, msg, yesAction, NoAction);
	} // YesNo
	
	static public void YesNo(final View parent, 
							 final String msg, 
							 final DialogInterface.OnClickListener yesAction,
							 final DialogInterface.OnClickListener noAction)
	{
        final AlertDialog.Builder alertbox = newBuilder(parent);
        alertbox.setMessage(msg);
        alertbox.setPositiveButton("Yes", yesAction);
        alertbox.setNegativeButton("No", noAction);
        
        show(alertbox);
	} // YesNo
	
	static public void OK(final View parent,
			              final String msg)
	{
		OK(parent, msg, NoAction);
	} // OK
	
	static public void OK(final View parent,
						  final String msg,
						  final DialogInterface.OnClickListener okAction)
	{
		final AlertDialog.Builder alertbox = newBuilder(parent);
		alertbox.setMessage(msg);
		alertbox.setPositiveButton("OK", okAction);
		show(alertbox);
	} // OK
	
	static public void OKAndFinish(final View view,
								   final String msg,
								   final Activity activity,
								   final boolean finishOnOK)
	{
		MessageBox.OK(view, 
					  msg, 
					  new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface arg0, int arg1) {
    					  if(finishOnOK)
    						activity.finish();
    					}
    			  	  });
	} // OKAndFinish
	
	static private AlertDialog.Builder newBuilder(final View parent)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
		builder.setTitle("CycleStreets");
		return builder;
	} // newBuilder
	
	static private void show(final AlertDialog.Builder builder)
	{
		final AlertDialog ad = builder.create();
		ad.show();
	} // show
	
} // class MessageBox
