package ru.lad24.lib

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


public class ConnectBtThread {
    private var bTSocket: BluetoothSocket? = null
    fun connect(bTDevice: BluetoothDevice, mUUID: UUID): Boolean {

        bTSocket = try {
            bTDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
        } catch (e: IOException) {
            Log.d("ConnectBtThread", "Could not create RFCOMM socket:" + e.toString())
            return false
        }
        try {
            bTSocket?.connect()
        } catch (e: IOException) {
            Log.d("ConnectBtThread", "Could not connect: " + e.toString())
            this.cancel()
            return false

        }
        return true
    }

    fun addSocketListener(cb: (data: String) -> Unit) {
        val buffer = ByteArray(4)
        val input = ByteArrayInputStream(buffer)
        val inputStream: InputStream? = bTSocket?.getInputStream();
        var length = inputStream?.read(buffer)
        while (length !== -1) {

            cb(buffer.toString())

            Log.d("ConnectBtThread", "${buffer.toString()}")
            inputStream?.read(buffer)
        }


    }

    fun cancel(): Boolean {
        try {
            bTSocket?.close()
        } catch (e: IOException) {
            Log.d("ConnectBtThread", "Could not close connection:" + e.toString())
            return false
        }
        return true
    }
}