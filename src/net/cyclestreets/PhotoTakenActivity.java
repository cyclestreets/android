package net.cyclestreets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class PhotoTakenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get photo data
		Intent i = getIntent();
		byte[] data = i.getByteArrayExtra(CycleStreetsConstants.EXTRA_PHOTO);
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

		// show the photo
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(bitmap);
		setContentView(iv);
	}
}
