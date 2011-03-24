package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.PhotomapCategories;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AddPhotoActivity extends Activity 
							  implements View.OnClickListener
{
	private static PhotomapCategories photomapCategories;
	
	private enum AddStep
	{
		PHOTO(null),
		CAPTION(PHOTO),
		LOCATION(CAPTION),
		DETAILS(LOCATION),
		SUBMIT(DETAILS);
		
		private AddStep(AddStep p)
		{
			prev_ = p;
			if(prev_ != null)
				prev_.next_ = this;
		} // AddStep

		public AddStep prev() { return prev_; }
		public AddStep next() { return next_; }
		
		private AddStep prev_;
		private AddStep next_;
	} // AddStep
	
	private AddStep step_;
	private Bitmap photo_ = null;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// start reading categories
		if(photomapCategories == null)
			new GetPhotomapCategoriesTask().execute();
		
		step_ = AddStep.PHOTO;
		setupView();
	} // class AddPhotoActivity

	@Override 
	public void onBackPressed()
	{
		if(step_== AddStep.PHOTO)
		{
			super.onBackPressed();
			return;
		} // if ...
		
		step_ = step_.prev();
		setupView();
	} // onBackPressed
	

	private void nextStep()
	{
		switch(step_)
		{
		} // nextStep
		
		step_ = step_.next();
		setupView();
	} // nextStep
	
	private void setupView()
	{
		switch(step_)
		{
		case PHOTO:
			setContentView(R.layout.addphoto);
			setupButtonListener(R.id.takephoto_button);
			setupButtonListener(R.id.chooseexisting_button);
			break;
		case CAPTION:
			{
				setContentView(R.layout.addphotocaption);
				setupButtonListener(R.id.next);
				final ImageView iv = (ImageView)findViewById(R.id.photo);
				iv.setImageBitmap(photo_);
				int newHeight = getWindowManager().getDefaultDisplay().getHeight() / 2;
				int newWidth = getWindowManager().getDefaultDisplay().getWidth();

				iv.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
				iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			}
			break;
		} // switch ...
	} // setupView
	
	private void setupButtonListener(int id)
	{
		final Button b = (Button)findViewById(id);
		if(b != null)
			b.setOnClickListener(this);		
	} // setupButtonListener
	
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
			case R.id.next:
				nextStep();
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

        try {
        	final String photoPath = getImageFilePath(data);
        	if(photo_ != null)
        		photo_.recycle();
        	photo_ = BitmapFactory.decodeFile(photoPath);
		
        	nextStep();
        }
        catch(Exception e)
        {
        	Toast.makeText(this, "There was a problem grabbing the photo : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
	} // onActivityResult
	
	

	/*
	// get photo data
	Intent i = getIntent();
	byte[] data = i.getByteArrayExtra(CycleStreetsConstants.EXTRA_PHOTO);
	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

	// show the photo
	ImageView iv = new ImageView(this);
	iv.setImageBitmap(bitmap);
	setContentView(iv);
	*/

	
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
