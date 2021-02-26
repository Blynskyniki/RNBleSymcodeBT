package com.example.bt_cymcode


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
import android.util.Log
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
import ru.lad24.lib.BleSymcode
import trikita.anvil.BaseDSL.WRAP
import trikita.anvil.BaseDSL.layoutGravity
import trikita.anvil.BaseDSL.weight
import trikita.anvil.DSL.*
import trikita.anvil.RenderableAdapter
import trikita.anvil.RenderableView
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_OPEN_GPS = 1
    private val REQUEST_CODE_PERMISSION_LOCATION = 2
    private var symcode: BleSymcode? = null;
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        checkPermissions()
        symcode = BleSymcode(getApplication()!!);
        setContentView(this.Scanview(this))
    }

    fun Scanview(cntx: Context): RenderableView {

        return object : RenderableView(cntx) {
            override fun view() {

                linearLayout {
                    minHeight(dip(72))
                    orientation(LinearLayout.VERTICAL)
                    text("Выполните сканирование устройств")
                    button {
                        layoutGravity(CENTER_VERTICAL)
                        text("Scan")
                        onClick { v ->
                            symcode?.scan { err, scanResultList ->
                                if (err !== null) {
                                    err?.message?.let { toast(it) }
                                }


                                    setContentView(Selectview(cntx, scanResultList))



                            }

                        }
                    }


                }
            }
        }
    }

    fun Selectview(cntx: Context, list: List<BleDevice?>?): RenderableView {

        return object : RenderableView(cntx) {
            override fun view() {

                val mBtList = RenderableAdapter.withItems(
                    list
                ) { i: Int, device: BleDevice? ->
                    device.let {

                        linearLayout {
                            size(FILL, WRAP)
                            minHeight(dip(72))
                            textView {
                                size(0, WRAP)
                                weight(1f)
                                layoutGravity(CENTER_VERTICAL)
                                padding(dip(5))

                                text(device?.name.orEmpty())

                            }
                            textView {
                                size(0, WRAP)
                                weight(1f)
                                layoutGravity(CENTER_VERTICAL)
                                padding(dip(5))
                                if (device != null) {
                                    text(device.mac)
                                }
                            }
                            textView {
                                size(0, WRAP)
                                weight(1f)
                                layoutGravity(CENTER_VERTICAL)
                                padding(dip(5))
                                if (device != null) {
                                    text(device.key)
                                }
                            }
                            button {
                                size(0, WRAP)
                                weight(1f)
                                layoutGravity(CENTER_VERTICAL)
                                text("ТЫК")
                                onClick { v ->
                                    symcode!!.connect(device!!){ success ->
                                    if(success){
                                        toast("Урааа :)")
                                        symcode!!.enableNotify(device!!)
                                    }else{
                                        toast("Печалька :(")
                                    }

                                    }

                                }}

                        }

                    }

                }
                mBtList.notifyDataSetChanged();
                linearLayout {
                    size(MATCH, MATCH)
                    padding(dip(8))
                    orientation(LinearLayout.VERTICAL)
                    button {
                        size(FILL, WRAP)
                        padding(dip(5))
                        text("Back")

                        onClick { v -> setContentView(Scanview(cntx)) }
                    }
                    text("Select device")


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
            ?: return false
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


fun scan() {

}

fun log(data: String?) {
    data?.let {

        Log.w("Symcode", data)
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