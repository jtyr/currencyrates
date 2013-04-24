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

package cz.tyr.android.currencyrates.simplenumberspinner;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Class which implements the Number Spinner component.
 * 
 * @author Jiri Tyr
 * 
 */
public class SimpleNumberSpinner extends View {
    // Debug variables
    private static final String TAG = "CR: NumberSpinner";
    private static final int DEBUG = 0;

    // Sign variables
    public static int SIGN_NONE = 0;
    public static int SIGN_PLUS = 1;
    public static int SIGN_MINUS = 2;

    // Spinner type
    private int TYPE_NUMBER = 1;
    private int TYPE_SIGN = 2;

    // Default values
    private int digSize = 1;
    private int decSize = 0;
    private int PLUS_BUTTON = android.R.drawable.arrow_up_float;
    private int MINUS_BUTTON = android.R.drawable.arrow_down_float;
    private int ENABLE_SIGN_PART = SIGN_NONE;

    // Arrays where to store the created buttons and text values
    private ArrayList<Button> pba = new ArrayList<Button>();
    private ArrayList<TextView> tva = new ArrayList<TextView>();
    private ArrayList<Button> mba = new ArrayList<Button>();
    private TextView stv;

    /**
     * The top-level View which is passed as a result
     */
    private ScrollView sv;

    /**
     * Context where to display the ScrollView
     */
    private Context mContext;

    /**
     * Constructor.
     * 
     * @param context
     */
    public SimpleNumberSpinner(Context context) {
        super(context);

        mContext = context;
        init();
    }

    /**
     * Constructor.
     * 
     * @param context
     * @param digitSize
     *            Count of the numbers in front of the decimal point.
     */
    public SimpleNumberSpinner(Context context, int digitSize) {
        super(context);

        mContext = context;
        digSize = digitSize;

        init();
    }

    /**
     * Constructor.
     * 
     * @param context
     * @param digitSize
     *            Size of the numbers in front of the decimal point.
     * @param decimalSize
     *            Count of the numbers behind of the decimal point.
     */
    public SimpleNumberSpinner(Context context, int digitSize, int decimalSize) {
        super(context);

        mContext = context;
        digSize = digitSize;
        decSize = decimalSize;

        init();
    }

    /**
     * Gets the value of the currently displayed number.
     * 
     * @return Returns the value of the currently displayed number.
     */
    public float getValue() {
        float val = 0;
        String valStr = "";

        // Process digits part
        for (int i = 0; i < digSize; i++) {
            valStr += ((TextView) tva.get(i)).getText();
        }

        // Process decimal part
        if (decSize > 0) {
            valStr += ".";

            for (int i = 0; i < decSize; i++) {
                valStr += ((TextView) tva.get(digSize + i)).getText();
            }
        }

        // Process sign part
        if (ENABLE_SIGN_PART > SIGN_NONE && valStr.length() > 0) {
            valStr = stv.getText() + valStr;
        }

        if (DEBUG > 0)
            Log.d(TAG, "getValue=" + valStr);

        val = Float.parseFloat(valStr);

        return val;
    }

    /**
     * Sets the value of the component.
     * 
     * @param val
     *            Number which should be displayed.
     * @return Returns itself to be able to chain the functions.
     */
    public SimpleNumberSpinner setValue(float val) {
        setValueString("" + val);

        return this;
    }

    /**
     * Sets the value of the component.
     * 
     * @param val
     *            String which is representing the number which should be
     *            displayed.
     * @return Returns itself to be able to chain the functions.
     */
    public SimpleNumberSpinner setValue(String val) {
        if (val.matches("^(-|)[0-9]+\\.[0-9]+$")) {
            setValueString(val);
        } else {
            Log.e(TAG, "Wrong format!");
        }

        return this;
    }

