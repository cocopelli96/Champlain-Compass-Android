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

public class EventInfoActivity extends SingleFragmentActivity {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";
    private static final String sARG_EVENT = "event";

    //instance variables
    private int mAreaTitleResId;
    private CompassDataStructures.Event mEvent;

    //function to create new intent
    public static Intent newIntent(Context packageContext, int areaTitleResId, CompassDataStructures.Event event) {
        Intent intent = new Intent(packageContext, EventInfoActivity.class);
        intent.putExtra(sARG_AREA_TITLE_RES_ID, areaTitleResId);
        intent.putExtra(sARG_EVENT, event);
        return intent;
    }

    //function to create new fragment to display
    @Override
    protected Fragment createFragment() {
        return EventInfoFragment.newInstance(mAreaTitleResId, mEvent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //grab extras stored in the intent
        mAreaTitleResId = getIntent().getIntExtra(sARG_AREA_TITLE_RES_ID, R.string.residential_students);
        mEvent = (CompassDataStructures.Event) getIntent().getSerializableExtra(sARG_EVENT);

        super.onCreate(savedInstanceState);
    }
}
