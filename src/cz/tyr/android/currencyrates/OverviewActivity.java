/*
 * Copyright 2010 Currency Rates Open Source project
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

package cz.tyr.android.currencyrates;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class which shows the Overview informations.
 * 
 * @author Jiri Tyr
 * 
 */
public class OverviewActivity extends Activity {
    private static final String TAG = "CR: OverviewActivity";
    private static final int DEBUG = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG > 0)
            Log.d(TAG, "Starting dialog activity");

        super.onCreate(savedInstanceState);

        // Set activity view
        setContentView(R.layout.overview);

        // Fill in the values
        fillInValues();

        // OnClick method for the Close button
        Button close = (Button) findViewById(R.id.overviewCloseButton);
        close.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Browser button");

                finish();
            }
        });

        // OnClick method for the Refresh button
        Button refresh = (Button) findViewById(R.id.overviewRefreshButton);
        refresh.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Refresh button");

                fillInValues();
            }
        });
    }

    /**
     * Fill in the values in the layout.
     */
    private void fillInValues() {
        // Get the widget ID
        int WIDGET_ID = getIntent().getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID);

        // Define SharedPreferences file name
        String PREFS_NAME = getString(R.string.preferences_file_prefix) + WIDGET_ID;

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load values from the shared file
        String BANK = settings.getString(SettingsActivity.FKEY_BANK, getString(R.string.preferences_bank_default_value));
        BankHelper bnkh = new BankHelper(BANK);
        Bank bnk = bnkh.getBank(getResources());
        String CURRENCY = settings.getString(SettingsActivity.FKEY_CURRENCY, bnk.getDefaultCurrencyValue());
        Float RATE = settings.getFloat(SettingsActivity.FKEY_RATE, 0);
        Float RATE_PREV = settings.getFloat(SettingsActivity.FKEY_RATE_PREV, 0);
        String DATE = settings.getString(SettingsActivity.FKEY_DATE, getString(R.string.widget_unknown));
        String LAST_UPDATE = settings.getString(SettingsActivity.FKEY_LAST_UPDATE, getString(R.string.widget_unknown));
        String LAST_NOTIF = settings.getString(SettingsActivity.FKEY_LAST_NOTIF, getString(R.string.widget_unknown));
        Float LAST_NOTIF_RATE = settings.getFloat(SettingsActivity.FKEY_LAST_NOTIF_RATE, 0);
        String LAST_CHECK = settings.getString(SettingsActivity.FKEY_LAST_WIDGET_UPDATE, getString(R.string.widget_unknown));

        // Format for rates
        NumberFormat formatter = new DecimalFormat(bnk.getRateFormat());

        // Date and time formatter
        DateTimeFormat dtFormatter = new DateTimeFormat(getResources(), settings);

        // Set values in the activity
        TextView bank = (TextView) findViewById(R.id.overviewBank);
        bank.setText(bnkh.getBankName(this));
        TextView currency = (TextView) findViewById(R.id.overviewCurrency);
        currency.setText(CURRENCY);
        TextView rate = (TextView) findViewById(R.id.overviewRate);
        rate.setText(RATE == 0 ? getString(R.string.widget_unknown) : formatter.format(RATE));
        TextView ratePrev = (TextView) findViewById(R.id.overviewRatePrev);
        ratePrev.setText(RATE_PREV == 0 ? getString(R.string.widget_unknown) : formatter.format(RATE_PREV));
        TextView rateDiff = (TextView) findViewById(R.id.overviewRateDiff);
        if (RATE_PREV == 0) {
            rateDiff.setText(getString(R.string.widget_unknown));
        } else {
            rateDiff.setText((((RATE - RATE_PREV) > 0) ? "+" : "") + formatter.format(RATE - RATE_PREV));
        }
        TextView rateTime = (TextView) findViewById(R.id.overviewRateTime);
        rateTime.setText(dtFormatter.format(DATE));
        TextView lastUpdate = (TextView) findViewById(R.id.overviewLastUpdate);
        lastUpdate.setText(dtFormatter.format(LAST_UPDATE));
        TextView lastNotif = (TextView) findViewById(R.id.overviewLastNotif);
        if (LAST_NOTIF.equals(getString(R.string.widget_unknown))) {
            lastNotif.setText(LAST_NOTIF);
        } else {
            lastNotif.setText(dtFormatter.format(LAST_NOTIF));
        }
        TextView lastNotifRate = (TextView) findViewById(R.id.overviewLastNotifRate);
        lastNotifRate.setText(LAST_NOTIF_RATE == 0 ? getString(R.string.widget_unknown) : formatter.format(LAST_NOTIF_RATE));
        TextView lastNotifDiff = (TextView) findViewById(R.id.overviewLastNotifDiff);
        if (LAST_NOTIF_RATE == 0) {
            lastNotifDiff.setText(getString(R.string.widget_unknown));
        } else {
            lastNotifDiff.setText((((RATE - LAST_NOTIF_RATE) > 0) ? "+" : "") + formatter.format(RATE - LAST_NOTIF_RATE));
        }
        TextView lastCheck = (TextView) findViewById(R.id.overviewLastWidgetUpdate);
        lastCheck.setText(dtFormatter.format(LAST_CHECK));
    }
}
