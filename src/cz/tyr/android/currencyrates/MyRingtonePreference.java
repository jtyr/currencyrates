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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Class which allows to set up a ring tone to the widget.
 * 
 * @author Jiri Tyr
 * 
 */
public class MyRingtonePreference extends RingtonePreference {
    // Debug variables
    private static final String TAG = "CR: MyRingtonePreference";
    private static final int DEBUG = 0;

    // Global variables
    private int WIDGET_ID;
    private String TYPE;
    private Context CONTEXT;

    /**
     * Constructor.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MyRingtonePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        setVars(context);
    }

    /**
     * Constructor.
     * 
     * @param context
     * @param attrs
     */
    public MyRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setVars(context);
    }

    /**
     * Constructor.
     * 
     * @param context
     */
    public MyRingtonePreference(Context context) {
        super(context);

        setVars(context);
    }

    /**
     * Private method which allow to set the Context coming through the
     * constructor.
     * 
     * @param context
     */
    private void setVars(Context context) {
        CONTEXT = context;
    }

    @Override
    protected Uri onRestoreRingtone() {
        if (DEBUG > 0)
            Log.d(TAG, "Restoring the ringtone from SharedPreferences");

        // Prepare the settings
        String PREFS_NAME = CONTEXT.getString(R.string.preferences_file_prefix) + WIDGET_ID;
        if (DEBUG > 0)
            Log.d(TAG, " - PREFS_NAME: " + PREFS_NAME);

        SharedPreferences settings = CONTEXT.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uri = settings.getString(TYPE, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());

        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        Uri result = Uri.parse(uri);

        return result;
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        if (DEBUG > 0)
            Log.d(TAG, "Saving the ringtone into the SharedPreferences (URI=" + ringtoneUri.toString() + ")");

        // Prepare the settings
        String PREFS_NAME = CONTEXT.getString(R.string.preferences_file_prefix) + WIDGET_ID;
        if (DEBUG > 0)
            Log.d(TAG, " - PREFS_NAME: " + PREFS_NAME);

        SharedPreferences settings = CONTEXT.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings.edit().putString(TYPE, ringtoneUri.toString()).commit();
    }

    /**
     * Set some parameter.
     * 
     * @param widgetId
     *            Widget ID.
     * @param type
     *            Type of the ringtone?
     */
    protected void setParams(int widgetId, String type) {
        if (DEBUG > 0) {
            Log.d(TAG, "Setting WIDGET_ID=" + widgetId);
            Log.d(TAG, "Setting TYPE=" + type);
        }

        // Set the Widget ID
        WIDGET_ID = widgetId;
        TYPE = type;
    }
}
