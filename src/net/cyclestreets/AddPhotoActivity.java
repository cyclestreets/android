package net.cyclestreets;

import net.cyclestreets.api.PhotomapCategories;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AddPhotoActivity extends Activity {
	public static final int CHOOSE_IMAGE = 1;
	protected static PhotomapCategories photomapCategories;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addphoto);

		// setup take photo button
		Button takephoto = (Button) findViewById(R.id.takephoto_button);
		takephoto.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AddPhotoActivity.this, TakePhotoActivity.class));
			}
		});

		// setup choose existing button
		Button chooseexisting = (Button) findViewById(R.id.chooseexisting_button);
		chooseexisting.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
			               android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				startActivityForResult(i, CHOOSE_IMAGE);
			}
		});
		
		// start reading categories
		new GetPhotomapCategoriesTask().execute();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // See which child activity is calling us back.
	    switch (resultCode) {
	        case CHOOSE_IMAGE:
	            if (resultCode == RESULT_OK) {
	                Uri selectedImage = data.getData();
	                String[] filePathColumn = {MediaStore.Images.Media.DATA};

	                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	                cursor.moveToFirst();

	                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	                String filePath = cursor.getString(columnIndex);
	                cursor.close();

	        		Log.d(getClass().getSimpleName(), "chosen file: " + filePath);
	            }
	        default:
	            break;
	    }
	}

	// TODO: use content provider
	private class GetPhotomapCategoriesTask extends AsyncTask<Object,Void,PhotomapCategories> {
		protected PhotomapCategories doInBackground(Object... params) {
			PhotomapCategories photomapCategories;
			try {
				photomapCategories = CycleStreets.apiClient.getPhotomapCategories();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return photomapCategories;
		}
		
		@Override
		protected void onPostExecute(PhotomapCategories photomapCategories) {
			Log.d(getClass().getSimpleName(), "photomapcategories: " + photomapCategories);
			AddPhotoActivity.photomapCategories = photomapCategories;
		}
	}
}
