package ece.course.reference;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;


import java.sql.Time;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class BackgroundService extends Service {

    final static String TAG_REPORT = "tagReport";
    final static String TICKER_TEXT = "Lab 6";
    final static String NOTIFICATION_TITLE = "Location Updated";
    private final static String CHANNEL_ID = "MyChannel01";

    private NotificationManager mNotificationManager;
    private LocationManager mLocationManager;
    private Context mContext;
    private IBinder mBinder;
    private boolean isStarted;
    private int mMsgCount;
    private ArrayList<String> mLocations;


    public void onCreate() {
        super.onCreate();
        mBinder = new BackgroundBinder();
        isStarted = false;
        mMsgCount = 0;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mContext = getApplicationContext();
        mLocations = new ArrayList<String>();

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_TITLE, importance);
            mChannel.enableLights(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }


    public void onDestroy() {
        super.onDestroy();
        stopRun();
    }


    public void startRun() {
        if (isStarted)
            return;
        isStarted = true;
        mLocations.clear();
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        sendMsg(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }


    }


    public void stopRun() {
        if (!isStarted)
            return;
        isStarted = false;
        mLocationManager.removeUpdates(mLocationListener);


    }


    public boolean getStarted() {
        return isStarted;
    }

    public class BackgroundBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }



    public ArrayList<String> getReport() {
        return mLocations;
    }


    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            sendMsg(location);
        }
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void sendMsg(Location location) {
        if (isStarted && (location != null)) {
            long when = System.currentTimeMillis();
            Time time = new Time(when);
            //Notification notification = new Notification(R.drawable.icon, TICKER_TEXT, when);
            String contentText = "Latitude: " + location.getLatitude() + ",\n Longitude: " + location.getLongitude();

            Intent notificationIntent = new Intent(this, BackgroundService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
            Notification notification = builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.icon).setTicker(contentText).setWhen(when)
                    .setAutoCancel(true).setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(contentText).build();



            mNotificationManager.notify(mMsgCount++, notification);

            mLocations.add(contentText + "\n@" + time.toString());
        }
    }


}
