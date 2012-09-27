package net.cyclestreets.service;

import net.cyclestreets.CycleStreets;
import net.cyclestreets.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LiveRideService extends Service implements LocationListener
{
  private IBinder binder_;
  private LocationManager locationManager_;
  private boolean riding_;

  @Override
  public void onCreate() 
  {
    binder_ = new Binding();
    locationManager_ = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    notify("CycleStreets", "Here we go!", "We're off!");
  } // onCreate
  
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) 
  {    
    return Service.START_NOT_STICKY;
  } // onStartCommand
  
  @Override 
  public void onDestroy() 
  {
    stopRiding();
    cancelNotification();
    super.onDestroy();
  } // onDestroy
  
  @Override
  public IBinder onBind(final Intent intent)
  {
    return binder_;
  } // onBind
  
  public void startRiding() 
  {
    if(riding_)
      return;
    
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    riding_ = true;
  } // startRiding
  
  public void stopRiding() 
  {
    if(!riding_)
      return;
    
    locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    riding_ = false;
  } // stopRiding
  
  public boolean areRiding()
  {
    return riding_;
  } // onRide
  
  private <T> void notify(final String title, final String text, final String ticker)
  {
    final NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    final Notification notification = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    final Intent notificationIntent = new Intent(this, CycleStreets.class);
    final PendingIntent contentIntent = 
         PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(this, title, text, contentIntent);
    nm.notify(1, notification);
  } // notify
  
  void cancelNotification()
  {
    final NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(1);
  } // cancelNotification
  
  public class Binding extends Binder 
  {
    private LiveRideService service() 
    {
      return LiveRideService.this;
    } // getService
    
    public void startRiding() { service().startRiding(); }
    public void stopRiding() { service().stopRiding(); }
    public boolean areRiding() { return service().areRiding(); }
  } // class LocalBinder

  /////////////////////////////////////////////////
  // location listener
  @Override
  public void onLocationChanged(final Location location)
  {
    notify("CycleStreets", "We've moved", "Cooking with gas");
  } // onLocationChanged

  @Override
  public void onProviderDisabled(final String arg0)
  {
  } // onProviderDisabled

  @Override
  public void onProviderEnabled(String arg0)
  {
  } // onProviderEnabled

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2)
  {
  } // onStatusChanged
  
} // class LiveRideService
