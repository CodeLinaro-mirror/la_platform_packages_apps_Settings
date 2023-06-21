/*
 * Copyright (C) 2022 The Android Open Source Project
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
 *
 * Changes from Qualcomm Innovation Center are provided under the
 * following license:
 *
 * Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

/**
 * Preference controller to control A2DP Role
 */
public class BluetoothA2dpRolePreferenceController extends
        DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener,
        PreferenceControllerMixin {

    private static final String TAG = "A2dpRole";
    private static final String A2DP_SINK_ROLE_KEY =
            "bluetooth_disable_a2dp_sink_and_enable_a2dp_source";
    @VisibleForTesting
    static final String A2DP_SINK_ROLE_PROPERTY =
            "persist.vendor.service.bt.a2dp.sink";

    @VisibleForTesting
    static final String A2DP_SINK_ROLE_ENABLED = "true";
    @VisibleForTesting
    static final String A2DP_SINK_ROLE_DISABLED = "false";

    @VisibleForTesting
    boolean mChanged = false;

    private final DevelopmentSettingsDashboardFragment mFragment;

    public BluetoothA2dpRolePreferenceController(Context context,
            DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        mFragment = fragment;
    }

    @Override
    public String getPreferenceKey() {
        return A2DP_SINK_ROLE_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        A2dpSinkRebootDialog.show(mFragment);
        mChanged = true;
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final boolean currentValue =
                 SystemProperties.getBoolean(A2DP_SINK_ROLE_PROPERTY, true);
        ((SwitchPreference) mPreference).setChecked(currentValue);
    }


    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        try {
            SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, A2DP_SINK_ROLE_ENABLED);
            ((SwitchPreference) mPreference).setChecked(false);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to set A2DP sink system property: " + e.getMessage());
        }
    }


    /**
     * Check whether the current setting is the default value or not.
     */
    public boolean isDefaultValue() {
       try {
            final String currentValue = SystemProperties.get(A2DP_SINK_ROLE_PROPERTY);
            return !currentValue.equals(A2DP_SINK_ROLE_ENABLED);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to get A2DP sink system property: " + e.getMessage());
        }
        return true;
    }

    /**
     * Called when the A2dpSinkRebootDialog confirm is clicked.
     */
    public void onA2dpSinkRebootDialogConfirmed() {
        if (!mChanged) {
            return;
        }
        try {
            final String currentValue = SystemProperties
                    .get(A2DP_SINK_ROLE_PROPERTY, A2DP_SINK_ROLE_ENABLED);
            if (currentValue.equals(A2DP_SINK_ROLE_DISABLED)) {
                SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, A2DP_SINK_ROLE_ENABLED);
            } else {
                SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, A2DP_SINK_ROLE_DISABLED);
            }
            updateState(mPreference);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to set A2DP sink system property: " + e.getMessage());
        }
    }


    /**
     * Called when the A2dpSinkRebootDialog cancel is clicked.
     */
    public void onA2dpSinkRebootDialogCanceled() {
        mChanged = false;
    }
}
