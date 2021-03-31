package com.tuya.smart.rnsdk.activator


import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.facebook.react.bridge.*
import com.tuya.sdk.blelib.channel.Timer.stop
import com.tuya.smart.android.common.utils.WiFiUtil
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.builder.ActivatorBuilder
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder
import com.tuya.smart.home.sdk.builder.TuyaGwActivatorBuilder
import com.tuya.smart.home.sdk.builder.TuyaGwSubDevActivatorBuilder
import com.tuya.smart.rnsdk.camera.activity.StorageSettingActivity
import com.tuya.smart.rnsdk.camera.utils.ToastUtil
import com.tuya.smart.rnsdk.utils.Constant.DEVID
import com.tuya.smart.rnsdk.utils.Constant.HOMEID
import com.tuya.smart.rnsdk.utils.Constant.PASSWORD
import com.tuya.smart.rnsdk.utils.Constant.SSID
import com.tuya.smart.rnsdk.utils.Constant.TIME
import com.tuya.smart.rnsdk.utils.Constant.TOKEN
import com.tuya.smart.rnsdk.utils.Constant.TYPE
import com.tuya.smart.rnsdk.utils.JsonUtils
import com.tuya.smart.rnsdk.utils.ReactParamsCheck
import com.tuya.smart.rnsdk.utils.TuyaReactUtils
import com.tuya.smart.sdk.api.*
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.enums.ActivatorModelEnum
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK
import com.tuyasmart.camera.devicecontrol.api.ITuyaCameraDeviceControlCallback
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormat
import com.tuyasmart.camera.devicecontrol.bean.DpSDFormatStatus
import com.tuyasmart.camera.devicecontrol.bean.DpSDRecordModel
import com.tuyasmart.camera.devicecontrol.model.DpNotifyModel
import com.tuyasmart.camera.devicecontrol.model.RecordMode


class TuyaActivatorModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  var mITuyaActivator: ITuyaActivator? = null
  var mTuyaGWActivator: ITuyaActivator? = null
  var mITuyaCameraActivator: ITuyaCameraDevActivator? = null
  override fun getName(): String {
    return "TuyaActivatorModule"
  }

  @ReactMethod
  fun getCurrentWifi(params: ReadableMap, promise: Promise) {
    Log.d("elango-getCurrentWifi", WiFiUtil.getCurrentSSID(reactApplicationContext.applicationContext));
    promise.resolve(WiFiUtil.getCurrentSSID(reactApplicationContext.applicationContext));
    //promise.resolve(if(WiFiUtil.getCurrentSSID(reactApplicationContext.applicationContext).equals("<unknown ssid>")) "" else WiFiUtil.getCurrentSSID(reactApplicationContext.applicationContext));
  }

  @ReactMethod
  fun openNetworkSettings(params: ReadableMap) {
    val currentActivity = currentActivity
    if (currentActivity == null) {
      return
    }
    try {
      currentActivity.startActivity(Intent(Settings.ACTION_SETTINGS))
    } catch (e: Exception) {
    }

  }

  @ReactMethod
  fun getTokenForQRCode(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID), params)) {
      TuyaHomeSdk.getActivatorInstance().getActivatorToken(params.getString(HOMEID)!!.toLong(), object : ITuyaActivatorGetToken {
        override fun onSuccess(s: String) {
          //promise.resolve(TuyaReactUtils.parseToWritableMap(s))
          promise.resolve(s)
        }

        override fun onFailure(s: String, s1: String) {
          promise.reject(s, s1)
        }
      })
    }
  }

  @ReactMethod
  fun initActivatorForQRCode(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(SSID, PASSWORD, TIME, TOKEN), params)) {
      Log.d("elango-initActForQRCode", params.toString() + "-----" + params.getString(SSID) + " ; " + params.getString(PASSWORD) + " ; " + params.getInt(TIME) + " ; " + params.getString(TOKEN))
      mITuyaCameraActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(TuyaCameraActivatorBuilder()
              .setSsid(params.getString(SSID))
              .setContext(reactApplicationContext.applicationContext)
              .setPassword(params.getString(PASSWORD))
              //.setActivatorModel(ActivatorModelEnum.TY_QR)
              .setTimeOut(params.getInt(TIME).toLong())
              //.setTimeOut(params.getString(TIME)!!.toLong())
              .setToken(params.getString(TOKEN)).setListener(getITuyaSmartCameraActivatorListener(promise)))
      Log.d("elango-initActForQRCode", mITuyaCameraActivator.toString())
      mITuyaCameraActivator?.start()
    }

  }

  @ReactMethod
  fun registerForPushNotification(params: ReadableMap) {
      Log.d("elango-registerForPushNotification", params.toString() + "-----" + params.getString("token"))
      if (ReactParamsCheck.checkParams(arrayOf("token"), params)) {
          TuyaHomeSdk.getPushInstance().registerDevice(params.getString("token"), "FCM", object : IResultCallback {
            override fun onError(code: String, error: String) {
              Log.d("TAG-FCM", "Error-" + error)
            }

            override fun onSuccess() {
              Log.d("TAG-FCM", "Success")
            }
          })
      }
  }

  @ReactMethod
  fun resetDevice(params: ReadableMap, promise: Promise) {
    Log.d("elango-resetDevice", "elango-resetDevice")
    if (ReactParamsCheck.checkParams(arrayOf(DEVID), params)) {
      try {
        //set Chime setting to Mechanical
        val mTuyaDevice: ITuyaDevice? = TuyaHomeSdk.newDeviceInstance(params.getString(DEVID))
        mTuyaDevice?.publishDps("{\"165\": \"1\"}", object : IResultCallback {
          override fun onError(code: String, error: String) {
            //Log.d(TAG, " publishDps - onError : " + error);
          }

          override fun onSuccess() {
            //Log.d(TAG, " publishDps - onSuccess : ");
          }
        })

        val mTuyaCameraDevice: ITuyaCameraDevice?  = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(params.getString(DEVID))
        //set Recording to Event Recording
        mTuyaCameraDevice!!.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.dpValue)

        // Format the camera
        mTuyaCameraDevice.registorTuyaCameraDeviceControlCallback(DpSDFormat.ID, object : ITuyaCameraDeviceControlCallback<Boolean> {
          override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Boolean) {
            Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onSuccess : $s, $o")
            //ToastUtil.shortToast(this@StorageSettingActivity, "Successfully Formatted.")

//            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
            handleFormatting(params.getString(DEVID), promise)
            /*try {
              mTuyaCameraDevice.onDestroy()
            } catch (ex: Exception) {
              ex.printStackTrace()
            }*/
          }

          override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
            Log.d("elango-resetDevice", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onFailure : $s, $s1, $s2")
            //ToastUtil.shortToast(this@StorageSettingActivity, "Format failed.")

//            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
            promise.reject(s, s1)
            try {
              mTuyaCameraDevice.onDestroy()
            } catch (ex: Exception) {
              ex.printStackTrace()
            }
          }
        })
        mTuyaCameraDevice.publishCameraDps(DpSDFormat.ID, true)
      } catch (e: Exception) {
        e.printStackTrace()

//        promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
      }
    }
  }

  /*fun handleFormatting(devId: String?, promise: Promise) {
    val mTuyaCameraDevice: ITuyaCameraDevice?  = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId)
    // Format the camera
    mTuyaCameraDevice!!.registorTuyaCameraDeviceControlCallback(DpSDFormatStatus.ID, object : ITuyaCameraDeviceControlCallback<Boolean> {
      override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Boolean) {
        Log.d("elango-resetDevice", "elango-handleFormatting-DpSDFormatStatus-onSuccess : $s, $o")
        //ToastUtil.shortToast(this@StorageSettingActivity, "Successfully Formatted.")

//            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
        try {
          mTuyaCameraDevice.onDestroy()
        } catch (ex: Exception) {
          ex.printStackTrace()
        }
      }

      override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
        Log.d("elango-resetDevice", "elango-handleFormatting-DpSDFormatStatus-onFailure : $s, $s1, $s2")
        //ToastUtil.shortToast(this@StorageSettingActivity, "Format failed.")

        //promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
        promise.reject(s, s1)
        try {
          mTuyaCameraDevice.onDestroy()
        } catch (ex: Exception) {
          ex.printStackTrace()
        }
      }
    })

    mTuyaCameraDevice.publishCameraDps(DpSDFormatStatus.ID, null);
  }*/

  var formatStatus = 0
  private fun handleFormatting(devId: String?, promise: Promise) {
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

    // 30 second timeout
    var i = 30
    while (i > 0) {
      if (formatStatus >= 0 && formatStatus < 100) {
        try {
          Thread.sleep(1000)
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
        mTuyaCameraDevice?.publishCameraDps(DpSDFormatStatus.ID, null)
        //Log.d(TAG, "elango-publishCameraDps-DpSDFormatStatus :" + mTuyaCameraDevice.queryIntegerCurrentCameraDps(DpSDFormatStatus.ID));
      } else if (formatStatus == 100) {
        break
      } else {
        break
      }
      i--
    }

    promise.resolve(TuyaReactUtils.parseToWritableMap("success"))
    try {
      mTuyaCameraDevice?.onDestroy()
    } catch (ex: Exception) {
      ex.printStackTrace()
    }
  }

  @ReactMethod
  fun initActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID, SSID, PASSWORD, TIME, TYPE), params)) {
      TuyaHomeSdk.getActivatorInstance().getActivatorToken(params.getDouble(HOMEID).toLong(), object : ITuyaActivatorGetToken {
        override fun onSuccess(token: String) {
          mITuyaActivator = TuyaHomeSdk.getActivatorInstance().newActivator(ActivatorBuilder()
                  .setSsid(params.getString(SSID))
                  .setContext(reactApplicationContext.applicationContext)
                  .setPassword(params.getString(PASSWORD))
                  .setActivatorModel(ActivatorModelEnum.valueOf(params.getString(TYPE) as String))
                  .setTimeOut(params.getInt(TIME).toLong())
                  .setToken(token).setListener(getITuyaSmartActivatorListener(promise)))
          mITuyaActivator?.start()
        }


        override fun onFailure(s: String, s1: String) {
          promise.reject(s, s1)
        }
      })
    }
  }

  /**
   * ZigBee子设备配网需要ZigBee网关设备云在线的情况下才能发起,且子设备处于配网状态。
   */
  @ReactMethod
  fun newGwSubDevActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(DEVID, TIME), params)) {
      val builder = TuyaGwSubDevActivatorBuilder()
              //设置网关ID
              .setDevId(params.getString(DEVID))
              //设置配网超时时间
              .setTimeOut(params.getInt(TIME).toLong())
              .setListener(object : ITuyaSmartActivatorListener {
                override fun onError(var1: String, var2: String) {
                  promise.reject(var1, var2)
                }

                /**
                 * 设备配网成功,且设备上线（手机可以直接控制），可以通过
                 */
                override fun onActiveSuccess(var1: DeviceBean) {
                  promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
                }

                /**
                 * device_find 发现设备
                device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
                 */
                override fun onStep(var1: String, var2: Any) {
                  // promise.reject(var1,"")
                }
              })

      mTuyaGWActivator = TuyaHomeSdk.getActivatorInstance().newGwSubDevActivator(builder)
    }
  }

  @ReactMethod
  fun stopConfig() {
    mITuyaActivator?.stop()
    mTuyaGWActivator?.stop()
  }

  @ReactMethod
  fun onDestory() {
    mITuyaActivator?.onDestroy()
    mTuyaGWActivator?.onDestroy()
  }

  fun getITuyaSmartCameraActivatorListener(promise: Promise): ITuyaSmartCameraActivatorListener {
    return object : ITuyaSmartCameraActivatorListener {
      override fun onQRCodeSuccess(qrcodeUrl: String?) {
        TODO("Not yet implemented")
      }

      /**
       * 1001        网络错误
      1002        配网设备激活接口调用失败，接口调用不成功
      1003        配网设备激活失败，设备找不到。
      1004        token 获取失败
      1005        设备没有上线
      1006        配网超时
       */
      override fun onError(var1: String, var2: String) {
        Log.d("elango-initActForQRCode-onError", var1 + ", " + var2)
        //promise.reject(var1, var2)
        if (var1.equals("1006") && var2.equals("time out", true)) {
          val map: WritableMap = Arguments.createMap()
          map.putString("is_timeout", "true")
          promise.resolve(map)
        } else {
          promise.reject(var1, var2)
        }
      }

      /**
       * 设备配网成功,且设备上线（手机可以直接控制），可以通过
       */
      override fun onActiveSuccess(var1: DeviceBean) {
        Log.d("elango-initActForQRCode-onActiveSuccess", var1.toString())
        //Log.d("elango-initActForQRCode", var1.toString())
        Log.d("elango-initActForQRCode", var1.getDevId())
        Log.d("elango-initActForQRCode", TuyaReactUtils.parseToWritableMap(var1).toString())

        /*try {
          //set Chime setting to Mechanical
          val mTuyaDevice: ITuyaDevice? = TuyaHomeSdk.newDeviceInstance(var1.getDevId())
          mTuyaDevice?.publishDps("{\"165\": \"1\"}", object : IResultCallback {
            override fun onError(code: String, error: String) {
              //Log.d(TAG, " publishDps - onError : " + error);
            }

            override fun onSuccess() {
              //Log.d(TAG, " publishDps - onSuccess : ");
            }
          })

          val mTuyaCameraDevice: ITuyaCameraDevice?  = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(var1.getDevId())
          //set Recording to Event Recording
          mTuyaCameraDevice!!.publishCameraDps(DpSDRecordModel.ID, RecordMode.EVENT.dpValue)

          // Format the camera
          mTuyaCameraDevice!!.registorTuyaCameraDeviceControlCallback(DpSDFormat.ID, object : ITuyaCameraDeviceControlCallback<Boolean> {
            override fun onSuccess(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, o: Boolean) {
              //showPublishTxt.setText("LAN/Cloud query result: $o")
              Log.d("elango-initActForQRCode", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onSuccess : $s, $o")
              //ToastUtil.shortToast(this@StorageSettingActivity, "Successfully Formatted.")

              promise.resolve(TuyaReactUtils.parseToWritableMap(var1))

              try {
                mTuyaCameraDevice.onDestroy()
              } catch (ex: Exception) {
                ex.printStackTrace()
              }
            }

            override fun onFailure(s: String, action: DpNotifyModel.ACTION, sub_action: DpNotifyModel.SUB_ACTION, s1: String, s2: String) {
              Log.d("elango-initActForQRCode", "elango-registorTuyaCameraDeviceControlCallback-DpSDFormat-onFailure : $s, $s1, $s2")
              //ToastUtil.shortToast(this@StorageSettingActivity, "Format failed.")

              promise.resolve(TuyaReactUtils.parseToWritableMap(var1))

              try {
                mTuyaCameraDevice.onDestroy()
              } catch (ex: Exception) {
                ex.printStackTrace()
              }
            }
          })
          mTuyaCameraDevice.publishCameraDps(DpSDFormat.ID, true)


        } catch (e: Exception) {
          e.printStackTrace()

          promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
        }*/

        /* *-* moved to Format sdcard callback *-* */
        promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
      }

      /**
       * device_find 发现设备
      device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
       */
      /*override fun onStep(var1: String, var2: Any) {
        // IOS 没有onStep保持一致
        //promise.reject(var1,"")
      }*/
    }
  }

  fun getITuyaSmartActivatorListener(promise: Promise): ITuyaSmartActivatorListener {
    return object : ITuyaSmartActivatorListener {
      /**
       * 1001        网络错误
      1002        配网设备激活接口调用失败，接口调用不成功
      1003        配网设备激活失败，设备找不到。
      1004        token 获取失败
      1005        设备没有上线
      1006        配网超时
       */
      override fun onError(var1: String, var2: String) {
        promise.reject(var1, var2)
      }

      /**
       * 设备配网成功,且设备上线（手机可以直接控制），可以通过
       */
      override fun onActiveSuccess(var1: DeviceBean) {
        promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
      }

      /**
       * device_find 发现设备
      device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
       */
      override fun onStep(var1: String, var2: Any) {
        // IOS 没有onStep保持一致
        //promise.reject(var1,"")
      }
    }
  }

  //Zigbee wired gateway method
  @ReactMethod
  fun newGwActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID, TIME), params)) {
      TuyaHomeSdk.getActivatorInstance().getActivatorToken((params.getDouble(HOMEID)).toLong(), object : ITuyaActivatorGetToken {
        override fun onSuccess(token: String) {
          stop()
          val iTuyaSmartActivatorListener = object : ITuyaSmartActivatorListener {
            override fun onError(errorCode: String, errorMsg: String) {
              promise.reject(errorCode, errorMsg)
            }

            override fun onActiveSuccess(deviceBean: DeviceBean) {
              promise.resolve(TuyaReactUtils.parseToWritableMap(deviceBean))
            }

            override fun onStep(step: String, data: Any) {
              promise.resolve(JsonUtils.toString(data))
            }
          }
          mTuyaGWActivator = TuyaHomeSdk.getActivatorInstance().newGwActivator(TuyaGwActivatorBuilder()
                  .setToken(token)
                  .setTimeOut(params.getInt(TIME).toLong())
                  .setContext(reactApplicationContext)
                  .setListener(iTuyaSmartActivatorListener))
          mTuyaGWActivator?.start()
        }

        override fun onFailure(errorCode: String, errorMsg: String) {
          promise.reject(errorCode, errorMsg)
        }
      })

    }
  }
}