    /**
     * Sets the value of the component called from the setValue().
     * 
     * @param val
     *            String which is representing the number which should be
     *            displayed.
     */
    private void setValueString(String val) {
        String[] num = val.split("\\.");
        String dig = num[0];
        String dec = "";
        String sign = "+";
        if (num.length > 1) {
            dec = num[1];
        }

        // Get the sign
        if (dig.substring(0, 1).equals("-")) {
            sign = "-";
            dig.replace('-', '0');
        }

        if (DEBUG > 0)
            Log.d(TAG, "setValue=(" + dig + ";" + dec + ") (" + val + ")");

        int digLen = dig.length();
        int decLen = dec.length();

        // Fill the rest of the positions by zeros
        if (digLen > digSize) {
            dig = dig.substring((digLen - digSize), digLen);
        } else if (digLen < digSize) {
            for (int i = 0; i < (digSize - digLen); i++) {
                dig = "0" + dig;
            }
        }
        if (decLen > decSize) {
            dec = dec.substring((decLen - decSize), decLen);
        } else if (decLen < decSize) {
            for (int i = 0; i < (decSize - decLen); i++) {
                dec += "0";
            }
        }

        if (DEBUG > 0)
            Log.d(TAG, "AFTER: " + sign + ";" + dig + ";" + dec);

        // Set sign value
        if (ENABLE_SIGN_PART > SIGN_NONE) {
            stv.setText(sign);
        }

        // Digits part
        for (int i = 0; i < digSize; i++) {
            TextView tv = tva.get(i);
            String v = "0";

            v = dig.substring(i, i + 1);

            if (DEBUG > 0)
                Log.d(TAG, " * dig=" + v);

            tv.setText(v);
        }

        // Decimals part
        for (int i = digSize; i < (decSize + digSize); i++) {
            TextView tv = tva.get(i);
            String v = "0";

            v = dec.substring(i - digSize, i - digSize + 1);

            if (DEBUG > 0)
                Log.d(TAG, " * dec=" + v);

            tv.setText(v);
        }
    }

    /**
     * Sets the Plus button icon.
     * 
     * @param drawableIcon
     *            Resource ID of the icon.
     * @return Returns itself to be able to chain the functions.
     */
    public SimpleNumberSpinner setPlusButtonIcon(int drawableIcon) {
        for (int i = 0; i < pba.size(); i++) {
            ((TextView) pba.get(i)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawableIcon), null, null, null);
        }

        PLUS_BUTTON = drawableIcon;

