package com.rnsymcodebt

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import ru.lad24.SymCodeSpp
import java.lang.Exception

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

  private fun handlePromiseWrapper(promise: Promise, cb: () -> Unit) {
    try {
      cb()
    } catch (e: Exception) {
      log("${e.message}")
      promise.reject(e)
    }

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
    handlePromiseWrapper(promise) {
      driver.enableBt {
        promise.resolve(it)
      }
    }

  }


  @ReactMethod
  fun isPaired(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {

      promise.resolve(driver.isPaired(mac))
    }
  }

  @ReactMethod
  fun searchDevices(promise: Promise) {
    handlePromiseWrapper(promise) {

      driver.searchDevices {
        val arr = WritableNativeArray()
        it.forEach { d: BluetoothDevice ->
          arr.pushMap(Arguments.createMap().apply {
            putString("name", d.name)
            putString("mac", d.address)
            putString("bondState", d.bondState.toString())
          })
        }
        log("${arr}")
        promise.resolve(arr)
      }

    }

  }

  @ReactMethod
  fun pairDevice(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {
      driver.pairDevice(mac) {
        if (it !== null) {
          promise.reject(it)
        }
        promise.resolve(true)
      }
    }
  }

  @ReactMethod
  fun connect(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {
      if (driver.connect(mac)) {
        promise.resolve(true)
      }
      promise.reject("404", "Device not found! Please  rescan device")
    }
  }


  @ReactMethod
  fun disconnect(promise: Promise) {
    handlePromiseWrapper(promise) {
      driver.dicsonnect()
      promise.resolve(true)
    }
  }

  @ExperimentalStdlibApi
  @ReactMethod
  fun enableNotify(promise: Promise) {
    handlePromiseWrapper(promise) {
      driver.enableNotify() {
        val map = Arguments.createMap()
        map.putString("barcode", it)
        sendEvent(this.reactApplicationContext, BARCODE_SCAN_NOTIFY_EVENT_NAME, map)
      }
      promise.resolve(true)
    }
  }


  @ReactMethod
  fun disableNotify(promise: Promise) {
    handlePromiseWrapper(promise) {
      driver.disableNotify()
      promise.resolve(true)
    }
  }


}
