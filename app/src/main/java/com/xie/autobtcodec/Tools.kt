package com.xie.autobtcodec

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothCodecConfig
import android.bluetooth.BluetoothCodecStatus
import android.content.Context
import androidx.preference.PreferenceManager

/**
 * @Author XIE
 * @Date 2024/11/11
 * @Description
 */
class Tools {

    companion object {
        const val CODEC_TYPE = "codec_type"


        fun getIntPreferences(context: Context, key: String, defaultValue: Int): Int {
            val settings = PreferenceManager
                .getDefaultSharedPreferences(context)
            return settings.getInt(key, defaultValue)
        }

        fun setIntPreferences(context: Context, key: String, value: Int) {
            PreferenceManager
                .getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
        }

        @SuppressLint("MissingPermission")
        fun setCodecType(a2dpService: BluetoothA2dp, type: Int):BluetoothCodecConfig? {
            if (a2dpService.activeDevice != null) {
                val newConfig = BluetoothCodecConfig.Builder()
                    .setCodecType(type)
                    .setCodecPriority(BluetoothCodecConfig.CODEC_PRIORITY_HIGHEST)
                    .build()
                a2dpService.activeDevice?.let { deviceID ->
                    a2dpService.setCodecConfigPreference(deviceID, newConfig)
                }
                return newConfig
            }
            return null
        }


        @SuppressLint("MissingPermission")
        fun getCodecStatus(a2dpService: BluetoothA2dp): BluetoothCodecStatus? {
            a2dpService.activeDevice?.let { deviceID ->
                return a2dpService.getCodecStatus(deviceID)
            }
            return null
        }

        @SuppressLint("MissingPermission")
        fun getSelectableCodecList(a2dpService: BluetoothA2dp): List<BluetoothCodecConfig> {
            a2dpService.activeDevice?.let { deviceID ->
                val status = a2dpService.getCodecStatus(deviceID)
                return status.codecsSelectableCapabilities
            }
            return emptyList()
        }


        fun getCodecName(mCodecType: Int): String {
            when (mCodecType) {
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_SBC -> return "SBC"
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_AAC -> return "AAC"
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_APTX -> return "aptX"
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_APTX_HD -> return "aptX HD"
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_LDAC -> return "LDAC"
                5 -> return "LC3"
                6 -> return "Opus"
                7 -> return "aptX Adaptive"
                8 -> return "aptX TWS+"
                BluetoothCodecConfig.SOURCE_CODEC_TYPE_INVALID -> return "INVALID CODEC"
                else -> {}
            }
            return "UNKNOWN CODEC($mCodecType)"
        }
    }
}