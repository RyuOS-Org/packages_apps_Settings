/*
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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class RyuMaintainerPreferenceController(
  context: Context,
  key: String
) : BasePreferenceController(context, key) {

  companion object {
    private const val TAG = "RyuMaintainerPreferenceController"
    private const val ROM_PROPERTY = "ro.ryu.maintainer"
  }

  override fun getAvailabilityStatus(): Int {
    return AVAILABLE
  }

  override fun getSummary(): CharSequence {
    val dev = SystemProperties.get(ROM_PROPERTY, mContext.getString(R.string.ryu_maintainer_summary))
    return dev
  }
}