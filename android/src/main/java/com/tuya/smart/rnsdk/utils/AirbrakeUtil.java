package com.tuya.smart.rnsdk.utils;

import android.content.Context;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AirbrakeUtil {

    public static void init(Context context)
    {
        AirbrakeNotifier.register(context, "14dbe344bf7cd33918e15866c7d38ba7");
    }

    public static void notifyLog(String title, String msg) {
        RuntimeException e = new RuntimeException("Doorbell Logs - "+title + " - " + android.os.Build.MODEL + " - " + getTimestamp());
        HashMap<String, String> map = new HashMap<>();
        map.put("data", msg);
        AirbrakeNotifier.notify(e, map);
    }

    public static void notifyLog(String title, String msg1, String msg2) {
        RuntimeException e = new RuntimeException("Doorbell Logs - "+title + " - " + android.os.Build.MODEL + " - " + getTimestamp());
        HashMap<String, String> map = new HashMap<>();
        map.put("data1", msg1);
        map.put("data2", msg2);
        AirbrakeNotifier.notify(e, map);
    }

    private static String getTimestamp() {
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String format = simpleDateFormat.format(new Date());
        return format;
    }
}
