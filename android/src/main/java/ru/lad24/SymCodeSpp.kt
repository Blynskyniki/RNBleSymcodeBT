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
  var list = mutableSetOf<BluetoothDevice>()

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

  fun searchDevices(cb: (devices: HashSet<BluetoothDevice>) -> Unit) {
    try {
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
              val filteredDevices = list.filter { it.name !== null }.toHashSet()
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
    } catch (e: Error) {
      log("${e.message}");
    }

  }

  private fun createBond(btDevice: BluetoothDevice?, cb: (err: Exception?) -> Unit) {
    val class1 = Class.forName("android.bluetooth.BluetoothDevice")
    val createBondMethod: Method = class1.getMethod("createBond")
    createBondMethod.invoke(btDevice) as Boolean
    val ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST"
    val filter = IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
      addAction(ACTION_PAIRING_REQUEST)
    }
    cntx.registerReceiver(object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val action: String = intent.action!!
        when (action) {
          BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
            log("ACTION_BOND_STATE_CHANGED")

            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if (device.bondState == BluetoothDevice.BOND_BONDED && device.address === btDevice!!.address) {
              log("pairing success")

              cb(null)
            }


          }
          ACTION_PAIRING_REQUEST -> {
            log("ACTION_PAIRING_REQUEST")


          }

        }
      }
    }, filter)

  }

  fun pairDevice(mac: String, cb: (err: Exception?) -> Unit) {
    val device = list.find { it.address == mac }
    if (device !== null) {
      try {
        createBond(device, cb)

      } catch (e: java.lang.Exception) {
        log("${e}")
        cb(java.lang.Exception("Pairing failed"))
      }


    } else {
      log("Device not found")
      cb(java.lang.Exception("Device not found"))
    }


  }

  fun isPaired(mac: String): Boolean {
    val device = btAdapter!!.getRemoteDevice(mac)
    return device !== null && device.bondState == BluetoothDevice.BOND_BONDED
  }

  fun connect(mac: String): Boolean {
    log("connect ${mac}");
    val dev = BluetoothAdapter.getDefaultAdapter()!!.getRemoteDevice(mac)
    log("Bluetooth adapter available,${dev.name}");

    if (!isPaired(mac) || dev === null) {
      return false
    }


    dev.let { device ->
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
