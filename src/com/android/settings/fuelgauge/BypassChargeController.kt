package com.android.settings.fuelgauge

/*
* Copyright (C) 2025 Ryu-UI Org
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the
* License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/

import android.content.Context
import android.provider.Settings
import com.android.settings.core.TogglePreferenceController

class BypassChargeController(
  context: Context,
  preferenceKey: String
) : TogglePreferenceController(context, preferenceKey) {

  companion object {
    private const val PROP_SUPPORTED = "persist.sys.battery_bypass_supported"
    private const val PROP_ENABLED = "persist.sys.battery_health_bypass_enabled"
  }

  override fun getAvailabilityStatus(): Int {
    return AVAILABLE
  }

  override fun isChecked(): Boolean {
    return Settings.Global.getInt(
      mContext.contentResolver,
      PROP_ENABLED,
      0
    ) == 1
  }

  override fun setChecked(isChecked: Boolean): Boolean {
    return Settings.Global.putInt(
      mContext.contentResolver,
      PROP_ENABLED,
      if (isChecked) 1 else 0
    )
  }

  override fun isSliceable(): Boolean {
    return true
  }

  override fun getSliceHighlightMenuRes(): Int {
    return 0
  }
}