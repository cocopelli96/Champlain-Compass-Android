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

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartScreenFragment extends Fragment {

    //class variables
    private static final String sTAG = "StartScreenFragment";
    private static final String sARG_REFRESH_DATA = "refresh_data";
    private static final String[] sLOCATION_PERMISSIONS = new String[] {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int sREQUEST_LOCATION_PERMISSIONS = 0;

    //instance variables
    private ImageView mStartScreenMainImage;
    private TextView mClickToStartTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CompassDataLab mCompassDataLab;
    private ConstraintLayout mLoadingLayout;
    private TextView mLoadingIcon;
    private Boolean mRefreshData;

    //function to create a new instance of the fragment
    public static StartScreenFragment newInstance() {
        Bundle args = new Bundle();

        StartScreenFragment fragment = new StartScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //function to create a new instance of the fragment
    public static StartScreenFragment newInstance(Boolean refreshData) {
        Bundle args = new Bundle();

        StartScreenFragment fragment = new StartScreenFragment();
        fragment.setArguments(args);
        args.putSerializable(sARG_REFRESH_DATA, refreshData);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start_screen, container,false);

        //The following two lines of code have been borrowed from https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
        Typeface iconFont = FontManager.getTypeface(getActivity().getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(v.findViewById(R.id.loading_layout), iconFont);

        //anonymous authentication to firebase code borrowed from firebase.google.com
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(sTAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(sTAG, "signInAnonymously:failure", task.getException());
                        }
                    }
                });

        //get the refresh data argument
        mRefreshData = getArguments().getBoolean(sARG_REFRESH_DATA, false);

        //grab the database reference
        mCompassDataLab = CompassDataLab.get(getContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference themesRef = mDatabase.child("Orientation");
        DatabaseReference eventsRef = mDatabase.child("Events");
        DatabaseReference questionsRef = mDatabase.child("Frequently_Asked_Questions");
        DatabaseReference resourcesRef = mDatabase.child("Resources");
        DatabaseReference presentersRef = mDatabase.child("Presenters");
        DatabaseReference buildingRef = mDatabase.child("Building");

        //if data is not cached
        if (!mCompassDataLab.isCached() || mRefreshData) {
            mCompassDataLab.clear();
            //set data listener to retrieve themes
            themesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.OrientationTheme theme = dataSnapshot.getValue(CompassDataStructures.OrientationTheme.class);
                    String key = dataSnapshot.getKey();
                    mCompassDataLab.addOrientationTheme(key, theme);
                    mCompassDataLab.saveAsJson();

                    setColors();
                    setLogo();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.OrientationTheme theme = dataSnapshot.getValue(CompassDataStructures.OrientationTheme.class);
                    String key = dataSnapshot.getKey();
                    mCompassDataLab.removeOrientationTheme(key);
                    mCompassDataLab.addOrientationTheme(key, theme);
                    mCompassDataLab.saveAsJson();

                    setColors();
                    setLogo();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String key = dataSnapshot.getKey();
                    mCompassDataLab.removeOrientationTheme(key);
                    mCompassDataLab.saveAsJson();

                    setColors();
                    setLogo();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //set data listener to retrieve events
            eventsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Event event = dataSnapshot.getValue(CompassDataStructures.Event.class);
                    mCompassDataLab.addEvent(event);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Event event = dataSnapshot.getValue(CompassDataStructures.Event.class);
                    mCompassDataLab.removeEvent(event);
                    mCompassDataLab.addEvent(event);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    CompassDataStructures.Event event = dataSnapshot.getValue(CompassDataStructures.Event.class);
                    mCompassDataLab.removeEvent(event);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //set data listener to retrieve questions
            questionsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.FrequentlyAskedQuestion question = dataSnapshot.getValue(CompassDataStructures.FrequentlyAskedQuestion.class);
                    mCompassDataLab.addQuestion(question);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.FrequentlyAskedQuestion question = dataSnapshot.getValue(CompassDataStructures.FrequentlyAskedQuestion.class);
                    mCompassDataLab.removeQuestion(question);
                    mCompassDataLab.addQuestion(question);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    CompassDataStructures.FrequentlyAskedQuestion question = dataSnapshot.getValue(CompassDataStructures.FrequentlyAskedQuestion.class);
                    mCompassDataLab.removeQuestion(question);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //set data listener to retrieve resources
            resourcesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.OrientationResource resource = dataSnapshot.getValue(CompassDataStructures.OrientationResource.class);
                    mCompassDataLab.addResource(resource);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.OrientationResource resource = dataSnapshot.getValue(CompassDataStructures.OrientationResource.class);
                    mCompassDataLab.removeResource(resource);
                    mCompassDataLab.addResource(resource);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    CompassDataStructures.OrientationResource resource = dataSnapshot.getValue(CompassDataStructures.OrientationResource.class);
                    mCompassDataLab.removeResource(resource);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //set data listener to retrieve presenters
            presentersRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Presenter presenter = dataSnapshot.getValue(CompassDataStructures.Presenter.class);
                    mCompassDataLab.addPresenter(presenter);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Presenter presenter = dataSnapshot.getValue(CompassDataStructures.Presenter.class);
                    mCompassDataLab.removePresenter(presenter);
                    mCompassDataLab.addPresenter(presenter);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    CompassDataStructures.Presenter presenter = dataSnapshot.getValue(CompassDataStructures.Presenter.class);
                    mCompassDataLab.removePresenter(presenter);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //set data listener to retrieve buildings
            buildingRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Building building = dataSnapshot.getValue(CompassDataStructures.Building.class);
                    mCompassDataLab.addBuilding(building);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    CompassDataStructures.Building building= dataSnapshot.getValue(CompassDataStructures.Building.class);
                    mCompassDataLab.removeBuilding(building);
                    mCompassDataLab.addBuilding(building);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    CompassDataStructures.Building building = dataSnapshot.getValue(CompassDataStructures.Building.class);
                    mCompassDataLab.removeBuilding(building);
                    mCompassDataLab.saveAsJson();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //grab the constraint layout and apply the background
        mStartScreenMainImage = (ImageView) v.findViewById(R.id.start_screen_main);

        //setup the text view
        mClickToStartTextView = (TextView) v.findViewById(R.id.click_to_start);
        mClickToStartTextView.setVisibility(View.GONE);

        //setup loading layout
        mLoadingLayout = (ConstraintLayout) v.findViewById(R.id.loading_layout);
        mLoadingIcon = (TextView) v.findViewById(R.id.loading_icon);
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(6000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        mLoadingIcon.startAnimation(rotate);

        //if data was cached run the set colors and logo
        if (mCompassDataLab.isCached()) {
            setColors();
            setLogo();
        }

        //request permissions if needed
        if (!hasLocationPermission()) {
            requestPermissions(sLOCATION_PERMISSIONS, sREQUEST_LOCATION_PERMISSIONS);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    //function to set colors for the UI
    public void setColors() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            mClickToStartTextView.setTextColor(Color.parseColor(theme.Theme_Colors.Text_Click));
            mClickToStartTextView.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));
        }
    }

    //function to set logo for the start screen
    public void setLogo() {
        CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

        if (theme != null) {
            //grab the cloud storage reference
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference logoRef = storageRef.child(theme.Logo.File_Name);
            //grab image for the view code borrowed from firebase.google.com
            Glide.with(getActivity())
                    .using(new FirebaseImageLoader())
                    .load(logoRef)
                    .into(mStartScreenMainImage);

            //set the visibility of the loading layout
            if (mLoadingLayout.getVisibility() != View.GONE) {
                mLoadingIcon.clearAnimation();
                mLoadingLayout.setVisibility(View.GONE);
            }

            //set the visibility of the click text
            if (mClickToStartTextView.getVisibility() == View.GONE) {
                mClickToStartTextView.setVisibility(View.VISIBLE);

                //set click listener
                mStartScreenMainImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = GroupSelectionActivity.newIntent(getActivity());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    //function to determine if the app has location permissions
    public boolean hasLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), sLOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}

