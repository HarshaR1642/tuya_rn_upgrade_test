package com.tuya.smart.rnsdk.camera.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.camera.api.bean.CameraPushDataBean;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.network.http.BusinessResponse;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnRenderDirectionCallback;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.bean.ConfigCameraBean;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ICameraConfig;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.utils.Constants;
import com.tuya.smart.rnsdk.camera.utils.ToastUtil;
import com.tuya.smart.sdk.api.ITuyaGetBeanCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;
import com.tuyasmart.camera.devicecontrol.bean.DpPTZControl;
import com.tuyasmart.camera.devicecontrol.bean.DpPTZStop;
import com.tuyasmart.camera.devicecontrol.model.PTZDirection;
import com.tuyasmart.stencil.utils.MessageUtil;
import com.tuyasmart.stencil.utils.PreferencesUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CameraLivePreviewActivity extends AppCompatActivity  implements OnP2PCameraListener, View.OnClickListener, TuyaCameraView.CreateVideoViewCallback {

    private static final String TAG = "CameraLivePrevActivity";
    private Toolbar toolbar;
    private TuyaCameraView mVideoView;
    //private ImageView muteImg;
    private TextView muteImg;
    private TextView qualityTv;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt, settingTxt, cloudStorageTxt,messageCenterTxt;

    private ICameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", p2pWd = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;

    private String picPath, videoPath;

    private boolean mIsRunSoft;
    private int sdkProvider;

    private String devId;
    private ITuyaCameraDevice mDeviceControl;
    private ITuyaSmartCameraP2P mSmartCameraP2P;


    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTENT_LOCALKEY = "intent_localkey";
    public static final String INTENT_SDK_POROVIDER = "intent_sdk_provider";
    public static final String INTENT_HOME_ID = "intent_home_id";
    public static final String INTENT_COUNTRY_CODE = "intent_country_code";
    public static final String INTENT_UID = "intent_uid";
    public static final String INTENT_PASSWD = "intent_passwd";

    public static final int MSG_LOGIN_SUCCESS = 15;
    public static final int MSG_LOGIN_FAILURE = 16;
    private static ITuyaHomeCamera homeCamera;

    public static  long HOME_ID = 1099001;
    public static DeviceBean mCameraDevice;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_CREATE_DEVICE:
                    handleCreateDevice(msg);
                    break;
                case Constants.MSG_CONNECT:
                    handleConnect(msg);
                    break;
                case Constants.MSG_GET_CLARITY:
                    handleClarity(msg);
                    break;
                case Constants.MSG_MUTE:
                    handleMute(msg);
                    break;
                case Constants.MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case Constants.MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(CameraLivePreviewActivity.this, "record start success");
                    break;
                case Constants.MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(CameraLivePreviewActivity.this, "record start fail");
                    break;
                case Constants.MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
                case Constants.MSG_TALK_BACK_BEGIN:
                    handleStartTalk(msg);
                    break;
                case Constants.MSG_TALK_BACK_OVER:
                    handleStopTalk(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleStopTalk(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "stop talk success" + msg.obj);
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }

    private void handleStartTalk(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "start talk success" + msg.obj);
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "record success " + msg.obj);
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "snapshot success " + msg.obj);
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }


    private void handleClarity(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            qualityTv.setText(videoClarity == ICameraP2P.HD ? "HD" : "SD");
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "operation fail");
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "connect fail");
        }
    }


    private void handleCreateDevice(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            connect();
        } else {
            ToastUtil.shortToast(CameraLivePreviewActivity.this, "create device fail");
        }
    }


    /**
     * the lower power Doorbell device change to true
     */
    private boolean isDoorbell = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_live_preview);
        String countryCode = getIntent().getStringExtra(INTENT_COUNTRY_CODE);
        String uid = getIntent().getStringExtra(INTENT_UID);
        String passwd = getIntent().getStringExtra(INTENT_PASSWD);
        initView();
        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, passwd, mLoginCallback);
        afterLogin();

        if(mDeviceControl != null && mDeviceControl.isSupportCameraDps(DpPTZControl.ID)) {
            mVideoView.setOnRenderDirectionCallback(new OnRenderDirectionCallback() {

                @Override
                public void onLeft() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID, PTZDirection.LEFT.getDpValue());
                }

                @Override
                public void onRight() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID,PTZDirection.RIGHT.getDpValue());

                }

                @Override
                public void onUp() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID,PTZDirection.UP.getDpValue());

                }

                @Override
                public void onDown() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID,PTZDirection.DOWN.getDpValue());

                }

                @Override
                public void onCancel() {
                    mDeviceControl.publishCameraDps(DpPTZStop.ID,true);

                }
            });
        }
    }

    public static void afterLogin() {

        //there is the somethings that need to set.For example the lat and lon;
        //   TuyaSdk.setLatAndLong();
        homeCamera = TuyaHomeSdk.getCameraInstance();
        if (homeCamera != null) {
            homeCamera.registerCameraPushListener(mTuyaGetBeanCallback);
        }
    }

    private static ITuyaGetBeanCallback<CameraPushDataBean> mTuyaGetBeanCallback = new ITuyaGetBeanCallback<CameraPushDataBean>() {
        @Override
        public void onResult(CameraPushDataBean o) {
            L.d(TAG, "onMqtt_43_Result on callback");
            L.d(TAG, "timestamp=" + o.getTimestamp());
            L.d(TAG, "devid=" + o.getDevId());
            L.d(TAG, "msgid=" + o.getEdata());
            L.d(TAG, "etype=" + o.getEtype());

        }
    };

    private void initView() {
        //toolbar = findViewById(R.id.toolbar_view);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
        mVideoView = findViewById(R.id.camera_video_view);
        muteImg = findViewById(R.id.camera_mute);
        qualityTv = findViewById(R.id.camera_quality);
        speakTxt = findViewById(R.id.speak_Txt);
        recordTxt = findViewById(R.id.record_Txt);
        photoTxt = findViewById(R.id.photo_Txt);
        replayTxt = findViewById(R.id.replay_Txt);
        settingTxt = findViewById(R.id.setting_Txt);
        settingTxt.setOnClickListener(this);
        cloudStorageTxt = findViewById(R.id.cloud_Txt);
        messageCenterTxt =  findViewById(R.id.message_center_Txt);

        /*WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        // layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);*/

        muteImg.setSelected(true);
    }

    private void initData() {
        try {

            devId = getIntent().getStringExtra(INTENT_DEVID);
            mCameraDevice =  TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
            localKey = mCameraDevice.getLocalKey();
            Map<String, Object> map = mCameraDevice.getSkills();
            int p2pType = -1;
            if (map == null || map.size() == 0) {
                p2pType = -1;
            } else {
                p2pType = (Integer) (map.get("p2pType"));
            }


            // 拿到的是p2pType ，需要转化成 sdkProvider
            int intentP2pType = p2pType;
            mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", true);
            mCameraP2P = TuyaSmartCameraP2PFactory.generateTuyaSmartCamera(intentP2pType);
            mVideoView.setCameraViewCallback(this);
            sdkProvider = intentP2pType == 1 ? 1 : 2;
            mVideoView.createVideoView(sdkProvider);
            if (null == mCameraP2P) {
                showNotSupportToast();
            } else {
                mCameraP2P.isEchoData(true);
                mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);

                initListener();
                getApi();
            }
        } catch (Exception ex){
            Log.d("INFO ", "exception"+ex);
        }
    }

    private ILoginCallback mLoginCallback = new ILoginCallback() {
        @Override
        public void onSuccess(User user) {
            mHandler.sendEmptyMessage(MSG_LOGIN_SUCCESS);
            getDataFromServer();
        }

        @Override
        public void onError(String s, String s1) {
            Message msg = MessageUtil.getCallFailMessage(MSG_LOGIN_FAILURE, s, s1);
            mHandler.sendMessage(msg);
        }
    };

    public void getDataFromServer() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                if (homeBeans.size() == 0) {
                    // mView.gotoCreateHome();
                    return;
                }

                final long homeId = homeBeans.get(0).getHomeId();

                HOME_ID = homeId;
                PreferencesUtil.set("homeId", HOME_ID);
                TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {
                        initData();
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
                TuyaHomeSdk.newHomeInstance(homeId).registerHomeStatusListener(new ITuyaHomeStatusListener() {
                    @Override
                    public void onDeviceAdded(String devId) {

                    }

                    @Override
                    public void onDeviceRemoved(String devId) {

                    }

                    @Override
                    public void onGroupAdded(long groupId) {

                    }

                    @Override
                    public void onGroupRemoved(long groupId) {

                    }

                    @Override
                    public void onMeshAdded(String meshId) {
                        L.d(TAG, "onMeshAdded: " + meshId);
                    }


                });

            }

            @Override
            public void onError(String errorCode, String error) {
                TuyaHomeSdk.newHomeInstance(HOME_ID).getHomeLocalCache(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {
                        L.d(TAG, com.alibaba.fastjson.JSONObject.toJSONString(bean));
                        Log.d("TAG", bean.getDeviceList().toString());
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
            }
        });
    }

    private void showNotSupportToast() {
        ToastUtil.shortToast(CameraLivePreviewActivity.this, "device is not support!");
    }

    private void initCameraView(ConfigCameraBean bean) {
        mCameraP2P.createDevice(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

                Log.d(TAG, "init camera view onsuccess");
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_CREATE_DEVICE, Constants.ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_CREATE_DEVICE, Constants.ARG1_OPERATE_FAIL));
            }
        },bean);
    }


    private void connect() {

        mCameraP2P.connect(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_CONNECT, Constants.ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_CONNECT, Constants.ARG1_OPERATE_FAIL, errCode));
            }
        });
    }


    private void preview() {
        mCameraP2P.startPreview(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.d(TAG, "start preview onSuccess");

                // mVideoView.onResume();
                isPlay = true;
                if (null != mCameraP2P){
                    AudioUtils.getModel(CameraLivePreviewActivity.this);
                    mCameraP2P.registorOnP2PCameraListener(CameraLivePreviewActivity.this);
                    mCameraP2P.generateCameraView(mVideoView.createdView());
                }
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                isPlay = false;
            }
        });
    }

    private void getApi() {
        try {
            Map postData = new HashMap();
            postData.put("devId", devId);
            mSmartCameraP2P = new TuyaSmartCameraP2P();

            mSmartCameraP2P.requestCameraInfo(devId, new ICameraConfig() {
                @Override
                public void onFailure(BusinessResponse var1, ConfigCameraBean var2, String var3) {
                    ToastUtil.shortToast(CameraLivePreviewActivity.this, "get cameraInfo failed");
                }

                @Override
                public void onSuccess(BusinessResponse var1, ConfigCameraBean var2, String var3) {

                    p2pWd = var2.getPassword();
                    p2pId = var2.getP2pId();
                    initCameraView(var2);
                }
            });
        } catch (Exception ex) {
            Log.d(TAG, "error "+ex.getMessage());
        }
    }

    private void initListener() {
        if (mCameraP2P == null) return;

        muteImg.setOnClickListener(this);
        qualityTv.setOnClickListener(this);
        speakTxt.setOnClickListener(this);
        recordTxt.setOnClickListener(this);
        photoTxt.setOnClickListener(this);
        replayTxt.setOnClickListener(this);

        cloudStorageTxt.setOnClickListener(this);
        messageCenterTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_mute) {
            muteClick();
        } else if (id == R.id.camera_quality) {
            setVideoClarity();
        } else if (id == R.id.speak_Txt) {
            speakClick();
        } else if (id == R.id.record_Txt) {
            recordClick();
        } else if (id == R.id.photo_Txt) {
            snapShotClick();
        } else if (id == R.id.replay_Txt) {
//            Intent intent = new Intent(CameraLivePreviewActivity.this, CameraPlaybackActivity.class);
//            intent.putExtra("isRunsoft", mIsRunSoft);
//            intent.putExtra("p2pId", p2pId);
//            intent.putExtra("p2pWd", p2pWd);
//            intent.putExtra("localKey", localKey);
//            intent.putExtra("p2pType", sdkProvider);
//            startActivity(intent);
        } else if (id == R.id.setting_Txt) {
//            Intent intent1 = new Intent(CameraLivePreviewActivity.this, SettingActivity.class);
//            intent1.putExtra("devId", devId);
//            startActivity(intent1);
        } else if (id == R.id.cloud_Txt) {
//            if (sdkProvider == SDK_PROVIDER_V1) {
//                showNotSupportToast();
//                return;
//            }
//            Intent intent2 = new Intent(CameraLivePreviewActivity.this, CameraCloudStorageActivity.class);
//            intent2.putExtra(INTENT_DEVID, devId);
//            intent2.putExtra(INTENT_SDK_POROVIDER, sdkProvider);
//            intent2.putExtra(INTENT_HOME_ID, HOME_ID);
//            startActivity(intent2);
        } else if (id == R.id.message_center_Txt) {//                Intent intent3 = new Intent(CameraLivePreviewActivity.this, AlarmDetectionActivity.class);
//                intent3.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, devId);
//                startActivity(intent3);
        }
    }

    private void recordClick() {
        if (!isRecording) {
            if (Constants.hasStoragePermission()) {
                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                File file = new File(picPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String fileName = System.currentTimeMillis() + ".mp4";
                videoPath = picPath + fileName;
                mCameraP2P.startRecordLocalMp4(picPath, fileName, CameraLivePreviewActivity.this, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isRecording = true;
                        mHandler.sendEmptyMessage(Constants.MSG_VIDEO_RECORD_BEGIN);

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendEmptyMessage(Constants.MSG_VIDEO_RECORD_FAIL);
                    }
                });
                recordStatue(true);
            } else {
                Constants.requestPermission(CameraLivePreviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "open_storage");
            }
        } else {
            mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_VIDEO_RECORD_OVER, Constants.ARG1_OPERATE_SUCCESS, data));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_VIDEO_RECORD_OVER, Constants.ARG1_OPERATE_FAIL));
                }
            });
            recordStatue(false);
        }
    }

    private void snapShotClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            picPath = path;
        }
        mCameraP2P.snapshot(picPath, CameraLivePreviewActivity.this, ICameraP2P.PLAYMODE.LIVE, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_SCREENSHOT, Constants.ARG1_OPERATE_SUCCESS, data));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_SCREENSHOT, Constants.ARG1_OPERATE_FAIL));
            }
        });
    }

    private void muteClick() {
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(ICameraP2P.PLAYMODE.LIVE, mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                previewMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_MUTE, Constants.ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_MUTE, Constants.ARG1_OPERATE_FAIL));
            }
        });
    }

    private void speakClick() {
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_TALK_BACK_OVER, Constants.ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_TALK_BACK_OVER, Constants.ARG1_OPERATE_FAIL));

                }
            });
        } else {
            if (Constants.hasRecordPermission()) {
                mCameraP2P.setEchoData(true);
                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isSpeaking = true;
                        mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_TALK_BACK_BEGIN, Constants.ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isSpeaking = false;
                        mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_TALK_BACK_BEGIN, Constants.ARG1_OPERATE_FAIL));
                    }
                });
            } else {
                Constants.requestPermission(CameraLivePreviewActivity.this, Manifest.permission.RECORD_AUDIO, Constants.EXTERNAL_AUDIO_REQ_CODE, "open_recording");
            }
        }
    }

    private void setVideoClarity() {
        mCameraP2P.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                videoClarity = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_GET_CLARITY, Constants.ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(Constants.MSG_GET_CLARITY, Constants.ARG1_OPERATE_FAIL));
            }
        });

    }

    private void recordStatue(boolean isRecording) {
        speakTxt.setEnabled(!isRecording);
        photoTxt.setEnabled(!isRecording);
        replayTxt.setEnabled(!isRecording);
        recordTxt.setEnabled(true);
        recordTxt.setSelected(isRecording);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        //must register again,or can't callback
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registorOnP2PCameraListener(this);
            mCameraP2P.generateCameraView(mVideoView.createdView());
            if (mCameraP2P.isConnecting()) {
                mCameraP2P.startPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlay = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(null);
        }
        if (isPlay) {
            mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
            isPlay = false;
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
        }
        AudioUtils.changeToNomal(this);
        if (mSmartCameraP2P != null) {
            mSmartCameraP2P.destroyCameraBusiness();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCameraP2P) {
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
        }
        TuyaSmartCameraP2PFactory.onDestroyTuyaSmartCamera();
    }


    @Override
    public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

    }

    @Override
    public void onReceiveFrameYUVData(int sessionId, ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height, int nFrameRate, int nIsKeyFrame, long timestamp, long nProgress, long nDuration, Object camera) {
    }


    @Override
    public void onSessionStatusChanged(Object o, int i, int i1) {

    }


    @Override
    public void onReceiveSpeakerEchoData(ByteBuffer pcm, int sampleRate) {
        if (null != mCameraP2P){
            int length = pcm.capacity();
            Log.d(TAG, "receiveSpeakerEchoData pcmlength " + length + " sampleRate " + sampleRate);
            byte[] pcmData = new byte[length];
            pcm.get(pcmData, 0, length);
            mCameraP2P.sendAudioTalkData(pcmData,length);
        }
    }

    @Override
    public void onCreated(Object view) {
        if (null != mCameraP2P){
            mCameraP2P.generateCameraView(view);
        }
    }

    @Override
    public void videoViewClick() {

    }

    @Override
    public void startCameraMove(PTZDirection cameraDirection) {

    }

    @Override
    public void onActionUP() {

    }
}

