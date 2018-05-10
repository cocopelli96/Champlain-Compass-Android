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
import android.provider.CalendarContract;
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
import java.util.Calendar;

public class EventInfoFragment extends Fragment {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";
    private static final String sARG_EVENT = "event";

    //instance variables
    private int mAreaTitleResId;
    private TextView mEventTitle;
    private TextView mEventDate;
    private TextView mEventTime;
    private TextView mEventLocation;
    private Button mEventPresenter;
    private Button mAddToCalendarButton;
    private TextView mEventDescription;
    private LinearLayout mMainLayout;
    private CompassDataLab mCompassDataLab;
    private CompassDataStructures.Event mEvent;
    private CompassDataStructures.Presenter mPresenter;

    //function to create a new instance of the fragment
    public static EventInfoFragment newInstance(int areaTitleResId, CompassDataStructures.Event event) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);
        args.putSerializable(sARG_EVENT, event);

        EventInfoFragment fragment = new EventInfoFragment();
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
        View v = inflater.inflate(R.layout.fragment_event_info, container, false);

        //The following two lines of code have been borrowed from https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
        Typeface iconFont = FontManager.getTypeface(getActivity().getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(v.findViewById(R.id.icons_container), iconFont);

        //get the res id and event
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);
        mEvent = (CompassDataStructures.Event) getArguments().getSerializable(sARG_EVENT);

        if (mEvent == null) {
            Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
            upIntent.putExtra(sARG_AREA_TITLE_RES_ID, mAreaTitleResId);
            NavUtils.navigateUpTo(getActivity(), upIntent);
        }

        //get compass data lab
        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }
        mPresenter = mCompassDataLab.getPresenter(mEvent);

        //setup date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        //set up layouts and text views
        mMainLayout = (LinearLayout) v.findViewById(R.id.event_info_main);

        mEventTitle = (TextView) v.findViewById(R.id.event_title);
        mEventTitle.setText(mEvent.Name);

        mEventDate = (TextView) v.findViewById(R.id.event_start_time);
        mEventDate.setText(dateFormat.format(mEvent.getStartTime()));

        mEventTime = (TextView) v.findViewById(R.id.event_end_time);
        mEventTime.setText("Time: " + timeFormat.format(mEvent.getStartTime()) + " - " + timeFormat.format(mEvent.getEndTime()));

        mEventLocation = (TextView) v.findViewById(R.id.event_location);
        mEventLocation.setText(getResources().getString(R.string.event_location) + " " + mEvent.Location);

        mEventPresenter = (Button) v.findViewById(R.id.event_presenter);
        mEventPresenter.setText(getResources().getString(R.string.event_presenter) + " " + mEvent.Presenter + " " + getResources().getString(R.string.arrow_right));
        mEventPresenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = PresenterInfoActivity.newIntent(getContext(), mAreaTitleResId, mEvent, mPresenter);
                startActivity(intent);
            }
        });

        //if no presenter hide button
        if (mPresenter == null) {
            mEventPresenter.setVisibility(View.GONE);
        }

        mEventDescription = (TextView) v.findViewById(R.id.event_description);
        mEventDescription.setText(getResources().getString(R.string.event_description) + " " + mEvent.Description);

        mAddToCalendarButton = (Button) v.findViewById(R.id.add_to_calendar_button);
        mAddToCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get times for event
                Calendar beginTime = Calendar.getInstance();
                beginTime.setTime(mEvent.getStartTime());
                Calendar endTime = Calendar.getInstance();
                endTime.setTime(mEvent.getEndTime());

                //create intent to add event to calendar
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, mEvent.Name)
                        .putExtra(CalendarContract.Events.DESCRIPTION, mEvent.Description)
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, mEvent.Location)
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                startActivity(intent);

            }
        });

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

            mEventTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mEventTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mEventDate.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));
            mEventTime.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));
            mEventLocation.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));

            mEventPresenter.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mEventPresenter.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));

            mEventDescription.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Secondary));

            mAddToCalendarButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mAddToCalendarButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
        }
    }
}
