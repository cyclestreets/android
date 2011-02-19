package net.cyclestreets;

import java.util.List;
import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.views.overlay.ItemizedOverlay;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;

public class PhotomapActivity extends CycleMapActivity
							implements ItemizedOverlay.OnItemGestureListener<PhotoItem> 	
{
	private ItemizedOverlay<PhotoItem> markers_;
	private List<PhotoItem> photoList_;

	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        photoList_ = new ArrayList<PhotoItem>();
        
        markers_ = new ItemizedOverlay<PhotoItem>(this, 
        		photoList_,
        		getResources().getDrawable(R.drawable.icon),
        		new Point(10,10),
        		this,
        		new DefaultResourceProxyImpl(this));
        overlayPushBottom(markers_);
        
        mapView().setMapListener(new DelayedMapListener(new PhotomapListener(this, mapView(), photoList_)));
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
} // PhotomapActivity
