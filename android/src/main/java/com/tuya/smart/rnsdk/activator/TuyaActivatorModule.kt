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
import com.tuya.smart.rnsdk.utils.AirbrakeUtil
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
      if(params.getString(SSID) != null && params.getString(PASSWORD) != null && params.getString(TOKEN) != null) {
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
      } else {
        promise.reject("121", "Halt Due to Multiple call.")
      }
    }

  }

  @ReactMethod
  fun registerForPushNotification(params: ReadableMap) {
    Log.d("elango-registerForPushNotification", params.toString() + "-----" + params.getString("token"))
    AirbrakeUtil.init(reactApplicationContext)
    AirbrakeUtil.notifyLog("FCM TOKEN REGISTER!", "registerForPushNotification", params.getString("token"))

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
   * ZigBee?????????????????????ZigBee?????????????????????????????????????????????,?????????????????????????????????
   */
  @ReactMethod
  fun newGwSubDevActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(DEVID, TIME), params)) {
      val builder = TuyaGwSubDevActivatorBuilder()
              //????????????ID
              .setDevId(params.getString(DEVID))
              //????????????????????????
              .setTimeOut(params.getInt(TIME).toLong())
              .setListener(object : ITuyaSmartActivatorListener {
                override fun onError(var1: String, var2: String) {
                  promise.reject(var1, var2)
                }

                /**
                 * ??????????????????,????????????????????????????????????????????????????????????
                 */
                override fun onActiveSuccess(var1: DeviceBean) {
                  promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
                }

                /**
                 * device_find ????????????
                device_bind_success ?????????????????????????????????????????????????????????????????????????????????????????????
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
       * 1001        ????????????
      1002        ????????????????????????????????????????????????????????????
      1003        ?????????????????????????????????????????????
      1004        token ????????????
      1005        ??????????????????
      1006        ????????????
       */
      override fun onError(var1: String, var2: String) {
        Log.d("elango-initActForQRCode-onError", var1 + ", " + var2)

        AirbrakeUtil.init(reactApplicationContext)
        AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - onError", var1 + ", " + var2)

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
       * ??????????????????,????????????????????????????????????????????????????????????
       */
      override fun onActiveSuccess(var1: DeviceBean) {
        Log.d("elango-initActForQRCode-onActiveSuccess", var1.toString())

        AirbrakeUtil.init(reactApplicationContext)
        AirbrakeUtil.notifyLog("Camera Logs!", "Tuya Add Doorbell - onActiveSuccess", var1.toString())

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
        /*val map: WritableMap = Arguments.createMap()
        map.putString("is_timeout", "true")
        promise.resolve(map)*/
      }

      /**
       * device_find ????????????
      device_bind_success ?????????????????????????????????????????????????????????????????????????????????????????????
       */
      /*override fun onStep(var1: String, var2: Any) {
        // IOS ??????onStep????????????
        //promise.reject(var1,"")
      }*/
    }
  }

  fun getITuyaSmartActivatorListener(promise: Promise): ITuyaSmartActivatorListener {
    return object : ITuyaSmartActivatorListener {
      /**
       * 1001        ????????????
      1002        ????????????????????????????????????????????????????????????
      1003        ?????????????????????????????????????????????
      1004        token ????????????
      1005        ??????????????????
      1006        ????????????
       */
      override fun onError(var1: String, var2: String) {
        promise.reject(var1, var2)
      }

      /**
       * ??????????????????,????????????????????????????????????????????????????????????
       */
      override fun onActiveSuccess(var1: DeviceBean) {
        promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
      }

      /**
       * device_find ????????????
      device_bind_success ?????????????????????????????????????????????????????????????????????????????????????????????
       */
      override fun onStep(var1: String, var2: Any) {
        // IOS ??????onStep????????????
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
