package net.cyclestreets;

import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.views.CycleMapView;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.views.overlay.ItemizedOverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class PhotomapActivity extends Activity
							implements ItemizedOverlay.OnItemGestureListener<PhotoItem> 	
{
	private CycleMapView map_; 
	private ItemizedOverlay<PhotoItem> markers_;
	private List<PhotoItem> photoList_;

	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        map_ = new CycleMapView(this, "photo");

        photoList_ = new ArrayList<PhotoItem>();
        
        markers_ = new ItemizedOverlay<PhotoItem>(this, 
        		photoList_,
        		getResources().getDrawable(R.drawable.icon),
        		new Point(10,10),
        		this,
        		new DefaultResourceProxyImpl(this));
        map_.overlayPushBottom(markers_);
        
        map_.setMapListener(new DelayedMapListener(new PhotomapListener(this, map_, photoList_)));
        
        final RelativeLayout rl = new RelativeLayout(this);
        rl.addView(this.map_, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(rl);        
    } // onCreate

	//////////////////////////////////////////////
	public boolean onItemLongPress(int i, final PhotoItem item) 
	{
		showPhoto(item);
		return true;
	} // onItemLongPress
		
	public boolean onItemSingleTapUp(int i, final PhotoItem item) 
	{
		showPhoto(item);
		return true;
	} // onItemSingleTapUp
	
	private void showPhoto(final PhotoItem item)
	{
		final Intent intent = new Intent(PhotomapActivity.this, DisplayPhotoActivity.class);
		intent.setData(Uri.parse(item.photo.thumbnailUrl));
		intent.putExtra("caption", item.photo.caption);
		startActivity(intent);
	} // showPhoto

	/////////////////////////////////////////////////////
    @Override
    protected void onPause()
    {
    	map_.onPause();
        super.onPause();
    } // onPause

    @Override
    protected void onResume()
    {
    	super.onResume();
    	map_.onResume();
    } // onResume
} // PhotomapActivity
