package com.xie.autobtcodec

import android.Manifest
import android.bluetooth.BluetoothCodecConfig
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xie.autobtcodec.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

//LHDC是9
@RuntimePermissions
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val adapter by lazy { BTCodecSpinnerAdapter() }
    private var btWatchService: BTWatchForegroundService? = null
    private var codecType: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = Color.BLACK
        refreshView()
        binding.spCodecTypeSelect.adapter = adapter
        binding.btnEnter.setOnClickListener {
            val config = (binding.spCodecTypeSelect.selectedItem as BluetoothCodecConfig)
            Tools.setIntPreferences(this@MainActivity, Tools.CODEC_TYPE, config.codecType)
            btWatchService?.onNewCodecSet()
            Toast.makeText(
                this,
                "应用${Tools.getCodecName(config.codecType)}",
                Toast.LENGTH_SHORT
            ).show()
        }
        initAfterPermissionsWithPermissionCheck()
    }

    @NeedsPermission(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    fun initAfterPermissions() {
        lifecycleScope.launch{
            delay(1000)
            startWatchService()
        }
    }

    private fun startWatchService() {
        val serviceIntent = Intent(this, BTWatchForegroundService::class.java)
        startForegroundService(serviceIntent)
        bindService(serviceIntent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                btWatchService = (service as? BTWatchForegroundService.BTWatchBinder)?.service
                startQueryService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }

        }, Context.BIND_AUTO_CREATE)
    }

    private fun startQueryService() {
        lifecycleScope.launch {
            while (btWatchService != null) {
                btWatchService?.let {
                    codecType = it.getBTState()
                    refreshView()
                    adapter.setItems(it.getSupportCodecList())
                }
                //每秒读取一次蓝牙状态
                delay(1000)
            }
        }
    }


    private fun refreshView() {
        val isConnected = codecType != -1
        if (!isConnected) {
            binding.llCodecSelect.visibility = View.GONE
            binding.btnEnter.visibility = View.GONE
            binding.tvConnectCodec.text = ""
            binding.tvConnectState.text = getString(R.string.bt_disconnect)
        } else {
            binding.llCodecSelect.visibility = View.VISIBLE
            binding.btnEnter.visibility = View.VISIBLE
            binding.tvConnectCodec.text = String.format(
                "%s:%s",
                getString(R.string.bluetooth_connect_codec),
                Tools.getCodecName(codecType)
            )
            binding.tvConnectState.text = getString(R.string.bt_connected)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}