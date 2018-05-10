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
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class AreaSelectionActivity extends SingleFragmentActivity {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";

    //instance variables
    private int mAreaTitleResId;

    //function to create new intent
    public static Intent newIntent(Context packageContext, int areaTitleResId) {
        Intent intent = new Intent(packageContext, AreaSelectionActivity.class);

        //store data as intent extras
        intent.putExtra(sARG_AREA_TITLE_RES_ID, areaTitleResId);
        return intent;
    }

    //function to create new fragment to display
    @Override
    protected Fragment createFragment() {
        return AreaSelectionFragment.newInstance(mAreaTitleResId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //grab extras stored in the intent
        mAreaTitleResId = getIntent().getIntExtra(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        super.onCreate(savedInstanceState);
    }
}
