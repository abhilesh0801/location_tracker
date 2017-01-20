package com.abhi.locationapp.Activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.locationapp.Common.Utility;
import com.abhi.locationapp.R;
import com.abhi.locationapp.Service.LocationService;

public class MainActivity extends AppCompatActivity {

    TextView mStatusTextView;
    LinearLayout timerLayout;
    Chronometer textClock;
    Button mStartButton;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusTextView = (TextView) findViewById(R.id.textStatus);
        textClock = (Chronometer) findViewById(R.id.textTime);
        mStartButton = (Button) findViewById(R.id.btnStart);
        timerLayout = (LinearLayout) findViewById(R.id.timerLayout);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMyServiceRunning(LocationService.class)) {
                    stopService();
                } else {
                    if (!hasPermissions(PERMISSIONS)) {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                    } else {
                        startService();
                    }
                }

            }
        });
    }

    private void startService() {
        startService(new Intent(MainActivity.this, LocationService.class));
        startTimer();
        mStatusTextView.setText(getString(R.string.running));
        mStartButton.setText(getString(R.string.stop_service));
        timerLayout.setVisibility(View.VISIBLE);
    }

    private void stopService() {
        stopService(new Intent(MainActivity.this, LocationService.class));
        mStatusTextView.setText(getString(R.string.stopped));
        mStartButton.setText(getString(R.string.start_service));
        textClock.stop();
        timerLayout.setVisibility(View.GONE);
    }

    private void startTimer() {
        textClock.setBase(SystemClock.elapsedRealtime());
        textClock.start();
    }

    private void updateTimer() {
        textClock.setBase(Utility.getSharedLong(getApplicationContext(), getString(R.string.service_start_time)));
        textClock.start();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isMyServiceRunning(LocationService.class)) {
            updateTimer();
            mStartButton.setText(getString(R.string.stop_service));
            mStatusTextView.setText(getString(R.string.running));
            timerLayout.setVisibility(View.VISIBLE);
        } else {
            timerLayout.setVisibility(View.GONE);
            mStatusTextView.setText(getString(R.string.stopped));
        }
    }

    // Requests permissions from users, if device is using Marshmallow or higher
    private boolean hasPermissions(String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == PERMISSION_ALL) {
            //If permission is granted
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                Toast.makeText(this, "Permission(s) denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
