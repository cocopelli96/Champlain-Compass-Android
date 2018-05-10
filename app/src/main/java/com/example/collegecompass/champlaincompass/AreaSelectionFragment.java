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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class AreaSelectionFragment extends Fragment {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";

    //instance variables
    private TextView mAreaTitleTextView;
    private int mAreaTitleResId;
    private Button mScheduleButton;
    private Button mResourceButton;
    private Button mMapButton;
    private LinearLayout mMainLayout;
    private CompassDataLab mCompassDataLab;

    //function to create a new instance of the fragment
    public static AreaSelectionFragment newInstance(int areaTitleResId) {
        //create a bundle to store data
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);

        //create the fragment
        AreaSelectionFragment fragment = new AreaSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set options menu for the screen
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_area_selection, container, false);

        //get the res id
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //The following two lines of code have been borrowed from https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
        Typeface iconFont = FontManager.getTypeface(getActivity().getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(v.findViewById(R.id.icons_container), iconFont);

        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }

        //setup linear layout
        mMainLayout = (LinearLayout) v.findViewById(R.id.area_selection_main);

        //setup title text view
        mAreaTitleTextView = (TextView) v.findViewById(R.id.area_title);
        mAreaTitleTextView.setText(mAreaTitleResId);

        //setup buttons
        mScheduleButton = (Button) v.findViewById(R.id.schedule_button);
        mScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventListActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(intent);
            }
        });

        mResourceButton = (Button) v.findViewById(R.id.resource_button);
        mResourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ResourceListActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(intent);
            }
        });

        mMapButton = (Button) v.findViewById(R.id.map_button);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MapActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(intent);
            }
        });

        //hide buttons based on the
        if (mAreaTitleResId == R.string.family_friends) {
            mResourceButton.setVisibility(View.GONE);
        }

        setColors();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu_item:
                Intent settingsIntent = SettingsActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            //set main layout background color
            mMainLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            //set title text view colors
            mAreaTitleTextView.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mAreaTitleTextView.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            //set button colors
            mScheduleButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mScheduleButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mResourceButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mResourceButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mMapButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mMapButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
        }
    }
}
