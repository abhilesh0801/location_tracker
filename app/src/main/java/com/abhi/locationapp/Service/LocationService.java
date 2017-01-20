package com.abhi.locationapp.Service;

/**
 * Created by User on 1/13/2017.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.abhi.locationapp.Common.Utility;
import com.abhi.locationapp.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.UTCProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements android.location.LocationListener {

    private static final String TAG = "LOCATION_TRACKER";

    private final String MAIN_DIRECTORY = "location_tracker";
    private final String LOCATION_DIRECTORY = "location_data";
    private final String SYSTEM_DIRECTORY = "system_data";
    private final String SYSTEM_FILE_KEY = "system_file";
    private final String LOCATION_FILE_KEY = "location_file";
    private final String COMMA_SEPARATOR = ", ";

    private final float MAX_TOLERABLE_ACCURACY = 20.0f;
    private final float MIN_VELOCITY = 5.0f;
    private final float MIN_DISTANCE = 50.0f;
    private final int MIN_SATELLITES_LOCKED = 5;
    private final int TEN_MINUTES = 10 * 60 * 1000;

    private LocationManager mLocationManager = null;

    private boolean isRequestingUpdates;

    private int PACKET_INTERVAL = 0;
    private Location mLastLocation;

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: " + location);
        if (isRequestingUpdates && isValidData(location)) {
            storeLocationData(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, "onStatusChanged: " + provider);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        // Save file names with time stamps to shared preferences
        String dateTime = new DateTime(DateTimeZone.UTC).toString();
        Utility.putSharedString(getApplicationContext(), SYSTEM_FILE_KEY, dateTime);
        Utility.putSharedString(getApplicationContext(), LOCATION_FILE_KEY, dateTime);

        // Save start time to shared preferences
        Utility.putSharedLong(getApplicationContext(), getString(R.string.service_start_time), SystemClock.elapsedRealtime());

        requestUpdates();

        // Set timer to save system status every 10 minutes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                storeSystemData();
            }
        }, 0, TEN_MINUTES);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        createDirectories();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopUpdates();
        com.abhi.locationapp.Common.Utility.clearSharedPreferences(getApplicationContext());
        super.onDestroy();
    }

    private void createDirectories() {
        //Create a main directory in primary storage
        File mainDir = new File(Environment.getExternalStorageDirectory(), MAIN_DIRECTORY);
        if (!mainDir.exists()) {
            mainDir.mkdirs();
        }

        //Create a directory to store location data files
        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + MAIN_DIRECTORY, LOCATION_DIRECTORY);
        if (!f1.exists()) {
            f1.mkdirs();
        }

        //Create a directory to store system data files
        File f2 = new File(Environment.getExternalStorageDirectory() + "/" + MAIN_DIRECTORY, SYSTEM_DIRECTORY);
        if (!f2.exists()) {
            f2.mkdirs();
        }
    }

    private void requestUpdates() {
        Log.e(TAG, "requestUpdates");
        isRequestingUpdates = true;
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void stopUpdates() {
        Log.e(TAG, "stopUpdates");
        isRequestingUpdates = false;
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    // Sets packet time interval based on current mode
    private void setPacketInterval(Location location) {
        if (mLastLocation == null)
            mLastLocation = new Location(location);

        if (velocityInKmph(location.getSpeed()) > MIN_VELOCITY || mLastLocation.distanceTo(location) > MIN_DISTANCE) {
            PACKET_INTERVAL = 2;    //Active mode : interval = 2 minutes
        } else {
            PACKET_INTERVAL = 30;   //Idle mode : interval = 30 minutes
        }

        mLastLocation.set(location);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    // Returns true if location data meets accuracy and satellite requirements
    private boolean isValidData(Location location) {
        return location.getAccuracy() <= MAX_TOLERABLE_ACCURACY && getLockedSatellites(location) >= MIN_SATELLITES_LOCKED;
    }

    // Converts velocity from m/s to km/h
    private float velocityInKmph(float speed) {
        return speed * 3.6f;
    }

    // Returns number of satellites locked for current location value
    private int getLockedSatellites(Location location) {
        return location.getExtras().getInt("satellites");
    }

    // Prepares location data to be written to file
    protected void storeLocationData(Location location) {
        Log.e(TAG, "storeLocationData");

        stopUpdates();

        StringBuilder stringBuilder = new StringBuilder(Utility.formatDate(location.getTime()));
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getString(R.string.latitude_format, location.getLatitude()));
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getString(R.string.longitude_format, location.getLongitude()));
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getString(R.string.velocity_format, velocityInKmph(location.getSpeed())));
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getString(R.string.accuracy_format, location.getAccuracy()));
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getString(R.string.satellites_format, getLockedSatellites(location)));

        writeToFile("/" + MAIN_DIRECTORY + "/" + LOCATION_DIRECTORY, stringBuilder.toString(), LOCATION_FILE_KEY);

        setPacketInterval(location);

        // Put update request to sleep for "PACKET INTERVAL" minutes
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestUpdates();
            }
        }, PACKET_INTERVAL * 60000);
    }

    // Prepares system data to be written to file
    protected void storeSystemData() {
        Log.e(TAG, "storeSystemData");
        StringBuilder stringBuilder = new StringBuilder(new DateTime(DateTimeZone.UTC).toString());
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getBatteryLevel() + "%");
        stringBuilder.append(COMMA_SEPARATOR);
        stringBuilder.append(getNetworkData());

        writeToFile("/" + MAIN_DIRECTORY + "/" + SYSTEM_DIRECTORY, stringBuilder.toString(), SYSTEM_FILE_KEY);
    }

    // Returns network name and type
    public String getNetworkData() {
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName() + COMMA_SEPARATOR + Utility.getNetworkTypeName(telephonyManager);
    }

    // Returns battery level in percentage value
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return ((float) level / (float) scale) * 100.0f;
    }

    // Writes data to respective file
    private void writeToFile(String path, String data, String key) {
        Log.e(TAG, "writeToFile: " + key);
        try {
            String lineBreak = "\n";
            File myFile = new File(Environment.getExternalStorageDirectory() + path, Utility.getSharedString(getApplicationContext(), key) + ".txt");

            //Checks if file size exceeds 1MB
            if (fileExceedsSize(myFile)) {
                String fileName = new DateTime(DateTimeZone.UTC).toString();
                myFile = new File(Environment.getExternalStorageDirectory() + path, fileName + ".txt");
                Utility.putSharedString(getApplicationContext(), key, fileName);
            }

            // Creates a new file, if not present already
            if (!myFile.exists()) {
                myFile.createNewFile();
                lineBreak = "";
            }

            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(myFile, true));
            osw.write(lineBreak + data);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Converts from bytes to KB, returns true if size is over 1024KB(1MB).
    private boolean fileExceedsSize(File file) {
        long fileSize = file.length() / 1024;
        return fileSize >= 1024;
    }
}