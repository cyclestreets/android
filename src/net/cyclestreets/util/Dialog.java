package net.cyclestreets.util;

import android.app.ProgressDialog;
import android.content.Context;

public class Dialog 
{
	static public ProgressDialog createProgressDialog(final Context context,
											   		   final int messageId)
	{
		return createProgressDialog(context, context.getString(messageId));
	} // createProgressDialog
	
	static public ProgressDialog createProgressDialog(final Context context,
											   final String message)
	{
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setMessage(message);
		progress.setIndeterminate(true);
		progress.setCancelable(false);
		return progress;
	} // createProgressDialog
} // class Dialog
