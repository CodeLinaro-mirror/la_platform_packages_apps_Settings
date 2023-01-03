/*
 * Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
*/

package com.android.settings.development;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;


public class DiagPortPreferenceController extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final String TAG = "DiagPortCtrl";
    private static final String PREFERENCE_KEY = "enable_diag_port";
    private static final String PROPERTY_USB_CONFIG_NAME = "persist.sys.usb.config";
    private static final String PROPERTY_USB_CONFIG_DIAG = "diag,serial_cdev,rmnet,dpl,qdss,adb";
    private static final String PROPERTY_USB_CONFIG_ADB = "adb";

    private RestrictedSwitchPreference mPreference;
    private final Handler mDiagHandler = new Handler();

    public DiagPortPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enableDiag = (Boolean) newValue;
        if (enableDiag) {
            Log.i(TAG, "Enabling adb & diag");
            android.provider.Settings.Global.putInt(mContext.getContentResolver(),
                android.provider.Settings.Global.ADB_ENABLED, 1);
            mDiagHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                   SystemProperties.set(PROPERTY_USB_CONFIG_NAME, PROPERTY_USB_CONFIG_DIAG);
                   Toast.makeText(mContext,"diag,adb is enabled", Toast.LENGTH_LONG).show();
                }
            },5000);

            mPreference.setTitle(R.string.disable_diag_port);
            mPreference.setSummary(R.string.disable_diag_port_summary);
          } else {
              Log.i(TAG, "Disabling diag");
              android.provider.Settings.Global.putInt(mContext.getContentResolver(),
                  android.provider.Settings.Global.ADB_ENABLED, 1);
              mDiagHandler.postDelayed(new Runnable(){
                  @Override
                  public void run() {
                      SystemProperties.set(PROPERTY_USB_CONFIG_NAME, PROPERTY_USB_CONFIG_ADB);
                      Toast.makeText(mContext,"diag is disabled", Toast.LENGTH_LONG).show();
                  }
              },5000);

              mPreference.setTitle(R.string.enable_diag_port);
              mPreference.setSummary(R.string.enable_diag_port_summary);
        }

        return true;
    }

    @Override
    public void updateState(Preference preference) {
        final String diagConfigString = SystemProperties.get(
            PROPERTY_USB_CONFIG_NAME, PROPERTY_USB_CONFIG_ADB);
        if (diagConfigString.contains("diag")) {
            Log.i(TAG, "Diag is enabled");
            ((SwitchPreference) mPreference).setChecked(true);
            mPreference.setTitle(R.string.disable_diag_port);
            mPreference.setSummary(R.string.disable_diag_port_summary);
        } else {
            Log.i(TAG, "Diag is disabled");
            ((SwitchPreference) mPreference).setChecked(false);
            mPreference.setTitle(R.string.enable_diag_port);
            mPreference.setSummary(R.string.enable_diag_port_summary);
        }
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        mPreference.setChecked(false);
    }

    @VisibleForTesting
    RestrictedLockUtils.EnforcedAdmin checkIfMaximumTimeToLockSetByAdmin() {
        return RestrictedLockUtilsInternal.checkIfMaximumTimeToLockIsSet(mContext);
    }

}
