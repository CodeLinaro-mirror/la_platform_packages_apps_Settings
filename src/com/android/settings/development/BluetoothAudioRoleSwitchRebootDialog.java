/*
 * Copyright 2022 The Android Open Source Project
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
 * Copyright (c) 2023,2025 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.settings.development;

import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

import java.lang.Thread;

/**
 * The A2dpSink  switch should reboot the device to take effect,
 * the dialog is to ask the user to reboot the device.
 */
public class BluetoothAudioRoleSwitchRebootDialog extends InstrumentedDialogFragment
        implements DialogInterface.OnClickListener {

    public static final String TAG = "BluetoothAudioRoleSwitchRebootDialog";
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static boolean disableTriggered = false;
    /**
     * The function to show the Dialog.
     */
    public static void show(DevelopmentSettingsDashboardFragment host) {
        final FragmentManager manager = host.getActivity().getSupportFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            final BluetoothAudioRoleSwitchRebootDialog dialog = new BluetoothAudioRoleSwitchRebootDialog();
            dialog.setTargetFragment(host, 0 /* requestCode */);
            dialog.show(manager, TAG);
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DIALOG_A2DP_SINK_DISABLE;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.bluetooth_reboot_dialog_message)
                .setTitle(R.string.bluetooth_reboot_dialog_title)
                .setPositiveButton(
                        R.string.bluetooth_reboot_dialog_confirm, this)
                .setNegativeButton(
                        android.R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final OnBluetoothAudioRoleSwitchRebootDialogConfirmedListener host =
                (OnBluetoothAudioRoleSwitchRebootDialogConfirmedListener) getTargetFragment();
        if (host == null) {
            return;
        }
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            host.onBluetoothAudioRoleSwitchRebootDialogConfirmed();
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    /*Disabling bluetooth*/
                    mBluetoothAdapter.disable();
                    disableTriggered = true;
                }
            } else {
                Log.e(TAG, "BluetoothAdapter is NULL, not able to restart BT");
            }
            mBluetoothAdapter = null;
        } else {
            host.onBluetoothAudioRoleSwitchRebootDialogCanceled();
        }
    }

    public static void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && disableTriggered) {
            Log.d(TAG, "Enabling bluetooth after resetting the bluetooth profiles role");
            disableTriggered = false;
            mBluetoothAdapter.enable();
        } else {
            Log.e(TAG, "BluetoothAdapter is NULL or Disable triggered from other user");
        }
    }

    /**
     * Interface for EnableAdbWarningDialog callbacks.
     */
    public interface OnBluetoothAudioRoleSwitchRebootDialogConfirmedListener {
        /**
         * Called when the user presses enable on the warning dialog.
         */
        void onBluetoothAudioRoleSwitchRebootDialogConfirmed();

        /**
         * Called when the user presses cancel on the warning dialog.
         */
        void onBluetoothAudioRoleSwitchRebootDialogCanceled();
    }
}
