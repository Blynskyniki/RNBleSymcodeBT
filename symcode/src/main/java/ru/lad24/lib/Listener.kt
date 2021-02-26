package ru.lad24.lib

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


class Listener(val BT_UUID: UUID, val cb: (data: String) -> Unit) : Thread() {
    private var btAdapter: BluetoothAdapter? = null
    private val socketString = "a random string"
    private var btServerSocket: BluetoothServerSocket? = null
    private var btConnectedSocket: BluetoothSocket? = null
    private val TAG = "Symcode"
    private var connected: Boolean
    override fun run() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        try {
            Log.i(TAG, "getting socket from adapter")
            btServerSocket =
                btAdapter?.listenUsingRfcommWithServiceRecord(socketString, BT_UUID)
            listen()
        } catch (ex: IOException) {
            Log.e(TAG, "error while initializing")
        }
    }

    private fun listen() {
        Log.i(TAG, "listening")
        btConnectedSocket = null
        while (!connected) {
            Log.i(TAG,"${btConnectedSocket != null}")

            try {
                btConnectedSocket = btServerSocket!!.accept()

            } catch (ex: IOException) {
                Log.e(TAG, "connection failed")
                connectionFailed()
            }
            if (btConnectedSocket != null) {
                broadcast()
            } else {
                Log.i(TAG, "socket is null")
                connectionFailed()
            }
        }
    }

    private fun broadcast() {
        try {
            Log.i(TAG, "connected? " + btConnectedSocket!!.isConnected)
            connected = true
            val buffer = ByteArray(4)
            val inputStream: InputStream? = btConnectedSocket?.getInputStream();
            var length = inputStream?.read(buffer)
            while (length !== -1) {

                cb(buffer.toString())

                Log.d("ConnectBtThread", "${buffer.toString()}")
                inputStream?.read(buffer)
            }
        } catch (runTimeEx: RuntimeException) {
        }
        closeServerSocket()
    }

    private fun connectionFailed() {}
    fun closeServerSocket() {
        try {
            btServerSocket!!.close()
        } catch (ex: IOException) {
            Log.e("$TAG:cancel", "error while closing server socket")
        }
    }

    companion object {
        /*package-protected*/
        const val ACTION = "Bluetooth socket is connected"
    }

    init {
        connected = false
    }
}