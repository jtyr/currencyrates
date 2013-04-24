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

package cz.tyr.android.currencyrates.bank;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import cz.tyr.android.currencyrates.Bank;
import cz.tyr.android.currencyrates.R;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

/**
 * Class for the Testing Bank. Testing Bank is just a fake of a bank which
 * produces random currency values.
 * 
 * @author Jiri Tyr
 * 
 */
public class TestingBank extends Bank {
    private final String TAG = "CR: TestingBank";
    private final int DEBUG = 0;

    private Resources mResources;

    /**
     * Constructor.
     * 
     * @param resources
     */
    public TestingBank(Resources resources) {
        mResources = resources;
    }

    @Override
    public int downloadData() {
        if (mCurrency == null) {
            mCurrency = getDefaultCurrencyValue();
        }

        if (DEBUG > 0)
            Log.d(TAG, "TB Currency=" + mCurrency);

        Random rnd = new Random();
        float f = rnd.nextFloat();

        Date date = new Date();
        Format formatter = new SimpleDateFormat(mResources.getString(R.string.date_time_format));

        setCurrencyDate(formatter.format(date));
        setCurrencyRate(((float) ((long) (f * 1000))) / 1000);

        return 0;
    }

    @Override
    public int getCurrencyEntries() {
        return R.array.TB_select_currency;
    }

    @Override
    public int getCurrencyEntryValues() {
        return R.array.TB_select_currency_values;
    }

    @Override
    public String getDefaultCurrencyValue() {
        return mResources.getString(R.string.TB_default_currency);
    }

    @Override
    public Uri getWebUri() {
        return Uri.parse(mResources.getString(R.string.TB_url_web));
    }

    @Override
    public Uri getDataUri() {
        return Uri.parse(mResources.getString(R.string.TB_url_data));
    }

    @Override
    public String getDataUrl() {
        return mResources.getString(R.string.TB_url_data);
    }

    @Override
    public String getAlias() {
        return mResources.getString(R.string.TB_alias);
    }

    @Override
    public String getRateFormat() {
        return mResources.getString(R.string.TB_rate_format);
    }
}