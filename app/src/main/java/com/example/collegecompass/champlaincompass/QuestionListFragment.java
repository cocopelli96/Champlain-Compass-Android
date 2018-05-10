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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class QuestionListFragment extends Fragment {

    //class variables
    private static final int sFILES_TAB_POSITION = 0;
    private static final int sFAQS_TAB_POSITION = 1;
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";

    //instance variables
    private RecyclerView mQuestionRecycler;
    private LinearLayout mMainLayout;
    private TabLayout mTabLayout;
    private TextView mQuestionListTitle;
    private CompassDataLab mCompassDataLab;
    private QuestionAdapter mAdapter;
    private int mAreaTitleResId;

    //function to create a new instance of the fragment
    public static QuestionListFragment newInstance(int areaTitleResId) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);

        QuestionListFragment fragment = new QuestionListFragment();
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
        View v = inflater.inflate(R.layout.fragment_question_list, container, false);

        //get the res id
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //setup recycler view
        mQuestionRecycler = (RecyclerView) v.findViewById(R.id.question_list_recycler_view);
        mQuestionRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        //setup main layout view
        mMainLayout = (LinearLayout) v.findViewById(R.id.question_list_main);

        //setup the list title
        mQuestionListTitle = (TextView) v.findViewById(R.id.question_list_title);

        //setup the tabs
        mTabLayout = (TabLayout) v.findViewById(R.id.question_tab_layout);
        mTabLayout.getTabAt(sFAQS_TAB_POSITION).select();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == sFILES_TAB_POSITION) {
                    Intent intent = ResourceListActivity.newIntent(getActivity(), mAreaTitleResId);
                    startActivity(intent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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
        List<CompassDataStructures.FrequentlyAskedQuestion> questions = mCompassDataLab.getActiveQuestions();

        //set adapter
        if (mAdapter == null) {
            mAdapter = new QuestionAdapter(questions);
            mQuestionRecycler.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        setColors();
    }

    //function to set colors for the UI
    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            mMainLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mQuestionRecycler.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mQuestionListTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mQuestionListTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mTabLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mTabLayout.setTabTextColors(Color.parseColor(theme.Theme_Colors.Text), Color.parseColor(theme.Theme_Colors.Text));
        }
    }

    private class QuestionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //instance variables
        private CompassDataStructures.FrequentlyAskedQuestion mQuestion;
        private ConstraintLayout mQuestionItemLayout;
        private TextView mQuestionTitle;
        private TextView mQuestionAnswer;

        public QuestionHolder(LayoutInflater inflater, ViewGroup parent, int resId){
            super(inflater.inflate(resId, parent, false));
            itemView.setOnClickListener(this);

            //setup text views
            mQuestionItemLayout = (ConstraintLayout) itemView.findViewById(R.id.question_item_main);
            mQuestionTitle = (TextView) itemView.findViewById(R.id.question_title);
            mQuestionAnswer = (TextView) itemView.findViewById(R.id.question_answer);
        }

        public void bind(CompassDataStructures.FrequentlyAskedQuestion question) {
            mQuestion = question;

            mQuestionTitle.setText(question.Question);
            mQuestionAnswer.setText(question.Answer);

            setColors();
        }

        //function to set colors for the UI
        private void setColors() {
            CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

            if (theme != null) {
                mQuestionItemLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));

                mQuestionTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mQuestionAnswer.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            }
        }

        @Override
        public void onClick(View view) {

        }
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionHolder> {

        //instance variables
        private List<CompassDataStructures.FrequentlyAskedQuestion> mQuestionList;

        public QuestionAdapter(List<CompassDataStructures.FrequentlyAskedQuestion> questions){
            mQuestionList = questions;
        }

        @Override
        public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new QuestionHolder(inflater, parent, R.layout.question_list_item);
        }

        @Override
        public void onBindViewHolder(QuestionHolder holder, int position) {
            CompassDataStructures.FrequentlyAskedQuestion question = mQuestionList.get(position);
            holder.bind(question);
        }

        @Override
        public int getItemCount() {
            return mQuestionList.size();
        }
    }
}
