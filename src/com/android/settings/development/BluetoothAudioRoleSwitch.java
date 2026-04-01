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
 * Changes from Qualcomm Technologies, Inc. are provided under the following license:
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.settings.development;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
public class BluetoothAudioRoleSwitch extends
        DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener,
        PreferenceControllerMixin {

    private static final String TAG = "A2dpRole";
    private static final String A2DP_SINK_ROLE_KEY =
            "bluetooth_enable_audio_source_role";
    @VisibleForTesting
    static final String A2DP_SINK_ROLE_PROPERTY =
            "persist.vendor.service.bt.a2dp.sink";

    @VisibleForTesting
    static final String BLUETOOTH_PROFILES_SOURCE_ROLE_PROPERTY =
            "persist.vendor.service.bt.source.role.enabled";

    @VisibleForTesting
    static final String BLUETOOTH_PROFILES_SINK_ROLE_PROPERTY =
            "persist.vendor.service.bt.sink.role.enabled";

    @VisibleForTesting
    static final String HFP_CLIENT_ROLE_PROPERTY =
            "persist.vendor.service.bt.hfp.client";

    @VisibleForTesting
    static final String ROLE_ENABLED = "true";
    @VisibleForTesting
    static final String ROLE_DISABLED = "false";

    @VisibleForTesting
    boolean mChanged = false;
    private BroadcastReceiver mReceiver;
    private Context mContext;

    private final DevelopmentSettingsDashboardFragment mFragment;

    public BluetoothAudioRoleSwitch(Context context,
            DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        mContext = context;
        mFragment = fragment;
        mReceiver =  new BTStateChangeReceiver();
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public String getPreferenceKey() {
        return A2DP_SINK_ROLE_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        BluetoothAudioRoleSwitchRebootDialog.show(mFragment);
        mChanged = true;
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final boolean currentValue =
                 SystemProperties.getBoolean(A2DP_SINK_ROLE_PROPERTY, false);
        ((SwitchPreference) mPreference).setChecked(!currentValue);
    }


    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        try {
            SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, ROLE_ENABLED);
            SystemProperties.set(HFP_CLIENT_ROLE_PROPERTY, ROLE_ENABLED);
            SystemProperties.set(BLUETOOTH_PROFILES_SINK_ROLE_PROPERTY, ROLE_ENABLED);
            SystemProperties.set(BLUETOOTH_PROFILES_SOURCE_ROLE_PROPERTY, ROLE_DISABLED);
            ((SwitchPreference) mPreference).setChecked(false);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to set A2DP sink and HFP Client system property: " + e.getMessage());
        }
    }


    /**
     * Check whether the current setting is the default value or not.
     */
    public boolean isDefaultValue() {
       try {
            final String currentValue = SystemProperties.get(A2DP_SINK_ROLE_PROPERTY);
            return !currentValue.equals(ROLE_ENABLED);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to get A2DP sink system property: " + e.getMessage());
        }
        return true;
    }

    /**
     * Called when the BluetoothAudioRoleSwitchRebootDialog confirm is clicked.
     */
    public void onBluetoothAudioRoleSwitchRebootDialogConfirmed() {
        if (!mChanged) {
            return;
        }
        try {
            final String currentA2dpSinkValue = SystemProperties
                    .get(A2DP_SINK_ROLE_PROPERTY, ROLE_DISABLED);
            if (currentA2dpSinkValue.equals(ROLE_DISABLED)) {
                SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, ROLE_ENABLED);
            } else {
                SystemProperties.set(A2DP_SINK_ROLE_PROPERTY, ROLE_DISABLED);
            }
            final String currentHFPValue = SystemProperties
                    .get(HFP_CLIENT_ROLE_PROPERTY, ROLE_DISABLED);
            if (currentHFPValue.equals(ROLE_DISABLED)) {
                SystemProperties.set(HFP_CLIENT_ROLE_PROPERTY, ROLE_ENABLED);
            } else {
                SystemProperties.set(HFP_CLIENT_ROLE_PROPERTY, ROLE_DISABLED);
            }
            final String currentSinkValue = SystemProperties
                    .get(BLUETOOTH_PROFILES_SINK_ROLE_PROPERTY, ROLE_DISABLED);
            if (currentSinkValue.equals(ROLE_DISABLED)) {
                SystemProperties.set(BLUETOOTH_PROFILES_SINK_ROLE_PROPERTY, ROLE_ENABLED);
            } else {
                SystemProperties.set(BLUETOOTH_PROFILES_SINK_ROLE_PROPERTY, ROLE_DISABLED);
            }
            final String currentSourceValue = SystemProperties
                    .get(BLUETOOTH_PROFILES_SOURCE_ROLE_PROPERTY, ROLE_DISABLED);
            if (currentSourceValue.equals(ROLE_DISABLED)) {
                SystemProperties.set(BLUETOOTH_PROFILES_SOURCE_ROLE_PROPERTY, ROLE_ENABLED);
            } else {
                SystemProperties.set(BLUETOOTH_PROFILES_SOURCE_ROLE_PROPERTY, ROLE_DISABLED);
            }
            updateState(mPreference);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to set A2DP sink and HFP Client system property: " + e.getMessage());
        }
    }


    /**
     * Called when the BluetoothAudioRoleSwitchRebootDialog cancel is clicked.
     */
    public void onBluetoothAudioRoleSwitchRebootDialogCanceled() {
        mChanged = false;
    }

    private final class BTStateChangeReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())
                     && intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                     == BluetoothAdapter.STATE_OFF) {
                BluetoothAudioRoleSwitchRebootDialog.enableBluetooth();
            }
        }
    }
}
