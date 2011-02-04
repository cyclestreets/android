package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HtmlActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		final WebView view = new WebView(this); 
		final String page = getIntent().getStringExtra("page-to-open");
		view.loadUrl("file:///android_asset/" + page);
		setContentView(view);
	} // onCreate
} // HtmlActivity
