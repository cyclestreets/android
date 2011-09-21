package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
    final WebView htmlView = (WebView)findViewById(R.id.html_view);
    htmlView.loadUrl("file:///android_asset/credits.html");
	} // onCreate
} // HtmlActivity
