/*
 * Copyright (C) 2014-2016 The CyanogenMod Project
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

package com.android.systemui;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.tuner.TunerService;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback, TunerService.Tunable {

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT =
            "system:" + Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT;

    private static final String STATUS_BAR_BATTERY_STYLE =
            "system:" + Settings.System.STATUS_BAR_BATTERY_STYLE;

    private BatteryController mBatteryController;

    private boolean mRequestedVisibility;

    private boolean mBatteryStyleTextOnly;

    private int mLevel;
    private boolean mCharging;

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        mLevel = level;
        mCharging = charging;
        updateBatteryLevelText();
    }

    private void updateBatteryLevelText() {
        if (mCharging & mBatteryStyleTextOnly) {
            setText(getResources().getString(R.string.battery_level_template_charging, mLevel));
        } else {
            setText(getResources().getString(R.string.battery_level_template, mLevel));
        }
    }

    public void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;
        mBatteryController.addStateChangedCallback(this);
        TunerService.get(getContext()).addTunable(this, true,
                STATUS_BAR_SHOW_BATTERY_PERCENT, STATUS_BAR_BATTERY_STYLE);
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        // Unused
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        TunerService.get(getContext()).removeTunable(this);
        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
        }
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        switch (key) {
            case STATUS_BAR_SHOW_BATTERY_PERCENT:
                mRequestedVisibility = newValue != null && Integer.parseInt(newValue) == 2;
                setVisibility(mRequestedVisibility ? View.VISIBLE : View.GONE);
                break;
            case STATUS_BAR_BATTERY_STYLE:
                final int value = newValue == null ?
                        BatteryMeterDrawable.BATTERY_STYLE_PORTRAIT : Integer.parseInt(newValue);
                mBatteryStyleTextOnly = false;
                switch (value) {
                    case BatteryMeterDrawable.BATTERY_STYLE_TEXT:
                        setVisibility(View.VISIBLE);
                        mBatteryStyleTextOnly = true;
                        break;
                    case BatteryMeterDrawable.BATTERY_STYLE_HIDDEN:
                        setVisibility(View.GONE);
                        break;
                    default:
                        setVisibility(mRequestedVisibility ? View.VISIBLE : View.GONE);
                        break;
                }
                updateBatteryLevelText();
                break;
            default:
                break;
        }
    }
}
