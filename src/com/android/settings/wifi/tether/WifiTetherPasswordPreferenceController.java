/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * Changes from Qualcomm Innovation Center are provided under the following license:
 * Copyright (c) 2022 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settings.wifi.WifiUtils;

import java.util.UUID;

public class WifiTetherPasswordPreferenceController extends WifiTetherBasePreferenceController
        implements ValidatedEditTextPreference.Validator {

    private static final String PREF_KEY = "wifi_tether_network_password";

    private String mPassword;

    public WifiTetherPasswordPreferenceController(Context context,
            OnTetherConfigUpdateListener listener) {
        super(context, listener);
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        updateDisplay();
    }

    private void updateDisplay() {
        final WifiConfiguration config = mWifiManager.getWifiApConfiguration();
        if (config == null || (config.getAuthType() == WifiConfiguration.KeyMgmt.WPA2_PSK
                && TextUtils.isEmpty(config.preSharedKey))) {
            mPassword = generateRandomPassword();
        } else {
            mPassword = config.preSharedKey;
        }
        ((ValidatedEditTextPreference) mPreference).setValidator(this);
        ((ValidatedEditTextPreference) mPreference).setIsPassword(true);
        updatePasswordDisplay((EditTextPreference) mPreference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mPassword = (String) newValue;
        updatePasswordDisplay((EditTextPreference) mPreference);
        mListener.onTetherConfigUpdated();
        return true;
    }

    /**
     * This method returns the current password if it is valid for the indicated security type. If
     * the password currently set is invalid it will forcefully set a random password that is valid.
     *
     * @param securityType The security type for the password.
     * @return The current password if it is valid for the indicated security type. A new randomly
     * generated password if it is not.
     */
    public String getPasswordValidated(int securityType) {
        // don't actually overwrite unless we get a new config in case it was accidentally toggled.
        if (securityType == WifiConfiguration.KeyMgmt.NONE
              || securityType == WifiConfiguration.KeyMgmt.OWE) {
            return "";
        } else if (!isTextValid(mPassword)) {
            mPassword = generateRandomPassword();
            updatePasswordDisplay((EditTextPreference) mPreference);
        }
        return mPassword;
    }

    public void updateVisibility(int securityType) {
        mPreference.setVisible(securityType != WifiConfiguration.KeyMgmt.NONE
                             && securityType != WifiConfiguration.KeyMgmt.OWE);
    }

    @Override
    public boolean isTextValid(String value) {
        return WifiUtils.isPasswordValid(value);
    }

    private static String generateRandomPassword() {
        String randomUUID = UUID.randomUUID().toString();
        //first 12 chars from xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
        return randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
    }

    private void updatePasswordDisplay(EditTextPreference preference) {
        ValidatedEditTextPreference pref = (ValidatedEditTextPreference) preference;
        pref.setText(mPassword);
        if (!TextUtils.isEmpty(mPassword)) {
            pref.setIsPassword(true);
            pref.setSummary(mPassword);
            pref.setVisible(true);
        } else {
            pref.setIsPassword(false);
            pref.setSummary(R.string.wifi_hotspot_no_password_subtext);
            pref.setVisible(false);
        }
    }
}
