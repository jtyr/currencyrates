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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Class which takes care of the widget update.
 * 
 * @author Jiri Tyr
 * 
 */
public class UpdateService extends Service implements Runnable {
    // Debug variables
    private static final String TAG = "CR: UpdateService";
    private static final int DEBUG = 0;

    /**
     * Widget update interval (in minutes)
     */
    private final int WIDGET_UPDATE_INTERVAL = 10;

    /**
     * Specific {@link Intent#setAction(String)} used when performing a full
     * update of all widgets, usually when an update alarm goes off.
     */
    private static final String ACTION_UPDATE_ALL = "cz.tyr.android.currencyrates.UPDATE_ALL";

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private static Object sLock = new Object();

    /**
     * Flag if there is an update thread already running. We only launch a new
     * thread if one isn't already running.
     */
    private static boolean sThreadRunning = false;

    /**
     * Internal queue of requested widget updates. You <b>must</b> access
     * through {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make
     * sure your access is correctly synchronized.
     */
    private static Queue<Integer> sAppWidgetIds = new LinkedList<Integer>();

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // If requested, trigger update of all widgets
        if (ACTION_UPDATE_ALL.equals(intent.getAction())) {
            if (DEBUG > 0)
                Log.d(TAG, "Requested UPDATE_ALL action");
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            requestUpdate(manager.getAppWidgetIds(new ComponentName(this, CurrencyRates.class)));
        } else if (DEBUG > 0)
            Log.d(TAG, "Requested some other action: " + intent.getAction());

