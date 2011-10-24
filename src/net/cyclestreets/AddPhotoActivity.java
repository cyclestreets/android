package net.cyclestreets;

import net.cyclestreets.api.ApiClient;
import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.ICategory;
import net.cyclestreets.api.UploadResult;
import net.cyclestreets.util.Bitmaps;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Share;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.ThereOverlay;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

public class AddPhotoActivity extends Activity 
                implements View.OnClickListener
{
  public enum AddStep
  {
    PHOTO(null),
    CAPTION(PHOTO),
    CATEGORY(CAPTION),
    LOCATION(CATEGORY),
    ACCOUNT(LOCATION),
    SUBMIT(LOCATION),
    VIEW(SUBMIT),
    DONE(VIEW);
    
    private AddStep(AddStep p)
    {
      prev_ = p;
      if(prev_ != null)
        prev_.next_ = this;
      save(this);
    } // AddStep
    
    public AddStep prev() { return prev_; }
    public AddStep next() { return next_; }
    
    public int value() { return Value_.get(this); }
    
    private AddStep prev_;
    private AddStep next_;
    
    public static AddStep fromInt(int a) 
    {
      for(AddStep s : Value_.keySet())
        if(s.value() == a)
          return s;
      return null;
    } // AddStep
    
    private static void save(AddStep a)
    {
      if(Value_ == null)
        Value_ = new HashMap<AddStep, Integer>();
      Value_.put(a, Value_.size());
    } // save

    private static Map<AddStep, Integer> Value_;
  } // AddStep

  private View photoView_;
  private View photoCaption_;
  private View photoCategory_;
  private View photoLocation_;
  private View photoWebView_;
  
  private CycleMapView map_;
  private ThereOverlay there_;
  private static PhotomapCategories photomapCategories;
  
  private AddStep step_;
  
  private String photoFile_ = null;
  private Bitmap photo_ = null;
  private String caption_;
  private String dateTime_;
  private int metaCatId_;
  private int catId_;
  private String uploadedUrl_;
  
  private LayoutInflater inflater_;
  private InputMethodManager imm_;
  
  @Override
  protected void onCreate(final Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
  
    inflater_ = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    imm_ = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    
    step_ = AddStep.PHOTO;
    caption_ = "";
    dateTime_ = "";
    metaCatId_ = -1;
    catId_ = -1;

    photoView_ = inflater_.inflate(R.layout.addphoto, null);
    {
      final Button takePhoto = (Button)photoView_.findViewById(R.id.takephoto_button);
      if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        takePhoto.setOnClickListener(this);
      else
        takePhoto.setEnabled(false);
    }
    ((Button)photoView_.findViewById(R.id.chooseexisting_button)).setOnClickListener(this);    
    
    photoCategory_ = inflater_.inflate(R.layout.addphotocategory, null);
    photoLocation_ = inflater_.inflate(R.layout.addphotolocation, null);
    final Button u = (Button)photoLocation_.findViewById(R.id.next);
    u.setText("Upload!");
    photoWebView_ = inflater_.inflate(R.layout.addphotoview, null);
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

  private void store()
  {
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putInt("STEP", step_.value());
    edit.putString("PHOTOFILE", photoFile_);
    edit.putString("DATETIME", dateTime_);
    edit.putString("CAPTION", caption_);
    edit.commit();
  } // store
  
  @Override
  protected void onPause()
  {
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString("CAPTION", captionText());
    edit.putInt("METACAT", metaCategoryId());
    edit.putInt("CATEGORY", categoryId());        
    final GeoPoint p = there_.there();
    if(p != null)
    {
      edit.putInt("THERE-LAT", p.getLatitudeE6());
      edit.putInt("THERE-LON", p.getLongitudeE6());
    } 
    else
      edit.putInt("THERE-LAT", -1);
    edit.commit();

    map_.onPause();        
    super.onPause();
  } // onPause
  
  @Override
  protected void onResume()
  {
    final SharedPreferences prefs = prefs();
    step_ = AddStep.fromInt(prefs.getInt("STEP", 0));
    photoFile_ = prefs.getString("PHOTOFILE", photoFile_);
    if(photo_ == null && photoFile_ != null)
      photo_ = Bitmaps.loadFile(photoFile_);
    dateTime_ = prefs.getString("DATETIME", "");
    caption_ = prefs.getString("CAPTION", "");
    
    metaCatId_ = prefs.getInt("METACAT", -1);
    catId_ = prefs.getInt("CATEGORY", -1);
    setSpinnerSelections();
      
    final int tlat = prefs.getInt("THERE-LAT", -1);
    final int tlon = prefs.getInt("THERE-LON", -1);
    if((tlat != -1) && (tlon != -1))
      there_.noOverThere(new GeoPoint(tlat, tlon));

    map_.onResume();
    super.onResume();

    setupView();
  } // onResume
  
  private SharedPreferences prefs()
  {
    return getSharedPreferences("net.cyclestreets.AddPhotoActivity", Context.MODE_PRIVATE);
  } // prefs()
    
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
    store();
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

    store();
    setupView();
  } // nextStep
  
  private void setupView()
  {
    switch(step_)
    {
    case PHOTO:
      metaCategorySpinner().setSelection(0);
      categorySpinner().setSelection(0);
      caption_ = "";
      there_.noOverThere(null);
      setContentView(photoView_);      
      break;
    case CAPTION:
      // why recreate this view each time - well *sigh* because we have to force the 
      // keyboard to hide, if we don't recreate the view afresh, Android won't redisplay 
      // the keyboard if we come back to this view
      photoCaption_ = inflater_.inflate(R.layout.addphotocaption, null);
      setContentView(photoCaption_);
      captionEditor().setText(caption_);
      break;
    case CATEGORY:
      caption_ = captionText();
      store();
      setContentView(photoCategory_);
      break;
    case LOCATION:
      metaCatId_ = metaCategoryId();
      catId_ = categoryId();
      setContentView(photoLocation_);
      break;
    case SUBMIT:
      upload();
      break;
    case VIEW:
      setContentView(photoWebView_);
      {
        final TextView text = (TextView)photoWebView_.findViewById(R.id.photo_text);
        text.setText(caption_);
        final TextView url = (TextView)photoWebView_.findViewById(R.id.photo_url);
        url.setText(uploadedUrl_);
        final Button share = (Button)findViewById(R.id.photo_share);
        share.setOnClickListener(this);
      }
      break;
    case DONE:
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
    final Button b = (Button)findViewById(R.id.next);
    if(b == null)
      return;
    b.setOnClickListener(this);    
  } // hookUpNext
  
  private EditText captionEditor() { return (EditText)photoCaption_.findViewById(R.id.caption); }
  private String captionText() 
  { 
    if(photoCaption_ == null)
      return caption_; 
    imm_.hideSoftInputFromWindow(captionEditor().getWindowToken(), 0);
    return captionEditor().getText().toString(); 
  } // captionText
  private int metaCategoryId() { return (int)metaCategorySpinner().getSelectedItemId(); }
  private int categoryId() { return (int)categorySpinner().getSelectedItemId(); }
  private Spinner metaCategorySpinner() { return (Spinner)photoCategory_.findViewById(R.id.metacat); }
  private Spinner categorySpinner() { return (Spinner)photoCategory_.findViewById(R.id.category); }

  private void setupSpinners()
  {
    metaCategorySpinner().setAdapter(new CategoryAdapter(this, photomapCategories.metacategories));
    categorySpinner().setAdapter(new CategoryAdapter(this, photomapCategories.categories));
    
    setSpinnerSelections();
  } // setupSpinners
  
  private void setSpinnerSelections()
  {
    // ids == position
    if(metaCatId_ != -1)
      metaCategorySpinner().setSelection(metaCatId_);
    if(catId_ != -1)
      categorySpinner().setSelection(catId_);
  } // setSpinnerSelections
  
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
    case R.id.photo_share:
      Share.Url(this, uploadedUrl_, caption_, "Photo on CycleStreets.net");
      break;
    case R.id.next:
      if(step_ == AddStep.LOCATION && !CycleStreetsPreferences.accountOK())
        i = new Intent(this, AccountDetailsActivity.class);
      else
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

    try
    {
      photoFile_ = getImageFilePath(data);
      if(photo_ != null)
        photo_.recycle();
      photo_ = Bitmaps.loadFile(photoFile_);
          
      final ExifInterface exif = new ExifInterface(photoFile_);

      dateTime_ = photoTimestamp(exif);
      there_.noOverThere(photoLocation(exif));
          
      nextStep();
    }
    catch(Exception e)
    {
      Toast.makeText(this, "There was a problem grabbing the photo : " + e.getMessage(), Toast.LENGTH_LONG).show();
      if(requestCode == R.id.takephoto_button)
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                                          android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                               R.id.chooseexisting_button);        
    }
  } // onActivityResult
  
  private String getImageFilePath(final Intent data)
  {
    final Uri selectedImage = data.getData();
    final String[] filePathColumn = { MediaStore.Images.Media.DATA };

    final Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
    try
    {
      cursor.moveToFirst();
      return cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
    } // try
    finally 
    {
      cursor.close();
    } // finally
  } // getImageFilePath
  
  private void upload() 
  {
    try 
    {
      doUpload();
    }
    catch(Exception e)  
    { 
      Toast.makeText(this, "Could not upload photo.  Please check your network connection.", Toast.LENGTH_LONG).show();
      step_ = AddStep.LOCATION;
    }
  } // upload
  
  private void doUpload() throws Exception
  {
    final String filename = photoFile_;
    final String username = CycleStreetsPreferences.username();
    final String password = CycleStreetsPreferences.password();
    final GeoPoint location = there_.there();
    final String metaCat = photomapCategories.metacategories.get(metaCatId_).getTag();
    final String category = photomapCategories.categories.get(catId_).getTag();
    final String dateTime = dateTime_;
    final String caption = caption_;

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
  
  private void uploadComplete(final String photo_url)
  {
    uploadedUrl_ = photo_url;
    nextStep();
  } // uploadComplete
  
  private void uploadFailed(final String msg)
  {
    MessageBox.OK(photoLocation_, 
                  msg,
                  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      step_ = AddStep.LOCATION;
                      setupView();
                    }
                  });
  } // uploadFailed
  
  private GeoPoint photoLocation(final ExifInterface photoExif)
  {
    final float[] coords = new float[2];
    if(!photoExif.getLatLong(coords))
      return null;
    int lat = (int)(((double)coords[0]) * 1E6);
    int lon = (int)(((double)coords[1]) * 1E6);
    return new GeoPoint(lat, lon);
  } // photoLocation
  
  private String photoTimestamp(final ExifInterface photoExif)
  {
    Date date = new Date();
    
    try 
    {
      final DateFormat df = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
      final String dateString = photoExif.getAttribute(ExifInterface.TAG_DATETIME);
      if(dateString != null && dateString.length() > 0)
        date = df.parse(dateString);
    } // try
    catch(Exception e) 
    {
      // ah well
    } // catch

    return Long.toString(date.getTime() / 1000);
  } // photoTimestamp

  ///////////////////////////////////////////////////////////////////////////
  private class GetPhotomapCategoriesTask extends AsyncTask<Object, Void, PhotomapCategories> 
  {
    protected PhotomapCategories doInBackground(Object... params) 
    {
      PhotomapCategories photomapCategories = null;
      try {
        photomapCategories = ApiClient.getPhotomapCategories();
      }
      catch (Exception ex) {
      }
      return photomapCategories;
    } // PhotomapCategories
    
    @Override
    protected void onPostExecute(PhotomapCategories photomapCategories) 
    {
      if(photomapCategories == null) 
      {
        Toast.makeText(AddPhotoActivity.this, "Could not load photomap categories.  Please check network connection.", Toast.LENGTH_LONG).show();
        return;
      } // if ...
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
    private final boolean smallImage_;
    
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
      smallImage_ = CycleStreetsPreferences.uploadSmallImages();
      filename_ = smallImage_ ? Bitmaps.resizePhoto(filename) : filename;
      username_ = username;
      password_ = password;
      location_ = location;
      metaCat_ = metaCat;
      category_ = category;
      dateTime_ = dateTime;
      caption_ = caption;
      
      progress_ = Dialog.createProgressDialog(context, R.string.uploading_photo);
    } // UploadPhotoTask
    
    @Override
    protected void onPreExecute() 
    {
      super.onPreExecute();
      progress_.show();
    } // onPreExecute
    
    protected UploadResult doInBackground(Object... params)
    {
      try {
        return ApiClient.uploadPhoto(filename_, 
                       username_, 
                       password_, 
                       location_, 
                       metaCat_, 
                       category_, 
                       dateTime_, 
                       caption_);
      } // try
      catch(Exception e) {
        return new UploadResult("There was a problem uploading your photo: \n" + e.getMessage());
      }
    } // doInBackground
    
    @Override
    protected void onPostExecute(final UploadResult result) 
    {
      if(smallImage_)
        new File(filename_).delete();
      progress_.dismiss();
      if(result.ok())
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
