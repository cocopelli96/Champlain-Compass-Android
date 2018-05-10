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
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventListFragment extends Fragment {

    //class variables
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";
    private static final String sTAG = "EventListFragment";

    //instance variables
    private CalendarView mEventCalendar;
    private RecyclerView mEventRecycler;
    private LinearLayout mMainLayout;
    private TextView mEventListTitle;
    private TextView mNoEventsText;
    private CompassDataLab mCompassDataLab;
    private EventAdapter mAdapter;
    private int mAreaTitleResId;

    //function to create a new instance of the fragment
    public static EventListFragment newInstance(int areaTitleResId) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);

        EventListFragment fragment = new EventListFragment();
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
        View v = inflater.inflate(R.layout.fragment_events_list, container, false);

        //get the res id
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //setup recycler view
        mEventRecycler = (RecyclerView) v.findViewById(R.id.event_list_recycler_view);
        mEventRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        //setup calendar view
        mEventCalendar = (CalendarView) v.findViewById(R.id.event_calendar);
        mEventCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                updateUI(month + 1, day, year);
            }
        });
        mEventCalendar.setWeekDayTextAppearance(R.style.ChamplainCompass_WeekDayText);

        //setup main layout view
        mMainLayout = (LinearLayout) v.findViewById(R.id.event_list_main);

        //setup event list title
        mEventListTitle = (TextView) v.findViewById(R.id.event_list_title);

        //setup no events text view
        mNoEventsText = (TextView) v.findViewById(R.id.no_event_text);
        mNoEventsText.setVisibility(View.GONE);

        updateUI();

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

    //function to update UI
    public void updateUI() {
        //get data lab
        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }
        String group = getResources().getString(mAreaTitleResId);
        List<CompassDataStructures.Event> events;

        //get date on the calendar view
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date calendarDate = new Date(mEventCalendar.getDate());
        try {
            String dateString = format.format(calendarDate);
            int year = Integer.parseInt(dateString.substring(0, 4));
            int month = Integer.parseInt(dateString.substring(5, 7));
            int day = Integer.parseInt(dateString.substring(8));
            events = mCompassDataLab.getEventsForDay(group, month, day, year);
        } catch (Exception e) {
            Log.e(sTAG, e.getMessage());
            events = mCompassDataLab.getEventsForGroup(group);
        }

        //set adapter
        if (mAdapter == null) {
            mAdapter = new EventAdapter(events);
            mEventRecycler.setAdapter(mAdapter);
        } else {
            mAdapter.setEventList(events);
            mAdapter.notifyDataSetChanged();
        }

        //set list visibility
        if (events.size() <= 0) {
            setListVisibility(View.GONE);
        } else {
            setListVisibility(View.VISIBLE);
        }

        setColors();
    }

    //function to update UI based on selected date
    public void updateUI(int month, int day, int year) {
        //get data lab
        mCompassDataLab = CompassDataLab.get(getContext());
        String group = getResources().getString(mAreaTitleResId);
        List<CompassDataStructures.Event> events = mCompassDataLab.getEventsForDay(group, month, day, year);

        //set adapter
        if (mAdapter == null) {
            mAdapter = new EventAdapter(events);
            mEventRecycler.setAdapter(mAdapter);
        } else {
            mAdapter.setEventList(events);
            mAdapter.notifyDataSetChanged();
        }

        //set list visibility
        if (events.size() <= 0) {
            setListVisibility(View.GONE);
        } else {
            setListVisibility(View.VISIBLE);
        }

        setColors();
    }

    //function to set list visibility
    public void setListVisibility(int visibility) {
        if (visibility == View.GONE) {
            if (mNoEventsText.getVisibility() == View.GONE) {
                mNoEventsText.setVisibility(View.VISIBLE);
            }
            if (mEventRecycler.getVisibility() != View.GONE) {
                mEventRecycler.setVisibility(View.GONE);
            }
        } else {
            if (mNoEventsText.getVisibility() != View.GONE) {
                mNoEventsText.setVisibility(View.GONE);
            }
            if (mEventRecycler.getVisibility() == View.GONE) {
                mEventRecycler.setVisibility(View.VISIBLE);
            }
        }
    }

    //function to set colors for the UI
    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            mMainLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mEventRecycler.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mEventCalendar.setBackgroundColor(getResources().getColor(R.color.white));

            mEventListTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mEventListTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mNoEventsText.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mNoEventsText.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
        }
    }

    private class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //instance variables
        private CompassDataStructures.Event mEvent;
        private ConstraintLayout mEventItemLayout;
        private TextView mEventName;
        private TextView mEventDate;
        private TextView mEventTime;
        private ImageView mArrowImage;

        public EventHolder(LayoutInflater inflater, ViewGroup parent, int resId){
            super(inflater.inflate(resId, parent, false));
            itemView.setOnClickListener(this);

            //setup text views
            mEventItemLayout = (ConstraintLayout) itemView.findViewById(R.id.event_item_main);
            mEventName = (TextView) itemView.findViewById(R.id.event_name);
            mEventDate = (TextView) itemView.findViewById(R.id.event_date);
            mEventTime = (TextView) itemView.findViewById(R.id.event_time);
            mArrowImage = (ImageView) itemView.findViewById(R.id.event_arrow_image);
            mArrowImage.setColorFilter(getResources().getColor(R.color.white));
        }

        public void bind(CompassDataStructures.Event event) {
            mEvent = event;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

            mEventName.setText(event.Name);
            mEventDate.setText(dateFormat.format(event.getStartTime()));
            mEventTime.setText(timeFormat.format(event.getStartTime()));

            setColors();
        }

        //function to set colors for the UI
        private void setColors() {
            CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

            if (theme != null) {
                mEventItemLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));

                mEventDate.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mEventTime.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mEventName.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mArrowImage.setColorFilter(Color.parseColor(theme.Theme_Colors.Text));
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent = EventInfoActivity.newIntent(getContext(), mAreaTitleResId, mEvent);
            startActivity(intent);
        }
    }

    private class EventAdapter extends RecyclerView.Adapter<EventHolder> {

        //instance variables
        private List<CompassDataStructures.Event> mEventList;

        public EventAdapter(List<CompassDataStructures.Event> events){
            mEventList = events;
        }

        @Override
        public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new EventHolder(inflater, parent, R.layout.event_list_item);
        }

        @Override
        public void onBindViewHolder(EventHolder holder, int position) {
            CompassDataStructures.Event event = mEventList.get(position);
            holder.bind(event);
        }

        @Override
        public int getItemCount() {
            return mEventList.size();
        }

        public void setEventList(List<CompassDataStructures.Event> eventList) {
            mEventList = eventList;
        }
    }
}
