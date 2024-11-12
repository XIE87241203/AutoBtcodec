package com.xie.autobtcodec

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * @Author XIE
 * @Date 2024/11/11
 * @Description
 */
// TODO: 使用前台服务替代，在服务内注册广播，打包之后还原android32
class BTBroadcast private constructor(private val btCallback: BTCallback) : BroadcastReceiver() {
    companion object {
        fun register(callback: BTCallback, context: Context): BTBroadcast {
            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            val receiver = BTBroadcast(callback)
            context.registerReceiver(receiver, filter)
            return receiver
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BTBroadcast", "BroadcastReceiver onReceive ${intent.action}")
        if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
            btCallback.onBluetoothConnected()
        }
        if (intent.action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            btCallback.onBluetoothDisconnected()
        }
        if (intent.action == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) {
            btCallback.onBluetoothState()
        }
    }
}

interface BTCallback {
    fun onBluetoothConnected()
    fun onBluetoothDisconnected()
    fun onBluetoothState()
}