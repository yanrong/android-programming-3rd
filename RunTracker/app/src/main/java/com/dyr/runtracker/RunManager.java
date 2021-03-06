package com.dyr.runtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class RunManager {
    private static final String TAG = "RunManager";
    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    public static final String ACTION_LOCATION = "com.dyr.runtracker.ACTION_LOCATION";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    //The private constructor forces users to user RunManager.get(context)
    private RunManager(Context context) {
        mAppContext = context;
        mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static RunManager get(Context c) {
        if (sRunManager == null) {
            //Use The application context to avoid leaking activities
            sRunManager = new RunManager(c.getApplicationContext());
        }

        ActivityCompat.requestPermissions((Activity) c, new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION },
                123);

        return sRunManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;
        //If you have the test provider and it's enabled, ues it
        if(mLocationManager.getProvider(TEST_PROVIDER) != null &&
        mLocationManager.isProviderEnabled(TEST_PROVIDER)){
            provider = TEST_PROVIDER;
        }
        Log.d(TAG, "Using provider "+ provider);
        //Get the last known location and broadcast it if you have one
        Location lastKnown = mLocationManager.getLastKnownLocation(provider);
        if(lastKnown != null){
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

        //Start update from location manager
        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
    }

    private void broadcastLocation(Location location){
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }

    public void stopLocationUpdates(){
        PendingIntent pi = getLocationPendingIntent(false);
        if(pi != null){
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun(){
        return getLocationPendingIntent(false) != null;
    }
}
