package com.tuya.smart.rnsdk.camera.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.bean.RecordInfoBean;
import com.tuya.smart.rnsdk.camera.bean.TimePieceBean;
import com.tuya.smart.rnsdk.camera.utils.Constants;
import com.tuya.smart.rnsdk.utils.MessageUtil;
import com.tuya.smart.rnsdk.utils.ToastUtil;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.bean.MonthDays;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.rnsdk.camera.adapter.CameraPlaybackTimeAdapter;
import com.tuyasmart.camera.devicecontrol.model.PTZDirection;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.tuya.smart.rnsdk.utils.Constant.ARG1_OPERATE_FAIL;
import static com.tuya.smart.rnsdk.utils.Constant.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.rnsdk.utils.Constant.MSG_DATA_DATE;
import static com.tuya.smart.rnsdk.utils.Constant.MSG_DATA_DATE_BY_DAY_FAIL;
import static com.tuya.smart.rnsdk.utils.Constant.MSG_DATA_DATE_BY_DAY_SUCC;
import static com.tuya.smart.rnsdk.utils.Constant.MSG_MUTE;


/**
 * @author chenbj
 */
public class CameraPlaybackActivity extends AppCompatActivity implements OnP2PCameraListener, View.OnClickListener, TuyaCameraView.CreateVideoViewCallback {

    private static final String TAG = "CameraPlaybackActivity";
    private Toolbar toolbar;
    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    //private Button queryBtn, startBtn, pauseBtn, resumeBtn, stopBtn;
    private Button queryBtn;
    private ImageView record_btn, pauseBtn, photo_btn;
    private TextView txt_NoData;

    private ICameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", p2pWd = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private int queryDay;
    private CameraPlaybackTimeAdapter adapter;
    private List<TimePieceBean> queryDateList;

    private boolean isRecording = false;
    private String picPath, videoPath;

    private boolean isPlayback = false;

    protected Map<String, List<String>> mBackDataMonthCache;
    protected Map<String, List<TimePieceBean>> mBackDataDayCache;
    private int mPlaybackMute = ICameraP2P.MUTE;
    private boolean mIsRunSoft;
    private int p2pType;

