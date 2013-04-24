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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import cz.tyr.android.currencyrates.Bank;
import cz.tyr.android.currencyrates.R;

/**
 * Class for the UniCredit Bank.
 * 
 * @author Jiri Tyr
 * 
 */
public class UniCreditBank extends Bank {
    private final String TAG = "CR: UniCreditBank";
    private final int DEBUG = 0;

    private Resources mResources;

    /**
     * Constructor.
     * 
     * @param resources
     */
    public UniCreditBank(Resources resources) {
        mResources = resources;
    }

    @Override
    public int downloadData() {
        if (mCurrency == null) {
            mCurrency = getDefaultCurrencyValue();
        }

        String dateStr = null;
        String rateStr = null;

        String url = getDataUrl();

        if (DEBUG > 0) {
            Log.d(TAG, "Download data for UCB_CZ");
            Log.d(TAG, " * url = : " + url);
            Log.d(TAG, " * currency = " + mCurrency);
            Log.d(TAG, " * exchange = " + mExchangeType);
            Log.d(TAG, " * direction = " + mExchangeDirection);
        }

        HttpClient sClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        InputStream stream = null;

        try {
            stream = sClient.execute(request).getEntity().getContent();
        } catch (IOException e) {
            Log.d(TAG, "Problem downloading the XML data.");
            return 1;
        }

        try {
            if (DEBUG > 1)
                Log.d(TAG, " - Factory start");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(stream);
            if (DEBUG > 1)
                Log.d(TAG, " - Factory end");

            if (doc != null && doc.hasChildNodes()) {
                if (DEBUG > 1)
                    Log.d(TAG, " - Parse start");

                // find the root element
                for (int i = 0; i < doc.getChildNodes().getLength(); i++) {
                    Node root = doc.getChildNodes().item(i);

                    if (root.getNodeType() == Node.ELEMENT_NODE || root.getNodeName().equals("exchange_rates")) {
                        NodeList list = doc.getChildNodes().item(i).getChildNodes();

                        // find first node
                        for (int j = 0; j < list.getLength(); j++) {
                            Node n = list.item(j);

                            // check the attributes of this element
                            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("exchange_rate")) {
                                boolean go = false;

                                if (DEBUG > 1)
                                    Log.d(TAG, " # Got EXCHANGE_RATE element!");

                                for (int k = 0; k < n.getAttributes().getLength(); k++) {
                                    Node a = n.getAttributes().item(k);

                                    if (a.getNodeName().equals("type") && a.getNodeValue().equals("XML_RATE_TYPE_UCB_" + mExchangeDirection + "_" + mExchangeType)) {
                                        if (DEBUG > 1)
                                            Log.d(TAG, " - CORRECT ELEMENT! TAKE THE DATE!");

                                        go = true;
                                    } else if (go && a.getNodeName().equals("valid_from")) {
                                        if (DEBUG > 1)
                                            Log.d(TAG, " - GOT DATE! " + a.getNodeValue());

                                        DateFormat formatter = new SimpleDateFormat("HH:mm");
                                        String time = formatter.format(new Date());
                                        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                        Date fdate = (Date) formatter.parse(a.getNodeValue() + " " + time);
                                        formatter = new SimpleDateFormat(mResources.getString(R.string.date_time_format));

                                        dateStr = formatter.format(fdate);

                                        // stop the loop
                                        break;
                                    }
                                }

                                // if it is correct element, go for the rate
                                if (go) {
                                    if (DEBUG > 1)
                                        Log.d(TAG, " - Searching for the rate!");

                                    NodeList currencies = n.getChildNodes();

                                    // check the attributes
                                    for (int k = 0; k < currencies.getLength(); k++) {
                                        Node c = currencies.item(k);

                                        if (c.getNodeType() == Node.ELEMENT_NODE && c.getNodeName().equals("currency")) {
                                            boolean bool = false;
                                            String rateTmp = null;

                                            for (int l = 0; l < c.getAttributes().getLength(); l++) {
                                                Node a = c.getAttributes().item(l);

                                                if (a.getNodeName().equals("name") && a.getNodeValue().equals(mCurrency)) {
                                                    if (DEBUG > 1)
                                                        Log.d(TAG, " -- Got the Currency!!!");

                                                    bool = true;
                                                } else if (a.getNodeName().equals("rate")) {
                                                    if (DEBUG > 1)
                                                        Log.d(TAG, " -- Got the RATE!!!" + a.getNodeValue());
                                                    rateTmp = a.getNodeValue();
                                                }

                                                if (bool && rateTmp != null) {
                                                    rateStr = rateTmp;

                                                    if (DEBUG > 1)
                                                        Log.d(TAG, " -- Got the Currency VALUE: " + rateStr);

                                                    // stop the loop
                                                    break;
                                                }
                                            }
                                        }

                                        // stop the loop
                                        if (rateStr != null) {
                                            break;
                                        }
                                    }
                                }
                            }

                            // stop the loop
                            if (rateStr != null) {
                                break;
                            }
                        }
                    }

                    // stop the loop
                    if (rateStr != null) {
                        break;
                    }
                }

                if (DEBUG > 1)
                    Log.d(TAG, " - Parse end");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        // Check the values
        if (dateStr == null || rateStr == null) {
            Log.d(TAG, " # One of the values is null!");
            return 1;
        }

        setCurrencyDate(dateStr);
        setCurrencyRate(Float.parseFloat(rateStr));

        return 0;
    }

    @Override
    public int getCurrencyEntries() {
        return R.array.UCB_CZ_select_currency;
    }

    @Override
    public int getCurrencyEntryValues() {
        return R.array.UCB_CZ_select_currency_values;
    }

    @Override
    public String getDefaultCurrencyValue() {
        return mResources.getString(R.string.UCB_CZ_default_currency);
    }

    @Override
    public Uri getWebUri() {
        return Uri.parse(mResources.getString(R.string.UCB_CZ_url_data));
    }

    @Override
    public Uri getDataUri() {
        return Uri.parse(getDataUrl());
    }

    @Override
    public String getDataUrl() {
        Calendar cal = Calendar.getInstance();
        return mResources.getString(R.string.UCB_CZ_url_data, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    @Override
    public String getAlias() {
        return mResources.getString(R.string.UCB_CZ_alias);
    }

    @Override
    public String getRateFormat() {
        return mResources.getString(R.string.UCB_CZ_rate_format);
    }
}