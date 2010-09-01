package uk.org.invisibility.cycloid;

import org.andnav.osm.views.util.OpenStreetMapTileProviderDirect;

import android.os.Handler;
import android.os.Message;
import android.view.View;

/*
 * Custom tile provider to implement preloading and caching etc.
 * Requires osmdroid changes so not currently used.  
 */
public class CycloidTileProvider extends OpenStreetMapTileProviderDirect
{
	public CycloidTileProvider(String aCloudmadeKey, final View view)
	{
		super
		(
			   	new Handler()
				{
			   		View map = view;
		   		
					public void handleMessage(final Message msg)
					{
						/*
						switch (msg.what)
						{
							case OpenStreetMapTile.MAPTILE_SUCCESS_ID:
			 					Log.i(LOGTAG, "MAPTILE SUCCESS");
								break;
							default:
			 					Log.i(LOGTAG, "Unknown message");
						}
						*/
						super.handleMessage(msg);
						if (map != null)
							map.invalidate();
			    	}
				},
				aCloudmadeKey
		);
	}
}
