package com.xie.autobtcodec

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothCodecConfig
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author XIE
 * @Date 2024/11/12
 * @Description
 */
class BTWatchForegroundService : Service(), BTCallback {
    private var btBroadcast: BTBroadcast? = null
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val bluetoothManager by lazy { getSystemService(BLUETOOTH_SERVICE) as BluetoothManager }
    private var a2dpService: BluetoothA2dp? = null
    private var codecType: Int = CODEC_TYPE_NONE
    private val binder: IBinder by lazy {
        BTWatchBinder(this@BTWatchForegroundService)
    }

    companion object {
        private const val CHANNEL_ID = "BT_WATCH"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "BTWatchForegroundService"
        const val CODEC_TYPE_NONE = -1
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        btBroadcast = BTBroadcast.register(this, this)
        startForegroundNotification()
        initBTAdapter()
        return super.onStartCommand(intent, flags, startId)
    }

    fun getBTState(): Int {
        return codecType
    }

    fun getSupportCodecList(): List<BluetoothCodecConfig> {
        a2dpService?.let {
            val codecStatus = Tools.getCodecStatus(it)
            return codecStatus?.codecsSelectableCapabilities ?: emptyList()
        }
        return emptyList()
    }

    fun onNewCodecSet() {
        refreshCodec(a2dpService)
    }

    private fun startForegroundNotification() {
        val chanel =
            NotificationChannel(CHANNEL_ID, "蓝牙监听Channel", NotificationManager.IMPORTANCE_LOW)
        chanel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(chanel)

        startForeground(
            NOTIFICATION_ID, getNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            } else {
                0
            }
        )
    }

    private fun getNotification(): Notification {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentText(
                if (codecType == CODEC_TYPE_NONE) "正在等待耳机连接" else "当前编解码器：${
                    Tools.getCodecName(
                        codecType
                    )
                }"
            )
            .setContentTitle("蓝牙解码器自动")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        return notification
    }

    private fun refreshNotification() {
        Log.i(TAG, "refreshNotification: ")
        notificationManager.notify(NOTIFICATION_ID, getNotification())
    }

    private fun initBTAdapter() {
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getProfileProxy(
                applicationContext,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        if (profile == BluetoothProfile.A2DP) {
                            a2dpService = proxy as? BluetoothA2dp
                            a2dpService?.let {
                                refreshCodec(it)
                            }
                        }
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        if (profile == BluetoothProfile.A2DP) {
                            a2dpService = null
                            refreshCodec(null)
                        }
                    }
                },
                BluetoothProfile.A2DP
            )
        }
    }

    private fun refreshCodec(a2dpService: BluetoothA2dp?) {
        if (a2dpService != null) {
            val myCodecType =
                Tools.getIntPreferences(applicationContext, Tools.CODEC_TYPE, CODEC_TYPE_NONE)
            val codecStatus = Tools.getCodecStatus(a2dpService)
            val currentCodecType = codecStatus?.codecConfig?.codecType ?: CODEC_TYPE_NONE
            if (myCodecType != CODEC_TYPE_NONE && currentCodecType != CODEC_TYPE_NONE && currentCodecType != myCodecType) {
                Tools.setCodecType(a2dpService, myCodecType)
                codecType = myCodecType
            } else {
                codecType = currentCodecType
            }
        } else {
            codecType = CODEC_TYPE_NONE
        }
        refreshNotification()
    }

    override fun onBluetoothConnected() {
        GlobalScope.launch {
            //需要延迟1秒等待系统设置设备
            delay(1000)
            refreshCodec(a2dpService)
        }
        Log.i("BTWatchForegroundService", "onBluetoothConnected: ")
    }

    override fun onBluetoothDisconnected() {
        Log.i("BTWatchForegroundService", "onBluetoothDisconnected: ")
        refreshCodec(a2dpService)
    }

    override fun onBluetoothState() {
        Log.i("BTWatchForegroundService", "onBluetoothState: ")
        if (bluetoothManager.adapter?.isEnabled != true) {
            refreshCodec(a2dpService)
        }
    }

    override fun onDestroy() {
        stopForeground(true)
        btBroadcast?.let {
            unregisterReceiver(it)
        }
        super.onDestroy()
    }

    class BTWatchBinder(val service: BTWatchForegroundService) : Binder()
}