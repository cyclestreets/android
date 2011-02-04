package net.cyclestreets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class MoreActivity extends Activity implements View.OnClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more);
		
		setButtonListener(R.id.settings_button);
		setButtonListener(R.id.about_button);
		
		final WebView donate = (WebView)findViewById(R.id.donate_view);
		donate.loadUrl("file:///android_asset/donate.html");
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
				startActivity(htmlIntent("credits.html"));
				break;
		} // switch
	} // onClick
	
	private Intent htmlIntent(final String asset)
	{
		final Intent intent = new Intent(this, HtmlActivity.class);
		intent.putExtra("page-to-open", asset);
		return intent;
	} // htmlIntent
} // class MoreActivity
