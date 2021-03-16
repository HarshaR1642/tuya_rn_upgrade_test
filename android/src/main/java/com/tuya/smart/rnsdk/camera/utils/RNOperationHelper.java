package com.tuya.smart.rnsdk.camera.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.tuya.smart.rnsdk.camera.KeylessModule;

import java.text.ParseException;
import java.util.ArrayList;

public class RNOperationHelper {

    public enum Operation implements Parcelable {
        /*LOCK,
        UNLOCK,
        ADD_CUSTOM_CODE,
        DELETE_CUSTOM_CODE,
        DELETE_AUTO_CODE,
        START_SCAN,
        STOP_SCAN;*/
        REMOVE_CAMERA;

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Operation> CREATOR = new Creator<Operation>() {
            @Override
            public Operation createFromParcel(Parcel in) {
                return Operation.values()[in.readInt()];
            }

            @Override
            public Operation[] newArray(int size) {
                return new Operation[size];
            }
        };
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;

    private ReactApplication reactApp;
    private AppCompatActivity activity;
    private OperationCallback callback;

    public RNOperationHelper(ReactApplication reactApp, AppCompatActivity appCompatActivity, OperationCallback callback) {
        this.reactApp = reactApp;
        this.activity = appCompatActivity;
        this.callback = callback;
    }

    public interface OperationCallback {

        public void onSuccess(Operation operation);

        public void onFailure(String message);

        public void foundLock(ReadableMap lock);
    }

