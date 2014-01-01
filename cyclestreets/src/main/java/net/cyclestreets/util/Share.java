package net.cyclestreets.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class Share 
{
	static public void Url(final Activity activity,
						   final String url,
				           final String caption,
				           final String title)
	{
		do_url(activity, url, caption, title);
	} // Url
	
	static public void Url(final View view,
						   final String url,
						   final String caption,
						   final String title)
	{
		do_url(view.getContext(), url, caption, title);
	} // Url
	
	static private void do_url(final Context context,
							   final String url,
							   final String caption,
							   final String title)
	{
		final Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, url + " - " + caption);
		share.putExtra(Intent.EXTRA_SUBJECT, title);
		context.startActivity(Intent.createChooser(share, "Share"));
	} // Url
} // class Share
