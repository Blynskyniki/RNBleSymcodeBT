package com.rnsymcodebt

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import ru.lad24.SymCodeSpp

class RnSymcodeBtModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  val driver by lazy { SymCodeSpp(reactContext.applicationContext as Application) }
  override fun getName(): String {
    return "RnSymcodeBt"
  }

  private val TAG = "ru.lad24.sppSymcode"

  fun log(data: String) {
    Log.d(TAG, data)
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
  fun enableBluetooth(promise: Promise) {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    bluetoothAdapter.enable()
    promise.resolve(true)
  }

  @ReactMethod
  fun isPaired(mac: String, promise: Promise) {
    promise.resolve(driver.isPaired(mac))
  }

  @ReactMethod
  fun searchDevices(promise: Promise) {
  try {
    driver.searchDevices {
      val mutableList = WritableNativeArray();
      it.forEach { d: BluetoothDevice ->
          val map = Arguments.createMap()
          map.putString("name", d.name)
          map.putString("mac", d.address)
          map.putString("bondState", d.bondState.toString())
          mutableList.pushMap(map)
        }
      log("${mutableList}")
        promise.resolve(mutableList)
    }
  } catch (e: Error) {
    log("${e.message}");
  }



  }

  @ReactMethod
  fun pairDevice(mac: String, promise: Promise) {
    driver.pairDevice(mac) {
      if (it !== null) {
        promise.reject(it)
      }
      promise.resolve({})
    }
  }

  @ReactMethod
  fun connect(mac: String, promise: Promise) {
    if (driver.connect(mac)) {
      promise.resolve(true)
    }
    promise.reject("404", "Device not found! Please  rescan device")

  }

  @ReactMethod
  fun isConnected(mac: String, promise: Promise) {
    if (driver.isConnected()) {
      promise.resolve(true)
    }
    promise.resolve(false)

  }

  @ReactMethod
  fun disconnect(promise: Promise) {
    driver.dicsonnect()
    promise.resolve("ok")
  }

  @ExperimentalStdlibApi
  @ReactMethod
  fun enableNotify(promise: Promise) {
    driver.enableNotify() {
      val map = Arguments.createMap()
      map.putString("barcode", it)
      sendEvent(this.reactApplicationContext, BARCODE_SCAN_NOTIFY_EVENT_NAME, map)
    }
  }


  @ReactMethod
  fun disableNotify(promise: Promise) {
    driver.disableNotify()
    promise.resolve("ok")
  }


}
