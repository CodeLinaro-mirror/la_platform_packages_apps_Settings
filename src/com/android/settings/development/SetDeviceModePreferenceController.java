/*
 * Copyright (c) 2025 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class SetDeviceModePreferenceController extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_DEVICE_MODE = "device_mode";
    private static final String TAG = "SetDeviceModePreferenceController";

    @VisibleForTesting
    private static String SETTING_VALUE_ON = "host";
    @VisibleForTesting
    private static String SETTING_VALUE_OFF = "peripheral";
    private static final String DEVICE_MODE = "persist.sys.device.mode";
    private static final String IS_SKU2_DEVICE = "vendor.sku2.enable";

    public SetDeviceModePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return SystemProperties.getBoolean(IS_SKU2_DEVICE, false);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DEVICE_MODE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean isEnabled = (Boolean) newValue;
        SystemProperties.set(DEVICE_MODE ,
                isEnabled ? SETTING_VALUE_ON : SETTING_VALUE_OFF);
        String mode = SystemProperties.get(DEVICE_MODE);
        Log.d(TAG, "onPreferenceChange : mode = " + mode);
        mPreference.setSummary(mode.equals(SETTING_VALUE_ON) ? R.string.host_mode
                : R.string.peripheral_mode);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        final String mode = SystemProperties.get(DEVICE_MODE);
        Log.d(TAG,"updateState = " + mode);
        mPreference.setSummary(mode.equals(SETTING_VALUE_ON) ? R.string.host_mode
                : R.string.peripheral_mode);
        ((SwitchPreference) mPreference).setChecked(mode.equals(SETTING_VALUE_ON));
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(DEVICE_MODE, SETTING_VALUE_OFF);
        ((SwitchPreference) mPreference).setChecked(false);
    }
}
