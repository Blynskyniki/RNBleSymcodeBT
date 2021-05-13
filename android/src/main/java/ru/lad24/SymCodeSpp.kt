package ru.lad24

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class SymCodeSpp(val cntx: Application) {

  val MY_UUID_SECURE: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
  private var btAdapter: BluetoothAdapter? = null
  private var btDevice: BluetoothDevice? = null
  private var btSocket: BluetoothSocket? = null
  private var reader: BufferedReader? = null
  private var notifyTask: Thread? = null

  init {
    checkPermissions(cntx)
    btAdapter = BluetoothAdapter.getDefaultAdapter()
    if (btAdapter == null || btAdapter?.isEnabled == false) {
      log("Нет доступа к устройству")
      throw Exception("Нет доступа к устройству")
    }
  }

  private fun log(str: String) {
    Log.e("ru.lad24.sppSymcode", str)
  }

  fun connect(): Boolean {

//       Получаем устройство
    log("Bluetooth adapter available");

    val bondedDevices: Set<BluetoothDevice> = btAdapter!!.bondedDevices
    for (dev in bondedDevices) {
      log(
        "Paired device: " + dev.getName().toString() + " (" + dev.getAddress()
          .toString() + ")"
      )
      if (dev.getName().equals("BarCode Scanner spp")) {
        btDevice = dev
        btDevice?.let { device ->
          log("Target Bluetooth device found  ${dev.getName()}")
          try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE)

          } catch (ex: IOException) {
            log("Failed to create RfComm socket: " + ex.toString());
            return false
          }
          log("Created a bluetooth socket. ");
          btSocket?.let { bluetoothSocket ->
            for (i in 1..5) {
              try {
                btSocket!!.connect()
                reader = BufferedReader(InputStreamReader(bluetoothSocket.inputStream, "ASCII"))

                break
              } catch (ex: IOException) {
                if (i < 5) {
                  log("Failed to connect. Retrying: $ex")
                  continue
                }
                log("Failed to connect: $ex")
                return false
              }
            }

          }
        }

      }
    }
    return true
  }

  fun enableNotify(notifyCb: (message: String) -> Unit) {


    notifyTask = Thread {
      try {
        var output: String?
        while (!Thread.currentThread().isInterrupted) {
          output = reader?.readLine()
          output?.let {
            notifyCb(output)
          }

        }
      } catch (e: java.lang.Exception) {
        log("enableNotify: ${e.message}")

      }


    }
    notifyTask!!.start()
  }

  fun disableNotify() {
    notifyTask?.interrupt()
  }

  fun dicsonnect() {
    try {
     this.disableNotify()
      btSocket!!.close()
    } catch (ex: IOException) {
      log("Failed to close the bluetooth socket.")
      return
    }
  }


  fun isConnected(): Boolean {
    return btAdapter !== null && btDevice !== null && btSocket !== null && reader !== null
  }

  private fun checkPermissions(cntx: Context) {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (!bluetoothAdapter.isEnabled) {
      throw  Exception("Bluetooth выключен")
    }
    val permissions = arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.BLUETOOTH
    )
    val permissionDeniedList: MutableList<String> = ArrayList()
    for (permission in permissions) {
      val permissionCheck: Int = ContextCompat.checkSelfPermission(cntx, permission)
      if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        permissionDeniedList.add(permission)
      }
    }
    if (permissionDeniedList.isNotEmpty()) {
      val deniedPermissions = permissionDeniedList.toTypedArray()
      throw Exception("Недостаточно прав:  ${deniedPermissions.first()}")
    }
  }
}
