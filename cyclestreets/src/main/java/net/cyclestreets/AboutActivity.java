package net.cyclestreets;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutActivity extends Activity 
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);
		
    final WebView htmlView = (WebView)findViewById(R.id.html_view);
    htmlView.loadUrl("file:///android_asset/credits.html");
    
    final TextView versionView = (TextView)findViewById(R.id.version_view);
    versionView.setText(versionName());
  } // onCreate
	
  private String versionName()
  {
    return ((CycleStreetsApp)getApplication()).version();
  } // versionName
} // HtmlActivity
