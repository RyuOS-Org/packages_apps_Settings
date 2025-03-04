/*
 * Copyright (C) 2023 the RisingOS Android Project
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
package com.android.settings.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.hardware.display.DisplayManager
import android.view.Display
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.storage.StorageManager
import com.android.settings.R

import com.android.internal.os.PowerProfile
import com.android.internal.util.MemInfoReader

import com.android.settingslib.deviceinfo.PrivateStorageInfo
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider

import kotlin.math.ceil
import kotlin.math.roundToInt

object DeviceInfoUtil {

    fun getProcessor(): String {
        val model = SystemProperties.get("ro.product.model", "").lowercase()
        val numberMatch = Regex("""\b(pixel\s*)(\d+)([a-z\s]*)\b""").find(model)
        val number = numberMatch?.groups?.get(2)?.value?.toIntOrNull()
        return when (number) {
            6 -> "Google Tensor"
            7 -> "Google Tensor G2"
            8 -> "Google Tensor G3"
            9 -> "Google Tensor G4"
            else -> SystemProperties.get("persist.sys.device_processor_info", "Unknown")
        }
    }

    fun getTotalRam(): String {
        val memInfoReader = MemInfoReader()
        memInfoReader.readMemInfo()
        val totalMemoryBytes = memInfoReader.totalSize
        val totalMemoryGB = totalMemoryBytes / (1024.0 * 1024.0 * 1024.0)
        val roundedMemoryGB = roundToNearestKnownRamSize(totalMemoryGB)
        return "$roundedMemoryGB GB"
    }

    private fun roundToNearestKnownRamSize(memoryGB: Double): Int {
        val knownSizes = arrayOf(1, 2, 3, 4, 6, 8, 10, 12, 16, 32, 48, 64)
        if (memoryGB <= 0) return 1
        for (size in knownSizes) {
            if (memoryGB <= size) return size
        }
        return knownSizes.last()
    }

    fun getStorageTotal(context: Context): String {
        val storageManager = context.getSystemService(StorageManager::class.java)
        val volumeProvider = StorageManagerVolumeProvider(storageManager)
        val info = PrivateStorageInfo.getPrivateStorageInfo(volumeProvider)
        val totalStorageBytes = info.totalBytes
        val totalStorageGB = totalStorageBytes / (1024.0 * 1024.0 * 1024.0)
        val roundedStorageGB = roundToNearestKnownStorageSize(totalStorageGB)
        return if (roundedStorageGB >= 1024) {
            "${roundedStorageGB / 1024} TB"
        } else {
            "$roundedStorageGB GB"
        }
    }

    fun getStorageUsed(context: Context): String {
        val storageManager = context.getSystemService(StorageManager::class.java)
        val volumeProvider = StorageManagerVolumeProvider(storageManager)
        val info = PrivateStorageInfo.getPrivateStorageInfo(volumeProvider)
        val usedBytes = info.totalBytes - info.freeBytes
        val usedGB = usedBytes / (1024.0 * 1024.0 * 1024.0)
        val formattedUsedGB = String.format("%.1f", usedGB)

        return if (usedGB >= 1024) {
            val usedTB = usedGB / 1024
            String.format("%.2f TB", usedTB)
        } else {
            "$formattedUsedGB GB"
        }
    }

    fun getStorageAvailable(context: Context): String {
        val storageManager = context.getSystemService(StorageManager::class.java)
        val volumeProvider = StorageManagerVolumeProvider(storageManager)
        val info = PrivateStorageInfo.getPrivateStorageInfo(volumeProvider)
        val availableGB = info.freeBytes / (1024.0 * 1024.0 * 1024.0)
        val roundedAvailableGB = availableGB.roundToInt()
        return if (roundedAvailableGB >= 1024) {
            "${roundedAvailableGB / 1024} TB"
        } else {
            "$roundedAvailableGB GB"
        }
    }

    private fun roundToNearestKnownStorageSize(storageGB: Double): Int {
        val knownSizes = arrayOf(16, 32, 64, 128, 256, 512, 1024)
        if (storageGB <= 8) return ceil(storageGB).toInt()
        for (size in knownSizes) {
            if (storageGB <= size) return size
        }
        return ceil(storageGB).toInt()
    }

    fun getBatteryCapacity(context: Context): String {
        val model = SystemProperties.get("ro.product.model", "").lowercase()
        val numberMatch = Regex("""\b(pixel\s*)(\d+)([a-z\s]*)\b""").find(model)
        val number = numberMatch?.groups?.get(2)?.value?.toIntOrNull()
        val variant = numberMatch?.groups?.get(3)?.value?.trim() ?: ""
        val batteryCapacity = when (number) {
            6 -> when {
                variant.contains("pro") -> 5003
                variant.contains("a") -> 4410
                else -> 4614
            }
            7 -> when {
                variant.contains("pro") -> 5000
                variant.contains("a") -> 4385
                else -> 4355
            }
            8 -> when {
                variant.contains("pro") -> 5050
                variant.contains("a") -> 4492
                else -> 4575
            }
            9 -> when {
                variant.contains("pro xl") -> 5060
                variant.contains("pro") -> 4700
                else -> 4700
            }
            else -> {
                PowerProfile(context).getAveragePower(PowerProfile.POWER_BATTERY_CAPACITY).roundToInt()
            }
        }
        return "$batteryCapacity mAh"
    }

    fun getScreenResolution(context: Context): String {
        val dm = context.getSystemService(DisplayManager::class.java)
        val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
        val height = display?.mode?.physicalHeight
        val width = display?.mode?.physicalWidth
        return "${width} x ${height}"
    }

    fun getFrontCameraMegapixels(context: Context): String {
      return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var frontCameraMp = context.getString(R.string.device_not_available)

        for (cameraId in cameraManager.cameraIdList) {
          val characteristics = cameraManager.getCameraCharacteristics(cameraId)

          if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)!!
            val mp = (sensorSize.width * sensorSize.height) / 1_000_000.0

            frontCameraMp = "${context.getString(R.string.device_front_camera)} ${"%.1f".format(mp)} MP"
            break
          }
        }
        frontCameraMp
      } catch (e: Exception) {
        context.getString(R.string.not_available)
      }
    }

    fun getRearCameraMegapixels(context: Context): String {
      return try {
          val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
          val rearCameras = mutableListOf<Pair<Float, String>>()

          cameraManager.cameraIdList.forEach { cameraId ->
              val characteristics = cameraManager.getCameraCharacteristics(cameraId)

              if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                  val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                  val colorFilter = characteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)

                  if (sensorSize != null && colorFilter != ColorFilterArrangement.MONO) {
                      val mp = (sensorSize.width * sensorSize.height) / 1_000_000f
                      val type = when {
                          characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                              ?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA) == true -> ""
                          mp > 40 -> context.getString(R.string.ultra_high_res)
                          mp > 12 -> context.getString(R.string.main_camera)
                          mp > 8 -> context.getString(R.string.wide_camera)
                          else -> context.getString(R.string.other_camera)
                      }
                      rearCameras.add(mp to type)
                  }
              }
          }
          formatCameraSpecs(context, rearCameras)
      } catch (e: CameraAccessException) {
          context.getString(R.string.camera_access_error)
      } catch (e: SecurityException) {
          context.getString(R.string.camera_permission_denied)
      } catch (e: Exception) {
          context.getString(R.string.unknown)
      }
  }

  private fun formatCameraSpecs(cameras: List<Pair<Float, String>>): String {
    return if (cameras.isNotEmpty()) {
        val specs = cameras.sortedByDescending { it.first }.map { (mp, type) ->
            val formattedMp = "%.1f".format(mp).removeSuffix(".0")
            when {
                type.isNotEmpty() -> "$formattedMp MP ($type)"
                else -> "$formattedMp MP"
            }
        }
        "${context.getString(R.string.device_rear_camera)}: " + specs.joinToString(" + ")
    } else {
      "Unknown"
    }
  }
}