    Calendar myCalendar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_DATA_DATE:
                    handleDataDate(msg);
                    break;
                case MSG_DATA_DATE_BY_DAY_SUCC:
                case MSG_DATA_DATE_BY_DAY_FAIL:
                    handleDataDay(msg);
                    break;
                case Constants.MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case Constants.MSG_VIDEO_RECORD_BEGIN:
                    //ToastUtil.shortToast(CameraPlaybackActivity.this, "record start success");
                    break;
                case Constants.MSG_VIDEO_RECORD_FAIL:
                    //ToastUtil.shortToast(CameraPlaybackActivity.this, "record start fail");
                    break;
                case Constants.MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            //ToastUtil.shortToast(CameraLivePreviewActivity.this, "record success " + msg.obj);
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraPlaybackActivity.this);
            builder.setTitle("Success");
            builder.setMessage("A video has been saved to your photo gallery.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to record the video");
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == Constants.ARG1_OPERATE_SUCCESS) {
            //ToastUtil.shortToast(CameraLivePreviewActivity.this, "snapshot success " + msg.obj);
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraPlaybackActivity.this);
            builder.setTitle("Success");
            builder.setMessage("A screenshot has been saved to your photo gallery.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to save the photo");
        }
    }

    private void handleDataDay(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            queryDateList.clear();
            //Timepieces with data for the query day
            List<TimePieceBean> timePieceBeans = mBackDataDayCache.get(mCameraP2P.getDayKey());
            if (timePieceBeans != null) {
                queryRv.setVisibility(View.VISIBLE);
                txt_NoData.setVisibility(View.GONE);
                Log.d(TAG, "elango-playback-timePieceBeans : " + timePieceBeans);
                Collections.reverse(timePieceBeans); // now the list is in reverse order
                queryDateList.addAll(timePieceBeans);
                Log.d(TAG, "elango-playback-timePieceBeans reverse : " + timePieceBeans);

                // to play first item on opening
                if(timePieceBeans.size() > 0) {
                    mCameraP2P.startPlayBack(timePieceBeans.get(0).getStartTime(),
                            timePieceBeans.get(0).getEndTime(),
                            timePieceBeans.get(0).getStartTime(), new OperationDelegateCallBack() {
                                @Override
                                public void onSuccess(int sessionId, int requestId, String data) {
                                    //isPlayback = true;
                                    setPlayBackFlag(true);
                                }

                                @Override
                                public void onFailure(int sessionId, int requestId, int errCode) {
                                    //isPlayback = false;
                                    setPlayBackFlag(false);
                                }
                            }, new OperationDelegateCallBack() {
                                @Override
                                public void onSuccess(int sessionId, int requestId, String data) {
                                    //isPlayback = false;
                                    setPlayBackFlag(false);
                                }

                                @Override
                                public void onFailure(int sessionId, int requestId, int errCode) {
                                    //isPlayback = false;
                                    setPlayBackFlag(false);
                                }
                            });
                }
            } else {
                //showErrorToast();
                queryRv.setVisibility(View.GONE);
                txt_NoData.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        } else {
            queryRv.setVisibility(View.GONE);
            txt_NoData.setVisibility(View.VISIBLE);
        }
    }

    private void handleDataDate(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            List<String> days = mBackDataMonthCache.get(mCameraP2P.getMonthKey());

            try {
                if (days== null || days.size() == 0) {
                    //showErrorToast();
                    queryRv.setVisibility(View.GONE);
                    txt_NoData.setVisibility(View.VISIBLE);
                    return;
                }
                queryRv.setVisibility(View.VISIBLE);
                txt_NoData.setVisibility(View.GONE);
                final String inputStr = dateInputEdt.getText().toString();
                if (!TextUtils.isEmpty(inputStr) && inputStr.contains("/")) {
                    String[] substring = inputStr.split("/");
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);
                    int day = Integer.parseInt(substring[2]);
                    mCameraP2P.queryRecordTimeSliceByDay(year, mouth, day, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            L.e(TAG, inputStr + " --- " + data);
                            Log.d(TAG, "elango-playback-queryRecordTimeSliceByDay : " + data);
                            parsePlaybackData(data);
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendEmptyMessage(MSG_DATA_DATE_BY_DAY_FAIL);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private void parsePlaybackData(Object obj) {
        RecordInfoBean recordInfoBean = JSONObject.parseObject(obj.toString(), RecordInfoBean.class);
        if (recordInfoBean.getCount() != 0) {
            List<TimePieceBean> timePieceBeanList = recordInfoBean.getItems();
            if (timePieceBeanList != null && timePieceBeanList.size() != 0) {
                mBackDataDayCache.put(mCameraP2P.getDayKey(), timePieceBeanList);
            }
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_SUCC, ARG1_OPERATE_SUCCESS));
        } else {
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_FAIL, ARG1_OPERATE_FAIL));
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(mPlaybackMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to mute/unmute");
        }

        if(mPlaybackMute == ICameraP2P.MUTE) {
            muteImg.setBackground(ContextCompat.getDrawable(CameraPlaybackActivity.this, R.drawable.tuya_bottom_btn_bg_trans));
            setImageViewSrc(muteImg, R.drawable.ic_sound_off);
        } else {
            muteImg.setBackground(ContextCompat.getDrawable(CameraPlaybackActivity.this, R.drawable.tuya_bottom_btn_bg_blue));
            setImageViewSrc(muteImg, R.drawable.ic_sound_on);
        }
    }

    private void setImageViewSrc(ImageView imgView, int res){
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), res, getApplicationContext().getTheme()));
        } else {
            imgView.setImageDrawable(getResources().getDrawable(res));
        }*/
        imgView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), res, getApplicationContext().getTheme()));
    }

    private void setPlayBackFlag(boolean playBack) {
        isPlayback = playBack;
        if(isPlayback) {
            setImageViewSrc(pauseBtn, R.drawable.ic_pause);
        } else {
            setImageViewSrc(pauseBtn, R.drawable.ic_play);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_playback);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.tuya_header_bg_grey)));

        mVideoView = findViewById(R.id.camera_video_view);
        muteImg = findViewById(R.id.camera_mute);
        dateInputEdt = findViewById(R.id.date_input_edt);
        queryBtn = findViewById(R.id.query_btn);
        //startBtn = findViewById(R.id.start_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        //resumeBtn = findViewById(R.id.resume_btn);
        //stopBtn = findViewById(R.id.stop_btn);
        record_btn = findViewById(R.id.record_btn);
        photo_btn = findViewById(R.id.photo_btn);

        queryRv = findViewById(R.id.query_list);
        txt_NoData = findViewById(R.id.txt_NoData);

        //播放器view最好宽高比设置16:9
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);
    }

    private void initData() {
        mBackDataMonthCache = new HashMap<>();
        mBackDataDayCache = new HashMap<>();
        mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", false);
        p2pId = getIntent().getStringExtra("p2pId");
        p2pWd = getIntent().getStringExtra("p2pWd");
        localKey = getIntent().getStringExtra("localKey");
        p2pType = getIntent().getIntExtra("p2pType", 1);

        mVideoView.createVideoView(p2pType);
        mVideoView.setCameraViewCallback(this);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        //queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        queryDateList = new ArrayList<>();
        adapter = new CameraPlaybackTimeAdapter(this, queryDateList);
        queryRv.setAdapter(adapter);

        //there is no need to reconnect（createDevice） with a single column object（Of course，you can create it again）
        mCameraP2P = TuyaSmartCameraP2PFactory.generateTuyaSmartCamera(p2pType);
        mCameraP2P.connectPlayback();

        muteImg.setSelected(true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateInputEdt.setText(simpleDateFormat.format(date));

        myCalendar = Calendar.getInstance();

        queryDayByMonthClick();
        /*final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dateInputEdt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(CameraPlaybackActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });*/
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();

            stopPlayBack();
            queryDayByMonthClick();
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_playback_date) {
            //Toast.makeText(this, "Action clicked", Toast.LENGTH_LONG).show();
            new DatePickerDialog(CameraPlaybackActivity.this, dateSetListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateInputEdt.setText(sdf.format(myCalendar.getTime()));
    }

    private void initListener() {
        muteImg.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        //startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        //resumeBtn.setOnClickListener(this);
        //stopBtn.setOnClickListener(this);
        record_btn.setOnClickListener(this);
        photo_btn.setOnClickListener(this);
        adapter.setListener(new CameraPlaybackTimeAdapter.OnTimeItemListener() {
            @Override
            public void onClick(TimePieceBean timePieceBean) {

                mCameraP2P.startPlayBack(timePieceBean.getStartTime(),
                        timePieceBean.getEndTime(),
                        timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                //isPlayback = true;
                                setPlayBackFlag(true);
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                //isPlayback = false;
                                setPlayBackFlag(false);
                                com.tuya.smart.rnsdk.camera.utils.ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to start playback");
                            }
                        }, new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                //isPlayback = false;
                                setPlayBackFlag(false);
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                //isPlayback = false;
                                setPlayBackFlag(false);
                            }
                        });
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_mute) {
            muteClick();
        } else if (id == R.id.query_btn) {
            queryDayByMonthClick();
        } else if (id == R.id.pause_btn) {
            if(isPlayback) {
                pauseClick();
                setImageViewSrc(pauseBtn, R.drawable.ic_play);
            } else {
                resumeClick();
                setImageViewSrc(pauseBtn, R.drawable.ic_pause);
            }
        } else if (id == R.id.record_btn) {
            recordClick();
        } else if (id == R.id.photo_btn) {
            snapShotClick();
        } /*else if (id == R.id.start_btn) {
            startPlayback();
        } else if (id == R.id.resume_btn) {
            resumeClick();
        } else if (id == R.id.stop_btn) {
            stopClick();
        }*/
    }

    /*private void startPlayback() {
        if (null != queryDateList && queryDateList.size() > 0) {
            TimePieceBean timePieceBean = queryDateList.get(0);
            if (null != timePieceBean) {
                mCameraP2P.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
            }
        } else {
            ToastUtil.shortToast(this, "No data for query date");
        }
    }
*/
    private void stopClick() {
        mCameraP2P.stopPlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
        isPlayback = false;
    }

    private void stopPlayBack() {
        if(mCameraP2P != null) {
            mCameraP2P.stopPlayBack(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
        }
        //isPlayback = false;
        setPlayBackFlag(false);
    }

    private void resumeClick() {
        mCameraP2P.resumePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to start playback");
            }
        });
    }

    private void pauseClick() {
        mCameraP2P.pausePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = false;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                ToastUtil.shortToast(CameraPlaybackActivity.this, "Failed to pause playback");
            }
        });
    }

    private void queryDayByMonthClick() {
        String inputStr = dateInputEdt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            return;
        }
        if (inputStr.contains("/")) {
            String[] substring = inputStr.split("/");
            if (substring.length > 2) {
                try {
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);
                    queryDay = Integer.parseInt(substring[2]);
                    mCameraP2P.queryRecordDaysByMonth(year, mouth, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            MonthDays monthDays = JSONObject.parseObject(data, MonthDays.class);
                            mBackDataMonthCache.put(mCameraP2P.getMonthKey(), monthDays.getDataDays());
                            L.e(TAG,   "MonthDays --- " + data);

                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_SUCCESS, data));
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_FAIL));
                        }
                    });
                } catch (Exception e) {
                    ToastUtil.shortToast(CameraPlaybackActivity.this, "Input Error");
                }
            }
        }
    }

    private void muteClick() {
        int mute;
        mute = mPlaybackMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(ICameraP2P.PLAYMODE.PLAYBACK, mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mPlaybackMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void recordClick() {
        Log.d("elango-recordClick", "elango-recordClick : ");
        try {
            if (!isRecording) {
                Log.d("elango-recordClick", "elango-recordClick hasStoragePermission : " + Constants.hasStoragePermission());
                if (Constants.hasStoragePermission()) {
                    String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                    File file = new File(picPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + ".mp4";
                    videoPath = picPath + fileName;
                    Log.d("elango-recordClick", "elango-recordClick videoPath : " + videoPath);
                    mCameraP2P.startRecordLocalMp4(picPath, fileName, CameraPlaybackActivity.this, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            Log.d("elango-recordClick", "elango-recordClick : " + data);
                            isRecording = true;
                            mHandler.sendEmptyMessage(Constants.MSG_VIDEO_RECORD_BEGIN);

                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            Log.d("elango-recordClick", "elango-recordClick : " + errCode);
                            mHandler.sendEmptyMessage(Constants.MSG_VIDEO_RECORD_FAIL);
                        }
                    });
                    recordStatue(true);
                } else {
                    Constants.requestPermission(CameraPlaybackActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "Please enable the storage permission in app setting");
                }
            } else {
                mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isRecording = false;
                        mHandler.sendMessage(com.tuyasmart.stencil.utils.MessageUtil.getMessage(Constants.MSG_VIDEO_RECORD_OVER, Constants.ARG1_OPERATE_SUCCESS, data));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isRecording = false;
                        mHandler.sendMessage(com.tuyasmart.stencil.utils.MessageUtil.getMessage(Constants.MSG_VIDEO_RECORD_OVER, Constants.ARG1_OPERATE_FAIL));
                    }
                });
                recordStatue(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void snapShotClick() {
        if (Constants.hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                picPath = path;
            }
            mCameraP2P.snapshot(picPath, CameraPlaybackActivity.this, ICameraP2P.PLAYMODE.LIVE, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    mHandler.sendMessage(com.tuyasmart.stencil.utils.MessageUtil.getMessage(Constants.MSG_SCREENSHOT, Constants.ARG1_OPERATE_SUCCESS, data));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendMessage(com.tuyasmart.stencil.utils.MessageUtil.getMessage(Constants.MSG_SCREENSHOT, Constants.ARG1_OPERATE_FAIL));
                }
            });
        } else {
            Constants.requestPermission(CameraPlaybackActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "Please enable the storage permission in app setting");
        }
    }

    private void recordStatue(boolean isRecording) {
        //ToastUtil.shortToast(CameraPlaybackActivity.this, "recordStatue - " + isRecording);
        Log.d("elango-recordClick", "elango-recordClick : " + isRecording);
        pauseBtn.setEnabled(!isRecording);
        photo_btn.setEnabled(!isRecording);

        if(isRecording) {
            record_btn.setBackground(ContextCompat.getDrawable(CameraPlaybackActivity.this, R.drawable.tuya_bottom_btn_bg_red));
        } else {
            record_btn.setBackground(ContextCompat.getDrawable(CameraPlaybackActivity.this, R.drawable.tuya_bottom_btn_bg_trans));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registorOnP2PCameraListener(this);
            mCameraP2P.generateCameraView(mVideoView.createdView());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isPlayback) {
            mCameraP2P.stopPlayBack(null);
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
        }
        AudioUtils.changeToNomal(this);
    }


    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.shortToast(CameraPlaybackActivity.this, "No data for query date");
            }
        });
    }

    @Override
    public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

    }

    @Override
    public void onReceiveFrameYUVData(int i, ByteBuffer byteBuffer, ByteBuffer byteBuffer1, ByteBuffer byteBuffer2, int i1, int i2, int i3, int i4, long l, long l1, long l2, Object o) {

    }

    @Override
    public void onSessionStatusChanged(Object o, int i, int i1) {

    }

    @Override
    public void onReceiveSpeakerEchoData(ByteBuffer byteBuffer, int i) {

    }

    @Override
    public void onCreated(Object o) {
        mCameraP2P.generateCameraView(mVideoView.createdView());
    }

    @Override
    public void videoViewClick() {

    }

    @Override
    public void startCameraMove(PTZDirection ptzDirection) {

    }

    @Override
    public void onActionUP() {

    }
}