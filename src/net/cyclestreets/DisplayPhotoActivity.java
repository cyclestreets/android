package net.cyclestreets;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayPhotoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showphoto);
		
		Intent i = getIntent();
		Uri uri = i.getData();

		// TODO: load the image through a ContentProvider using AndroidHttpClient
    	try {
    		final URL url = new URL(uri.toString());
    		final Bitmap photo = BitmapFactory.decodeStream(url.openConnection().getInputStream());
    		final ImageView iv = (ImageView)findViewById(R.id.photo);
    		iv.setImageBitmap(photo);
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    	
    	final TextView text = (TextView)findViewById(R.id.photo_text);
    	text.setText(i.getStringExtra("caption"));
	}
}
