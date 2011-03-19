package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.PhotomapCategories;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

public class AddPhotoActivity extends Activity 
							  implements View.OnClickListener
{
	protected static PhotomapCategories photomapCategories;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addphoto);

		// setup take photo button
		for(int id : new int[] { R.id.takephoto_button, R.id.chooseexisting_button })
		{
			final Button b = (Button)findViewById(id);
			b.setOnClickListener(this);
		} // for ...
		
		// start reading categories
		new GetPhotomapCategoriesTask().execute();
	} // class AddPhotoActivity
	
	@Override
	public void onClick(final View v) 
	{
		Intent i = null;
		
		switch(v.getId())
		{
			case R.id.takephoto_button:
				i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);				
				break;
			case R.id.chooseexisting_button:
				i = new Intent(Intent.ACTION_PICK,
							   android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				break;
		} // switch
		
		if(i == null)
			return;
		
		startActivityForResult(i, v.getId());
	} // onClick
	
	@Override
	protected void onActivityResult(final int requestCode, 
									final int resultCode, 
									final Intent data) 
	{
        if (resultCode != RESULT_OK)
        	return;
        
        // so let's bounce things on to our next activity
        data.setClass(this, AddPhotoLocateActivity.class);
        startActivity(data);
	} // onActivityResult

	private String getImageFilePath(final Intent data)
	{
        final Uri selectedImage = data.getData();
        final String[] filePathColumn = { MediaStore.Images.Media.DATA };

        final Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        try {
        	cursor.moveToFirst();
        	return cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
        } // try
        finally {
        	cursor.close();
        } // finally
	} // getImageFilePath

	private class GetPhotomapCategoriesTask extends AsyncTask<Object,Void,PhotomapCategories> 
	{
		protected PhotomapCategories doInBackground(Object... params) 
		{
			PhotomapCategories photomapCategories;
			try {
				photomapCategories = ApiClient.getPhotomapCategories();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return photomapCategories;
		} // PhotomapCategories
		
		@Override
		protected void onPostExecute(PhotomapCategories photomapCategories) 
		{
			AddPhotoActivity.photomapCategories = photomapCategories;
		} // onPostExecute
	} // class GetPhotomapCategoriesTask
} // class AddPhotoActivity
