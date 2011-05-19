package net.cyclestreets;

import net.cyclestreets.views.overlay.ItemizedOverlay;
import net.cyclestreets.views.overlay.PhotoItemOverlay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PhotomapActivity extends CycleMapActivity
							implements ItemizedOverlay.OnItemGestureListener<PhotoItemOverlay.PhotoItem> 	
{
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        overlayPushBottom(new PhotoItemOverlay(this, mapView(), this));
    } // onCreate

	//////////////////////////////////////////////
	public boolean onItemLongPress(int i, final PhotoItemOverlay.PhotoItem item) 
	{
		showPhoto(item);
		return true;
	} // onItemLongPress
		
	public boolean onItemSingleTapUp(int i, final PhotoItemOverlay.PhotoItem item) 
	{
		showPhoto(item);
		return true;
	} // onItemSingleTapUp
	
	private void showPhoto(final PhotoItemOverlay.PhotoItem item)
	{
		final Intent intent = new Intent(PhotomapActivity.this, DisplayPhotoActivity.class);
		intent.setData(Uri.parse(item.photo().thumbnailUrl));
		intent.putExtra("caption", item.photo().caption);
		startActivity(intent);
	} // showPhoto
} // PhotomapActivity
