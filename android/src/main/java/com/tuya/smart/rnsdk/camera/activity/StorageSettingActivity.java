package com.tuya.smart.rnsdk.camera.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.utils.ToastUtil;
import com.tuya.smart.sdk.api.IDevListener;
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
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormat;
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormatStatus;
import com.tuyasmart.camera.devicecontrol.bean.DpSDStatus;
import com.tuyasmart.camera.devicecontrol.bean.DpSDStorage;
import com.tuyasmart.camera.devicecontrol.model.DpNotifyModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StorageSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StorageSettingActivity";
    private List<String> mData;
    ITuyaCameraDevice mTuyaCameraDevice;
    ITuyaDevice mTuyaDevice;
    private Toolbar toolbar;
    private TextView showQueryTxt;
    private TextView showPublishTxt;
    private String devId;

    private TextView txt_Total, txt_Used, txt_Remaining;
    private Button btn_Format;

    private Handler handler;
    private ProgressDialog progressDialog;
    //private RNOperationHelper rnOperationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_setting);

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

        txt_Total = findViewById(R.id.txt_Total);
        txt_Used = findViewById(R.id.txt_Used);
        txt_Remaining = findViewById(R.id.txt_Remaining);

        btn_Format = findViewById(R.id.btn_Format);

        btn_Format.setOnClickListener(this);

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

        mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDStorage.ID, new ITuyaCameraDeviceControlCallback<String>() {
            @Override
            public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String o) {
                showPublishTxt.setText("LAN/Cloud query result: " + o);
                Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-onSuccess : " + s + ", " + o);
                updateSetting(DpSDStorage.ID, o);

            }

            @Override
            public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {
                Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-onFailure : " + s + ", " + s1 + ", " + s2);
            }
        });
        mTuyaCameraDevice.publishCameraDps(DpSDStorage.ID, null);

    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(StorageSettingActivity.this);
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateSetting(String key, String value) {
        Log.d(TAG, " elango-updateSetting : key-" + key + ", value-" + value);

        if(key.equalsIgnoreCase(DpSDStorage.ID)) {
            //{"109":"61706240|2957312|58748928"}
            if (value.contains("|")) {
                String[] arr = value.split("\\|");
                if(arr.length == 3) {
                    double l1 = Long.parseLong(arr[0]);
                    double lgb1 = l1 / 1024 / 1024;
                    txt_Total.setText(String.format("%.1fG", lgb1));

                    double l2 = Long.parseLong(arr[1]);
                    double lgb2 = l2 / 1024 / 1024;
                    txt_Used.setText(String.format("%.1fG", lgb2));

                    double l3 = Long.parseLong(arr[2]);
                    double lgb3 = l3 / 1024 / 1024;
                    txt_Remaining.setText(String.format("%.1fG", lgb3));
                }
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
        if (R.id.btn_Format == v.getId()) {
            Log.d(TAG, "elango-Format devId:" + devId);

            AlertDialog.Builder builder = new AlertDialog.Builder(StorageSettingActivity.this);
            builder.setTitle("Format SD Card ?");
            builder.setMessage("Formatting the SD Card will erase all your saved videos");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    showProgressDialog();

                    //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDStatus.ID));

                    mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormat.ID, new ITuyaCameraDeviceControlCallback<Boolean>() {
                        @Override
                        public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Boolean o) {
                            showPublishTxt.setText("LAN/Cloud query result: " + o);
                            Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onSuccess : " + s + ", " + o);
                            /*hideProgressDialog();
                            ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");*/

                            //handleFormatting();
                            new SDCardFormatting().execute();
                        }

                        @Override
                        public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {
                            Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onFailure : " + s + ", " + s1 + ", " + s2);
                            hideProgressDialog();
                            ToastUtil.shortToast(StorageSettingActivity.this,"Format failed.");
                        }
                    });
                    mTuyaCameraDevice.publishCameraDps(DpSDFormat.ID, true);

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

    int formatStatus = 0;
    private void handleFormatting() {
        formatStatus = 0;
        mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormatStatus.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
            @Override
            public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                //showPublishTxt.setText("LAN/Cloud query result: " + o);
                Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onSuccess : " + s + ", " + o);
                //hideProgressDialog();
                //ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");

                formatStatus = o;
            }

            @Override
            public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {
                Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onFailure : " + s + ", " + s1 + ", " + s2);
                //hideProgressDialog();
                //ToastUtil.shortToast(StorageSettingActivity.this,"Format failed.");

                formatStatus = -1;
            }
        });
        mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
        //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus");

        // 30 second timeout
        int i = 30;
        while(i>0) {
            if (formatStatus >= 0 && formatStatus < 100) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
                //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDFormatStatus.ID));

            } else if (formatStatus == 100) {

                break;
            } else {
                break;
            }
            i--;
        }
        hideProgressDialog();
        ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");
    }

    class SDCardFormatting extends AsyncTask<Void, Void, Integer> {

        //int formatStatus = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            formatStatus = 0;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormatStatus.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                @Override
                public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                    //showPublishTxt.setText("LAN/Cloud query result: " + o);
                    Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onSuccess : " + s + ", " + o);
                    //hideProgressDialog();
                    //ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");

                    formatStatus = o;
                }

                @Override
                public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {
                    Log.d(TAG, "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onFailure : " + s + ", " + s1 + ", " + s2);
                    //hideProgressDialog();
                    //ToastUtil.shortToast(StorageSettingActivity.this,"Format failed.");

                    formatStatus = -1;
                }
            });
            //mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
            //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus");

            // 30 second timeout
            int i = 30;
            while(i>0) {
                if (formatStatus >= 0 && formatStatus < 100) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
                    //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDStatus.ID));
                    //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryStringCurrentCameraDps(DpSDStorage.ID));
                    //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDFormatStatus.ID));


                } else if (formatStatus == 100) {

                    break;
                } else {
                    break;
                }
                i--;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            hideProgressDialog();
            ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");
        }
    }
}
