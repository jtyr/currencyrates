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
import android.content.res.Resources;
import cz.tyr.android.currencyrates.bank.RaiffeisenBank;
import cz.tyr.android.currencyrates.bank.TestingBank;
import cz.tyr.android.currencyrates.bank.UniCreditBank;

/**
 * Class which helps to handle multiple banks.
 * 
 * @author Jiri Tyr
 * 
 */
public class BankHelper {
    private static String mBankName;

    /**
     * Constructor.
     * 
     * @param bankId
     *            ID of the bank.
     */
    public BankHelper(String bankId) {
        mBankName = bankId;
    }

    /**
     * Get the bank by its ID.
     * 
     * @param res
     *            Resources.
     * @return Returns Bank object.
     */
    public Bank getBank(Resources res) {
        Bank bank;

        if (mBankName.equals("UCB_CZ")) {
            bank = new UniCreditBank(res);
        } else if (mBankName.equals("TB")) {
            bank = new TestingBank(res);
        } else {
            // Default bank
            bank = new RaiffeisenBank(res);
        }

        return bank;
    }

    /**
     * Get the bank's real name
     * 
     * @param context
     *            Context.
     * @return Returns Bank's real name.
     */
    public String getBankName(Context context) {
        String bankName = null;

        CharSequence[] bValue = context.getResources().getTextArray(R.array.select_bank_values);
        CharSequence[] bName = context.getResources().getTextArray(R.array.select_bank);
        for (int i = 0; i < bValue.length; i++) {
            if (bValue[i].equals(mBankName)) {
                bankName = (String) bName[i];
            }
        }

        return bankName;
    }
}
