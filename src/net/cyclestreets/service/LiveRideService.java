package net.cyclestreets.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LiveRideService extends Service
{
  private final IBinder binder_ = new LocalBinder();
  
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) 
  {
    return Service.START_NOT_STICKY;
  } // onStartCommand
  
  @Override 
  public void onDestroy() 
  {
    super.onDestroy();
  } // onDestroy
  
  @Override
  public IBinder onBind(final Intent intent)
  {
    return binder_;
  } // onBind

  public class LocalBinder extends Binder 
  {
    public LiveRideService getService() 
    {
      return LiveRideService.this;
    } // getService
  } // class LocalBinder
  
} // class LiveRideService
