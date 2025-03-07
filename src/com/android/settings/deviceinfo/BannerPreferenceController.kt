/*
 * Copyright (C) 2025 AxionAOSP Android Project
 * Copyright (C) 2025 Ryu-UI Org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo

import android.content.Context
import android.os.storage.StorageManager
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference
import com.android.settings.utils.DeviceInfoUtil
import com.android.settings.utils.LottieAnimationUtils
import com.airbnb.lottie.LottieAnimationView

import com.android.settingslib.deviceinfo.PrivateStorageInfo
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider

class BannerPreferenceController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val bannerPreference = screen.findPreference<LayoutPreference>(KEY_ROM_BANNER) ?: return

        val deviceNameText = bannerPreference.findViewById<TextView>(R.id.device_name)
        deviceNameText?.text = getDeviceName()

        bannerPreference.findViewById<TextView>(R.id.banner_text)?.text =
            "Ryu-UI | ${getOSVersion()}"

        bannerPreference.findViewById<TextView>(R.id.storage_info)?.text =
            "${DeviceInfoUtil.getStorageUsed(mContext)} / ${DeviceInfoUtil.getStorageTotal(mContext)}"
        bannerPreference.findViewById<TextView>(R.id.processor_info)?.text =
            DeviceInfoUtil.getProcessor(bannerPreference.context)
        bannerPreference.findViewById<TextView>(R.id.ram_info)?.text =
            DeviceInfoUtil.getTotalRam()
        bannerPreference.findViewById<TextView>(R.id.camera_info)?.text =
            DeviceInfoUtil.getCamera(bannerPreference.context)
        bannerPreference.findViewById<TextView>(R.id.display_info)?.text =
            DeviceInfoUtil.getScreenResolution(mContext)
        bannerPreference.findViewById<TextView>(R.id.battery_info)?.text =
            DeviceInfoUtil.getBatteryCapacity(mContext)

        val waveView: LottieAnimationView = bannerPreference.findViewById(R.id.wave_view)
        LottieAnimationUtils.applyAnimationColor(mContext, waveView)

        val storageManager = mContext.getSystemService(StorageManager::class.java)
        val volumeProvider = StorageManagerVolumeProvider(storageManager)
        val info = PrivateStorageInfo.getPrivateStorageInfo(volumeProvider)
        val totalBytes = info.totalBytes
        val usedBytes = totalBytes - info.freeBytes

        if (totalBytes > 0) {
            val storagePercentage = (usedBytes.toDouble() / totalBytes.toDouble()) * 100
            updateWaveTranslation(waveView, storagePercentage)
        }

        bannerPreference.findViewById<android.view.View>(R.id.device_name_card)?.setOnClickListener {
            showEditDeviceNameDialog(deviceNameText)
        }
    }

    private fun updateWaveTranslation(waveView: LottieAnimationView, storagePercentage: Double) {
        val maxTranslationYDp = 26f
        val translationYDp = (maxTranslationYDp * (1 - (storagePercentage / 100f))).toFloat()
        val density = mContext.resources.displayMetrics.density
        waveView.translationY = translationYDp * density
    }

    private fun getOSVersion(): String {
        return android.os.SystemProperties.get("ro.ryu.version", "Wakaranai?")
    }

    private fun getDeviceName(): String {
        return Settings.Global.getString(mContext.contentResolver, Settings.Global.DEVICE_NAME)
            ?: android.os.Build.MODEL
    }

    private fun showEditDeviceNameDialog(deviceNameText: TextView?) {
        val editText = EditText(mContext).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(getDeviceName())
        }

        AlertDialog.Builder(mContext)
            .setTitle(R.string.edit_device_name_title)
            .setView(editText)
            .setPositiveButton(R.string.ok) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    Settings.Global.putString(mContext.contentResolver, Settings.Global.DEVICE_NAME, newName)
                    deviceNameText?.text = newName
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY_ROM_BANNER

    companion object {
        private const val KEY_ROM_BANNER = "rom_banner"
    }
}