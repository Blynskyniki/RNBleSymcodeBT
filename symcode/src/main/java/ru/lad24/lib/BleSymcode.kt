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

class BleSymcode(app: Application?) {
    companion object {
        val TAG = "BleSymcodeBT"
        val NOTIFY_SERVICE_ID = 40;
        fun l(s: String) {
            Log.d(TAG, s)
        }
    }

    interface onNotifyEnableResult {
        fun result(error: Exception?)
    }

    private var notifyDevice: BleDevice? = null
    private var notifyServiceUuid: String? = null
    private var notifyServiceCharacteristicsUuid: String? = null

    init {
        BleManager.getInstance().init(app);
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setSplitWriteNum(20)
            .setConnectOverTime(10000)
            .setOperateTimeout(5000);
    }


    fun scan(cb: (error: Exception?, scanResultList: List<BleDevice?>?) -> Unit) {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                l("start scan$success")
            }

            override fun onScanning(bleDevice: BleDevice?) {
                bleDevice?.name?.let { l(it) }
            }

            override fun onScanFinished(scanResultList: List<BleDevice?>?) {
                l("finish ${scanResultList?.size}")
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

    fun connect(bleDevice: BleDevice, cb: (success: Boolean) -> Unit) {
        val isConnected = BleManager.getInstance().isConnected(bleDevice);
        val name = bleDevice.getName();
        val mac = bleDevice.getMac();
        val rssi = bleDevice.getRssi();
        l("connect:$isConnected : $name ->> $mac --> $rssi")
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
                override fun onStartConnect() {
                    l("onStartConnect")

                }

                override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                    l("onConnectFail")
                    cb(false)
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice,
                    gatt: BluetoothGatt,
                    status: Int
                ) {
                    l("onConnectSuccess")
                    cb(true)

                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    bleDevice: BleDevice,
                    gatt: BluetoothGatt,
                    status: Int
                ) {
                    l("onConnectSuccess")

                }
            })
        }
    }

    fun enableNotify(
        bleDevice: BleDevice,
        resultCb: onNotifyEnableResult,
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
                        l("onNotifySuccess")
                        this@BleSymcode.notifyDevice = bleDevice
                        this@BleSymcode.notifyServiceUuid = notifyService.uuid.toString()
                        this@BleSymcode.notifyServiceCharacteristicsUuid =
                            notifyService.characteristics[0].uuid.toString()

                        resultCb.result(null)
                    }

                    override fun onNotifyFailure(exception: BleException) {
                        l("onNotifyFailure")
                        resultCb.result(exception as Exception)
                    }

                    override fun onCharacteristicChanged(data: ByteArray) {
                        l("onCharacteristicChanged--> ${data.decodeToString()}")
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
        l("Device $notifyServiceUuid ->disconected")
        this@BleSymcode.notifyDevice = null
        this@BleSymcode.notifyServiceUuid = null
        this@BleSymcode.notifyServiceCharacteristicsUuid = null

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
        this@BleSymcode.notifyDevice = null
        this@BleSymcode.notifyServiceUuid = null
        this@BleSymcode.notifyServiceCharacteristicsUuid = null
    }

}
//if ((btn.getText().toString() == getActivity().getString(R.string.open_notification))){
//    btn.setText(getActivity().getString(R.string.close_notification))

//} else {
//    btn.setText(getActivity().getString(R.string.open_notification))

//}
