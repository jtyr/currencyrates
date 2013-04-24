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

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TimePicker;
import cz.tyr.android.currencyrates.simplenumberspinner.SimpleNumberSpinner;

/**
 * Class which shows the Settings activity.
 * 
 * @author Jiri Tyr
 * 
 */
public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // Debug variables
    private static final String TAG = "CR: SettingsActivity";
    private static final int DEBUG = 0;

    // IDs of the default value from the Resources - Resource Default ID (RDID)
    public static final int RDID_BANK = R.string.preferences_bank_default_value;
    public static final int RDID_EXCHANGE = R.string.preferences_exchange_default_value;
    public static final int RDID_DIRECTION = R.string.preferences_direction_default_value;
    public static final int RDID_DATE_FORMAT = R.string.preferences_date_format_default_value;
    public static final int RDID_TIME_FORMAT = R.string.preferences_time_format_default_value;
    public static final int RDID_PERIOD = R.string.preferences_period_default_value;
    public static final int RDID_CHECKFROM_HOUR = R.string.preferences_checkfrom_hour_default;
    public static final int RDID_CHECKFROM_MINUTE = R.string.preferences_checkfrom_minute_default;
    public static final int RDID_CHECKTO_HOUR = R.string.preferences_checkto_hour_default;
    public static final int RDID_CHECKTO_MINUTE = R.string.preferences_checkto_minute_default;
    public static final int RDID_CHECKDAY = R.string.preferences_checkday_default_value;
    public static final int RDID_NOTIFICATION = R.string.preferences_notification_default_value;
    public static final int RDID_TOLERANCE = R.string.preferences_tolerance_default_value;
    public static final int RDID_SOUND = R.string.preferences_sound_default_value;
    public static final int RDID_VIBRATE = R.string.preferences_vibrate_default_value;
    public static final int RDID_LED = R.string.preferences_led_default_value;

    // Name of the keys in the Preferences XML file - Screen KEY (SKEY)
    public static final String SKEY_BANK = "preference_bank_list";
    public static final String SKEY_BANK_ALIAS = "preference_bank_alias";
    public static final String SKEY_CURRENCY = "preference_currency_list";
    public static final String SKEY_EXCHANGE = "preference_exchange_list";
    public static final String SKEY_DIRECTION = "preference_direction_list";
    public static final String SKEY_DATE_FORMAT = "preference_date_format_list";
    public static final String SKEY_TIME_FORMAT = "preference_time_format";
    public static final String SKEY_PERIOD = "preference_period_list";
    public static final String SKEY_CHECKFROM = "preference_checkfrom";
    public static final String SKEY_CHECKTO = "preference_checkto";
    public static final String SKEY_CHECKDAY = "preference_checkday";
    public static final String SKEY_NOTIFICATION = "preference_notification_checkbox";
    public static final String SKEY_TOLERANCE = "preference_tolerance";
    public static final String SKEY_SOUND = "preference_sound_checkbox";
    public static final String SKEY_RINGTONE_POS = "preference_ringtone_pos";
    public static final String SKEY_RINGTONE_NEG = "preference_ringtone_neg";
    public static final String SKEY_VIBRATE = "preference_vibrate_checkbox";
    public static final String SKEY_LED = "preference_led_checkbox";

    // Keys under which are the values saved into the file - File KEY (FKEY)
    public static final String FKEY_BANK = "bank";
    public static final String FKEY_BANK_ALIAS = "bank_alias";
    public static final String FKEY_CURRENCY = "currency";
    public static final String FKEY_EXCHANGE = "exchange";
    public static final String FKEY_DIRECTION = "direction";
    public static final String FKEY_DATE_FORMAT = "date_format";
    public static final String FKEY_TIME_FORMAT = "time_format";
    public static final String FKEY_PERIOD = "period";
    public static final String FKEY_CHECKFROM_HOUR = "checkfrom_hour";
    public static final String FKEY_CHECKFROM_MINUTE = "checkfrom_minute";
    public static final String FKEY_CHECKTO_HOUR = "checkto_hour";
    public static final String FKEY_CHECKTO_MINUTE = "checkto_minute";
    public static final String FKEY_CHECKDAY = "checkday";
    public static final String FKEY_NOTIFICATION = "notification";
    public static final String FKEY_TOLERANCE = "tolerance";
    public static final String FKEY_SOUND = "sound";
    public static final String FKEY_RINGTONE_POS = "ringtone_pos";
    public static final String FKEY_RINGTONE_NEG = "ringtone_neg";
    public static final String FKEY_VIBRATE = "vibrate";
    public static final String FKEY_LED = "led";
    public static final String FKEY_RATE = "rate";
    public static final String FKEY_DATE = "date";
    public static final String FKEY_RATE_PREV = "rate_prev";
    public static final String FKEY_RATE_TIME = "rate_time";
    public static final String FKEY_LAST_UPDATE = "last_update";
    public static final String FKEY_LAST_NOTIF = "last_notif";
    public static final String FKEY_LAST_NOTIF_RATE = "last_notif_rate";
    public static final String FKEY_LAST_WIDGET_UPDATE = "last_widget_update";
    public static final String FKEY_FIRST_RUN = "first_run";

    // Dialog identification
    private static final int CHECKFROM_DIALOG_ID = 0;
    private static final int CHECKTO_DIALOG_ID = 1;
    private static final int CHECKDAY_DIALOG_ID = 2;
    private static final int TOLERANCE_DIALOG_ID = 3;

    // Some global variables
    private static String PREFS_NAME;
    private static int WIDGET_ID;
    private static boolean UPDATE_WIDGET = false;
    private static SharedPreferences settings;
    private SimpleNumberSpinner toleranceSpinner;
    // Default selection of the days
    private static boolean[] days = { true, true, true, true, true, false, false };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        WIDGET_ID = getIntent().getIntExtra("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID);
        PREFS_NAME = getString(R.string.preferences_file_prefix) + WIDGET_ID;
        if (DEBUG > 0)
            Log.d(TAG, "Starting widget settings with prefs_name = " + PREFS_NAME);

        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);

        // Load default values from the Preference file
        PreferenceScreen prefSet = getPreferenceScreen();
        ListPreference bankList = (ListPreference) prefSet.findPreference(SKEY_BANK);
        EditTextPreference bankAlias = (EditTextPreference) prefSet.findPreference(SKEY_BANK_ALIAS);
        ListPreference currencyList = (ListPreference) prefSet.findPreference(SKEY_CURRENCY);
        ListPreference exchangeList = (ListPreference) prefSet.findPreference(SKEY_EXCHANGE);
        ListPreference directionList = (ListPreference) prefSet.findPreference(SKEY_DIRECTION);
        ListPreference dateFormatList = (ListPreference) prefSet.findPreference(SKEY_DATE_FORMAT);
        CheckBoxPreference timeFormat = (CheckBoxPreference) prefSet.findPreference(SKEY_TIME_FORMAT);
        ListPreference periodList = (ListPreference) prefSet.findPreference(SKEY_PERIOD);
        Preference checkfrom = (Preference) prefSet.findPreference(SKEY_CHECKFROM);
        Preference checkto = (Preference) prefSet.findPreference(SKEY_CHECKTO);
        Preference checkday = (Preference) prefSet.findPreference(SKEY_CHECKDAY);
        CheckBoxPreference notification = (CheckBoxPreference) prefSet.findPreference(SKEY_NOTIFICATION);
        Preference tolerance = (Preference) prefSet.findPreference(SKEY_TOLERANCE);
        CheckBoxPreference sound = (CheckBoxPreference) prefSet.findPreference(SKEY_SOUND);
        MyRingtonePreference ringtonePos = (MyRingtonePreference) prefSet.findPreference(SKEY_RINGTONE_POS);
        MyRingtonePreference ringtoneNeg = (MyRingtonePreference) prefSet.findPreference(SKEY_RINGTONE_NEG);
        CheckBoxPreference vibrate = (CheckBoxPreference) prefSet.findPreference(SKEY_VIBRATE);
        CheckBoxPreference led = (CheckBoxPreference) prefSet.findPreference(SKEY_LED);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set bank
        if (bankList != null) {
            bankList.setValue(settings.getString(FKEY_BANK, getString(RDID_BANK)));
        }

        // Set bank alias
        if (bankAlias != null) {
            bankAlias.setText(settings.getString(FKEY_BANK_ALIAS, getString(RDID_BANK)));
        }

        Bank bank = new BankHelper(bankList.getValue()).getBank(getResources());

        // Get currency lists and values for the specific bank
        currencyList.setEntries(bank.getCurrencyEntries());
        currencyList.setEntryValues(bank.getCurrencyEntryValues());
        if (currencyList != null) {
            currencyList.setValue(settings.getString(FKEY_CURRENCY, bank.getDefaultCurrencyValue()));
        }
        if (exchangeList != null) {
            exchangeList.setValue(settings.getString(FKEY_EXCHANGE, getString(RDID_EXCHANGE)));
        }
        if (directionList != null) {
            directionList.setValue(settings.getString(FKEY_DIRECTION, getString(RDID_DIRECTION)));
        }
        if (dateFormatList != null) {
            // Generate examples with a real date numbers
            Calendar mDummyDate = Calendar.getInstance();
            mDummyDate.set(mDummyDate.get(Calendar.YEAR), 11, 31, 13, 0, 0);
            String[] dateFormats = getResources().getStringArray(R.array.select_date_format);
            String[] formattedDates = new String[dateFormats.length];
            for (int i = 0; i < formattedDates.length; i++) {
                formattedDates[i] = DateFormat.format(dateFormats[i], mDummyDate).toString();
            }

            dateFormatList.setEntries(formattedDates);
            dateFormatList.setValue(settings.getString(FKEY_DATE_FORMAT, getString(RDID_DATE_FORMAT)));
        }
        if (timeFormat != null) {
            timeFormat.setChecked(settings.getBoolean(FKEY_TIME_FORMAT, Boolean.parseBoolean(getString(R.string.preferences_time_format_default_value))));
        }
        if (periodList != null) {
            periodList.setValue(settings.getString(FKEY_PERIOD, getString(RDID_PERIOD)));
        }
        if (checkfrom != null) {
            checkfrom.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showDialog(CHECKFROM_DIALOG_ID);
                    if (DEBUG > 0)
                        Log.d(TAG, "Clicked on CHECK_FROM");
                    return false;
                }
            });
        }
        if (checkto != null) {
            checkto.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showDialog(CHECKTO_DIALOG_ID);
                    if (DEBUG > 0)
                        Log.d(TAG, "Clicked on CHECK_TO");
                    return false;
                }
            });
        }
        if (checkday != null) {
            checkday.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showDialog(CHECKDAY_DIALOG_ID);
                    if (DEBUG > 0)
                        Log.d(TAG, "Clicked on CHECK_DAY");
                    return false;
                }
            });
        }
        if (notification != null) {
            notification.setChecked(settings.getBoolean(FKEY_NOTIFICATION, Boolean.parseBoolean(getString(R.string.preferences_notification_default_value))));
        }
        if (tolerance != null) {
            tolerance.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    showDialog(TOLERANCE_DIALOG_ID);
                    if (DEBUG > 0)
                        Log.d(TAG, "Clicked on TOLERANCE");
                    return false;
                }
            });
        }
        if (sound != null) {
            sound.setChecked(settings.getBoolean(FKEY_SOUND, Boolean.parseBoolean(getString(R.string.preferences_sound_default_value))));
        }
        if (ringtonePos != null) {
            ringtonePos.setParams(WIDGET_ID, SettingsActivity.FKEY_RINGTONE_POS);
        }
        if (ringtoneNeg != null) {
            ringtoneNeg.setParams(WIDGET_ID, SettingsActivity.FKEY_RINGTONE_NEG);
        }
        if (vibrate != null) {
            vibrate.setChecked(settings.getBoolean(FKEY_VIBRATE, Boolean.parseBoolean(getString(R.string.preferences_vibrate_default_value))));
        }
        if (led != null) {
            led.setChecked(settings.getBoolean(FKEY_LED, Boolean.parseBoolean(getString(R.string.preferences_led_default_value))));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // Save user preferences. We need an Editor object to make changes. All
        // objects are from android.context.Context
        if (DEBUG > 0)
            Log.d(TAG, "Prefs changed. Saving into the file prefs_name = " + PREFS_NAME);
        SharedPreferences.Editor editor = settings.edit();

        if (key.equals(SKEY_BANK)) {
            String val = sharedPreferences.getString(SKEY_BANK, getString(RDID_BANK));

            if (DEBUG > 0)
                Log.d(TAG, " * Bank list changed! - value=" + val);

            editor.putString(FKEY_BANK, val);

            Bank bank = new BankHelper(val).getBank(getResources());

            // If the bank has been changed, change also the currency to the
            // default value
            if (DEBUG > 0)
                Log.d(TAG, "   - change currency to the default value=" + bank.getDefaultCurrencyValue());
            editor.putString(FKEY_CURRENCY, bank.getDefaultCurrencyValue());

            ListPreference cl = (ListPreference) getPreferenceScreen().findPreference(SKEY_CURRENCY);
            // Load the bank's currency list and values
            cl.setEntries(bank.getCurrencyEntries());
            cl.setEntryValues(bank.getCurrencyEntryValues());
            // Set current value to default
            cl.setValue(bank.getDefaultCurrencyValue());

            // Set the default alias
            editor.putString(FKEY_BANK_ALIAS, bank.getAlias());
            EditTextPreference ba = (EditTextPreference) getPreferenceScreen().findPreference(SKEY_BANK_ALIAS);
            ba.setText(bank.getAlias());
        } else if (key.equals(SKEY_BANK_ALIAS)) {
            String val = sharedPreferences.getString(SKEY_BANK_ALIAS, getString(RDID_BANK));
            if (DEBUG > 0)
                Log.d(TAG, " * Bank alias changed! - value=" + val);
            editor.putString(FKEY_BANK_ALIAS, val);
        } else if (key.equals(SKEY_CURRENCY)) {
            Bank bank = new BankHelper(sharedPreferences.getString(SKEY_CURRENCY, getString(RDID_BANK))).getBank(getResources());
            String val = sharedPreferences.getString(SKEY_CURRENCY, bank.getDefaultCurrencyValue());
            if (DEBUG > 0)
                Log.d(TAG, " * Currency list changed! - value=" + val);
            editor.putString(FKEY_CURRENCY, val);
        } else if (key.equals(SKEY_EXCHANGE)) {
            String val = sharedPreferences.getString(SKEY_EXCHANGE, getString(RDID_EXCHANGE));
            if (DEBUG > 0)
                Log.d(TAG, " * Exchange list changed! - value=" + val);
            editor.putString(FKEY_EXCHANGE, val);
        } else if (key.equals(SKEY_DIRECTION)) {
            String val = sharedPreferences.getString(SKEY_DIRECTION, getString(RDID_DIRECTION));
            if (DEBUG > 0)
                Log.d(TAG, " * Direction list changed! - value=" + val);
            editor.putString(FKEY_DIRECTION, val);
        } else if (key.equals(SKEY_DATE_FORMAT)) {
            String val = sharedPreferences.getString(SKEY_DATE_FORMAT, getString(RDID_DATE_FORMAT));
            if (DEBUG > 0)
                Log.d(TAG, " * Date format list changed! - value=" + val);
            editor.putString(FKEY_DATE_FORMAT, val);
        } else if (key.equals(SKEY_TIME_FORMAT)) {
            boolean val = sharedPreferences.getBoolean(SKEY_TIME_FORMAT, Boolean.parseBoolean(getString(RDID_TIME_FORMAT)));
            if (DEBUG > 0)
                Log.d(TAG, " * Time format CheckBox changed! value=" + val);
            editor.putBoolean(FKEY_TIME_FORMAT, val);
        } else if (key.equals(SKEY_PERIOD)) {
            String val = sharedPreferences.getString(SKEY_PERIOD, getString(RDID_PERIOD));
            if (DEBUG > 0)
                Log.d(TAG, " * Period list changed! - value=" + val);
            editor.putString(FKEY_PERIOD, val);
        } else if (key.equals(SKEY_NOTIFICATION)) {
            boolean val = sharedPreferences.getBoolean(SKEY_NOTIFICATION, Boolean.parseBoolean(getString(RDID_NOTIFICATION)));
            if (DEBUG > 0)
                Log.d(TAG, " * Notification CheckBox changed! value=" + val);
            editor.putBoolean(FKEY_NOTIFICATION, val);
        } else if (key.equals(SKEY_SOUND)) {
            boolean val = sharedPreferences.getBoolean(SKEY_SOUND, Boolean.parseBoolean(getString(RDID_SOUND)));
            if (DEBUG > 0)
                Log.d(TAG, " * SOUND CheckBox changed! value=" + val);
            editor.putBoolean(FKEY_SOUND, val);
        } else if (key.equals(SKEY_VIBRATE)) {
            boolean val = sharedPreferences.getBoolean(SKEY_VIBRATE, Boolean.parseBoolean(getString(RDID_VIBRATE)));
            if (DEBUG > 0)
                Log.d(TAG, " * VIBRATE CheckBox changed! value=" + val);
            editor.putBoolean(FKEY_VIBRATE, val);
        } else if (key.equals(SKEY_LED)) {
            boolean val = sharedPreferences.getBoolean(SKEY_LED, Boolean.parseBoolean(getString(RDID_LED)));
            if (DEBUG > 0)
                Log.d(TAG, " * LED CheckBox changed! value=" + val);
            editor.putBoolean(FKEY_LED, val);
        } else {
            if (DEBUG > 0)
                Log.d(TAG, " * Other: " + key + " changed!");
        }

        // Don't forget to commit your edits!!!
        editor.commit();

        // If we change the bank, currency, exchange or direction, reset
        // the RATE_PREV, RATE_LAST and RATE_DISP
        if (key.equals(SKEY_BANK) || key.equals(SKEY_CURRENCY) || key.equals(SKEY_EXCHANGE) || key.equals(SKEY_DIRECTION)) {
            editor.remove(FKEY_RATE);
            editor.remove(FKEY_DATE);
            editor.remove(FKEY_RATE_PREV);
            editor.remove(FKEY_RATE_TIME);
            editor.remove(FKEY_LAST_UPDATE);
            editor.remove(FKEY_LAST_NOTIF);
            editor.remove(FKEY_LAST_NOTIF_RATE);
            editor.remove(FKEY_FIRST_RUN);
            editor.commit();

            UPDATE_WIDGET = true;
        } else if (key.equals(SKEY_BANK_ALIAS) || key.equals(SKEY_DATE_FORMAT) || key.equals(SKEY_TIME_FORMAT)) {
            // Update the widget
            UpdateService.requestUpdate(new int[] { WIDGET_ID });
            startService(new Intent(this, UpdateService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Refresh the widget if there is some important change
        if (UPDATE_WIDGET) {
            UPDATE_WIDGET = false;

            // Show appropriate message on the widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_message);
            updateViews.setTextViewText(R.id.message, getText(R.string.widget_loading));
            appWidgetManager.updateAppWidget(WIDGET_ID, updateViews);

            // Update the widget when we are done
            UpdateService.requestUpdate(new int[] { WIDGET_ID });
            startService(new Intent(this, UpdateService.class));
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case CHECKFROM_DIALOG_ID:
            return new TimePickerDialog(this, checkfromTimeSetListener, Integer.parseInt(settings.getString(FKEY_CHECKFROM_HOUR, getString(RDID_CHECKFROM_HOUR))), Integer.parseInt(settings.getString(FKEY_CHECKTO_MINUTE, getString(RDID_CHECKFROM_MINUTE))), true);
        case CHECKTO_DIALOG_ID:
            return new TimePickerDialog(this, checktoTimeSetListener, Integer.parseInt(settings.getString(FKEY_CHECKTO_HOUR, getString(RDID_CHECKTO_HOUR))), Integer.parseInt(settings.getString(FKEY_CHECKTO_MINUTE, getString(RDID_CHECKTO_MINUTE))), true);
        case CHECKDAY_DIALOG_ID:
            String d = settings.getString(FKEY_CHECKDAY, getString(RDID_CHECKDAY));
            String[] dSplit = d.split(":");
            for (int i = 0; i < dSplit.length; i++) {
                if (dSplit[i].equals("1")) {
                    days[i] = true;
                } else {
                    days[i] = false;
                }
            }

            Builder db = new AlertDialog.Builder(SettingsActivity.this);
            db.setIcon(android.R.drawable.ic_menu_week);
            db.setMultiChoiceItems(R.array.select_checkday, days, checkdayTimeSetListener);
            db.setTitle(R.string.preferences_checkday_dialog_title);
            db.setPositiveButton(android.R.string.ok, checkdayOkButtonClick);
            return db.create();
        case TOLERANCE_DIALOG_ID:
            float t = settings.getFloat(FKEY_TOLERANCE, Float.parseFloat(getString(RDID_TOLERANCE)));

            toleranceSpinner = new SimpleNumberSpinner(SettingsActivity.this, 1, 3);
            toleranceSpinner.setPlusButtonIcon(R.drawable.btn_flicker_plus);
            toleranceSpinner.setMinusButtonIcon(R.drawable.btn_flicker_minus);
            toleranceSpinner.setValue(t);

            Builder tb = new AlertDialog.Builder(SettingsActivity.this);
            tb.setIcon(android.R.drawable.ic_menu_sort_by_size);
            tb.setTitle(R.string.preferences_tolerance_dialog_title);
            tb.setView(toleranceSpinner.create());
            tb.setPositiveButton(android.R.string.ok, toleranceOkButtonClick);
            return tb.create();
        default:
            if (DEBUG > 0)
                Log.d(TAG, "Som other dialog! ID=" + id);
        }

        return null;
    }

    /**
     * Listener for the check-from-time dialog.
     */
    private TimePickerDialog.OnTimeSetListener checkfromTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (DEBUG > 0)
                Log.d(TAG, " * Saving CHECKFROM=" + hourOfDay + ":" + minute);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FKEY_CHECKFROM_HOUR, hourOfDay + "");
            editor.putString(FKEY_CHECKFROM_MINUTE, minute + "");
            editor.commit();
        }
    };

    /**
     * Listener for the check-to-time dialog.
     */
    private TimePickerDialog.OnTimeSetListener checktoTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (DEBUG > 0)
                Log.d(TAG, " * Saving CHECKTO=" + hourOfDay + ":" + minute);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FKEY_CHECKTO_HOUR, hourOfDay + "");
            editor.putString(FKEY_CHECKTO_MINUTE, minute + "");
            editor.commit();
        }
    };

    /**
     * Listener for the check-day dialog.
     */
    private OnMultiChoiceClickListener checkdayTimeSetListener = new DialogInterface.OnMultiChoiceClickListener() {
        public void onClick(DialogInterface dialog, int whichButton,
                boolean isChecked) {
            if (DEBUG > 0)
                Log.d(TAG, " * Clicked on checkbox " + whichButton);

            days[whichButton] = isChecked;
        }
    };

    /**
     * Listener for the OK button in the check-day dialog.
     */
    private OnClickListener checkdayOkButtonClick = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            String d_join = "";
            for (int i = 0; i < days.length; i++) {
                String prefix = ":";
                if (i == 0) {
                    prefix = "";
                }
                if (days[i]) {
                    d_join += prefix + "1";
                } else {
                    d_join += prefix + "0";
                }
            }

            if (DEBUG > 0)
                Log.d(TAG, " * Saving CHECKDAY: " + d_join);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FKEY_CHECKDAY, d_join);
            editor.commit();
        }
    };

    /**
     * Listener for the OK button in the tolerance dialog.
     */
    private OnClickListener toleranceOkButtonClick = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            float tolerance = toleranceSpinner.getValue();

            if (DEBUG > 0)
                Log.d(TAG, " * Saving TOLERANCE: " + tolerance);

            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat(FKEY_TOLERANCE, tolerance);
            editor.commit();
        }
    };
}