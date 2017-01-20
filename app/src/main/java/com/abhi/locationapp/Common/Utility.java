package com.abhi.locationapp.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.abhi.locationapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by User on 1/19/2017.
 */

public class Utility {

    public static String getSharedString(Context mContext, String key) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
        return settings.getString(key, null);
    }

    public static void putSharedString(Context mContext, String key, String value) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
        settings.edit().putString(key, value).commit();
    }

    public static long getSharedLong(Context mContext, String key) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
        return settings.getLong(key, 0);
    }

    public static void putSharedLong(Context mContext, String key, long value) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
        settings.edit().putLong(key, value).commit();
    }

    public static void clearSharedPreferences(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    // Creates a UTC date and time string
    public static String formatDate(long longTime) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(longTime);
    }

    // Returns network type name based on network type value
    public static String getNetworkTypeName(TelephonyManager telephonyManager) {
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
        }
        return "UNKNOWN";
    }
}
