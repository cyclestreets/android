package net.cyclestreets;

import net.cyclestreets.util.ImageDownloader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayPhotoActivity extends Activity 
{
	private ImageDownloader loader_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showphoto);
		
		final Intent i = getIntent();
		final Uri uri = i.getData();

		final ImageView iv = (ImageView)findViewById(R.id.photo);
		loader_ = new ImageDownloader();		
		loader_.get(uri.toString(), iv);
    	
    	final TextView text = (TextView)findViewById(R.id.photo_text);
    	text.setText(i.getStringExtra("caption"));
	} // onCreate
} // DiasplayPhotoActivity
