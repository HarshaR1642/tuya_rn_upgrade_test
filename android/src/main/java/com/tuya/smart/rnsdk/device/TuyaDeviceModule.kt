package com.tuya.smart.rnsdk.device

import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.facebook.react.bridge.*
import com.tuya.smart.android.device.api.IGetDataPointStatCallback
import com.tuya.smart.android.device.bean.DataPointStatBean
import com.tuya.smart.android.device.enums.DataPointTypeEnum
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.rnsdk.camera.utils.ToastUtil
import com.tuya.smart.rnsdk.utils.AirbrakeUtil
import com.tuya.smart.rnsdk.utils.BridgeUtils
import com.tuya.smart.rnsdk.utils.Constant.COMMAND
import com.tuya.smart.rnsdk.utils.Constant.DATAPOINTTYPEENUM
import com.tuya.smart.rnsdk.utils.Constant.DEVID
import com.tuya.smart.rnsdk.utils.Constant.DPID
import com.tuya.smart.rnsdk.utils.Constant.NAME
import com.tuya.smart.rnsdk.utils.Constant.NUMBER
import com.tuya.smart.rnsdk.utils.Constant.STARTTIME
import com.tuya.smart.rnsdk.utils.Constant.getIResultCallback
import com.tuya.smart.rnsdk.utils.ReactParamsCheck
import com.tuya.smart.rnsdk.utils.TuyaReactUtils
import com.tuya.smart.sdk.api.IDevListener
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.api.ITuyaDevice
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK
import com.tuyasmart.camera.devicecontrol.api.ITuyaCameraDeviceControlCallback
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormat
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormatStatus
import com.tuyasmart.camera.devicecontrol.bean.DpSDRecordModel
import com.tuyasmart.camera.devicecontrol.bean.DpSDStatus
import com.tuyasmart.camera.devicecontrol.model.DpNotifyModel
import com.tuyasmart.camera.devicecontrol.model.RecordMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TuyaDeviceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    var device: ITuyaDevice? = null

    override fun getName(): String {
        return "TuyaDeviceModule"
    }

    @ReactMethod
    fun getDevice(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            promise.resolve(TuyaReactUtils.parseToWritableMap(getDevice(params.getString(DEVID) as String)))
        }
    }

    @ReactMethod
    fun getDeviceData(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            promise.resolve(TuyaReactUtils.parseToWritableMap(TuyaHomeSdk.getDataInstance().getDeviceBean(params.getString(DEVID))))
        }
    }

    @ReactMethod
    fun registerDevListener(params: ReadableMap) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            device = getDevice(params.getString(DEVID) as String)
            device?.registerDevListener(object : IDevListener {
                override fun onDpUpdate(devId: String, dpStr: String) {
                    //dp数据更新:devId 和相应dp数据
                    val map = Arguments.createMap()
                    map.putString("devId", devId)
                    map.putString("dpStr", dpStr)
                    map.putString("type", "onDpUpdate");
                    BridgeUtils.devListener(reactApplicationContext, map, params.getString(DEVID) as String)
                }

                override fun onRemoved(devId: String) {
                    //设备被移除
                    val map = Arguments.createMap()
                    map.putString("devId", devId)
                    map.putString("type", "onRemoved");
                    BridgeUtils.devListener(reactApplicationContext, map, params.getString(DEVID) as String)
                }

                override fun onStatusChanged(devId: String, online: Boolean) {
                    //设备在线状态，online
                    val map = Arguments.createMap()
                    map.putString("devId", devId)
                    map.putBoolean("online", online)
                    map.putString("type", "onStatusChanged");
                    BridgeUtils.devListener(reactApplicationContext, map, params.getString(DEVID) as String)
                }

                override fun onNetworkStatusChanged(devId: String, status: Boolean) {
                    //网络状态监听
                    val map = Arguments.createMap()
                    map.putString("devId", devId)
                    map.putBoolean("status", status)
                    map.putString("type", "onNetworkStatusChanged");

                    BridgeUtils.devListener(reactApplicationContext, map, params.getString(DEVID) as String)
                }

                override fun onDevInfoUpdate(devId: String) {
                    //设备信息变更，目前只有设备名称变化，会调用该接口
                    val map = Arguments.createMap()
                    map.putString("devId", devId)
                    map.putString("type", "onDevInfoUpdate");
                    BridgeUtils.devListener(reactApplicationContext, map, params.getString(DEVID) as String)
                }
            })

        }
    }

    @ReactMethod
    fun unRegisterDevListener(params: ReadableMap) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            if (device != null) {
                device!!.unRegisterDevListener()
            }
        }
    }

    @ReactMethod
    fun onDestroy(params: ReadableMap) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            getDevice(params.getString(DEVID) as String)?.onDestroy()
        }
    }


    @ReactMethod
    fun send(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID, COMMAND), params)) {
            getDevice(params.getString(DEVID) as String)?.publishDps(JSONObject.toJSONString(TuyaReactUtils.parseToMap(params.getMap(COMMAND) as ReadableMap)), getIResultCallback(promise))
        }
    }


    @ReactMethod
    fun getDp(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID, DPID), params)) {
            promise.resolve(getDevice(params.getString(DEVID) as String)?.getDp(
                    params.getString(DPID),
                    getIResultCallback(promise)
            ))
        }
    }

    @ReactMethod
    fun renameDevice(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID, NAME), params)) {
            getDevice(params.getString(DEVID) as String)?.renameDevice(params.getString(NAME), getIResultCallback(promise))
        }
    }

    @ReactMethod
    fun getDataPointStat(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID, DATAPOINTTYPEENUM, NUMBER, DPID, STARTTIME), params)) {
            getDevice(params.getString(DEVID) as String)?.getDataPointStat(DataPointTypeEnum.valueOf(params.getString(DATAPOINTTYPEENUM) as String),
                    params.getDouble(STARTTIME).toLong(),
                    params.getInt(NUMBER),
                    params.getString(DPID),
                    getIGetDataPointStatCallback(promise)
            )
        }
    }

    @ReactMethod
    fun removeDevice(params: ReadableMap, promise: Promise) {
        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            getDevice(params.getString(DEVID) as String)?.removeDevice(getIResultCallback(promise))
        }
    }

    fun getIGetDataPointStatCallback(promise: Promise): IGetDataPointStatCallback {
        return object : IGetDataPointStatCallback {

            override fun onSuccess(p0: DataPointStatBean?) {
                promise.resolve(TuyaReactUtils.parseToWritableMap(p0))
            }


            override fun onError(code: String?, error: String?) {
                promise.reject(code, error)
            }
        }
    }


    fun getDevice(devId: String): ITuyaDevice {
        return TuyaHomeSdk.newDeviceInstance(devId);
    }
    @ReactMethod
    fun resetDevice(params: ReadableMap, promise: Promise) {
        Log.d("elango-resetDevice", "elango-resetDevice")

        AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - resetDevice")

        if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
            try {
                val mTuyaCameraDevice: ITuyaCameraDevice?  = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(params.getString(DEVID))
                val mTuyaDevice: ITuyaDevice? = TuyaHomeSdk.newDeviceInstance(params.getString(DEVID))

                //set Chime setting to Mechanical
                if (mTuyaDevice != null) {
                    mTuyaDevice.publishDps("{\"165\": \"1\"}", object : IResultCallback {
                        override fun onError(code: String, error: String) {
                            //Log.d(TAG, " publishDps - onError : " + error);
                        }

                        override fun onSuccess() {
                            //Log.d(TAG, " publishDps - onSuccess : ");
                        }
                    })
                }

                if(mTuyaCameraDevice != null && mTuyaCameraDevice.isSupportCameraDps(DpSDRecordModel.ID) && mTuyaCameraDevice.isSupportCameraDps(DpSDStatus.ID) ) {
                    Log.d("elango-resetDevice", "elango-resetDevice , " + mTuyaCameraDevice + ", " + mTuyaCameraDevice.isSupportCameraDps("165") + ", " + mTuyaCameraDevice.isSupportCameraDps(DpSDRecordModel.ID) + ", " + mTuyaCameraDevice.isSupportCameraDps(DpSDStatus.ID))
                    //set Recording to Event Recording
                    mTuyaCameraDevice.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.dpValue)

                    // SD card status
                    mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDStatus.ID, object : ITuyaCameraDeviceControlCallback<Int> {
                        override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Int) {
                            Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDStatus-onSuccess : $s, $o")

                            AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - DpSDStatus - onSuccess", s + ", " + o)
                            //ToastUtil.shortToast(this@StorageSettingActivity, "Successfully Formatted.")

                            if (o != 5) { // if(o != 5 && o != 4) {
                                formatSdCard(mTuyaCameraDevice, params, promise)
                            } /* else if(o == 4) {
                                new SDCardFormatting().execute();
                            }*/ else {
                                val map: WritableMap = Arguments.createMap()
                                map.putString("success", "false")
                                promise.resolve(map)
                                try {
                                    mTuyaCameraDevice.onDestroy()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
                            Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDStatus-onFailure : $s, $s1, $s2")

                            AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - DpSDStatus - onFailure", s + ", "+ s1 + ", " + s2)

                            //ToastUtil.shortToast(this@StorageSettingActivity, "Format failed.")

                            //            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
                            //promise.reject(s, s1)
                            val map: WritableMap = Arguments.createMap()
                            map.putString("success", "false")
                            promise.resolve(map)
                            try {
                                mTuyaCameraDevice.onDestroy()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    })
                    mTuyaCameraDevice.publishCameraDps(DpSDStatus.ID, true)

                } else {
                    Log.d("elango-resetDevice", "elango-resetDevice , " + mTuyaCameraDevice + ", fails")

                    val map: WritableMap = Arguments.createMap()
                    map.putString("success", "false")
                    promise.resolve(map)
                }
            } catch (e: Exception) {
                e.printStackTrace()

                val map: WritableMap = Arguments.createMap()
                map.putString("success", "false")
                promise.resolve(map)

                AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - onException", e.message)
            }
        }
    }

    private fun formatSdCard(mTuyaCameraDevice: ITuyaCameraDevice, params: ReadableMap, promise: Promise) {
        // Format the camera
        mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormat.ID, object : ITuyaCameraDeviceControlCallback<Boolean> {
            override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Boolean) {
                Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onSuccess : $s, $o")
                AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - DpSDFormat - onSuccess", s + ", "+ o)
                //ToastUtil.shortToast(this@StorageSettingActivity, "Successfully Formatted.")

                //            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
                GlobalScope.launch(Dispatchers.Main) {
                    handleFormatting(params.getString(DEVID), promise)
                }
            }

            override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
                Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onFailure : $s, $s1, $s2")
                AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - DpSDFormat - onFailure", s + ", "+ s1 + ", " + s2)
                //ToastUtil.shortToast(this@StorageSettingActivity, "Format failed.")

                //            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
                //promise.reject(s, s1)
                val map: WritableMap = Arguments.createMap()
                map.putString("success", "false")
                promise.resolve(map)
                try {
                    mTuyaCameraDevice.onDestroy()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
        mTuyaCameraDevice.publishCameraDps(DpSDFormat.ID, true)
    }

    var formatStatus = 0
    private suspend fun handleFormatting(devId: String?, promise: Promise) {
        val mTuyaCameraDevice: ITuyaCameraDevice?  = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId)
        formatStatus = 0
        if (mTuyaCameraDevice != null) {
            mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormatStatus.ID, object : ITuyaCameraDeviceControlCallback<Int> {
                override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Int) {
                    //showPublishTxt.setText("LAN/Cloud query result: " + o);
                    Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onSuccess : $s, $o")
                    //hideProgressDialog();
                    //ToastUtil.shortToast(StorageSettingActivity.this,"Successfully Formatted.");
                    formatStatus = o
                }

                override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
                    Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormatStatus-onFailure : $s, $s1, $s2")
                    //hideProgressDialog();
                    //ToastUtil.shortToast(StorageSettingActivity.this,"Format failed.");
                    formatStatus = -1
                }
            })
            mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null)
        } else {
            formatStatus = -1
        }

        //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus");

        // 60*2 second timeout
        var i = 60
        while (i > 0) {
            if (formatStatus >= 0 && formatStatus < 100) {
                /*try {
                  Thread.sleep(1000)
                } catch (e: InterruptedException) {
                  e.printStackTrace()
                }*/
                delay(2000L)
                mTuyaCameraDevice?.publishCameraDps(DpSDFormatStatus.ID, null)
                //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDFormatStatus.ID));
            } else if (formatStatus == 100) {
                break
            } else {
                break
            }
            i--
        }

        val map: WritableMap = Arguments.createMap()
        map.putString("success", "true")
        promise.resolve(map)

        AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - handleFormatting - DpSDFormatStatus - onSuccess", "formatStatus" + " - "+ formatStatus)

        try {
            mTuyaCameraDevice?.onDestroy()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}