        return this;
    }

    /**
     * Sets the Minu button icon.
     * 
     * @param drawableIcon
     *            Resource ID of the icon.
     * @return Returns itself to be able to chain the functions.
     */
    public SimpleNumberSpinner setMinusButtonIcon(int drawableIcon) {
        for (int i = 0; i < mba.size(); i++) {
            ((TextView) mba.get(i)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawableIcon), null, null, null);
        }

        MINUS_BUTTON = drawableIcon;

        return this;
    }

    /**
     * Sets the sign type.
     * 
     * @param sign
     *            Sign type (0 = NONE, 1 = PLUS sign, 2 = MINUS sign)
     * @return Returns itself to be able to chain the functions.
     */
    public SimpleNumberSpinner enableSignPart(int sign) {
        ENABLE_SIGN_PART = sign;

        return this;
    }

    /**
     * Creates the main layout.
     */
    private void init() {
        // Create all buttons and TextViews
        for (int i = 0; i < (digSize + decSize); i++) {
            // First create a TextView
            TextView ntv = new TextView(mContext);
            ntv.setText("0");
            ntv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            ntv.setGravity(Gravity.CENTER);

            // Create Plus button
            Button pb = new Button(mContext);
            pb.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(PLUS_BUTTON), null, null, null);
            pb.setOnClickListener(new NumberSpinnerOnClickListener(1, ntv, TYPE_NUMBER));

            // Create Minus button
            Button mb = new Button(mContext);
            mb.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(MINUS_BUTTON), null, null, null);
            mb.setOnClickListener(new NumberSpinnerOnClickListener(-1, ntv, TYPE_NUMBER));

            pba.add(pb);
            mba.add(mb);
            tva.add(ntv);
        }

        // Init also the sign TextView
        stv = new TextView(mContext);
        stv.setGravity(Gravity.RIGHT);
        stv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
        stv.setText("+");
    }

    /**
     * Creates the inner layout.
     * 
     * @return Returns the main ScrollView.
     */
    public View create() {
        sv = new ScrollView(mContext);
        sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        TableLayout tl = new TableLayout(mContext);
        tl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        tl.setColumnStretchable(0, true);
        int last_stretchable = digSize + 1;
        if (decSize > 0) {
            last_stretchable += decSize + 1;
        }
        if (ENABLE_SIGN_PART > SIGN_NONE) {
            last_stretchable++;
        }
        tl.setColumnStretchable(last_stretchable, true);

        /*
         * BEGIN OF THE FIRST ROW
         */

        // Row for Plus buttons
        TableRow tr1 = new TableRow(mContext);

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv11 = new TextView(mContext);
        tr1.addView(stretchableTv11);

        // Sign
        if (ENABLE_SIGN_PART > SIGN_NONE && ENABLE_SIGN_PART == (SIGN_PLUS | SIGN_MINUS)) {
            Button signBtn = new Button(mContext);
            signBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(PLUS_BUTTON), null, null, null);
            signBtn.setOnClickListener(new NumberSpinnerOnClickListener(1, stv, TYPE_SIGN));
            tr1.addView(signBtn);
        } else if (ENABLE_SIGN_PART > SIGN_NONE) {
            TextView signTv = new TextView(mContext);
            tr1.addView(signTv);
        }

        // Add all digit buttons
        for (int i = 0; i < digSize; i++) {
            tr1.addView((Button) pba.get(i));
        }

        if (decSize > 0) {
            // Add the decimal point
            TextView decimalPoint = new TextView(mContext);
            tr1.addView(decimalPoint);

            // Add all decimal buttons
            for (int i = 0; i < decSize; i++) {
                tr1.addView((Button) pba.get(digSize + i));
            }
        }

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv12 = new TextView(mContext);
        tr1.addView(stretchableTv12);

        tl.addView(tr1);

        /*
         * END OF THE FIRST ROW
         */

        /*
         * BEGIN OF THE SECOND ROW
         */

        // Row for Plus buttons
        TableRow tr2 = new TableRow(mContext);

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv21 = new TextView(mContext);
        tr2.addView(stretchableTv21);

        // Sign
        if (ENABLE_SIGN_PART > 0) {
            tr2.addView(stv);
        }

        // Add all digit buttons
        for (int i = 0; i < digSize; i++) {
            tr2.addView((TextView) tva.get(i));
        }

        if (decSize > 0) {
            // Add the decimal point
            TextView decimalPoint = new TextView(mContext);
            decimalPoint.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            decimalPoint.setGravity(Gravity.CENTER);
            decimalPoint.setText(".");
            tr2.addView(decimalPoint);

            // Add all decimal buttons
            for (int i = 0; i < decSize; i++) {
                tr2.addView((TextView) tva.get(digSize + i));
            }
        }

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv22 = new TextView(mContext);
        tr2.addView(stretchableTv22);

        tl.addView(tr2);

        /*
         * END OF THE SECOND ROW
         */

        /*
         * BEGIN OF THE THIRD ROW
         */

        // Row for Plus buttons
        TableRow tr3 = new TableRow(mContext);

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv31 = new TextView(mContext);
        tr3.addView(stretchableTv31);

        // Sign
        if (ENABLE_SIGN_PART > SIGN_NONE && ENABLE_SIGN_PART == (SIGN_PLUS | SIGN_MINUS)) {
            Button signBtn = new Button(mContext);
            signBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(MINUS_BUTTON), null, null, null);
            signBtn.setOnClickListener(new NumberSpinnerOnClickListener(-1, stv, TYPE_SIGN));
            tr3.addView(signBtn);
        } else if (ENABLE_SIGN_PART > SIGN_NONE) {
            TextView signTv = new TextView(mContext);
            tr3.addView(signTv);
        }

        // Add all digit buttons
        for (int i = 0; i < digSize; i++) {
            tr3.addView((Button) mba.get(i));
        }

        if (decSize > 0) {
            // Add the decimal point
            TextView decimalPoint = new TextView(mContext);
            tr3.addView(decimalPoint);

            // Add all decimal buttons
            for (int i = 0; i < decSize; i++) {
                tr3.addView((Button) mba.get(digSize + i));
            }
        }

        // Stretchable TextView which holds the view in the center
        TextView stretchableTv32 = new TextView(mContext);
        tr3.addView(stretchableTv32);

        tl.addView(tr3);

        /*
         * END OF THE THIRD ROW
         */

        sv.addView(tl);

        return sv;
    }

    /**
     * Class which implements OnClickListener for purpose of the NumberSpinner
     * component.
     * 
     * @author Jiri Tyr
     * 
     */
    private class NumberSpinnerOnClickListener implements View.OnClickListener {
        private int inc;
        private TextView tv;
        private int type;

        /**
         * Constructor.
         * 
         * @param increment
         *            Increment of the number.
         * @param textview
         *            Textview which is going to be changed.
         * @param spinnerType
         *            Spinner type (with o without the sign).
         */
        public NumberSpinnerOnClickListener(int increment, TextView textview,
                int spinnerType) {
            inc = increment;
            tv = textview;
            type = spinnerType;
        }

        @Override
        public void onClick(View clickedView) {
            if (type == TYPE_NUMBER) {
                int curVal = Integer.parseInt((String) tv.getText());

                if (inc > 0 && curVal < 9) {
                    tv.setText("" + (curVal + 1));
                } else if (inc < 0 && curVal > 0) {
                    tv.setText("" + (curVal - 1));
                }
            } else if (type == TYPE_SIGN) {
                String curVal = (String) tv.getText();

                if (inc > 0 && curVal.equals("-")) {
                    tv.setText("+");
                } else if (inc < 0 && curVal.equals("+")) {
                    tv.setText("-");
                }
            } else {
                Log.e(TAG, "Wrong type!");
            }
        }
    }
}