package com.rnsymcodebt

import android.app.Application
import androidx.annotation.Nullable
import com.clj.fastble.data.BleDevice
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import ru.lad24.Symcode

class RnSymcodeBtModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  val driver by lazy { Symcode(reactContext as Application) }
  val devices: HashMap<String, BleDevice> = hashMapOf()
  override fun getName(): String {
    return "RnSymcodeBt"
  }

  companion object {
    const val BARCODE_SCAN_NOTIFY_EVENT_NAME = "BARCODE_SCAN_NOTIFY_EVENT"
  }

  private fun sendEvent(
    reactContext: ReactContext,
    eventName: String,
    @Nullable params: WritableMap
  ) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }


  @ReactMethod
  fun scanDevices(promise: Promise) {
    driver.scan(object : Symcode.OnScanBtDevicesResult {
      override fun result(
        error: Exception?,
        scanResultList: List<BleDevice?>?
      ) {
        if (scanResultList != null) {
          val mutableList = mutableListOf<WritableMap>()
          scanResultList.forEach {
            if (it != null) {
              devices.set(it.mac, it)

              val map = Arguments.createMap()
              map.putString("name", it.name.orEmpty())
              map.putString("mac", it.mac)
              mutableList.add(map)
            }

          }
          promise.resolve(mutableList)
        } else {
          promise.resolve(mutableListOf<WritableMap>())
        }
      }

      override fun foundNewDevice(bleDevice: BleDevice?) {
      }
    })

  }

  @ReactMethod
  fun connect(mac: String, promise: Promise) {

    if (devices.containsKey(mac)) {
      driver.connect(devices[mac]!!) {
        promise.resolve(it)
      }
    } else {
      promise.reject("404", "Device not found! Please  rescan device")
    }
  }

  @ReactMethod
  fun disconnect(promise: Promise) {
    driver.disconnect()
    promise.resolve("ok")
  }

  @ExperimentalStdlibApi
  @ReactMethod
  fun enableNotify(promise: Promise) {
    driver.enableNotify(object : Symcode.OnNotifyEnabledResult {
      override fun result(error: Exception?) {
        if (error !== null) {
          promise.reject(error)
        }
        promise.resolve("ok")
      }
    }) {
      val map = Arguments.createMap()
      map.putString("barcode", it)
      sendEvent(this.reactApplicationContext, "BARCODE_SCAN_NOTIFY_EVENT_NAME", map)

    }
  }


  @ReactMethod
  fun disableNotify(promise: Promise) {
    driver.disableNotify()
    promise.resolve("ok")
  }


}