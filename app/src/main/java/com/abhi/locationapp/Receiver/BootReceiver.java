package com.abhi.locationapp.Receiver;

/**
 * Created by User on 1/13/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abhi.locationapp.Service.LocationService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LocationService.class));
    }
}
