package ru.lad24.lib

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.res.Resources
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException

class Symcode(app: Application?) {
  companion object {
    val TAG = "BLE-Symcode"
    val NOTIFY_SERVICE_ID = 40
    fun log(data: String) {
      Log.d(TAG, data)
    }
  }

  // Notify callback
  interface OnNotifyEnableResult {
    fun result(error: Exception?)
  }

  // Scan callback
  interface OnScanBtDevicesResult {
    fun result(error: Exception?, scanResultList: List<BleDevice?>?)
    fun foundNewDevice(bleDevice: BleDevice?)

  }

  private var notifyDevice: BleDevice? = null
  private var notifyServiceUuid: String? = null
  private var notifyServiceCharacteristicsUuid: String? = null

  init {

    BleManager.getInstance().init(app)
    BleManager.getInstance()
      .enableLog(true)
      .setReConnectCount(1, 5000)
      .setSplitWriteNum(20)
      .setConnectOverTime(10000).operateTimeout = 5000
  }


  private fun scan(cb: (error: Exception?, scanResultList: List<BleDevice?>?) -> Unit) {
    BleManager.getInstance().scan(object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            log("start scan$success")
        }

        override fun onScanning(bleDevice: BleDevice?) {
            bleDevice?.name?.let { log(it) }
        }

        override fun onScanFinished(scanResultList: List<BleDevice?>?) {
            log("finish ${scanResultList?.size}")
            if (scanResultList !== null) {
//                     clear empty names BT devices
                val data = scanResultList.filter { it?.name !== null }
                cb(null, data)
            } else {
                cb(Resources.NotFoundException(), null)
            }
        }
    })
  }

  fun scanv2(l: OnScanBtDevicesResult) {
    BleManager.getInstance().scan(object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            log("start scan$success")
        }

        override fun onScanning(bleDevice: BleDevice?) {

            bleDevice?.name?.let {
                l.foundNewDevice(bleDevice)
                log(it)

            }
        }

        override fun onScanFinished(scanResultList: List<BleDevice?>?) {
            log("finish ${scanResultList?.size}")
            if (scanResultList !== null) {
                // clear empty names BT devices
                val data = scanResultList.filter { it?.name !== null }
                l.result(null, data)
            } else {
                l.result(Resources.NotFoundException(), null)
            }
        }
    })
  }

  fun connect(bleDevice: BleDevice, cb: (success: Boolean) -> Unit) {
    val isConnected = BleManager.getInstance().isConnected(bleDevice)
    val name = bleDevice.name
    val mac = bleDevice.mac
    val rssi = bleDevice.rssi


    if (!BleManager.getInstance().isConnected(bleDevice)) {
      log("connect:$isConnected : $name ->> $mac --> $rssi")

      BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
          override fun onStartConnect() {
              log("Start connect")
          }

          override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
              log("Fail connect to device ${exception.description}")
              cb(false)
          }

          override fun onConnectSuccess(
              bleDevice: BleDevice,
              gatt: BluetoothGatt,
              status: Int
          ) {
              log("Connect successful ${bleDevice.mac} $status")
              cb(true)

          }

          override fun onDisConnected(
              isActiveDisConnected: Boolean,
              bleDevice: BleDevice,
              gatt: BluetoothGatt,
              status: Int
          ) {
              log("Device disconnected ${bleDevice.mac} $status")

          }
      })
    }
  }

  fun enableNotify(
    bleDevice: BleDevice,
    resultCb: OnNotifyEnableResult,
    notifyCb: (message: String) -> Unit
  ) {
    val services = BleManager.getInstance().getBluetoothGatt(bleDevice).services
    val notifyService = services.find { it.instanceId.equals(NOTIFY_SERVICE_ID) }
    if (notifyService !== null) {
      BleManager.getInstance().notify(
          bleDevice,
          notifyService.uuid.toString(),
          notifyService.characteristics[0].uuid.toString(),
          object : BleNotifyCallback() {
              override fun onNotifySuccess() {
                  log("onNotifySuccess")
                  this@Symcode.notifyDevice = bleDevice
                  this@Symcode.notifyServiceUuid = notifyService.uuid.toString()
                  this@Symcode.notifyServiceCharacteristicsUuid =
                      notifyService.characteristics[0].uuid.toString()

                  resultCb.result(null)
              }

              override fun onNotifyFailure(exception: BleException) {
                  log("onNotifyFailure")
                  resultCb.result(exception as Exception)
              }

              override fun onCharacteristicChanged(data: ByteArray) {
                  log("onCharacteristicChanged--> ${data.decodeToString()}")
                  notifyCb(data.decodeToString())
              }
          })
    } else {
      resultCb.result(Resources.NotFoundException("Notify service not found"))

    }
  }

  fun disconnect() {
    BleManager.getInstance().stopNotify(
        notifyDevice,
        notifyServiceUuid,
        notifyServiceCharacteristicsUuid
    )
    BleManager.getInstance().disconnect(
        notifyDevice,
    )
    log("Device $notifyServiceUuid -> disconected")
    this@Symcode.notifyDevice = null
    this@Symcode.notifyServiceUuid = null
    this@Symcode.notifyServiceCharacteristicsUuid = null

  }

  private fun isNotifyCharacteristic(prop: Int): Boolean {
    val merge = prop.and(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
    return merge > 0
  }

  fun disableNotify() {
    BleManager.getInstance().stopNotify(
        notifyDevice,
        notifyServiceUuid,
        notifyServiceCharacteristicsUuid
    )
    log("Device $notifyServiceUuid -> notify disabled")
    this@Symcode.notifyDevice = null
    this@Symcode.notifyServiceUuid = null
    this@Symcode.notifyServiceCharacteristicsUuid = null
  }

}
