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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsFragment extends Fragment {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";
    private static final String sPREFS_NAME = "ChamplainCompassSettings";

    //instance variables
    private int mAreaTitleResId;
    private LinearLayout mMainLayout;
    private TextView mSettingTitle;
    private TextView mOrientationThemeText;
    private CompassDataLab mCompassDataLab;
    private ToggleButton mOrientationThemeToggle;
    private Button mRefreshDataButton;
    private boolean mUseOrientationTheme;
    private int mUserGroup;

    //function to create a new instance of the fragment
    public static SettingsFragment newInstance(int areaTitleResId) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        //get the res id and event
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //get compass data lab
        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }

        //get shared preferences
        SharedPreferences settings = getActivity().getSharedPreferences(sPREFS_NAME, 0);
        mUserGroup = settings.getInt("UserGroup", R.string.residential_students);
        mUseOrientationTheme = settings.getBoolean("UseOrientationThemes", true);

        //setup main layout
        mMainLayout = (LinearLayout) v.findViewById(R.id.settings_main);

        //setup the title
        mSettingTitle = (TextView) v.findViewById(R.id.settings_title);

        //setup toggle switch
        mOrientationThemeToggle = (ToggleButton) v.findViewById(R.id.use_orientation_themes_toggle);
        mOrientationThemeToggle.setChecked(mUseOrientationTheme);
        mOrientationThemeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences settings = getActivity().getSharedPreferences(sPREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("UseOrientationThemes", b);
                editor.commit();

                setColors();
            }
        });

        //setup refresh data button
        mRefreshDataButton = (Button) v.findViewById(R.id.refresh_data_button);
        mRefreshDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = StartScreenActivity.newIntent(getActivity(), true);
                startActivity(intent);
            }
        });

        //setup text views
        mOrientationThemeText = (TextView) v.findViewById(R.id.use_orientation_themes);

        setColors();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.basic_menu, menu);

        //hide buttons based on the
        if (mAreaTitleResId == R.string.family_friends) {
            menu.getItem(2).setVisible(false);
        }
        menu.getItem(0).setVisible(false);
    }

    //function to set options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
                upIntent.putExtra(sARG_AREA_TITLE_RES_ID, mAreaTitleResId);
                NavUtils.navigateUpTo(getActivity(), upIntent);
                return true;
            case R.id.settings_menu_item:
                Intent settingsIntent = SettingsActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(settingsIntent);
                return true;
            case R.id.schedule_menu_item:
                Intent eventIntent = EventListActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(eventIntent);
                return true;
            case R.id.resources_menu_item:
                Intent resourceIntent = ResourceListActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(resourceIntent);
                return true;
            case R.id.map_menu_item:
                Intent mapIntent = MapActivity.newIntent(getActivity(), mAreaTitleResId);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //function to set colors for the UI
    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            mMainLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mSettingTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mSettingTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mOrientationThemeText.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));
            mRefreshDataButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mRefreshDataButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
        }
    }
}
