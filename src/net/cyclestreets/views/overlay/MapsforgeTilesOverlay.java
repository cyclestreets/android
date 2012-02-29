package net.cyclestreets.views.overlay;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Context;
import android.graphics.Canvas;

public class MapsforgeTilesOverlay extends TilesOverlay
{
  public MapsforgeTilesOverlay(final Context context)
  {
    super(context);
  } // MapsforgeTilesOverlay

  @Override
  public int getLoadingBackgroundColor()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getMaximumZoomLevel()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getMinimumZoomLevel()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setLoadingBackgroundColor(int arg0)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setUseDataConnection(boolean arg0)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean useDataConnection()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void draw(Canvas arg0, IMapView arg1, boolean arg2)
  {
    // TODO Auto-generated method stub
    
  }
} // class MapsforgeTilesOverlay
