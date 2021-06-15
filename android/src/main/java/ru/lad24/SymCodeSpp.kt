package ru.lad24

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SymCodeSpp(val cntx: Application) {

  val MY_UUID_SECURE: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
  private var btAdapter: BluetoothAdapter? = null
  private var btDevice: BluetoothDevice? = null
  private var btSocket: BluetoothSocket? = null
  private var reader: BufferedReader? = null
  private var notifyTask: Thread? = null
  var list = mutableListOf<BluetoothDevice>()

  init {
    btAdapter = BluetoothAdapter.getDefaultAdapter()
    if (btAdapter == null || btAdapter?.isEnabled == false) {
      log("Нет доступа к устройству")
      throw Exception("Нет доступа к устройству")
    }


  }

  private fun log(str: String) {
    Log.e("ru.lad24.sppSymcode", str)
  }

  fun searchDevices(cb: (devices: MutableList<BluetoothDevice>) -> Unit) {
    list.clear()
    if (btAdapter?.isDiscovering == true) btAdapter?.cancelDiscovery()
    btAdapter?.startDiscovery()

    val filter = IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_FOUND)
      addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
      addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
    }
    cntx.registerReceiver(object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val action: String = intent.action!!
        when (action) {
          BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
            log("ACTION_DISCOVERY_FINISHED")
            btAdapter?.cancelDiscovery()
            val filteredDevices = list.filter { it.name !== null }.toMutableList()
            filteredDevices.forEach {
              log("${it.name} ${it.address}")
            }

            cb(filteredDevices)
          }
          BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
            log("ACTION_DISCOVERY_STARTED")
          }
          BluetoothDevice.ACTION_FOUND -> {
            val device: BluetoothDevice =
              intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            list.add(device)
          }
        }
      }
    }, filter)
  }

  private fun createBond(btDevice: BluetoothDevice?): Boolean {
    val class1 = Class.forName("android.bluetooth.BluetoothDevice")
    val createBondMethod: Method = class1.getMethod("createBond")
    val returnValue = createBondMethod.invoke(btDevice) as Boolean
    return returnValue
  }

  fun pairDevice(mac: String, cb: (err: Exception?) -> Unit) {
    val device = list.find { it.address == mac }
    if (device !== null) {

      try {
        val isBonded = createBond(device)
        if (isBonded) {
          log("Paired")
        }
      } catch (e: java.lang.Exception) {
        e.message?.let { log(it) }
      }


    } else {
      log("Device not found")

      cb(java.lang.Exception("Device not found"))
    }


  }

  fun isPaired(mac: String): Boolean {
    return btAdapter!!.bondedDevices.find { it.address === mac } !== null
  }

  fun connect(mac: String): Boolean {
    val device = btAdapter!!.getRemoteDevice(mac)
    log("Bluetooth adapter available,${device.name}");

    log("Target Bluetooth device found  ${device.getName()}")
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
