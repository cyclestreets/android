package net.cyclestreets;

import net.cyclestreets.fragments.R;
import net.cyclestreets.api.PhotomapCategory;
import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.Upload;
import net.cyclestreets.util.Bitmaps;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.util.Share;
import net.cyclestreets.views.CycleMapView;
import net.cyclestreets.views.overlay.ThereOverlay;
import net.cyclestreets.views.overlay.ThereOverlay.LocationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import static net.cyclestreets.util.MenuHelper.createMenuItem;
import static net.cyclestreets.util.MenuHelper.enableMenuItem;

public class PhotoUploadFragment extends Fragment 
                implements View.OnClickListener, LocationListener, Undoable
{
  public enum AddStep
  {
    PHOTO(null),
    CAPTION(PHOTO),
    CATEGORY(CAPTION),
    LOCATION(CATEGORY),
    VIEW(LOCATION),
    DONE(VIEW);
    
    AddStep(AddStep p)
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
        Value_ = new HashMap<>();
      Value_.put(a, Value_.size());
    } // save

    private static Map<AddStep, Integer> Value_;
  } // AddStep

  private static final int TakePhoto = 2;
  private static final int ChoosePhoto = 3;
  private static final int AccountDetails = 4;

  private LinearLayout photoRoot_;
  private View photoView_;
  private View photoCaption_;
  private View photoCategory_;
  private View photoLocation_;
  private View photoWebView_;
  
  private CycleMapView map_;
  private ThereOverlay there_;
  private boolean geolocated_;
  private static PhotomapCategories photomapCategories;
  
  private AddStep step_;
  
  private String photoFile_ = null;
  private Bitmap photo_ = null;
  private String caption_;
  private String dateTime_;
  private int metaCatId_;
  private int catId_;
  private String uploadedUrl_;

  private boolean allowUploadByKey_;
  private boolean allowTextOnly_;
  private boolean noShare_;
  
  private LayoutInflater inflater_;
  private InputMethodManager imm_;
  
  @Override
  public View onCreateView(final LayoutInflater inflater, 
                           final ViewGroup container,
                           final Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    final String metaData = photoUploadMetaData();
    allowUploadByKey_ = metaData.contains("ByKey");
    allowTextOnly_ = metaData.contains("AllowTextOnly");
    noShare_ = metaData.contains("NoShare");

    inflater_ = LayoutInflater.from(getActivity());
    imm_ = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    
    photoRoot_ = (LinearLayout)inflater_.inflate(R.layout.addphoto, null);

    step_ = AddStep.PHOTO;
    caption_ = "";
    dateTime_ = "";
    metaCatId_ = -1;
    catId_ = -1;

    photoView_ = inflater_.inflate(R.layout.addphotostart, null);
    {
      final Button takePhoto = (Button)photoView_.findViewById(R.id.takephoto_button);
      if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        takePhoto.setOnClickListener(this);
      else
        takePhoto.setEnabled(false);
    }
    photoView_.findViewById(R.id.chooseexisting_button).setOnClickListener(this);
    {
      final Button textOnly = (Button)photoView_.findViewById(R.id.textonly_button);
      if (allowTextOnly_)
        textOnly.setOnClickListener(this);
      else
        textOnly.setVisibility(View.GONE);
    }
    
    photoCategory_ = inflater_.inflate(R.layout.addphotocategory, null);
    backNextButtons(photoCategory_, "Back", android.R.drawable.ic_media_rew, "Next", android.R.drawable.ic_media_ff);

    photoLocation_ = inflater_.inflate(R.layout.addphotolocation, null);
    backNextButtons(photoLocation_, "Back", android.R.drawable.ic_media_rew, "Upload!", android.R.drawable.ic_menu_upload);

    photoWebView_ = inflater_.inflate(R.layout.addphotoview, null);
    backNextButtons(photoWebView_, "Upload another", android.R.drawable.ic_menu_revert, "Close", android.R.drawable.ic_menu_close_clear_cancel);
    final Button closeButton = (Button)photoWebView_.findViewById(R.id.next);
    closeButton.setEnabled(false);
    closeButton.setVisibility(View.GONE);


    // start reading categories
    if(photomapCategories == null)
      new GetPhotomapCategoriesTask().execute();
    else
      setupSpinners();
    
    there_ = new ThereOverlay(getActivity());
    there_.setLocationListener(this);
  
    setupView();
    
    return photoRoot_;
  } // PhotoUploadFragment

  private void backNextButtons(final View parentView,
                               final String backText, final int backDrawable,
                               final String nextText, final int nextDrawable) {
    final Button back = (Button)parentView.findViewById(R.id.back);
    back.setText(backText);
    back.setCompoundDrawablesWithIntrinsicBounds(backDrawable, 0, 0, 0);
    final Button next = (Button)parentView.findViewById(R.id.next);
    next.setText(nextText);
    next.setCompoundDrawablesWithIntrinsicBounds(0, 0, nextDrawable, 0);
  } // backNextButtons

  private String photoUploadMetaData() {
    try {
      final ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
      final Bundle bundle = ai.metaData;
      final String upload = bundle.getString("CycleStreetsPhotoUpload");
      return upload != null ? upload : "";
    } catch(final Exception e) {
      return "";
    } // catch
  } // photoUploadMetaData
  
  private void setContentView(final View child)
  {
    photoRoot_.removeAllViewsInLayout();
    photoRoot_.addView(child);
  } // setContentView

  private void store()
  {
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putInt("STEP", step_.value());
    edit.putString("PHOTOFILE", photoFile_);
    edit.putString("DATETIME", dateTime_);
    edit.putString("CAPTION", caption_);
    edit.putBoolean("GEOLOC", geolocated_);
    edit.commit();
  } // store
  
  @Override
  public void onPause()
  {
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString("CAPTION", captionText());
    edit.putInt("METACAT", metaCategoryId());
    edit.putInt("CATEGORY", categoryId());        
    final IGeoPoint p = there_.there();
    if(p != null)
    {
      edit.putInt("THERE-LAT", p.getLatitudeE6());
      edit.putInt("THERE-LON", p.getLongitudeE6());
    } 
    else
      edit.putInt("THERE-LAT", -1);
    edit.putLong("WHEN", new Date().getTime());
    edit.putBoolean("GEOLOC", geolocated_);
    edit.commit();

    if(map_ != null)
      map_.onPause();        
    super.onPause();
  } // onPause
  
  private final long fiveMinutes = 5 * 60 * 1000;
  
  @Override
  public void onResume()
  {
    try {
      doOnResume();
    } // try
    catch(RuntimeException e) {
      step_ = AddStep.fromInt(0);
    } // catch
    
    super.onResume();
    setupView();
  } // onResume
  
  private void doOnResume()
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
    geolocated_ = prefs.getBoolean("GEOLOC", false);

    if(map_ != null)
      map_.onResume();

    final long now = new Date().getTime();
    final long when = prefs.getLong("WHEN", now);
    if((now - when) > fiveMinutes)
      step_ = AddStep.fromInt(0);
  } // doOnResume
  
  private SharedPreferences prefs()
  {
    return getActivity().getSharedPreferences("net.cyclestreets.AddPhotoActivity", Context.MODE_PRIVATE);
  } // prefs()
   
  ///////////////////////////////////////////////////////////////////
  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) 
  {
    createMenuItem(menu, R.string.all_menu_restart, Menu.NONE, R.drawable.ic_menu_rotate);
    createMenuItem(menu, R.string.all_menu_back, Menu.NONE, R.drawable.ic_menu_revert);
  } // onCreateOptionsMenu
  
  @Override
  public void onPrepareOptionsMenu(final Menu menu)
  {
    enableMenuItem(menu, R.string.all_menu_restart, step_ != AddStep.PHOTO);
    enableMenuItem(menu, R.string.all_menu_back, step_ != AddStep.PHOTO && step_ != AddStep.VIEW);
  } // onPrepareOptionsMenu
    
  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
  {
    final int menuItem = item.getItemId();

    if(R.string.all_menu_restart == menuItem) {
      step_ = AddStep.PHOTO;
      setupView();
      return true;
    }

    if(R.string.all_menu_back == menuItem) {
      onBackPressed();
      return true;
    }

    return false;
  } // onMenuItemSelected
  
  ///////////////////////////////////////////////////////////////////
  @Override
  public boolean onBackPressed()
  { 
    if(step_ == AddStep.PHOTO || step_ == AddStep.VIEW)
    {
      step_ = AddStep.PHOTO;
      store();
      
      return false;
    } // if ...
    
    step_ = step_.prev();
    store();
    setupView();
    
    return true;
  } // onBackPressed
  
  private void nextStep()
  {
    if((step_ == AddStep.LOCATION) && (there_.there() == null))
    {
      Toast.makeText(getActivity(), "Please set photo location", Toast.LENGTH_LONG).show();
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
      geolocated_ = false;
      there_.noOverThere(null);
      setContentView(photoView_);
      break;
    case CAPTION:
      // why recreate this view each time - well *sigh* because we have to force the 
      // keyboard to hide, if we don't recreate the view afresh, Android won't redisplay 
      // the keyboard if we come back to this view
      photoCaption_ = inflater_.inflate(R.layout.addphotocaption, null);
      backNextButtons(photoCaption_, getString(R.string.all_button_back), android.R.drawable.ic_media_rew, getString(R.string.all_button_next), android.R.drawable.ic_media_ff);
      setContentView(photoCaption_);
      captionEditor().setText(caption_);
      if (photo_ == null && allowTextOnly_) {
        ((TextView)photoRoot_.findViewById(R.id.label)).setText(R.string.report_title);
        ((EditText)photoRoot_.findViewById(R.id.caption)).setLines(10);
      } // if ...
      break;
    case CATEGORY:
      caption_ = captionText();
      store();
      setContentView(photoCategory_);
      break;
    case LOCATION:
      metaCatId_ = metaCategoryId();
      catId_ = categoryId();
      setupMap();
      setContentView(photoLocation_);
      there_.recentre();
      if (photo_ == null && allowTextOnly_) {
        ((TextView) photoRoot_.findViewById(R.id.label)).setText(R.string.report_location_hint);
        photoRoot_.findViewById(R.id.nogeo).setVisibility(View.GONE);
      }
      else {
        ((TextView) photoRoot_.findViewById(R.id.label)).setText(R.string.photo_location_hint);
        photoRoot_.findViewById(R.id.nogeo).setVisibility(geolocated_ ? View.GONE : View.VISIBLE);
      }
      break;
    case VIEW:
      setContentView(photoWebView_);
      {
        final TextView text = (TextView)photoWebView_.findViewById(R.id.photo_text);
        text.setText(caption_);
        final TextView url = (TextView)photoWebView_.findViewById(R.id.photo_url);
        final Button share = (Button)photoWebView_.findViewById(R.id.photo_share);
        if (noShare_) {
          url.setVisibility(View.GONE);
          share.setVisibility(View.GONE);
        } else {
          url.setText(uploadedUrl_);
          share.setOnClickListener(this);
        }
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
    final ImageView iv = (ImageView)photoRoot_.findViewById(R.id.photo);
    if(iv == null)
      return;

    if (photo_ == null && allowTextOnly_) {
      iv.setVisibility(View.GONE);
      return;
    }

    iv.setImageBitmap(photo_);
    int newHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight() / 10 * 4;
    int newWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();

    iv.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
    iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
  } // previewPhoto
  
  private void hookUpNext()
  {
    final Button b = (Button)photoRoot_.findViewById(R.id.back);
    if(b != null)
      b.setOnClickListener(this);

    final Button n = (Button)photoRoot_.findViewById(R.id.next);
    if(n != null)
      n.setOnClickListener(this);
    
    if(step_ == AddStep.LOCATION)
      n.setEnabled(there_.there() != null);
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
    metaCategorySpinner().setAdapter(new CategoryAdapter(getActivity(), photomapCategories.metaCategories()));
    categorySpinner().setAdapter(new CategoryAdapter(getActivity(), photomapCategories.categories()));
    
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
  
  private void setupMap()
  {
    final RelativeLayout v = (RelativeLayout)(photoLocation_.findViewById(R.id.mapholder));

    if(map_ != null) {
      map_.onPause();
      ((RelativeLayout)map_.getParent()).removeView(map_);
    }
    else
    {
      map_ = new CycleMapView(getActivity(), this.getClass().getName());
      map_.overlayPushTop(there_);
    } 
      
    v.addView(map_, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    map_.enableAndFollowLocation();
    map_.onResume();
    there_.setMapView(map_);
  } // setupMap
  
  @Override
  public void onClick(final View v) 
  {
    int clicked = v.getId();

    if (R.id.takephoto_button == clicked)
      startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE),
                             TakePhoto);

    if (R.id.chooseexisting_button == clicked)
      startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                             ChoosePhoto);

    if (R.id.textonly_button == clicked) {
      photo_ = null;
      photoFile_ = null;
      dateTime_ = null;
      nextStep();
    }

    if (R.id.photo_share == clicked)
      Share.Url(getActivity(), uploadedUrl_, caption_, "Photo on CycleStreets.net");

    if (R.id.back == clicked) {
      if (step_ == AddStep.VIEW) {
        step_ = AddStep.PHOTO;
        store();
        setupView();
      } else
        onBackPressed();
    }

    if (R.id.next == clicked) {
      if(step_ == AddStep.LOCATION) {
        final boolean needAccountDetails = !allowUploadByKey_ && !CycleStreetsPreferences.accountOK();
        if(needAccountDetails)
          startActivityForResult(new Intent(getActivity(), AccountDetailsActivity.class), AccountDetails);
        else
          upload();
      } else if (step_ == AddStep.VIEW) {

      }
      else
        nextStep();
    } // switch
  } // onClick

  @Override
  public void onSetLocation(IGeoPoint point)
  {
    final Button u = (Button)photoLocation_.findViewById(R.id.next);
    u.setEnabled(point != null);
  } // onSetLocation
  
  @Override
  public void onActivityResult(final int requestCode, 
                               final int resultCode, 
                               final Intent data) 
  {
    if (resultCode != Activity.RESULT_OK)
      return;

    try
    {
      /*
      String url = intent.getData().toString();
Bitmap bitmap = null;
InputStream is = null;
if (url.startsWith("content://com.google.android.apps.photos.content")){
       is = getContentResolver().openInputStream(Uri.parse(url));
       bitmap = getBitmapFromInputStream(is);
}
       */

      photoFile_ = getImageFilePath(data);
      if(photo_ != null)
        photo_.recycle();
      photo_ = Bitmaps.loadFile(photoFile_);
          
      final ExifInterface exif = new ExifInterface(photoFile_);

      dateTime_ = photoTimestamp(exif);
      final GeoPoint photoLoc = photoLocation(exif);
      geolocated_ = (photoLoc != null);
      there_.noOverThere(photoLocation(exif));
          
      nextStep();
    }
    catch(Exception e)
    {
      Toast.makeText(getActivity(), "There was a problem grabbing the photo : " + e.getMessage(), Toast.LENGTH_LONG).show();
      if(requestCode == TakePhoto)
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                                          android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                                          ChoosePhoto);
    }
  } // onActivityResult
  
  private String getImageFilePath(final Intent data)
  {
    final Uri selectedImage = data.getData();
    final String[] filePathColumn = { MediaStore.Images.Media.DATA };

    final Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
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
      Toast.makeText(getActivity(), R.string.photo_could_not_upload, Toast.LENGTH_LONG).show();
      step_ = AddStep.LOCATION;
    }
  } // upload
  
  private void doUpload() throws Exception
  {
    final String filename = photoFile_;
    final String username = CycleStreetsPreferences.username();
    final String password = CycleStreetsPreferences.password();
    final IGeoPoint location = there_.there();
    final String metaCat = photomapCategories.metaCategories().get(metaCatId_).getTag();
    final String category = photomapCategories.categories().get(catId_).getTag();
    final String dateTime = dateTime_ != null ? dateTime_ : Long.toString(new Date().getTime() / 1000);
    final String caption = caption_;

    final UploadPhotoTask uploader = new UploadPhotoTask(getActivity(),
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
        photomapCategories = PhotomapCategories.get();
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
        Toast.makeText(getActivity(), R.string.photo_could_not_load_categories, Toast.LENGTH_LONG).show();
        return;
      } // if ...
      PhotoUploadFragment.photomapCategories = photomapCategories;
      setupSpinners();
    } // onPostExecute
  } // class GetPhotomapCategoriesTask
  
  //////////////////////////////////////////////////////////////////////////
  private class UploadPhotoTask extends AsyncTask<Object, Void, Upload.Result>
  {
    private final String filename_;
    private final String username_;
    private final String password_;
    private final IGeoPoint location_;
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
                    final IGeoPoint location,
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
      
      progress_ = Dialog.createProgressDialog(context, R.string.photo_uploading);
    } // UploadPhotoTask
    
    @Override
    protected void onPreExecute() 
    {
      super.onPreExecute();
      progress_.show();
    } // onPreExecute
    
    protected Upload.Result doInBackground(Object... params)
    {
      try {
        return Upload.photo(filename_, 
                            username_, 
                            password_, 
                            location_, 
                            metaCat_, 
                            category_, 
                            dateTime_, 
                            caption_);
      } catch (Exception e) {
        return Upload.Result.error(e.getMessage());
      }
    } // doInBackground
    
    @Override
    protected void onPostExecute(final Upload.Result result) {
      if(smallImage_)
        new File(filename_).delete();
      progress_.dismiss();
      if(result.ok())
        uploadComplete(result.url());
      else
        uploadFailed(result.message());
    } // onPostExecute
  } // class UploadPhotoTask
  
  //////////////////////////////////////////////////////////
  static private class CategoryAdapter extends BaseAdapter
  {
    private final LayoutInflater inflater_;
    private final List<PhotomapCategory> list_;
    
    public CategoryAdapter(final Context context,
                           final List<PhotomapCategory> list)
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
      final PhotomapCategory c = list_.get(position);
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
