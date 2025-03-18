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

package com.android.settings.network.telephony.satellite;

import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC;
import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_MANUAL;
import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_NTN_CONNECT_TYPE_INT;
import static android.telephony.CarrierConfigManager.KEY_EMERGENCY_MESSAGING_SUPPORTED_BOOL;
import static android.telephony.CarrierConfigManager.KEY_SATELLITE_ATTACH_SUPPORTED_BOOL;
import static android.telephony.CarrierConfigManager.KEY_SATELLITE_ENTITLEMENT_SUPPORTED_BOOL;
import static android.telephony.CarrierConfigManager.KEY_SATELLITE_INFORMATION_REDIRECT_URL_STRING;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.widget.FooterPreference;

import java.util.Set;

/** Handle Satellite Setting Preference Layout. */
public class SatelliteSetting extends RestrictedDashboardFragment {
    private static final String TAG = "SatelliteSetting";
    private static final String PREF_KEY_CATEGORY_HOW_IT_WORKS = "key_category_how_it_works";
    private static final String KEY_FOOTER_PREFERENCE = "satellite_setting_extra_info_footer_pref";
    private static final String KEY_SATELLITE_CONNECTION_GUIDE = "key_satellite_connection_guide";
    private static final String KEY_SUPPORTED_SERVICE = "key_supported_service";


    static final String SUB_ID = "sub_id";
    static final String EXTRA_IS_SERVICE_DATA_TYPE = "is_service_data_type";
    static final String EXTRA_IS_SMS_AVAILABLE_FOR_MANUAL_TYPE = "is_sms_available";

    private Activity mActivity;
    private SatelliteManager mSatelliteManager;
    private PersistableBundle mConfigBundle;
    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private String mSimOperatorName = "";
    private boolean mIsServiceDataType = false;
    private boolean mIsSmsAvailableForManualType = false;

