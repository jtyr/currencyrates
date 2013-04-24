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

import android.net.Uri;

/**
 * Abstract class for all available banks.
 * 
 * @author Jiri Tyr
 * 
 */
public abstract class Bank {
    protected String mCurrency;
    protected String mCurrencyDate;
    protected float mCurrencyRate;
    protected String mExchangeType;
    protected String mExchangeDirection;

    /**
     * Download the latest currency rate data.
     * 
     * @return Returns the error value (0 = no error, 1 = error).
     */
    public abstract int downloadData();

    /**
     * Gets the resource ID of the labels of all available currencies.
     * 
     * @return Returns the resource ID of the list.
     */
    public abstract int getCurrencyEntries();

    /**
     * Gets the resource ID of the values of all available currencies.
     * 
     * @return Returns the resource ID of the list.
     */
    public abstract int getCurrencyEntryValues();

    /**
     * Gets the default currency value.
     * 
     * @return Returns the default currency value.
     */
    public abstract String getDefaultCurrencyValue();

    /**
     * Gets the URI which can be shown in the Web browser.
     * 
     * @return Returns the URI.
     */
    public abstract Uri getWebUri();

    /**
     * Gets the URI where is the currency rate available.
     * 
     * @return Returns the currency rate data URI.
     */
    public abstract Uri getDataUri();

    /**
     * Gets the URL where is the currency rate available.
     * 
     * @return Returns the currency rate data URL.
     */
    public abstract String getDataUrl();

    /**
     * Gets the bank alias from the resources.
     * 
     * @return Returns the bank alias.
     */
    public abstract String getAlias();

    /**
     * Gets the rate format from the resources.
     * 
     * @return Returns the rate format string.
     */
    public abstract String getRateFormat();

    /**
     * Sets the currency value.
     * 
     * @param currency
     *            Currency value.
     */
    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    /**
     * Sets the exchange type (valuta or deviza).
     * 
     * @param exchangeType
     *            Exchange type.
     */
    public void setExchangeType(String exchangeType) {
        mExchangeType = exchangeType;
    }

    /**
     * Sets the exchange direction (sale or purchase).
     * 
     * @param exchangeDirection
     *            Exchange direction.
     */
    public void setExchangeDirection(String exchangeDirection) {
        mExchangeDirection = exchangeDirection;
    }

    /**
     * Sets the currency rate.
     * 
     * @param currencyRate
     *            Currency rate.
     */
    public void setCurrencyRate(float currencyRate) {
        mCurrencyRate = currencyRate;
    }

    /**
     * Sets the date of the currency rate.
     * 
     * @param currencyDate
     *            Date of the currency rate.
     */
    public void setCurrencyDate(String currencyDate) {
        mCurrencyDate = currencyDate;
    }

    /**
     * Gets the currency rate.
     * 
     * @return Returns the currency rate as a number.
     */
    public float getCurrencyRate() {
        return mCurrencyRate;
    }

    /**
     * Gets the date of the currency rate.
     * 
     * @return Returns the date of the currency rate.
     */
    public String getCurrencyDate() {
        return mCurrencyDate;
    }
}