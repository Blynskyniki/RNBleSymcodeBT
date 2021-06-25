package com.lad24.testSymcode


import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.lad24.SymCodeSpp
import java.lang.Exception

class MainActivity : ComponentActivity() {
  private val REQUEST_CODE_OPEN_GPS = 1
  private val REQUEST_CODE_PERMISSION_LOCATION = 2
  private val MY_SSP_MAC = "AA:A8:A3:00:94:6D"
  var barcodeState: MutableState<String> = mutableStateOf("")
  lateinit var scaner: SymCodeSpp;


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    scaner = SymCodeSpp(application)
    setContent {
      SymcodeView(application)
    }
  }


  @Composable
  fun SymcodeView(c: Application) = MaterialTheme {
    Column() {

      Column(Modifier.padding(all = 0.dp)) {
        TopAppBar(
          title = { Text(text = "Symcode BT SPP TEST APP") }
        )
      }

      Column(Modifier.padding(all = 5.dp)) {
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {

          SymcodeButton("Сканирование") {
            barcodeState.value = ""

            scaner.enableBt {
              val paired = scaner.getPairedDevices()
              barcodeState.value =  "Paired: \n " +  paired.map { "${it.name} ${it.address}  \n" }.toString()
              scaner.searchDevices() {
                barcodeState.value = "discovery: \n" + it.map { "${it.name} ${it.address}  \n" }.toString()

              }
            }


          }
        }


      }
      Column(Modifier.padding(all = 2.dp)) {
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {

          SymcodeButton("Паринг с ${MY_SSP_MAC}") {
            barcodeState.value = ""

            scaner.pairDevice("AA:A8:A3:00:94:6D") {
              if (it !== null) {
                barcodeState.value = it.message.toString()
                return@pairDevice
              }
              barcodeState.value = "paired"
            }

          }
        }


      }


      Column(Modifier.padding(all = 2.dp)) {
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {

          SymcodeButton("Connect") {
            barcodeState.value = "Подключить"
            scaner = SymCodeSpp(c)
            if (scaner.connect(MY_SSP_MAC)) {
              barcodeState.value = "Сканер подключен"
            } else {
              barcodeState.value = "Не смог установить соединение :("
            }
            scaner.enableNotify {
              it
              barcodeState.value = it
            }
          }
          SymcodeButton("check ") {
            barcodeState.value = ""

            if(scaner.isConnected(MY_SSP_MAC)) {
              barcodeState.value = "check: Подключен"
            }else{
              barcodeState.value = "check: Не Подключен"
            }

          }
          SymcodeButton("disconnect") {
            barcodeState.value = "Сканер отключен"
            try {
              scaner.disableNotify()
              scaner.dicsonnect()
            } catch (e: Exception) {
              barcodeState.value = "Ошибка отключения : ${e.message}"
            }

          }


        }
        Card(
          Modifier
            .fillMaxWidth()
//            .fillMaxHeight()
            .border(BorderStroke(2.dp, MaterialTheme.colors.contentColorFor(Color.White))),


          ) {
          Text(
            text = barcodeState.value,
            Modifier
              .fillMaxWidth()
              .padding(all = 5.dp)
              .height(150.dp)
              .fillMaxHeight()
          )

        }
        Text(
          text = "Scan to SPP mode :)",
          Modifier

            .height(50.dp)
            .align(Alignment.CenterHorizontally)

        )


        Image(
          painter = painterResource(R.mipmap.qr),
          contentDescription = "Scan to SPP mode :)",
          Modifier
            .fillMaxWidth()
            .fillMaxHeight()
        )


      }
    }
  }


  @Composable
  fun SymcodeButton(name: String, cb: () -> Unit) {
    Column(
      modifier = Modifier.padding(all = 10.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(
        onClick = cb,
        modifier = Modifier.padding(horizontal = 5.dp),


        ) {
        Text(name)
      }
    }

  }

//  @Preview
//  @Composable
//  fun DefaultPreview() {
//    SymcodeView()
//  }


  fun log(str: String) {
    Log.e("ru.lad24.bt_cymcode", str)
  }


  private fun checkPermissions() {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (!bluetoothAdapter.isEnabled) {
      Toast.makeText(this, "Пожалуйста включите синий зуб)", Toast.LENGTH_LONG).show()
      return
    }
    val permissions = arrayOf(
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
    if (permissionDeniedList.isNotEmpty()) {
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
            { dialog, which -> finish() })
          .setPositiveButton("да"
          ) { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
          }
          .setCancelable(false)
          .show()
      }
    }
  }


}