    public SatelliteSetting() {
        super(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SATELLITE_SETTING;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mSubId = mActivity.getIntent().getIntExtra(SUB_ID,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        mConfigBundle = fetchCarrierConfigData(mSubId);
        mIsServiceDataType = getIntent().getBooleanExtra(EXTRA_IS_SERVICE_DATA_TYPE, false);
        mIsSmsAvailableForManualType = getIntent().getBooleanExtra(
                EXTRA_IS_SMS_AVAILABLE_FOR_MANUAL_TYPE, false);

        use(SatelliteAppListCategoryController.class).init();
        use(SatelliteSettingAboutContentController.class).init(mSubId);
        use(SatelliteSettingAccountInfoController.class).init(mSubId, mConfigBundle,
                mIsSmsAvailableForManualType, mIsServiceDataType);
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSatelliteManager = mActivity.getSystemService(SatelliteManager.class);
        if (mSatelliteManager == null) {
            Log.d(TAG, "SatelliteManager is null, do nothing.");
            finish();
            return;
        }

        if (!isSatelliteAttachSupported(mSubId)) {
            Log.d(TAG, "SatelliteSettings: KEY_SATELLITE_ATTACH_SUPPORTED_BOOL is false, "
                    + "do nothing.");
            finish();
            return;
        }

        mSimOperatorName = getSystemService(TelephonyManager.class).getSimOperatorName(mSubId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isSatelliteEligible = isSatelliteEligible();
        updateHowItWorksContent(isSatelliteEligible);
        updateFooterContent();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.satellite_setting;
    }

    private void updateHowItWorksContent(boolean isSatelliteEligible) {
        /* Composes "How it works" section, which guides how users can use satellite messaging, when
           satellite messaging is included in user's mobile plan, or it'll will be grey out. */
        if (!isSatelliteEligible) {
            PreferenceCategory category = findPreference(PREF_KEY_CATEGORY_HOW_IT_WORKS);
            category.setEnabled(false);
            category.setShouldDisableView(true);
        }
        if (!isCarrierRoamingNtnConnectedTypeManual()) {
            return;
        }
        Preference connectionGuide = findPreference(KEY_SATELLITE_CONNECTION_GUIDE);
        connectionGuide.setTitle(R.string.title_satellite_connection_guide_for_manual_type);
        connectionGuide.setSummary(R.string.summary_satellite_connection_guide_for_manual_type);
        Preference supportedService = findPreference(KEY_SUPPORTED_SERVICE);
        supportedService.setTitle(R.string.title_supported_service_for_manual_type);
        supportedService.setSummary(R.string.summary_supported_service_for_manual_type);
    }

    private void updateFooterContent() {
        // More about satellite messaging
        FooterPreference footerPreference = findPreference(KEY_FOOTER_PREFERENCE);
        if (footerPreference != null) {
            int summary = mConfigBundle.getBoolean(KEY_EMERGENCY_MESSAGING_SUPPORTED_BOOL)
                    ? R.string.satellite_setting_summary_more_information
                    : R.string.satellite_setting_summary_more_information_no_emergency_messaging;
            footerPreference.setSummary(getResources().getString(summary, mSimOperatorName));

            final String[] link = new String[1];
            link[0] = readSatelliteMoreInfoString();
            if (link[0] != null && !link[0].isEmpty()) {
                footerPreference.setLearnMoreAction(view -> {
                    if (!link[0].isEmpty()) {
                        Intent helpIntent = HelpUtils.getHelpIntent(mActivity, link[0],
                                this.getClass().getName());
                        if (helpIntent != null) {
                            mActivity.startActivityForResult(helpIntent, /*requestCode=*/ 0);
                        }
                    }
                });

                footerPreference.setLearnMoreText(
                        getString(R.string.more_about_satellite_messaging));
            }
        }
    }

    private boolean isSatelliteEligible() {
        if (isCarrierRoamingNtnConnectedTypeManual()) {
            return mIsSmsAvailableForManualType;
        }
        try {
            Set<Integer> restrictionReason =
                    mSatelliteManager.getAttachRestrictionReasonsForCarrier(mSubId);
            return !restrictionReason.contains(
                    SatelliteManager.SATELLITE_COMMUNICATION_RESTRICTION_REASON_ENTITLEMENT);
        } catch (SecurityException | IllegalStateException | IllegalArgumentException ex) {
            loge(ex.toString());
            return false;
        }
    }

    private PersistableBundle fetchCarrierConfigData(int subId) {
        CarrierConfigManager carrierConfigManager = mActivity.getSystemService(
                CarrierConfigManager.class);
        PersistableBundle bundle = CarrierConfigManager.getDefaultConfig();
        try {
            bundle = carrierConfigManager.getConfigForSubId(subId,
                    KEY_SATELLITE_ATTACH_SUPPORTED_BOOL,
                    KEY_SATELLITE_INFORMATION_REDIRECT_URL_STRING,
                    KEY_CARRIER_ROAMING_NTN_CONNECT_TYPE_INT,
                    KEY_SATELLITE_ENTITLEMENT_SUPPORTED_BOOL,
                    KEY_EMERGENCY_MESSAGING_SUPPORTED_BOOL);
            if (bundle.isEmpty()) {
                Log.d(TAG, "SatelliteSettings: getDefaultConfig");
                bundle = CarrierConfigManager.getDefaultConfig();
            }
        } catch (IllegalStateException exception) {
            Log.d(TAG, "SatelliteSettings exception : " + exception);
        }
        return bundle;
    }

    private String readSatelliteMoreInfoString() {
        return mConfigBundle.getString(KEY_SATELLITE_INFORMATION_REDIRECT_URL_STRING, "");
    }

    private boolean isCarrierRoamingNtnConnectedTypeManual() {
        return CARRIER_ROAMING_NTN_CONNECT_MANUAL == mConfigBundle.getInt(
                KEY_CARRIER_ROAMING_NTN_CONNECT_TYPE_INT, CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC);
    }

    private boolean isSatelliteAttachSupported(int subId) {
        return mConfigBundle.getBoolean(KEY_SATELLITE_ATTACH_SUPPORTED_BOOL, false);
    }

    private static void loge(String message) {
        Log.e(TAG, message);
    }
}
