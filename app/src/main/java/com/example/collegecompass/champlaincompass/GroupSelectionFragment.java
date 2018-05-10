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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class GroupSelectionFragment extends Fragment {

    //class variables
    private static final int sRC_SIGN_IN_FAMILY = 1;
    private static final int sRC_SIGN_IN_RESIDENTIAL = 2;
    private static final int sRC_SIGN_IN_COMMUTER = 3;
    private static final String sTAG = "GroupSelection";

    //instance variables
    private Button mFamilyButton;
    private Button mResidentialButton;
    private Button mCommuterButton;
    private LinearLayout mMainLayout;
    private TextView mTitleTextView;
    private FirebaseAuth mAuth;
    private CompassDataLab mCompassDataLab;

    //function to create a new instance of the fragment
    public static GroupSelectionFragment newInstance() {
        Bundle args = new Bundle();

        GroupSelectionFragment fragment = new GroupSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group_selection, container, false);

        //The following two lines of code have been borrowed from https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
        Typeface iconFont = FontManager.getTypeface(getActivity().getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(v.findViewById(R.id.icons_container), iconFont);

        mCompassDataLab = CompassDataLab.get(getContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(getContext(), SplashActivity.class);
            startActivity(intent);
        }

        mAuth = FirebaseAuth.getInstance();

        //setup linear layout
        mMainLayout = (LinearLayout) v.findViewById(R.id.group_selection_main);

        //setup title text view
        mTitleTextView = (TextView) v.findViewById(R.id.group_selection_title);

        //setup the group buttons
        mFamilyButton = (Button) v.findViewById(R.id.family_button);
        mFamilyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AreaSelectionActivity.newIntent(getActivity(), R.string.family_friends);
                startActivity(intent);
            }
        });

        mResidentialButton = (Button) v.findViewById(R.id.residential_button);
        mResidentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AreaSelectionActivity.newIntent(getActivity(), R.string.residential_students);
                startActivity(intent);
            }
        });

        mCommuterButton = (Button) v.findViewById(R.id.commuter_button);
        mCommuterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AreaSelectionActivity.newIntent(getActivity(), R.string.commuter_students);
                startActivity(intent);
            }
        });

        setColors();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    //function to set colors for the UI
    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            //set main layout background color
            mMainLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            //set title text view colors
            mTitleTextView.setText(theme.Theme_Name);
            mTitleTextView.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mTitleTextView.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            //set button colors
            mFamilyButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mFamilyButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mResidentialButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mResidentialButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
            mCommuterButton.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mCommuterButton.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
        }
    }
}
