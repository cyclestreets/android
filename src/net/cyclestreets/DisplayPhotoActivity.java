package net.cyclestreets;

import net.cyclestreets.util.ImageDownloader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayPhotoActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showphoto);
		
		final Intent i = getIntent();
		final Uri uri = i.getData();

		final ImageView iv = (ImageView)findViewById(R.id.photo);
		ImageDownloader.get(uri.toString(), iv, getWindowManager());
    	
    	final TextView text = (TextView)findViewById(R.id.photo_text);
    	text.setText(i.getStringExtra("caption"));
    	
	} // onCreate
	
	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_UP)
			finish();
		return false;
	} // onTouchEvent
} // DisplayPhotoActivity
