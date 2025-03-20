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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.settings.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupervisionSafeSearchPreferenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val searchFilterOnPreference = SupervisionSearchFilterOnPreference()

    private val searchFilterOffPreference = SupervisionSearchFilterOffPreference()

    @Test
    fun getTitle_filterOn() {
        assertThat(searchFilterOnPreference.title)
            .isEqualTo(R.string.supervision_web_content_filters_search_filter_on_title)
    }


    @Test
    fun getSummary_filterOn() {
        assertThat(searchFilterOnPreference.summary)
            .isEqualTo(R.string.supervision_web_content_filters_search_filter_on_summary)
    }

    @Test
    fun getTitle_filterOff() {
        assertThat(searchFilterOffPreference.title)
            .isEqualTo(R.string.supervision_web_content_filters_search_filter_off_title)
    }

    @Test
    fun getSummary_filterOff() {
        assertThat(searchFilterOffPreference.summary)
            .isEqualTo(
                R.string.supervision_web_content_filters_search_filter_off_summary
            )
    }
}
