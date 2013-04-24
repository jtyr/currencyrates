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

import java.io.File;
import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Main class which loads up the widget.
 * 
 * @author Jiri Tyr
 * 
 */
public class CurrencyRates extends AppWidgetProvider {
    private static final String TAG = "CR: CurrencyRates";
    private static final int DEBUG = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG > 0)
            Log.d(TAG, "Called onUpdate for [" + Arrays.toString(appWidgetIds) + "]");

        // If no specific widgets requested, collect list of all
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, CurrencyRates.class));
        }

        // Request update for these widgets and launch updater service
        UpdateService.requestUpdate(appWidgetIds);
        context.startService(new Intent(context, UpdateService.class));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            if (DEBUG > 0)
                Log.d(TAG, "Deleting appWidgetId=" + appWidgetId);

            // Remove the SharedPreference file (how to get the data directory
            // directly?)
            File spf = new File(context.getFilesDir() + "/../shared_prefs/" + context.getString(R.string.preferences_file_prefix) + appWidgetId + ".xml");
            boolean success = spf.delete();

            if (DEBUG > 0)
                if (!success) {
                    Log.d(TAG, "SharedPreferences deletion failed.");
                } else {
                    Log.d(TAG, "SharedPreferences deleted.");
                }
        }
    }

    /*-
     * Workaround for Cupcake 1.5 to call properly onDeleted() method
     * http://groups.google.com/group/android-developers/msg/e405ca19df2170e2?pli=1
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[] { appWidgetId });
            }
        } else {
            super.onReceive(context, intent);
        }
    }
}