    public void performOperation(Operation operation, String deviceId) {  // , Schedule deviceSchedule
        try {
            switch (operation) {
                case REMOVE_CAMERA:
                    doOperation(reactApp, operation, "removeCamera", getParamsForWrite(Operation.REMOVE_CAMERA, deviceId));
                    break;
                /*case UNLOCK:
                    doOperation(operation, "unlock", getParamsForLockWrite(Operation.LOCK, deviceId, null));
                    break;
                case ADD_CUSTOM_CODE:
                    if (deviceSchedule.getValidityType().equalsIgnoreCase("Daily Repeating")) {
                        doOperation(operation, "addCyclicPasscode", getParamsForLockWrite(Operation.ADD_CUSTOM_CODE,
                                deviceId, deviceSchedule));
                    } else {
                        doOperation(operation, "addPeriodPasscode", getParamsForLockWrite(Operation.ADD_CUSTOM_CODE,
                                deviceId, deviceSchedule));
                    }
                    break;
                case DELETE_CUSTOM_CODE:
                    doOperation(operation, "deleteCustomCode", getParamsForLockWrite(Operation.DELETE_CUSTOM_CODE, deviceId, deviceSchedule));
                    break;
                case DELETE_AUTO_CODE:
                    doOperation(operation, "deleteAutoCode", getParamsForLockWrite(Operation.DELETE_AUTO_CODE, deviceId, deviceSchedule));
                    break;
                case START_SCAN:
                    doOperation(operation, "startScan", getParamsForLockWrite(Operation.START_SCAN, deviceId, null));
                    break;
                case STOP_SCAN:
                    doOperation(operation, "stopScan", getParamsForLockWrite(Operation.STOP_SCAN, deviceId, null));
                    break;*/
            }
        } catch (Exception exception) {
            callback.onFailure(exception.getMessage());
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("Please enable location permission in settings to perform lock operations");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    void doOperation(ReactApplication app, final Operation operation, String methodName, WritableNativeArray tobeWritten) {
        //if (BluetoothUtility.isBluetoothEnabled() && isLocationPermissionGranted() && BluetoothUtility.isGpsEnabled(activity)) {
            try {
                //ReactNativeHost reactNativeHost = Controller.getInstance().getReactNativeHost();
                ReactNativeHost reactNativeHost = app.getReactNativeHost();
                ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
                ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                if (reactContext != null) {
                    Log.d("ReactContext : ", "reactContext.hasActiveCatalystInstance() : "+reactContext.hasActiveCatalystInstance() + "");

                    /*reactContext.setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                        @Override
                        public void handleException(Exception e) {
                            LogCat.e("ReactContext : ", "NativeModuleCallExceptionHandler : "+e.getMessage() + "");
                            e.printStackTrace();
                        }
                    });*/

                    if(!reactContext.hasActiveCatalystInstance()) {
                        reactInstanceManager.recreateReactContextInBackground();
                        Thread.sleep(2000);

                        reactNativeHost = app.getReactNativeHost();
                        reactInstanceManager = reactNativeHost.getReactInstanceManager();
                        reactContext = reactInstanceManager.getCurrentReactContext();
                    }

                    /*CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                    WritableNativeArray params = new WritableNativeArray();
                    //params.pushString("Set Extra Message was called!");
                    //params.pushString(NetworkingUrls.BASEURL);
                    catalystInstance.callFunction("DahaoLockOperation", "setServerUrl", params);*/
                } else {
                    reactInstanceManager.createReactContextInBackground();
                    /*try {
                        reactInstanceManager.createReactContextInBackground();
                    } catch (Exception e) {
                        Utility.printStackTrace(e);
                        reactInstanceManager.recreateReactContextInBackground();
                    }*/
                }

                if (reactContext != null) {
                    Log.d("ReactContext : ", "reactContext.hasActiveCatalystInstance() : "+reactContext.hasActiveCatalystInstance() + "");

                    CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                    catalystInstance.getNativeModule(KeylessModule.class).setRnOperationCallback(new KeylessModule.RNOperationCallback() {

                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess(operation);
                        }

                        @Override
                        public void onFailure(String message) {
                            callback.onFailure(message);
                        }

                        @Override
                        public void foundLock(ReadableMap lock) {callback.foundLock(lock);}
                    });
                    catalystInstance.callFunction("RNOperation", methodName, tobeWritten);
                } else {
                    reactInstanceManager.createReactContextInBackground();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        /*} else {
            if (!BluetoothUtility.isBluetoothEnabled()) {
                BluetoothUtility.enableBluetooth(activity);
            }
            if (!isLocationPermissionGranted()) {
                requestLocationPermission();
            }
            if (!BluetoothUtility.isGpsEnabled(activity)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder
                        //.setTitle(R.string.network_not_enabled)
                        .setMessage("Please turn on Location to continue")
                        .setPositiveButton("Settings",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }*/
    }

    private WritableNativeArray getParamsForWrite(Operation operation, String devId) throws ParseException {
        WritableNativeArray params = new WritableNativeArray();
        switch (operation) {
            case REMOVE_CAMERA:
                params.pushString(devId);
                break;
            /*case LOCK:
            case UNLOCK:
                params.pushString(macAddress);
                break;
            case ADD_CUSTOM_CODE:
                if (deviceSchedule.getValidityType().equalsIgnoreCase("Never Expires")) {
                    params.pushString(macAddress);
                    params.pushInt(0);
                    params.pushInt(0);
                    params.pushString(deviceSchedule.getSecurityCode());
                    params.pushString(deviceSchedule.getName());
                } else if (deviceSchedule.getValidityType().equalsIgnoreCase("Specific Period")) {
                    params.pushString(macAddress);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
                    long fromTime = 0, toTime = 0;
                    try {
                        fromTime = formatter.parse(deviceSchedule.getFromTime()).getTime();
                        toTime = formatter.parse(deviceSchedule.getToTime()).getTime();
                    } catch (ParseException e) {
                        formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
                        fromTime = formatter.parse(deviceSchedule.getFromTime()).getTime();
                        toTime = formatter.parse(deviceSchedule.getToTime()).getTime();
                    }
                    params.pushString(String.valueOf(fromTime));
                    params.pushString(String.valueOf(toTime));
                    params.pushString(deviceSchedule.getSecurityCode());
                    params.pushString(deviceSchedule.getName());
                } else if (deviceSchedule.getValidityType().equalsIgnoreCase("Daily Repeating")) {
                    params.pushString(macAddress);
                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
                    Date from = formatter.parse(deviceSchedule.getInfo(0).getFromTime());
                    String fromTime = outputFormat.format(from);
                    Date to = formatter.parse(deviceSchedule.getInfo(0).getToTime());
                    String toTime = outputFormat.format(to);
                    params.pushString(fromTime);
                    params.pushString(toTime);
                    params.pushString(deviceSchedule.getSecurityCode());
                    params.pushString(deviceSchedule.getName());
                    params.pushArray(getDaysArray(deviceSchedule.getInfo(0).getWeekDayList()));
                }
                break;
            case DELETE_CUSTOM_CODE:
            case DELETE_AUTO_CODE:
                params.pushString(macAddress);
                //params.pushString(deviceSchedule.getSecurityCode());
                params.pushString(deviceSchedule.getOld_security_code());
                break;*/
        }

        return params;
    }

    private WritableNativeArray getDaysArray(ArrayList<String> days) {
        WritableNativeArray daysOfWeek = new WritableNativeArray();
        for (int i = 0; i < 7; i++) {
            if (days.contains(String.valueOf(i))) {
                daysOfWeek.pushInt(1);
            } else {
                daysOfWeek.pushInt(0);
            }
        }
        return daysOfWeek;
    }
}
