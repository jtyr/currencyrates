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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.content.res.Resources;
import java.text.DateFormat;
import android.util.Log;

/**
 * Class which helps to format dates into the desired format.
 * 
 * @author Jiri Tyr
 * 
 */
public class DateTimeFormat {
    // Debug variables
    private final static String TAG = "CR: MyDateFormat";

    // Local variables
    private Resources mResources;
    private SharedPreferences mSettings;

    /**
     * Constructor.
     * 
     * @param resources
     *            Resources.
     * @param settings
     *            Shared preferences.
     */
    public DateTimeFormat(Resources resources, SharedPreferences settings) {
        mResources = resources;
        mSettings = settings;
    }

    /**
     * Return the formated date if the parameter is the Date.
     * 
     * @param date
     *            Input date.
     * @return Returns formated date as a String.
     */
    public String format(Date date) {
        return doFormat(date);
    }

    /**
     * Return the formated date if the parameter is the date string.
     * 
     * @param date
     *            Input date.
     * @return Returns formated date as a String.
     */
    public String format(String date) {
        // Expecting string in the default date_time_format
        DateFormat formatter = new SimpleDateFormat(mResources.getString(R.string.date_time_format));
        Date fdate = null;
        try {
            fdate = (Date) formatter.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "Can not parse date!");
        }

        return doFormat(fdate);
    }

    /**
     * Do the formating of the Date.
     * 
     * @param date
     *            Input date.
     * @return Returns formated date as a String.
     */
    private String doFormat(Date date) {
        String formatedDate = null;

        // Load values from the shared file
        String DATE_FORMAT = mSettings.getString(SettingsActivity.FKEY_DATE_FORMAT, mResources.getString(R.string.preferences_date_format_default_value));
        Boolean TIME_FORMAT = mSettings.getBoolean(SettingsActivity.FKEY_TIME_FORMAT, Boolean.parseBoolean(mResources.getString(R.string.preferences_time_format_default_value)));

        // 24-hour format is the default one (13:00)
        String T_FORMAT = mResources.getString(R.string.time_format_24);

        // Otherwise the 12-hour format is set (1:00PM)
        if (!TIME_FORMAT) {
            T_FORMAT = mResources.getString(R.string.time_format_12);
        }

        // If everything is OK, format the date
        if (date != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT + T_FORMAT);
            formatedDate = formatter.format(date);
        }

        return formatedDate;
    }

    /**
     * Converts a date to the Timestamp (number of seconds from 1.1.1970).
     * 
     * @param date
     *            Input date string (e.g. 20090921174200)
     * @return Returns the Timestamp as long number;
     */
    public static long getTimestamp(String date) {
        if (date.length() < 14) {
            Log.e(TAG, "Wrong date format: " + date);
            return -1;
        }

        int[] M = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int yearZero = 1970;

        int L = 0;
        long E = 0;

        // Format "YYYYMMddHHmmss"
        int year = str2int(date.substring(0, 4));
        int month = str2int(date.substring(4, 6));
        int day = str2int(date.substring(6, 8));
        int hour = str2int(date.substring(8, 10));
        int minute = str2int(date.substring(10, 12));
        int second = str2int(date.substring(12, 14));

        // Number of leap years (first leap year was 1972 => -2)
        L = (int) ((year - yearZero - 2) / 4);

        // Count number of seconds
        E += ((year - yearZero) * 365) + L + day;
        for (int i = 0; i < month - 1; i++) {
            E += M[i];
        }
        E *= 86400;
        E += hour * 3600;
        E += minute * 60;
        E += second;

        return E;
    }

    /**
     * Converts a string number representation into the integer.
     * 
     * @param str
     *            String as a number.
     * @return Returns the converted integer.
     */
    private static int str2int(String str) {
        int ret = 0;
        int len = str.length();

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 10; j++) {
                if (str.substring(len - i - 1, len - i).compareTo(j + "") == 0) {
                    ret += j * Math.pow(10, i); // power 10^i
                    break;
                }
            }
        }

        return ret;
    }
}
