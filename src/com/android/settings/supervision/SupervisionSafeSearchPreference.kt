/*
 * Copyright (C) 2025 The Android Open Source Project
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
package com.android.settings.supervision

import android.content.Context
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.Permissions
import com.android.settingslib.datastore.SettingsSecureStore
import com.android.settingslib.metadata.BooleanValuePreference
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.preference.PreferenceBinding
import com.android.settingslib.preference.forEachRecursively
import com.android.settingslib.widget.SelectorWithWidgetPreference

/** Base class of web content filters SafeSearch preferences. */
sealed class SupervisionSafeSearchPreference :
    BooleanValuePreference, SelectorWithWidgetPreference.OnClickListener, PreferenceBinding {
    override fun storage(context: Context): KeyValueStore = SettingsSecureStore.get(context)

    override fun getReadPermissions(context: Context) = Permissions.EMPTY

    override fun getWritePermissions(context: Context) = Permissions.EMPTY

    override fun getReadPermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermit(
        context: Context,
        value: Boolean?,
        callingPid: Int,
        callingUid: Int,
    ) = ReadWritePermit.DISALLOW

    override val sensitivityLevel
        get() = SensitivityLevel.NO_SENSITIVITY

    override fun createWidget(context: Context) = SelectorWithWidgetPreference(context)

    override fun onRadioButtonClicked(emiter: SelectorWithWidgetPreference) {
        emiter.parent?.forEachRecursively {
            if (it is SelectorWithWidgetPreference) {
                it.isChecked = it == emiter
            }
        }
    }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        (preference as SelectorWithWidgetPreference).also {
            // TODO(b/401568995): Set the isChecked value using stored values.
            it.isChecked = (it.key == SupervisionSearchFilterOffPreference.KEY)
            it.setOnClickListener(this)
        }
    }
}

/** The SafeSearch filter on preference. */
class SupervisionSearchFilterOnPreference : SupervisionSafeSearchPreference() {

    override val key
        get() = KEY

    override val title
        get() = R.string.supervision_web_content_filters_search_filter_on_title

    override val summary
        get() = R.string.supervision_web_content_filters_search_filter_on_summary

    companion object {
        const val KEY = "web_content_filters_search_filter_on"
    }
}

/** The SafeSearch filter off preference. */
class SupervisionSearchFilterOffPreference : SupervisionSafeSearchPreference() {

    override val key
        get() = KEY

    override val title
        get() = R.string.supervision_web_content_filters_search_filter_off_title

    override val summary
        get() = R.string.supervision_web_content_filters_search_filter_off_summary

    companion object {
        const val KEY = "web_content_filters_search_filter_off"
    }
}
