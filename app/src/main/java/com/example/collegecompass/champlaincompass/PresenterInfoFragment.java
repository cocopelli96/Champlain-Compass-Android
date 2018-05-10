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

import java.text.SimpleDateFormat;

public class PresenterInfoFragment extends Fragment {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";
    private static final String sARG_EVENT = "event";
    private static final String sARG_PRESENTER = "presenter";

    //instance variables
    private int mAreaTitleResId;
    private TextView mPresenterTitle;
    private TextView mPresenterJob;
    private TextView mPresenterBio;
    private LinearLayout mMainLayout;
    private CompassDataLab mCompassDataLab;
    private CompassDataStructures.Event mEvent;
    private CompassDataStructures.Presenter mPresenter;

    //function to create a new instance of the fragment
    public static PresenterInfoFragment newInstance(int areaTitleResId, CompassDataStructures.Event event, CompassDataStructures.Presenter presenter) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);
        args.putSerializable(sARG_EVENT, event);
        args.putSerializable(sARG_PRESENTER, presenter);

        PresenterInfoFragment fragment = new PresenterInfoFragment();
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
        View v = inflater.inflate(R.layout.fragment_presenter_info, container, false);

        //get the res id and event
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);
        mEvent = (CompassDataStructures.Event) getArguments().getSerializable(sARG_EVENT);
        mPresenter = (CompassDataStructures.Presenter) getArguments().getSerializable(sARG_PRESENTER);

        if (mPresenter == null) {
            Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
            upIntent.putExtra(sARG_AREA_TITLE_RES_ID, mAreaTitleResId);
            upIntent.putExtra(sARG_EVENT, mEvent);
            NavUtils.navigateUpTo(getActivity(), upIntent);
            return v;
        }

        //get compass data lab
        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }

        //set up layouts and text views
        mMainLayout = (LinearLayout) v.findViewById(R.id.presenter_info_main);

        mPresenterTitle = (TextView) v.findViewById(R.id.presenter_title);
        mPresenterTitle.setText(mPresenter.Name);

        mPresenterJob = (TextView) v.findViewById(R.id.presenter_job);
        mPresenterJob.setText(getResources().getString(R.string.presenter_job) + " " + mPresenter.Job_Title);

        mPresenterBio = (TextView) v.findViewById(R.id.presenter_bio);
        mPresenterBio.setText(getResources().getString(R.string.presenter_bio) + " " + mPresenter.Bio);

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
    }

    //function to set options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
                upIntent.putExtra(sARG_AREA_TITLE_RES_ID, mAreaTitleResId);
                upIntent.putExtra(sARG_EVENT, mEvent);
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

            mPresenterTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mPresenterTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mPresenterJob.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));
            mPresenterBio.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));
        }
    }
}
