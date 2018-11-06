package com.apihelper.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * Created by denis on 10.05.16.
 */
public class DevUtils {

    public static String getDeviceId(Context context) {
        String macAddress = "", tmDevice = "";
        try {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo wInfo;
            if (wifiManager != null
                    && (wInfo = wifiManager.getConnectionInfo()) != null) {
                macAddress = "" + wInfo.getMacAddress();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            tmDevice = "" + tm.getDeviceId();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        macAddress += getDeviceName();
        UUID deviceUuid = UUID.nameUUIDFromBytes((macAddress + tmDevice).getBytes());
        String deviceID = "FF" + deviceUuid.toString();
        return deviceID;
    }

    public static  String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
