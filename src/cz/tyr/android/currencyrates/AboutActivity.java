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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Class which shows the About informations.
 * 
 * @author Jiri Tyr
 * 
 */
public class AboutActivity extends Activity {
    private static final String TAG = "CR: About";
    private static final int DEBUG = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG > 0)
            Log.d(TAG, "Starting about activity");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        Button close = (Button) findViewById(R.id.aboutCloseButton);
        close.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
