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
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Class which shows the Action List dialog when the user taps on the widget.
 * 
 * @author Jiri Tyr
 * 
 */
public class DialogActivity extends Activity {
    private static final String TAG = "CR: DialogActivity";
    private static final int DEBUG = 0;
    private static int WIDGET_ID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String PREFS_NAME = null;
    private static Context context = null;

    // Share item IDs
    private static final int SHARE_SMS = 0;
    private static final int SHARE_EMAIL = 1;

    // IDs of the menu items
    private static final int MENU_UPDATE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG > 0)
            Log.d(TAG, "Starting dialog activity");

        Uri u = getIntent().getData();
        WIDGET_ID = new Integer(u.getAuthority());
        PREFS_NAME = getString(R.string.preferences_file_prefix) + WIDGET_ID;

        super.onCreate(savedInstanceState);

        context = this;

        // Set window animation
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.4f;
        lpWindow.windowAnimations = android.R.anim.fade_in | android.R.anim.fade_out;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog);

        Button browser = (Button) findViewById(R.id.browserButton);
        browser.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Browser button");

                // Read the URL for the particular bank
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                Bank bank = new BankHelper(settings.getString(SettingsActivity.FKEY_BANK, getResources().getString(R.string.preferences_bank_default_value))).getBank(getResources());

                // Open a web browser
                Intent myIntent = new Intent(Intent.ACTION_VIEW, bank.getWebUri());
                startActivity(myIntent);

                finish();
            }
        });

        Button overview = (Button) findViewById(R.id.overviewButton);
        overview.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Overview button");

                // Open the Overview activity
                Intent intent = new Intent(context, OverviewActivity.class);
                intent.putExtra("widget_id", WIDGET_ID);
                startActivity(intent);

                finish();
            }
        });

        Button update = (Button) findViewById(R.id.updateButton);
        update.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Update button");

                // Update the widget when we are done
                UpdateService.requestUpdate(new int[] { WIDGET_ID });
                startService(new Intent(context, UpdateService.class));

                // Set FIRST_RUN = true
                String PREFS_NAME = getString(R.string.preferences_file_prefix)    + WIDGET_ID;
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                settings.edit().putBoolean(SettingsActivity.FKEY_FIRST_RUN,    true).commit();

                finish();
            }
        });

        Button settings = (Button) findViewById(R.id.settingsButton);
        settings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Settings button");

                // Open the Settings activity
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.putExtra("widget_id", WIDGET_ID);
                startActivity(intent);

                finish();
            }
        });

        Button about = (Button) findViewById(R.id.aboutButton);
        about.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on About button");

                // Open the About activity
                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);

                finish();
            }
        });

        Button share = (Button) findViewById(R.id.shareButton);
        share.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (DEBUG > 0)
                    Log.d(TAG, " * clicked on Share button");

                // Create a dialog to select the way of sharing (SMS or E-mail)
                AlertDialog.Builder shareDialog = new AlertDialog.Builder(context);
                shareDialog.setTitle(R.string.dialog_share_title);
                shareDialog.setItems(R.array.select_share, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        String BANK = settings.getString(SettingsActivity.FKEY_BANK, getString(R.string.preferences_bank_default_value));
                        BankHelper bnkh = new BankHelper(BANK);
                        Bank bnk = bnkh.getBank(getResources());
                        String CURRENCY = settings.getString(SettingsActivity.FKEY_CURRENCY, bnk.getDefaultCurrencyValue());
                        Float RATE = settings.getFloat(SettingsActivity.FKEY_RATE, 0);
                        Float RATE_PREV = settings.getFloat(SettingsActivity.FKEY_RATE_PREV, 0);
                        String DATE = settings.getString(SettingsActivity.FKEY_DATE, getString(R.string.widget_unknown));

                        // Format for rates
                        NumberFormat formatter = new DecimalFormat(bnk.getRateFormat());

                        // Date and time formatter
                        DateTimeFormat dtFormatter = new DateTimeFormat(getResources(), settings);

                        String RATE_DIFF = getString(R.string.widget_unknown);
                        if (RATE_PREV > 0) {
                            RATE_DIFF = (((RATE - RATE_PREV) > 0) ? "+" : "") + formatter.format(RATE - RATE_PREV);
                        }

                        switch (which) {
                            case SHARE_SMS:
                                // Send SMS
                                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                                smsIntent.setType("vnd.android-dir/mms-sms");
                                smsIntent.putExtra("sms_body", getString(R.string.dialog_share_sms_message, CURRENCY, formatter.format(RATE), RATE_DIFF, dtFormatter.format(DATE), bnkh.getBankName(context)));
                                startActivity(smsIntent);
                                break;

                            case SHARE_EMAIL:
                                // Send E-mail
                                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                emailIntent.setType("plain/text");
                                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.dialog_share_email_subject, CURRENCY));
                                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.dialog_share_email_message, bnkh.getBankName(context), CURRENCY, formatter.format(RATE), RATE_DIFF, dtFormatter.format(DATE)));
                                startActivity(emailIntent);
                                break;
                        }

                        finish();
                    }
                });
                shareDialog.create();
                shareDialog.show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPDATE, 0, getString(R.string.dialog_menu_update)).setIcon(android.R.drawable.ic_menu_recent_history);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPDATE:
                if (DEBUG > 0)
                    Log.d(TAG, "Widget update requested");

                UpdateService.requestUpdate(new int[] { WIDGET_ID });
                startService(new Intent(context, UpdateService.class));

                finish();

                return true;
        }

        return false;
    }
}
