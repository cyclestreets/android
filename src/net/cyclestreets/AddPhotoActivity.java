package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.ICategory;
import net.cyclestreets.api.UploadResult;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.ThereOverlay;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class AddPhotoActivity extends Activity 
							  implements View.OnClickListener
{
	private enum AddStep
	{
		PHOTO(null),
		CAPTION(PHOTO),
		CATEGORY(CAPTION),
		LOCATION(CATEGORY),
		SUBMIT(LOCATION),
		VIEW(SUBMIT),
		DONE(VIEW);
		
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
	
	private String photoFile_ = null;
	private Bitmap photo_ = null;
	private ExifInterface photoExif_ = null;
	
	private View photoView_;
	private View photoCaption_;
	private View photoCategory_;
	private View photoLocation_;
	private View photoWebView_;
	
	private CycleMapView map_;
	private ThereOverlay there_;
	private static PhotomapCategories photomapCategories;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		step_ = AddStep.PHOTO;

		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		photoView_ = inflater.inflate(R.layout.addphoto, null);
		photoCaption_ = inflater.inflate(R.layout.addphotocaption, null);
		photoCategory_ = inflater.inflate(R.layout.addphotocategory, null);
		photoLocation_ = inflater.inflate(R.layout.addphotolocation, null);
		photoWebView_ = inflater.inflate(R.layout.addphotoview, null);
		final Button b = (Button)photoWebView_.findViewById(R.id.next);
		b.setText("Upload another");

		// start reading categories
		if(photomapCategories == null)
			new GetPhotomapCategoriesTask().execute();
		else
			setupSpinners();
		
		map_ = new CycleMapView(this, this.getClass().getName());
		map_.enableAndFollowLocation();
		map_.getController().setZoom(map_.getMaxZoomLevel());
		there_ = new ThereOverlay(this, map_);
		map_.overlayPushTop(there_);
	
		final LinearLayout v = (LinearLayout)(photoLocation_.findViewById(R.id.mapholder));
		v.addView(map_, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		setupView();
	} // class AddPhotoActivity

	@Override 
	public void onBackPressed()
	{
		if(step_ == AddStep.SUBMIT)
			return;
		
		if(step_ == AddStep.PHOTO || step_ == AddStep.VIEW)
		{
			super.onBackPressed();
			return;
		} // if ...
		
		step_ = step_.prev();
		setupView();
	} // onBackPressed
	

	private void nextStep()
	{
		if((step_ == AddStep.LOCATION) && (there_.there() == null))
		{
			Toast.makeText(this, "Please set photo location", Toast.LENGTH_LONG).show();
			return;
		} // if ...
			
		step_ = step_.next();
		setupView();
	} // nextStep
	
	private void setupView()
	{
		switch(step_)
		{
		case PHOTO:
			setContentView(photoView_);
			setupButtonListener(R.id.takephoto_button);
			setupButtonListener(R.id.chooseexisting_button);
			break;
		case CAPTION:
			setContentView(photoCaption_);
			break;
		case CATEGORY:
			setContentView(photoCategory_);
			break;
		case LOCATION:
			setContentView(photoLocation_);
			break;
		case SUBMIT:
			upload();
			break;
		case VIEW:
			setContentView(photoWebView_);
			break;
		case DONE:
			captionEditText().setText("");
			metaCategorySpinner().setSelection(0);
			categorySpinner().setSelection(0);
			step_ = AddStep.PHOTO;
			setupView();
			break;
		} // switch ...
		
		previewPhoto();
		hookUpNext();
	} // setupView
	
	private void previewPhoto()
	{
		final ImageView iv = (ImageView)findViewById(R.id.photo);
		if(iv == null)
			return;
		iv.setImageBitmap(photo_);
		int newHeight = getWindowManager().getDefaultDisplay().getHeight() / 10 * 4;
		int newWidth = getWindowManager().getDefaultDisplay().getWidth();

		iv.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
		iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	} // previewPhoto
	
	private void hookUpNext()
	{
		setupButtonListener(R.id.next);
	} // hookUpNext
	
	private EditText captionEditText() { return (EditText)photoCaption_.findViewById(R.id.caption); }
	private Spinner metaCategorySpinner() { return (Spinner)photoCategory_.findViewById(R.id.metacat); }
	private Spinner categorySpinner() { return (Spinner)photoCategory_.findViewById(R.id.category); }

	private void setupSpinners()
	{
		metaCategorySpinner().setAdapter(new CategoryAdapter(this, photomapCategories.metacategories));
		categorySpinner().setAdapter(new CategoryAdapter(this, photomapCategories.categories));
	} // setupSpinners
	
	private void setupButtonListener(int id)
	{
		final Button b = (Button)findViewById(id);
		if(b == null)
			return;
		
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
        	photoFile_ = getImageFilePath(data);
        	if(photo_ != null)
        		photo_.recycle();
        	final BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        	decodeOptions.inPurgeable = true;
        	decodeOptions.inSampleSize = 4;
        	photo_ = BitmapFactory.decodeFile(photoFile_, decodeOptions);
        	
        	photoExif_ = new ExifInterface(photoFile_);

        	there_.noOverThere(photoLocation());
        	
        	nextStep();
        }
        catch(Exception e)
        {
        	Toast.makeText(this, "There was a problem grabbing the photo : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
	
	private void upload()
	{
		final String filename = photoFile_;
		final String username = CycleStreetsPreferences.username();
		final String password = CycleStreetsPreferences.password();
		final GeoPoint location = there_.there();
		final String metaCat = photomapCategories.metacategories.get((int)metaCategorySpinner().getSelectedItemId()).getTag();
		final String category = photomapCategories.categories.get((int)categorySpinner().getSelectedItemId()).getTag();
		final String dateTime = photoTimestamp();
		final String caption = captionEditText().getText().toString();

		final UploadPhotoTask uploader = new UploadPhotoTask(this,
															 filename,
															 username,
															 password,
															 location,
															 metaCat,
															 category,
															 dateTime,
															 caption);
		uploader.execute();
	} // upload
	
	private void uploadComplete(final String url)
	{
		final WebView wv = (WebView)photoWebView_.findViewById(R.id.webview);
		wv.loadUrl(url);

       	nextStep();
	} // uploadComplete
	
	private void uploadFailed(final String msg)
	{
		Toast.makeText(this, "Upload failed: " + msg, Toast.LENGTH_LONG).show();
		
		step_ = AddStep.LOCATION;
		setupView();
	} // uploadFailed
	
	private GeoPoint photoLocation()
	{
		final float[] coords = new float[2];
		if(!photoExif_.getLatLong(coords))
			return null;
		int lat = (int)(((double)coords[0]) * 1E6);
		int lon = (int)(((double)coords[1]) * 1E6);
		return new GeoPoint(lat, lon);
	} // photoLocation
	
	private String photoTimestamp()
	{
		Date date = new Date();
		
		try {
			final DateFormat df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			final String dateString = photoExif_.getAttribute(ExifInterface.TAG_DATETIME);
			if(dateString != null && dateString.length() > 0)
				date = df.parse(dateString);
		} // try
		catch(Exception e) {
			// ah well
		} // catch

		return Long.toString(date.getTime() / 1000);
	} // photoTimestamp

	///////////////////////////////////////////////////////////////////////////
	private class GetPhotomapCategoriesTask extends AsyncTask<Object, Void, PhotomapCategories> 
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
			setupSpinners();
		} // onPostExecute
	} // class GetPhotomapCategoriesTask
	
	//////////////////////////////////////////////////////////////////////////
	private class UploadPhotoTask extends AsyncTask<Object, Void, UploadResult>
	{
		private final String filename_;
		private final String username_;
		private final String password_;
		private final GeoPoint location_;
		private final String metaCat_;
		private final String category_;
		private final String dateTime_;
		private final String caption_;
		private final ProgressDialog progress_;
		
		UploadPhotoTask(final Context context,
						final String filename,
	 				   	final String username,
	 				   	final String password,
	 				   	final GeoPoint location,
	 				   	final String metaCat,
	 				   	final String category,
	 				   	final String dateTime,
	 				   	final String caption) 
	    {
			filename_ = filename;
			username_ = username;
			password_ = password;
			location_ = location;
			metaCat_ = metaCat;
			category_ = category;
			dateTime_ = dateTime;
			caption_ = caption;
			
			progress_ = new ProgressDialog(context);
			progress_.setMessage(context.getString(R.string.uploading_photo));
			progress_.setIndeterminate(true);
			progress_.setCancelable(false);
	    } // UploadPhotoTask
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress_.show();
		} // onPreExecute
		
		protected UploadResult doInBackground(Object... params)
		{
			return ApiClient.uploadPhoto(filename_, 
										 username_, 
										 password_, 
										 location_, 
										 metaCat_, 
										 category_, 
										 dateTime_, 
										 caption_);
		} // doInBackground
		
		@Override
	    protected void onPostExecute(final UploadResult result) 
		{
	       	progress_.dismiss();
	       	if(result.errorMessage() == null)
	       		uploadComplete(result.url());
	       	else
	       		uploadFailed(result.errorMessage());
		} // onPostExecute
	} // class UploadPhotoTask
	
	//////////////////////////////////////////////////////////
	static private class CategoryAdapter extends BaseAdapter
	{
		private final LayoutInflater inflater_;
		private final List<?> list_;
		
		public CategoryAdapter(final Context context,
							   final List<?> list)
		{
			inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			list_ = list;
		} // CategoryAdapter
		
		@Override
		public int getCount()
		{
			return list_.size();
		} // getCount
		
		@Override
		public String getItem(final int position)
		{
			final ICategory c = (ICategory)list_.get(position);
			return c.getName();
		} // getItem
		
		@Override
		public long getItemId(final int position)
		{
			return position;
		} // getItemId
		
		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			final int id = (parent instanceof Spinner) ? android.R.layout.simple_spinner_item : android.R.layout.simple_spinner_dropdown_item;
			final TextView tv = (TextView)inflater_.inflate(id, parent, false);
			tv.setText(getItem(position));
			return tv;
		} // getView
	} // CategoryAdapter
	
} // class AddPhotoActivity
