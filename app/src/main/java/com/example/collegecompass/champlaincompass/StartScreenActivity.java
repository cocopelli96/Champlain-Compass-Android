//  Copyright 2018 Travis Spinelli
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.example.collegecompass.champlaincompass;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class StartScreenActivity extends SingleFragmentActivity {

    //class variables
    private static final String sARG_REFRESH_DATA = "refresh_data";

    //instance variables
    private boolean mRefreshData;

    //function to create new intent
    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, StartScreenActivity.class);
        return intent;
    }

    //function to create new intent
    public static Intent newIntent(Context packageContext, Boolean refreshData) {
        Intent intent = new Intent(packageContext, StartScreenActivity.class);
        intent.putExtra(sARG_REFRESH_DATA, refreshData);
        return intent;
    }

    //function to create new fragment to display
    @Override
    protected Fragment createFragment() {
        if (mRefreshData) {
            return StartScreenFragment.newInstance(mRefreshData);
        }
        return StartScreenFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //grab extras stored in the intent
        mRefreshData = getIntent().getBooleanExtra(sARG_REFRESH_DATA, false);

        super.onCreate(savedInstanceState);
    }
}
