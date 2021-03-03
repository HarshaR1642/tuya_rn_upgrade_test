package com.tuya.smart.rnsdk.camera.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
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

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private List<String> mData;
    ITuyaCameraDevice mTuyaCameraDevice;
    ITuyaDevice mTuyaDevice;
    private Toolbar toolbar;
    private TextView showQueryTxt;
    private TextView showPublishTxt;
    private String devId;

    private ToggleButton toggle_FlipScreen, toggle_OSD, toggle_MotionDetection, toggle_LocalRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
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

        toggle_FlipScreen.setOnCheckedChangeListener(this);
        toggle_OSD.setOnCheckedChangeListener(this);
        toggle_MotionDetection.setOnCheckedChangeListener(this);
        toggle_LocalRecording.setOnCheckedChangeListener(this);

        findViewById(R.id.txt_NightVision).setOnClickListener(this);
        findViewById(R.id.txt_ChimeType).setOnClickListener(this);
        findViewById(R.id.txt_RecordType).setOnClickListener(this);
        /*findViewById(R.id.btn_sdstatus).setOnClickListener(this);
        findViewById(R.id.btn_storage).setOnClickListener(this);
        findViewById(R.id.btn_basic_flip).setOnClickListener(this);
        findViewById(R.id.btn_motion_sensitivity).setOnClickListener(this);
        findViewById(R.id.btn_sd_format).setOnClickListener(this);
        findViewById(R.id.btn_sd_format_status).setOnClickListener(this);
        findViewById(R.id.btn_record_switch).setOnClickListener(this);
        findViewById(R.id.btn_record_model).setOnClickListener(this);
        findViewById(R.id.btn_wireless_powermode).setOnClickListener(this);
        findViewById(R.id.btn_wireless_lowpower).setOnClickListener(this);
        findViewById(R.id.btn_wireless_batterylock).setOnClickListener(this);
        findViewById(R.id.btn_wireless_electricity).setOnClickListener(this);*/
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
        mTuyaCameraDevice.setRegisterDevListener(new IDevListener()  {
            @Override
            public void onDpUpdate(String s, String s1) {
                L.d("SettingActivity", "elango-mTuyaCameraDevice-onDpUpdate devId:" + s + "  dps " + s1);
                Log.d("SettingActivity", "elango-mTuyaCameraDevice-onDpUpdate devId:" + s + "  dps " + s1);
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

        mTuyaDevice.registerDevListener(new IDevListener()  {
            @Override
            public void onDpUpdate(String s, String s1) {
                L.d("SettingActivity", "elango-mTuyaDevice-onDpUpdate devId:" + s + "  dps " + s1);
                Log.d("SettingActivity", "elango-mTuyaDevice-onDpUpdate devId:" + s + "  dps " + s1);
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

        List<String> list = new ArrayList<>();
        list.add("165");
        list.add(DpBasicFlip.ID);
        list.add(DpBasicNightvision.ID);
        list.add(DpBasicOSD.ID);
        list.add(DpMotionSwitch.ID);
        list.add(DpSDStorage.ID);
        list.add(DpSDRecordSwitch.ID);
        list.add(DpSDRecordModel.ID);
        mTuyaDevice.getDpList(list, new IResultCallback() {
            @Override
            public void onError(String code, String error) {

            }

            @Override
            public void onSuccess() {

            }
        });
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
        /*if (R.id.btn_sdstatus == v.getId()) {
            *//*if (mTuyaCameraDevice.isSupportCameraDps(DpSDStatus.ID)) {
                int o = mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDStatus.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDStatus.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDStatus.ID, null);
            }*//*
            if (mTuyaCameraDevice.isSupportCameraDps("155")) {
                int o = mTuyaCameraDevice.queryIntegerCurrentCameraDps("155");
                showQueryTxt.setText("local query result: " + o);
                *//*mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDStatus.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });*//*
                mTuyaCameraDevice.publishCameraDps("155", 1);
                //mTuyaDevice.publishDps("155", 1);
            }
        } else if (R.id.btn_basic_flip == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpBasicFlip.ID)) {
                boolean o = mTuyaCameraDevice.queryBooleanCameraDps(DpBasicFlip.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpBasicFlip.ID, new ITuyaCameraDeviceControlCallback<Boolean>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Boolean o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpBasicFlip.ID, true);
            }
        } else if (R.id.btn_motion_sensitivity == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpMotionSensitivity.ID)) {

                *//*mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, true);
                String o1 = mTuyaCameraDevice.queryStringCurrentCameraDps(DpMotionSwitch.ID);
                showQueryTxt.setText("DpMotionSwitch: " + o1);*//*

                String o = mTuyaCameraDevice.queryStringCurrentCameraDps(DpMotionSensitivity.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpMotionSensitivity.ID, new ITuyaCameraDeviceControlCallback<String>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpMotionSensitivity.ID, MotionSensitivityMode.HIGH.getDpValue());
                //mTuyaDevice.publishDps("155", );
            }
        } else if (R.id.btn_storage == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpSDStorage.ID)) {
                String o = mTuyaCameraDevice.queryStringCurrentCameraDps(DpSDStorage.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDStorage.ID, new ITuyaCameraDeviceControlCallback<String>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDStorage.ID, null);
            }
        } else if (R.id.btn_sd_format == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpSDFormat.ID)) {
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormat.ID, new ITuyaCameraDeviceControlCallback<Boolean>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Boolean o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDFormat.ID, true);
            }
        } else if (R.id.btn_sd_format_status == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpSDFormatStatus.ID)) {
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormatStatus.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
            }
        } else if (R.id.btn_record_switch == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpSDRecordSwitch.ID)) {
                boolean o = mTuyaCameraDevice.queryBooleanCameraDps(DpSDRecordSwitch.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDRecordSwitch.ID, new ITuyaCameraDeviceControlCallback<Boolean>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Boolean o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDRecordSwitch.ID, true);
            }
        } else if (R.id.btn_record_model == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpSDRecordModel.ID)) {
                String o = mTuyaCameraDevice.queryStringCurrentCameraDps(DpSDRecordModel.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDRecordModel.ID, new ITuyaCameraDeviceControlCallback<String>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.getDpValue());
            }
        } else if (R.id.btn_wireless_batterylock == v.getId()) {
            if (mTuyaCameraDevice.isSupportCameraDps(DpWirelessBatterylock.ID)) {
                boolean o = mTuyaCameraDevice.queryBooleanCameraDps(DpWirelessBatterylock.ID);
                showQueryTxt.setText("local query result: " + o);
                mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpWirelessBatterylock.ID, new ITuyaCameraDeviceControlCallback<Boolean>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Boolean o) {
                        showPublishTxt.setText("LAN/Cloud query result: " + o);
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mTuyaCameraDevice.publishCameraDps(DpWirelessBatterylock.ID, true);
            }
        } else if (R.id.btn_wireless_electricity == v.getId()) {
            int o = mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpWirelessElectricity.ID);
            showQueryTxt.setText("local query result: " + o);
            mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpWirelessElectricity.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                @Override
                public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                    showPublishTxt.setText("LAN/Cloud query result: " + o);
                }

                @Override
                public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                }
            });
            mTuyaCameraDevice.publishCameraDps(DpWirelessElectricity.ID, null);
        } else if (R.id.btn_wireless_lowpower == v.getId()) {
            int o = mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpWirelessLowpower.ID);
            showQueryTxt.setText("local query result: " + o);
            mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpWirelessLowpower.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                @Override
                public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                    showPublishTxt.setText("LAN/Cloud query result: " + o);
                }

                @Override
                public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                }
            });
            mTuyaCameraDevice.publishCameraDps(DpWirelessLowpower.ID, 20);
        } else if (R.id.btn_wireless_powermode == v.getId()) {
            String o = mTuyaCameraDevice.queryStringCurrentCameraDps(DpWirelessPowermode.ID);
            showQueryTxt.setText("local query result: " + o);
            mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpWirelessPowermode.ID, new ITuyaCameraDeviceControlCallback<String>() {
                @Override
                public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String o) {
                    showPublishTxt.setText("LAN/Cloud query result: " + o);
                }

                @Override
                public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                }
            });
            mTuyaCameraDevice.publishCameraDps(DpWirelessPowermode.ID, null);
        } else if (R.id.btn_resetore == v.getId()) {
            //mTuyaCameraDevice.publishCameraDps(DpRestore.ID, true);
            *//*mTuyaDevice.queryData("165", new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    Log.d("SettingsActivity", " queryData - onError : " + error);
                }

                @Override
                public void onSuccess() {

                }
            });*//*
            mTuyaDevice.getDp("165", new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    Log.d("SettingActivity", " getDp - onError : " + error);
                }

                @Override
                public void onSuccess() {
                    Log.d("SettingActivity", " getDp - onSuccess : ");
                }
            });
            mTuyaDevice.publishDps("{\"165\": \"1\"}", new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    Log.d("SettingActivity", " publishDps - onError : " + error);
                }

                @Override
                public void onSuccess() {
                    Log.d("SettingActivity", " publishDps - onSuccess : ");
                }
            });
        }*/
        if (R.id.txt_NightVision == v.getId()) {
            openIRNightVisionDialog();
        } else if (R.id.txt_ChimeType == v.getId()) {
            openChimeTypeDialog();
        } else if (R.id.txt_RecordType == v.getId()) {
            openRecordingTypeDialog();
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
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.AUTO.getDpValue());
                        break;
                    case 1: // On
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.OPEN.getDpValue());
                        break;
                    case 2: // Off
                        mTuyaCameraDevice.publishCameraDps(DpBasicNightvision.ID, NightStatusMode.CLOSE.getDpValue());
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
        String[] strings = {"Mechanical", "Wireless", "No Bells"};
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Mechanical
                        mTuyaDevice.publishDps("{\"165\": \"1\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d("SettingActivity", " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d("SettingActivity", " publishDps - onSuccess : ");
                            }
                        });
                        break;
                    case 1: // Wireless
                        mTuyaDevice.publishDps("{\"165\": \"2\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d("SettingActivity", " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d("SettingActivity", " publishDps - onSuccess : ");
                            }
                        });
                        break;
                    case 2: // No Bells
                        mTuyaDevice.publishDps("{\"165\": \"3\"}", new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                //Log.d("SettingActivity", " publishDps - onError : " + error);
                            }

                            @Override
                            public void onSuccess() {
                                //Log.d("SettingActivity", " publishDps - onSuccess : ");
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
                        mTuyaCameraDevice.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.getDpValue());
                        break;
                    case 1: // Non-Stop
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
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpBasicFlip.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpBasicFlip.ID, false);
            }
        } else if (R.id.toggle_OSD == buttonView.getId()) {
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpBasicOSD.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpBasicOSD.ID, false);
            }
        } else if (R.id.toggle_MotionDetection == buttonView.getId()) {
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, false);
            }
        } else if (R.id.toggle_LocalRecording == buttonView.getId()) {
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpSDRecordSwitch.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpSDRecordSwitch.ID, false);
            }
        } else if (R.id.toggle_MotionDetection == buttonView.getId()) {
            if(isChecked) {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, true);
            } else {
                mTuyaCameraDevice.publishCameraDps(DpMotionSwitch.ID, false);
            }
        }
    }
}
