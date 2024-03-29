package com.reactnativesymcodebt

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

class SymcodeBtModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val driver by lazy { SymCodeSpp.getInstance(reactContext.applicationContext as Application) }
  private val cntx by lazy { reactContext.applicationContext as Application }
  override fun getName(): String {
    return "SymcodeBt"
  }

  private val TAG = "symcodebt"

  private fun log(data: String) {
    Log.d(TAG, data)
  }
  @ReactMethod
  fun addListener(type: String?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun removeListeners(type: Int?) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  companion object {
    const val BARCODE_SCAN_NOTIFY_EVENT_NAME = "BARCODE_SCAN_NOTIFY_EVENT"
  }

  private fun handlePromiseWrapper(promise: Promise, cb: () -> Unit) {
    try {
      if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
        throw Exception("Bluetooth отключен")
      }
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
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private fun checkGPSIsOpen(): Boolean {
    val locationManager: LocationManager =
      cntx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }

  @ReactMethod
  fun enableBluetooth(promise: Promise) {
    if (!checkGPSIsOpen()) {
      promise.reject(Exception("Требуется включить GPS"))
    } else {
      driver.enableBt {
        promise.resolve(it)
      }
    }
  }

  @ReactMethod
  fun getPairedDevices(promise: Promise) {
    handlePromiseWrapper(promise) {
      val devices = driver.getPairedDevices()
      val arr = WritableNativeArray()
      devices.forEach {
        arr.pushMap(Arguments.createMap().apply {
          putString("name", it.name)
          putString("mac", it.address)
          putBoolean("isPaired", it.bondState == BluetoothDevice.BOND_BONDED)
        })
      }
      promise.resolve(arr)
    }
  }

  @ReactMethod
  fun isPaired(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {
      promise.resolve(driver.isPaired(mac))
    }
  }

  @ReactMethod
  fun isConnected(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {
      promise.resolve(driver.isConnected(mac))
    }
  }

  @ReactMethod
  fun searchDevices(promise: Promise) {
      driver.searchDevices {
        val arr = WritableNativeArray()
        it.forEach { d: BluetoothDevice ->
          arr.pushMap(Arguments.createMap().apply {
            putString("name", d.name)
            putString("mac", d.address)
            putBoolean("isPaired", d.bondState.equals(BluetoothDevice.BOND_BONDED))

          })
        }
        log("$arr")
        promise.resolve(arr)
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
      promise.reject("404", "Устрройство не найдено. Выполните поиск устройств заново")
    }
  }

  @ReactMethod
  fun asyncConnectWithTimeout(mac: String, promise: Promise) {
    handlePromiseWrapper(promise) {
      driver.asyncConnectWithTimeout(mac)
      promise.resolve(true)
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
