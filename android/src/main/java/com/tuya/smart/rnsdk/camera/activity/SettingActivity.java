package com.tuya.smart.rnsdk.camera.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.ReadableMap;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.device.api.IPropertyCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.utils.RNOperationHelper;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IDeviceListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;
import com.tuyasmart.camera.devicecontrol.api.ITuyaCameraDeviceControlCallback;
import com.tuyasmart.camera.devicecontrol.bean.DpBasicFlip;
import com.tuyasmart.camera.devicecontrol.bean.DpBasicIndicator;
import com.tuyasmart.camera.devicecontrol.bean.DpBasicNightvision;
import com.tuyasmart.camera.devicecontrol.bean.DpBasicOSD;
import com.tuyasmart.camera.devicecontrol.bean.DpBasicPrivate;
import com.tuyasmart.camera.devicecontrol.bean.DpMotionSensitivity;
import com.tuyasmart.camera.devicecontrol.bean.DpMotionSwitch;
import com.tuyasmart.camera.devicecontrol.bean.DpRestore;
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormat;
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormatStatus;
import com.tuyasmart.camera.devicecontrol.bean.DpSDRecordModel;
import com.tuyasmart.camera.devicecontrol.bean.DpSDRecordSwitch;
import com.tuyasmart.camera.devicecontrol.bean.DpSDStatus;
import com.tuyasmart.camera.devicecontrol.bean.DpSDStorage;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessBatterylock;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessElectricity;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessLowpower;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessPowermode;
import com.tuyasmart.camera.devicecontrol.model.DpNotifyModel;
import com.tuyasmart.camera.devicecontrol.model.MotionSensitivityMode;
import com.tuyasmart.camera.devicecontrol.model.NightStatusMode;
import com.tuyasmart.camera.devicecontrol.model.RecordMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingActivity";
    private List<String> mData;
    ITuyaCameraDevice mTuyaCameraDevice;
    ITuyaDevice mTuyaDevice;
    private Toolbar toolbar;
    private TextView showQueryTxt;
    private TextView showPublishTxt;
    private String devId;

    private ToggleButton toggle_FlipScreen, toggle_OSD, toggle_MotionDetection, toggle_LocalRecording;
    private TextView txt_NightVision, txt_ChimeType, txt_MotionSensitivity, txt_RecordType;
    private LinearLayout layout_MotionSensitivity, layout_RecordType, layout_StorageSetting, layout_ResetWifi;
    private Button btn_RemoveDevice;

    private Handler handler;
    private ProgressDialog progressDialog;
    private RNOperationHelper rnOperationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        handler = new Handler();

        toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        showQueryTxt = findViewById(R.id.tv_show_query);
        showPublishTxt = findViewById(R.id.tv_show_publish);
        findViewById(R.id.btn_resetore).setOnClickListener(this);

        toggle_FlipScreen = findViewById(R.id.toggle_FlipScreen);
        toggle_OSD = findViewById(R.id.toggle_OSD);
        toggle_MotionDetection = findViewById(R.id.toggle_MotionDetection);
        toggle_LocalRecording = findViewById(R.id.toggle_LocalRecording);

        layout_MotionSensitivity = findViewById(R.id.layout_MotionSensitivity);
        layout_RecordType = findViewById(R.id.layout_RecordType);
        layout_StorageSetting = findViewById(R.id.layout_StorageSetting);
        layout_ResetWifi = findViewById(R.id.layout_ResetWifi);

        toggle_FlipScreen.setOnCheckedChangeListener(this);
        toggle_OSD.setOnCheckedChangeListener(this);
        toggle_MotionDetection.setOnCheckedChangeListener(this);
        toggle_LocalRecording.setOnCheckedChangeListener(this);

        txt_NightVision = findViewById(R.id.txt_NightVision);
        txt_ChimeType = findViewById(R.id.txt_ChimeType);
        txt_RecordType = findViewById(R.id.txt_RecordType);
        txt_MotionSensitivity = findViewById(R.id.txt_MotionSensitivity);

        btn_RemoveDevice = findViewById(R.id.btn_RemoveDevice);

        txt_NightVision.setOnClickListener(this);
        txt_ChimeType.setOnClickListener(this);
        txt_RecordType.setOnClickListener(this);
        txt_MotionSensitivity.setOnClickListener(this);
        layout_StorageSetting.setOnClickListener(this);
        layout_ResetWifi.setOnClickListener(this);
        btn_RemoveDevice.setOnClickListener(this);

        ReactApplication rApp = (ReactApplication) getApplication();
        rnOperationHelper = new RNOperationHelper(rApp, SettingActivity.this, new RNOperationHelper.OperationCallback() {
            @Override
            public void onSuccess(RNOperationHelper.Operation operation) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "elango-RemoveDevice onSuccess-:" + devId);
                        hideProgressDialog();
                        setResult(RESULT_OK, null);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(final String message) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "elango-RemoveDevice onFailure-:" + message);
                        hideProgressDialog();

                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Error in removing the camera.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }

            @Override
            public void foundLock(ReadableMap lock) {

            }
        });

        initData();
        initDeviceControl();
        initAllDevicePointControl();
    }

    private void initDeviceControl() {
        devId = getIntent().getStringExtra("devId");
        mTuyaCameraDevice = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);
        mTuyaDevice = TuyaHomeSdk.newDeviceInstance(devId);
    }

    private void initAllDevicePointControl() {

        DeviceBean mCameraDevice =  TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        Log.d(TAG, "elango-mCameraDevice getDps:" + mCameraDevice.getDps());
        for (Map.Entry<String, Object> entry : mCameraDevice.getDps().entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());
            if(entry.getKey() != null && entry.getValue() != null)
                updateSetting(entry.getKey(), entry.getValue().toString());
        }


        mTuyaCameraDevice.setRegisterDevListener(new IDevListener()  {
            @Override
            public void onDpUpdate(String s, String s1) {
                L.d(TAG, "elango-mTuyaCameraDevice-onDpUpdate devId:" + s + "  dps " + s1);
                Log.d(TAG, "elango-mTuyaCameraDevice-onDpUpdate devId:" + s + "  dps " + s1);

                try {
                    JSONObject obj = new JSONObject(s1);
                    Iterator<String> keys = obj.keys();
                    String key = keys.next();
                    if(key != null && obj.getString(key) != null)
                        updateSetting(key, obj.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRemoved(String s) {

            }

            @Override
            public void onStatusChanged(String s, boolean b) {

            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {

            }

            @Override
            public void onDevInfoUpdate(String s) {

            }
        });

        mTuyaDevice.registerDevListener(new IDevListener()  {
            @Override
            public void onDpUpdate(String s, String s1) {
                L.d(TAG, "elango-mTuyaDevice-onDpUpdate devId:" + s + "  dps " + s1);
                Log.d(TAG, "elango-mTuyaDevice-onDpUpdate devId:" + s + "  dps " + s1);
                //此处监听所有dp点的信息
            }

            @Override
            public void onRemoved(String s) {

            }

            @Override
            public void onStatusChanged(String s, boolean b) {

            }

            @Override
            public void onNetworkStatusChanged(String s, boolean b) {

            }

            @Override
            public void onDevInfoUpdate(String s) {

            }
        });

        /*mTuyaDevice.publishDps("{\"165\": \"2\"}", new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, " publishDps - onError : " + error);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, " publishDps - onSuccess : ");
            }
        });*/

        /*List<String> list = new ArrayList<>();
        list.add(DpBasicFlip.ID);
        list.add(DpBasicNightvision.ID);
        list.add(DpBasicOSD.ID);
        list.add(DpMotionSwitch.ID);
        list.add(DpSDStorage.ID);
        list.add(DpSDRecordSwitch.ID);
        list.add(DpSDRecordModel.ID);
        list.add("165");
        mTuyaDevice.getDpList(list, new IResultCallback() {
            @Override
            public void onError(String code, String error) {

            }

            @Override
            public void onSuccess() {

            }
        });

        mTuyaDevice.getDp("165", new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, " getDp - onError : " + error);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, " getDp - onSuccess : ");
            }
        });*/
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(SettingActivity.this);
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateSetting(String key, String value) {
        Log.d(TAG, " elango-updateSetting : key-" + key + ", value-" + value);

        hideProgressDialog();
        if(key.equalsIgnoreCase(DpBasicFlip.ID)) {
            toggle_FlipScreen.setOnCheckedChangeListener (null); // removing listener
            toggle_FlipScreen.setChecked(Boolean.parseBoolean(value));
            toggle_FlipScreen.setOnCheckedChangeListener(this); // adding listener
        } else if(key.equalsIgnoreCase(DpBasicOSD.ID)) {
            toggle_OSD.setOnCheckedChangeListener (null); // removing listener
            toggle_OSD.setChecked(Boolean.parseBoolean(value));
            toggle_OSD.setOnCheckedChangeListener(this); // adding listener
        } else if(key.equalsIgnoreCase(DpMotionSwitch.ID)) {
            toggle_MotionDetection.setOnCheckedChangeListener (null); // removing listener
            toggle_MotionDetection.setChecked(Boolean.parseBoolean(value));
            toggle_MotionDetection.setOnCheckedChangeListener(this); // adding listener
            if(Boolean.parseBoolean(value)) {
                layout_MotionSensitivity.setVisibility(View.VISIBLE);
            } else {
                layout_MotionSensitivity.setVisibility(View.GONE);
            }
        } else if(key.equalsIgnoreCase(DpSDRecordSwitch.ID)) {
            toggle_LocalRecording.setOnCheckedChangeListener (null); // removing listener
            toggle_LocalRecording.setChecked(Boolean.parseBoolean(value));
            toggle_LocalRecording.setOnCheckedChangeListener(this); // adding listener
            if(Boolean.parseBoolean(value)) {
                layout_RecordType.setVisibility(View.VISIBLE);
            } else {
                layout_RecordType.setVisibility(View.GONE);
            }
        } else if(key.equalsIgnoreCase(DpBasicNightvision.ID)) {
            switch (Integer.parseInt(value)) {
                case 0: // Auto
                    txt_NightVision.setText("Auto");
                    break;
                case 1: // Off
                    txt_NightVision.setText("Off");
                    break;
                case 2: // On
                    txt_NightVision.setText("On");
                    break;
            }
        } else if(key.equalsIgnoreCase(DpMotionSensitivity.ID)) {
            switch (Integer.parseInt(value)) {
                case 0: // Low
                    txt_MotionSensitivity.setText("Low");
                    break;
                case 1: // Medium
                    txt_MotionSensitivity.setText("Medium");
                    break;
                case 2: // High
                    txt_MotionSensitivity.setText("High");
                    break;
            }
        } else if(key.equalsIgnoreCase("165")) {
            switch (Integer.parseInt(value)) {
                case 0: // none
                    txt_ChimeType.setText("Not Selected");
                    break;
                case 1: // Mechanical
                    txt_ChimeType.setText("Mechanical");
                    break;
                case 2: // Digital
                    txt_ChimeType.setText("Digital");
                    break;
                case 3: // No Bells
                    txt_ChimeType.setText("No Bells");
                    break;
            }
        } else if(key.equalsIgnoreCase(DpSDRecordModel.ID)) {
            switch (Integer.parseInt(value)) {
                case 1: // Event Recording
                    txt_RecordType.setText("Event Recording");
                    break;
                case 2: // Non-Stop
                    txt_RecordType.setText("Non-Stop");
                    break;
            }
        }
    }

    private void initData() {
        mData = new ArrayList<>();
        mData.add(DpSDStatus.ID);
        mData.add(DpBasicFlip.ID);
        mData.add(DpMotionSensitivity.ID);
        mData.add(DpBasicOSD.ID);
        mData.add(DpBasicIndicator.ID);
        mData.add(DpBasicPrivate.ID);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTuyaCameraDevice != null) {
            mTuyaCameraDevice.onDestroy();
        }
        if (mTuyaDevice != null) {
            mTuyaDevice.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.txt_NightVision == v.getId()) {
            openIRNightVisionDialog();
        } else if (R.id.txt_ChimeType == v.getId()) {
            openChimeTypeDialog();
        } else if (R.id.txt_RecordType == v.getId()) {
            openRecordingTypeDialog();
        } else if (R.id.txt_MotionSensitivity == v.getId()) {
            openMotionSensitivityDialog();
        } else if (R.id.layout_StorageSetting == v.getId()) {
            Intent intent1 = new Intent(SettingActivity.this, StorageSettingActivity.class);
            intent1.putExtra("devId", devId);
            //startActivityForResult(intent1, REQUEST_EXIT);
            startActivity(intent1);
        } else if (R.id.layout_ResetWifi == v.getId()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("Reset WiFi");
            builder.setMessage("Please go to Manage tab then Add Camera and follow the reset instruction video shown on Add Camera screen and add your camera again.");
            builder.setPositiveButton("Reset WiFi", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    setResult(RESULT_OK, null);
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (R.id.btn_RemoveDevice == v.getId()) {
            Log.d(TAG, "elango-RemoveDevice devId:" + devId);

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("Remove Device");
            builder.setMessage("After the device is disconnected, all the device related settings and data will be deleted.");
            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    rnOperationHelper.performOperation(RNOperationHelper.Operation.REMOVE_CAMERA, devId);
                    showProgressDialog();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void openIRNightVisionDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Night Vision");

        // add a list
        String[] strings = {"Auto", "On", "Off"};
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Auto
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.AUTO.getDpValue());
                        break;
                    case 1: // On
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.OPEN.getDpValue());
                        break;
                    case 2: // Off
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.CLOSE.getDpValue());
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openMotionSensitivityDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Motion Sensitivity");

        // add a list
        String[] strings = {"Low", "Medium", "High"};
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Low
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpMotionSensitivity.ID, MotionSensitivityMode.LOW.getDpValue());
                        break;
                    case 1: // Medium
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpMotionSensitivity.ID, MotionSensitivityMode.MIDDLE.getDpValue());
                        break;
                    case 2: // High
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpMotionSensitivity.ID, MotionSensitivityMode.HIGH.getDpValue());
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openChimeTypeDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Chime Type");

        // add a list
        String[] strings = {"Mechanical", "Digital", "No Bells"};
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Mechanical
                        showProgressDialog();
                        mTuyaDevice.publishDps("{\"165\": \"1\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d(TAG, " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d(TAG, " publishDps - onSuccess : ");
                            }
                        });
                        break;
                    case 1: // Digital
                        showProgressDialog();
                        mTuyaDevice.publishDps("{\"165\": \"2\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d(TAG, " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d(TAG, " publishDps - onSuccess : ");
                            }
                        });
                        break;
                    case 2: // No Bells
                        showProgressDialog();
                        mTuyaDevice.publishDps("{\"165\": \"3\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d(TAG, " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d(TAG, " publishDps - onSuccess : ");
                            }
                        });
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openRecordingTypeDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Recording Type");

        // add a list
        String[] strings = {"Event Recording", "Non-Stop"};
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Event Recording
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.getDpValue());
                        break;
                    case 1: // Non-Stop
                        showProgressDialog();
                        mTuyaCameraDevice.publishCameraDps(DpSDRecordModel.ID, RecordMode.CONTINUOUS_RECORD.getDpValue());
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (R.id.toggle_FlipScreen == buttonView.getId()) {
            showProgressDialog();
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpBasicFlip.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpBasicFlip.ID, false);
            }
        } else if (R.id.toggle_OSD == buttonView.getId()) {
            showProgressDialog();
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpBasicOSD.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpBasicOSD.ID, false);
            }
        } else if (R.id.toggle_MotionDetection == buttonView.getId()) {
            showProgressDialog();
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, true);
                layout_MotionSensitivity.setVisibility(View.VISIBLE);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, false);
                layout_MotionSensitivity.setVisibility(View.GONE);
            }
        } else if (R.id.toggle_LocalRecording == buttonView.getId()) {
            showProgressDialog();
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpSDRecordSwitch.ID, true);
                layout_RecordType.setVisibility(View.VISIBLE);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpSDRecordSwitch.ID, false);
                layout_RecordType.setVisibility(View.GONE);
            }
        }
    }

    /*static final int REQUEST_EXIT = 102;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EXIT) {
            if (resultCode == RESULT_OK) {

                this.finish();
            }
        }
    }*/
}
