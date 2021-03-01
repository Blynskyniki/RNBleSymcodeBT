package com.lad24.testSymcode


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clj.fastble.data.BleDevice
import org.jetbrains.anko.toast
import ru.lad24.lib.Symcode
import ru.lad24.lib.Symcode.OnNotifyEnableResult
import trikita.anvil.Anvil
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.layoutGravity
import trikita.anvil.BaseDSL.weight
import trikita.anvil.DSL.*
import trikita.anvil.RenderableAdapter
import trikita.anvil.RenderableView
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
  private val REQUEST_CODE_OPEN_GPS = 1
  private val REQUEST_CODE_PERMISSION_LOCATION = 2
  private var symcode: Symcode? = null
  override fun onCreate(b: Bundle?) {
    super.onCreate(b)
    checkPermissions()
    symcode = Symcode(application!!)
    setContentView(this.Scanview(this))
  }

  fun Scanview(cntx: Context): RenderableView {
    val list = mutableListOf<BleDevice>()
    return object : RenderableView(cntx) {
      override fun view() {
        val mBtList = RenderableAdapter.withItems(
            list
        ) { i: Int, device: BleDevice? ->
          device.let {

            linearLayout {
              size(FILL, WRAP)
              minHeight(dip(72))
              linearLayout {

                linearLayout {
                  orientation(LinearLayout.VERTICAL)
                  textView {
//                                    size(0, WRAP)
//                                    weight(1f)
//                                    layoutGravity(CENTER_VERTICAL)
//                                    padding(dip(5))

                    text(device?.name.orEmpty())

                  }
                  textView {
//                                    size(0, WRAP)
//                                    weight(1f)
//                                    layoutGravity(CENTER_VERTICAL)
//                                    padding(dip(5))
                    if (device != null) {
                      text(device.mac)
                    }
                  }

                }

                button {
                  size(0, WRAP)
                  weight(1f)
                  layoutGravity(CENTER_VERTICAL)
                  text("ВКЛ")
                  onClick { v ->
                    symcode!!.connect(device!!) { success ->
                      if (success) {

                        symcode!!.enableNotify(
                            device,
                            object : OnNotifyEnableResult {
                                override fun result(error: Exception?) {
                                    toast("нотификация включена)")
                                }


                            }) {

                          toast("Code : $it")

                        }
                      } else {
                        toast("Печалька :(")
                      }

                    }

                  }
                }
                button {
                  size(0, WRAP)
                  weight(1f)
                  layoutGravity(CENTER_VERTICAL)
                  text("ОТКЛ")
                  onClick { v ->
                    symcode.let {
                      it?.disconnect()
                    }

                  }
                }
              }

            }

          }

        }
        linearLayout {
          minHeight(dip(72))
          orientation(LinearLayout.VERTICAL)
          text("Выполните сканирование устройств")
          button {
            layoutGravity(CENTER_VERTICAL)
            text("Scan")
            onClick { v ->

              symcode?.scanv2(object : Symcode.OnScanBtDevicesResult {
                  override fun result(
                      error: Exception?,
                      scanResultList: List<BleDevice?>?
                  ) {
                      toast("Сканирование завершено")
                  }

                  override fun foundNewDevice(bleDevice: BleDevice?) {
                      bleDevice.let {
                          it?.let { it1 -> list.add(it1) }
                          Anvil.render()
                      }
                  }

              })

            }
          }

          listView {
            size(FILL, WRAP)
            itemsCanFocus(true)
            onItemClick { parent: AdapterView<*>?, v: View?, pos: Int, id: Long ->

            }
            adapter(mBtList)
          }
        }
      }
    }
  }


  private fun checkPermissions() {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (!bluetoothAdapter.isEnabled) {
      Toast.makeText(this, "Пожалуйста включите синий зуб)", Toast.LENGTH_LONG).show()
      return
    }
    val permissions = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH
    )
    val permissionDeniedList: MutableList<String> = ArrayList()
    for (permission in permissions) {
      val permissionCheck: Int = ContextCompat.checkSelfPermission(this, permission)
      if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        onPermissionGranted(permission)
      } else {
        permissionDeniedList.add(permission)
      }
    }
    if (!permissionDeniedList.isEmpty()) {
      val deniedPermissions = permissionDeniedList.toTypedArray()
      ActivityCompat.requestPermissions(
          this,
          deniedPermissions,
          REQUEST_CODE_PERMISSION_LOCATION
      )
    }
  }

  private fun checkGPSIsOpen(): Boolean {
    val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }

  private fun onPermissionGranted(permission: String) {
    when (permission) {
        Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
            AlertDialog.Builder(this)
                .setTitle("GPs")
                .setMessage("надо включить")
                .setNegativeButton("нет",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .setPositiveButton("да",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                    })
                .setCancelable(false)
                .show()
        }
    }
  }


}


//        setContentView(object : RenderableView(this) {
//            override fun view() {
//                linearLayout {
//                    size(MATCH, MATCH)
//                    padding(dip(8))
//                    orientation(LinearLayout.VERTICAL)
//                    textView {
//                        size(MATCH, WRAP)
//                        text("Tick-tock: $ticktock")
//                    }
//                    button {
//                        size(MATCH, WRAP)
//                        text("Close")
//                        // Finish current activity when the button is clicked
//                        onClick { v -> finish() }
//                    }
//                }
//            }
//        })