        // Only start processing thread if not already running
        synchronized (sLock) {
            if (!sThreadRunning) {
                sThreadRunning = true;
                new Thread(this).start();
            }
        }
    }

    /**
     * Update new values and save them into the preference file.
     */
    private int downloadNewValues(int appWidgetId) {
        if (DEBUG > 1)
            Log.d(TAG, "    - Updating values for widget ID = " + appWidgetId);

        // Define the shared preferences file
        String PREFS_NAME = getString(R.string.preferences_file_prefix) + appWidgetId;

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String BANK = settings.getString(SettingsActivity.FKEY_BANK, getString(R.string.preferences_bank_default_value));

        // Get default currency value
        Bank bank = new BankHelper(BANK).getBank(getResources());
        String CURRENCY = settings.getString(SettingsActivity.FKEY_CURRENCY, bank.getDefaultCurrencyValue());

        // Get the rest of the values
        String BANK_ALIAS = settings.getString(SettingsActivity.FKEY_BANK_ALIAS, getString(R.string.preferences_bank_default_value));
        String EXCHANGE = settings.getString(SettingsActivity.FKEY_EXCHANGE, getString(R.string.preferences_exchange_default_value));
        String DIRECTION = settings.getString(SettingsActivity.FKEY_DIRECTION, getString(R.string.preferences_direction_default_value));
        Boolean NOTIFICATION = settings.getBoolean(SettingsActivity.FKEY_NOTIFICATION, Boolean.parseBoolean(getString(R.string.preferences_notification_default_value)));
        float TOLERANCE = settings.getFloat(SettingsActivity.FKEY_TOLERANCE, Float.parseFloat(getString(R.string.preferences_tolerance_default_value)));
        int PERIOD = Integer.parseInt(settings.getString(SettingsActivity.FKEY_PERIOD, getString(R.string.preferences_period_default_value)));
        Boolean SOUND = settings.getBoolean(SettingsActivity.FKEY_SOUND, Boolean.parseBoolean(getString(R.string.preferences_sound_default_value)));
        String RINGTONE_POS = settings.getString(SettingsActivity.FKEY_RINGTONE_POS, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
        String RINGTONE_NEG = settings.getString(SettingsActivity.FKEY_RINGTONE_NEG, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
        Boolean VIBRATE = settings.getBoolean(SettingsActivity.FKEY_VIBRATE, Boolean.parseBoolean(getString(R.string.preferences_vibrate_default_value)));
        Boolean LED = settings.getBoolean(SettingsActivity.FKEY_LED, Boolean.parseBoolean(getString(R.string.preferences_led_default_value)));
        float RATE = settings.getFloat(SettingsActivity.FKEY_RATE, 0);
        float RATE_PREV = settings.getFloat(SettingsActivity.FKEY_RATE_PREV, 0);
        float LAST_NOTIF_RATE = settings.getFloat(SettingsActivity.FKEY_LAST_NOTIF_RATE, 0);
        boolean FIRST_RUN = settings.getBoolean(SettingsActivity.FKEY_FIRST_RUN, true);

        // Define the editor
        Editor editor = settings.edit();

        // Set the necessary values for the download
        bank.setCurrency(CURRENCY);
        bank.setExchangeType(EXCHANGE);
        bank.setExchangeDirection(DIRECTION);

        // Download the new data
        int d_error = bank.downloadData();

        // If there is a problem during the download
        if (d_error > 0) {
            return 1;
        }

        // Get the new data
        float rate = bank.getCurrencyRate();
        String date = bank.getCurrencyDate();

        if (DEBUG > 2) {
            Log.d(TAG, "     * bank=" + BANK);
            Log.d(TAG, "     * bank_alias=" + BANK_ALIAS);
            Log.d(TAG, "     * currency=" + CURRENCY);
            Log.d(TAG, "     * exchange=" + EXCHANGE);
            Log.d(TAG, "     * direction=" + DIRECTION);
            Log.d(TAG, "     * period=" + PERIOD);
            Log.d(TAG, "     * notification=" + NOTIFICATION);
            Log.d(TAG, "     * tolerance=" + TOLERANCE);
            Log.d(TAG, "     * sound=" + SOUND);
            Log.d(TAG, "     * ringtone_pos=" + RINGTONE_POS);
            Log.d(TAG, "     * ringtone_neg=" + RINGTONE_NEG);
            Log.d(TAG, "     * vibrate=" + VIBRATE);
            Log.d(TAG, "     * led=" + LED);
            Log.d(TAG, "     * rate=" + rate);
            Log.d(TAG, "     * rate_prev=" + RATE_PREV);
            Log.d(TAG, "     * date=" + date);
        }

        // Always save the current rate
        editor.putFloat(SettingsActivity.FKEY_RATE, rate);

        // Save the previous "current rate" as the "previous rate"
        if (rate != RATE) {
            editor.putFloat(SettingsActivity.FKEY_RATE_PREV, RATE);
        }

        // Always save the rate date
        editor.putString(SettingsActivity.FKEY_DATE, date);

        // We can always save the time of the last update
        DateFormat dt_formatter = new SimpleDateFormat(getString(R.string.date_time_format));
        String currentTime = dt_formatter.format(new Date());
        editor.putString(SettingsActivity.FKEY_LAST_UPDATE, currentTime);

        // It is not first run anymore
        if (FIRST_RUN) {
            editor.putBoolean(SettingsActivity.FKEY_FIRST_RUN, false);
        }

        /*
         * NOTIFICATION PART
         */

        // Redefine some variables for more logical manipulation
        RATE_PREV = RATE;
        RATE = rate;

        // Is there some change in the rate?
        boolean showNotification = false;
        if (RATE_PREV != 0 && (RATE < RATE_PREV || RATE > RATE_PREV)) {
            showNotification = true;
        }

        if (DEBUG > 1)
            Log.d(TAG, "    - NOTIFICATION COND: if (" + NOTIFICATION + " && " + showNotification + " && (" + RATE_PREV + " == 0 || " + Math.abs(RATE_PREV - RATE) + " >= " + TOLERANCE + "))");

        // Show the notification if defined
        if (NOTIFICATION && showNotification && (Math.abs(LAST_NOTIF_RATE - RATE) >= TOLERANCE)) {
            // Define the rate difference
            float rateDiff;
            if (LAST_NOTIF_RATE == 0) {
                rateDiff = RATE - RATE_PREV;
            } else {
                rateDiff = RATE - LAST_NOTIF_RATE;
            }
            String rateSign = (rateDiff > 0) ? "+" : "";

            // Format for rates
            NumberFormat formatter = new DecimalFormat(bank.getRateFormat());

            // Define the notification message: CUR1 0.385 (+0.015)
            String message = getString(R.string.notification_ticker_message, CURRENCY, formatter.format(RATE), rateSign + formatter.format(rateDiff));

            if (DEBUG > 0)
                Log.d(TAG, "      - NOTIFICATION: " + message);

            // Look up the notification manager service
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Define an Intent for the overview
            Intent intent = new Intent(this, OverviewActivity.class);
            intent.putExtra("widget_id", appWidgetId);

            // The PendingIntent to launch our activity if the user selects this
            // notification
            // IF ERROR: Last parameter was 0!!!!!!!!!!!!!!!
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Construct the Notification object
            Notification notif = new Notification(R.drawable.notification_icon, getString(R.string.notification_ticker, CURRENCY), System.currentTimeMillis());

            // Set the info for the views that show in the notification panel
            notif.setLatestEventInfo(this, message, getText(R.string.notification_comment), contentIntent);

            // Wait for 100ms, vibrate for 250ms, wait for 100 ms and then
            // vibrate for 500ms
            if (VIBRATE) {
                notif.vibrate = new long[] { 100, 250, 100, 250 };
            }

            // Play a sound
            if (SOUND) {
                if (rateDiff > 0) {
                    // When the rate is growing
                    notif.sound = Uri.parse(RINGTONE_POS);
                } else {
                    // When the rate is falling
                    notif.sound = Uri.parse(RINGTONE_NEG);
                }
            }

            // Blink the LED
            if (LED) {
                if (rateDiff > 0) {
                    // When the rate is growing - blue color
                    notif.ledARGB = 0xff0000ff;
                } else {
                    // When the rate is falling - white color
                    notif.ledARGB = 0xffffffff;
                }
                notif.ledOnMS = 300;
                notif.ledOffMS = 1000;
                notif.flags |= Notification.FLAG_SHOW_LIGHTS;
            }

            // Cancel automatically when user click on the notification
            notif.flags |= Notification.FLAG_AUTO_CANCEL;

            // Note that we use R.layout.app_name as the ID for the
            // notification. It could be any integer you want, but we use
            // the convention of using a resource id for a string related to
            // the notification. It will always be a unique number within
            // your application.
            nm.notify(R.string.app_name + appWidgetId, notif);

            // Save current date as the date of the last notification
            editor.putString(SettingsActivity.FKEY_LAST_NOTIF, currentTime);

            // Save current rate as the last notified one
            editor.putFloat(SettingsActivity.FKEY_LAST_NOTIF_RATE, RATE);
        }

        // Do not forget to commit the editor changes!
        editor.commit();

        return 0;
    }

    /**
     * Update the widget values.
     */
    private RemoteViews buildWidgetUpdate(int appWidgetId) {
        if (DEBUG > 0)
            Log.d(TAG, "   - Building layout for the widget ID = " + appWidgetId);

        // Define the shared preferences file
        String PREFS_NAME = getString(R.string.preferences_file_prefix) + appWidgetId;

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String BANK = settings.getString(SettingsActivity.FKEY_BANK, getString(R.string.preferences_bank_default_value));

        // Get default currency value
        Bank bank = new BankHelper(BANK).getBank(getResources());
        String CURRENCY = settings.getString(SettingsActivity.FKEY_CURRENCY, bank.getDefaultCurrencyValue());

        // Get the rest of the values
        String BANK_ALIAS = settings.getString(SettingsActivity.FKEY_BANK_ALIAS, getString(R.string.preferences_bank_default_value));
        String EXCHANGE = settings.getString(SettingsActivity.FKEY_EXCHANGE, getString(R.string.preferences_exchange_default_value));
        String DIRECTION = settings.getString(SettingsActivity.FKEY_DIRECTION, getString(R.string.preferences_direction_default_value));
        float RATE = settings.getFloat(SettingsActivity.FKEY_RATE, 0);
        String DATE = settings.getString(SettingsActivity.FKEY_DATE, getString(R.string.widget_unknown));
        float RATE_PREV = settings.getFloat(SettingsActivity.FKEY_RATE_PREV, 0);

        // Set the widget layout
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_currency);

        // Format for rates
        NumberFormat formatter = new DecimalFormat(bank.getRateFormat());

        // Always updated values
        updateViews.setTextViewText(R.id.bank, BANK_ALIAS);
        updateViews.setTextViewText(R.id.currency, CURRENCY);
        updateViews.setTextViewText(R.id.rate, "" + formatter.format(RATE));
        updateViews.setTextViewText(R.id.date, new DateTimeFormat(getResources(), settings).format(DATE));

        // Update the exchange type
        if (EXCHANGE.equals(getString(R.string.preferences_exchange_default_value))) {
            updateViews.setImageViewResource(R.id.exchange, R.drawable.exchange_deviza);
        } else {
            updateViews.setImageViewResource(R.id.exchange, R.drawable.exchange_valuta);
        }

        // Update the exchange direction
        if (DIRECTION.equals(getString(R.string.preferences_direction_default_value))) {
            updateViews.setImageViewResource(R.id.direction, R.drawable.direction_purchase);
        } else {
            updateViews.setImageViewResource(R.id.direction, R.drawable.direction_sale);
        }

        // Update previous rate
        if (RATE_PREV == 0) {
            updateViews.setTextViewText(R.id.ratePrev, "(" + getString(R.string.widget_unknown) + ")");
        } else {
            updateViews.setTextViewText(R.id.ratePrev, "(" + formatter.format(RATE_PREV) + ")");
        }

        // Set the currency COLOR (falling/growing/unknown)
        if (RATE_PREV != 0 && RATE < RATE_PREV) {
            // Only when the rate is falling (red)
            updateViews.setTextColor(R.id.rate, Color.RED);
        } else if (RATE_PREV != 0 && RATE > RATE_PREV) {
            // Only when the rate is growing (green)
            updateViews.setTextColor(R.id.rate, Color.rgb(0, 192, 0));
        } else {
            // Otherwise use gray color
            updateViews.setTextColor(R.id.rate, Color.GRAY);
        }

        return updateViews;
    }

    @Override
    public void run() {
        Date date = new Date();

        if (DEBUG > 0)
            Log.d(TAG, "Processing thread started (" + date + ")");

        // Get system time
        long nowMillis = System.currentTimeMillis();
        // Reset seconds and minutes to zero
        long nowMillisFlat = (nowMillis / (WIDGET_UPDATE_INTERVAL * 60000)) * (WIDGET_UPDATE_INTERVAL * 60000);

        // Schedule next update alarm
        Time time = new Time();
        time.set(nowMillis);
        // time.second = 0;

        // Get current widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // Here we go through all widgets one by one
        while (hasMoreUpdates()) {
            int appWidgetId = getNextUpdate();

            if (DEBUG > 0)
                Log.d(TAG, " - Processing WIDGET ID = " + appWidgetId);

            // ///
            // Here we have to read the widget setting
            // ///
            String PREFS_NAME = getString(R.string.preferences_file_prefix) + appWidgetId;
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // Save the last widget update time
            DateFormat dt_formatter = new SimpleDateFormat(getString(R.string.date_time_format));
            settings.edit().putString(SettingsActivity.FKEY_LAST_WIDGET_UPDATE, dt_formatter.format(date)).commit();

            int period = new Integer(settings.getString(SettingsActivity.FKEY_PERIOD, getString(SettingsActivity.RDID_PERIOD)));
            int checkFromHour = new Integer(settings.getString(SettingsActivity.FKEY_CHECKFROM_HOUR, getString(SettingsActivity.RDID_CHECKFROM_HOUR)));
            int checkFromMinute = new Integer(settings.getString(SettingsActivity.FKEY_CHECKFROM_MINUTE, getString(SettingsActivity.RDID_CHECKFROM_MINUTE)));
            int checkToHour = new Integer(settings.getString(SettingsActivity.FKEY_CHECKTO_HOUR, getString(SettingsActivity.RDID_CHECKTO_HOUR)));
            int checkToMinute = new Integer(settings.getString(SettingsActivity.FKEY_CHECKTO_MINUTE, getString(SettingsActivity.RDID_CHECKTO_MINUTE)));
            String checkDayString = settings.getString(SettingsActivity.FKEY_CHECKDAY, getString(SettingsActivity.RDID_CHECKDAY));
            boolean firstRun = settings.getBoolean(SettingsActivity.FKEY_FIRST_RUN, true);

            // Get the timezone offset
            int timezone = date.getTimezoneOffset() * 60;

            // Last update in timestamp
            long lastUpdate = DateTimeFormat.getTimestamp(settings.getString(SettingsActivity.FKEY_LAST_UPDATE, "0"));
            if (lastUpdate != 0) {
                // Subtract the timezone
                lastUpdate += timezone;
            }

            String checkDay[] = checkDayString.split(":");

            // Modify the first day of the week
            // (By Google it is Sunday, by me it is Monday)
            int weekDay = time.weekDay;
            weekDay--;
            if (weekDay == -1) {
                weekDay = 6;
            }

            // Create timestamps
            long curTime = DateTimeFormat.getTimestamp(String.format("%4d%02d%02d%02d%02d00", time.year, time.month + 1, time.monthDay, time.hour, time.minute)) + timezone;
            long checkFromTime = DateTimeFormat.getTimestamp(String.format("%4d%02d%02d%02d%02d00", time.year, time.month + 1, time.monthDay, checkFromHour, checkFromMinute)) + timezone;
            long checkToTime = DateTimeFormat.getTimestamp(String.format("%4d%02d%02d%02d%02d00", time.year, time.month + 1, time.monthDay, checkToHour, checkToMinute)) + timezone;

            // If the (start > end), then the stop time is on the next day, then
            // we have to add 24h to the stop time
            if (checkFromTime > checkToTime) {
                checkToTime += 24 * 60 * 60;
            }

            if (DEBUG > 1) {
                Log.d(TAG, " - UPDATE COND: if (" + firstRun + " || (" + checkDay[weekDay].equals("1") + " && (" + (curTime >= checkFromTime) + " && " + (lastUpdate < checkFromTime) + ") || (" + (curTime >= checkToTime) + " && " + (lastUpdate < checkToTime) + "))) || (" + checkDay[weekDay].equals("1") + " && " + (curTime >= checkFromTime) + " && " + (curTime <= checkToTime) + " && (" + (curTime % (period * 60) == 0) + " || " + (nowMillis / 1000 - lastUpdate > period * 60) + ")))");
            }

            int error = 0;

            // Decide whether to update or not
            /*-
             * (first run || (correct day && (start time or we are late after the start || end time or we are late after the end)) || (in between the start and end && (in period || we are late)))
             */
            if (firstRun || (checkDay[weekDay].equals("1") && (curTime >= checkFromTime && lastUpdate < checkFromTime) || (curTime >= checkToTime && lastUpdate < checkToTime)) || (checkDay[weekDay].equals("1") && curTime >= checkFromTime && curTime <= checkToTime && (curTime % (period * 60) == 0 || nowMillis / 1000 - lastUpdate > period * 60))) {
                if (DEBUG > 0)
                    Log.d(TAG, " - Download widget values");

                error = downloadNewValues(appWidgetId);
            } else if (DEBUG > 0) {
                Log.d(TAG, " - NO download of new values");
            }

            // Definition of the default layout
            RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget_message);

            // Select widget layout and set its values
            if (error == 0) {
                if (DEBUG > 0)
                    Log.d(TAG, " - Build rate widget");

                // Update the values on the widget
                updateViews = buildWidgetUpdate(appWidgetId);
            } else {
                if (DEBUG > 0)
                    Log.d(TAG, " - Building message widget");

                // Get connectivity informations
                ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo inf[] = connectivity.getAllNetworkInfo();

                // Check if there is a network connection
                boolean has_network = false;

                for (int i = 0; i < inf.length; i++) {
                    if (inf[i].getType() == ConnectivityManager.TYPE_MOBILE || inf[i].getType() == ConnectivityManager.TYPE_WIFI) {
                        if (DEBUG > 0)
                            Log.d(TAG, "    * IS CONNECTED: (" + inf[i].getTypeName() + ") " + inf[i].isConnected());

                        if (inf[i].isConnected()) {
                            has_network = true;
                            break;
                        }
                    }
                }

                // Update the message dialog - show the error message
                if (!has_network) {
                    if (DEBUG > 0)
                        Log.d(TAG, "    *" + getString(R.string.widget_network_error));

                    updateViews.setTextViewText(R.id.message, getString(R.string.widget_network_error));
                } else {
                    if (DEBUG > 0)
                        Log.d(TAG, "    *" + getString(R.string.widget_download_error));

                    updateViews.setTextViewText(R.id.message, getString(R.string.widget_download_error));
                }
            }

            if (DEBUG > 0)
                Log.d(TAG, " - Setting the onClickPendingListener");

            // Define intent for on click
            Intent intent = new Intent(this, DialogActivity.class);
            // Pass the widget ID into the activity via intent.setData()
            Uri u = Uri.parse("custom://" + appWidgetId);
            intent.setData(u);
            // Connect click intent to the launch dialog
            // IF ERROR: Last parameter was 0!!!!!!!!!!!!!!!
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Set the pending intent for the widget
            updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Push this update to the surface
            if (updateViews != null) {
                if (DEBUG > 0)
                    Log.d(TAG, " - Updating WidgetView " + appWidgetId);

                appWidgetManager.updateAppWidget(appWidgetId, updateViews);
            }
        }

        // Wake-up every single minute (every 10 minutes)
        long nextUpdate = nowMillisFlat + WIDGET_UPDATE_INTERVAL * 60 * 1000;

        long deltaMinutes = (nextUpdate - nowMillis) / DateUtils.SECOND_IN_MILLIS;
        if (DEBUG > 0)
            Log.d(TAG, "Requesting next update at " + nextUpdate + ", in " + deltaMinutes + " sec");

        // Prepare the update
        Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
        updateIntent.setClass(this, UpdateService.class);

        // IF ERROR: Last parameter was 0!!!!!!!!!!!!!!!
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule alarm, and force the device awake for this update
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);

        // No updates remaining, so stop service
        stopSelf();
    }

    /**
     * Request updates for the given widgets. Will only queue them up, you are
     * still responsible for starting a processing thread if needed, usually by
     * starting the parent service.
     */
    public static void requestUpdate(int[] appWidgetIds) {
        if (DEBUG > 0)
            Log.d(TAG, "Requesting update for " + appWidgetIds.length + " IDs");

        synchronized (sLock) {
            for (int appWidgetId : appWidgetIds) {
                // Do not allow to add the same widget twice
                if (! sAppWidgetIds.contains(appWidgetId)) {
                    sAppWidgetIds.add(appWidgetId);
                } else if (DEBUG > 0) {
                    Log.d(TAG, "Widget ID " + appWidgetId + " is already beeing updated.");
                }
            }
        }
    }

    /**
     * Peek if we have more updates to perform. This method is special because
     * it assumes you're calling from the update thread, and that you will
     * terminate if no updates remain. (It atomically resets
     * {@link #sThreadRunning} when none remain to prevent race conditions.)
     */
    private static boolean hasMoreUpdates() {
        synchronized (sLock) {
            boolean hasMore = !sAppWidgetIds.isEmpty();

            if (!hasMore) {
                sThreadRunning = false;
            }

            return hasMore;
        }
    }

    /**
     * Poll the next widget update in the queue.
     */
    private static int getNextUpdate() {
        synchronized (sLock) {
            if (sAppWidgetIds.peek() == null) {
                return AppWidgetManager.INVALID_APPWIDGET_ID;
            } else {
                return sAppWidgetIds.poll();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
