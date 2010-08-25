package net.cyclestreets;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayPhotoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		Uri uri = i.getData();

		// TODO: load the image through a ContentProvider using AndroidHttpClient
    	try {
    		URL url = new URL(uri.toString());
    		Bitmap mIcon_val = BitmapFactory.decodeStream(url.openConnection().getInputStream());
    		ImageView iv = new ImageView(this);
    		iv.setImageBitmap(mIcon_val);
    		setContentView(iv);
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
	}
}
