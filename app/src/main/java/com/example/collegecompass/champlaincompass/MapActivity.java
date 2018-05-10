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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/* This file was initially created through the use of Android Studio's Google Map Activity creation feature */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    //class variables
    private static final String sTAG = "MapActivity";
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";

    //instance variables
    private GoogleMap mMap;
    private LinearLayout mMainLayout;
    private TextView mMapTitle;
    private TextView mBuildingListTitle;
    private CompassDataLab mCompassDataLab;
    private RecyclerView mBuildingRecycler;
    private BuildingAdapter mAdapter;
    private Geocoder mGeocoder;
    private HashMap<String, Marker> mMarkers;
    private int mAreaTitleResId;

    //function to create new intent
    public static Intent newIntent(Context packageContext, int areaTitleResId) {
        Intent intent = new Intent(packageContext, MapActivity.class);
        intent.putExtra(sARG_AREA_TITLE_RES_ID, areaTitleResId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //get extras from intent
        mAreaTitleResId = getIntent().getIntExtra(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //get compass data lab
        mCompassDataLab = CompassDataLab.get(getApplicationContext());
        if (!mCompassDataLab.isCached()) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }

        //setup recycler view
        mBuildingRecycler = (RecyclerView) findViewById(R.id.building_list_recycler_view);
        mBuildingRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //setup main layout
        mMainLayout = (LinearLayout) findViewById(R.id.map_main_layout);

        //setup title
        mMapTitle = (TextView) findViewById(R.id.map_title);
        mBuildingListTitle = (TextView) findViewById(R.id.building_list_title);

        //setup Geocoder
        mGeocoder = new Geocoder(getApplicationContext());

        //setup marker hash map
        mMarkers = new HashMap<String, Marker>();

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basic_menu, menu);

        //hide buttons based on the
        if (mAreaTitleResId == R.string.family_friends) {
            menu.getItem(2).setVisible(false);
        }

        return true;
    }

    //function to set options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra(sARG_AREA_TITLE_RES_ID, mAreaTitleResId);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            case R.id.settings_menu_item:
                Intent settingsIntent = SettingsActivity.newIntent(this, mAreaTitleResId);
                startActivity(settingsIntent);
                return true;
            case R.id.schedule_menu_item:
                Intent eventIntent = EventListActivity.newIntent(this, mAreaTitleResId);
                startActivity(eventIntent);
                return true;
            case R.id.resources_menu_item:
                Intent resourceIntent = ResourceListActivity.newIntent(this, mAreaTitleResId);
                startActivity(resourceIntent);
                return true;
            case R.id.map_menu_item:
                Intent mapIntent = MapActivity.newIntent(this, mAreaTitleResId);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //function to update UI
    public void updateUI() {
        //get data lab
        mCompassDataLab = CompassDataLab.get(getApplicationContext());
        List<CompassDataStructures.Building> buildings = mCompassDataLab.getActiveBuildings();

        //set adapter
        if (mAdapter == null) {
            mAdapter = new BuildingAdapter(buildings);
            mBuildingRecycler.setAdapter(mAdapter);
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

            mMapTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mMapTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mBuildingListTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mBuildingListTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //set info window design
        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.map_marker_info_window, null);

                String title = marker.getTitle();
                TextView titleUi = ((TextView) v.findViewById(R.id.marker_title));
                if (title != null) {
                    // Spannable string allows us to edit the formatting of the text.
                    SpannableString titleText = new SpannableString(title);
                    titleUi.setText(titleText);
                } else {
                    titleUi.setText("");
                }

                String snippet = marker.getSnippet();
                TextView snippetUi = ((TextView) v.findViewById(R.id.marker_snippet));
                if (snippet != null && snippet.length() > 12) {
                    SpannableString snippetText = new SpannableString(snippet);
                    snippetUi.setText(snippetText);
                } else {
                    snippetUi.setText("");
                }

                return v;
            }
        };
        mMap.setInfoWindowAdapter(infoWindowAdapter);

        // get location for Champlain College and move the camera
        LatLng champlainCollege = new LatLng(44.4731, -73.2041);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(champlainCollege));
        mMap.setBuildingsEnabled(true);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        //set other options for the map
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);

        //set the on click for the info windows
        mMap.setOnInfoWindowClickListener(this);
    }

    //function to run on info window click
    @Override
    public void onInfoWindowClick(Marker marker) {
        //get lat and long of marker
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;

        //create uri
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w");

        //open maps app
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private class BuildingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //instance variables
        private CompassDataStructures.Building mBuilding;
        private ConstraintLayout mBuildingItemLayout;
        private TextView mBuildingName;
        private ImageView mIconImage;

        public BuildingHolder(LayoutInflater inflater, ViewGroup parent, int resId){
            super(inflater.inflate(resId, parent, false));
            itemView.setOnClickListener(this);

            //setup text views
            mBuildingItemLayout = (ConstraintLayout) itemView.findViewById(R.id.building_item_main);
            mBuildingName = (TextView) itemView.findViewById(R.id.building_title);
            mIconImage = (ImageView) itemView.findViewById(R.id.building_list_icon);
            mIconImage.setColorFilter(getResources().getColor(R.color.white));
        }

        public void bind(CompassDataStructures.Building building) {
            mBuilding = building;

            mBuildingName.setText(building.Name);

            setColors();
        }

        //function to set colors for the UI
        private void setColors() {
            CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

            if (theme != null) {
                mBuildingItemLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));

                mBuildingName.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mIconImage.setColorFilter(Color.parseColor(theme.Theme_Colors.Text));
            }
        }

        //function to drop building pin on list item selection
        @Override
        public void onClick(View view) {
            try {
                List<Address> addressList = mGeocoder.getFromLocationName(mBuilding.Address, 5);

                if (addressList == null || addressList.size() == 0) {
                    Log.e(sTAG, "No address found.");
                    return;
                } else {
                    Address address = addressList.get(0);

                    LatLng buildingAddress = new LatLng(address.getLatitude(), address.getLongitude());
                    if (!mMarkers.containsKey(mBuilding.Name)) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(buildingAddress).title(mBuilding.Name).snippet(mBuilding.Address));
                        mMarkers.put(mBuilding.Name, marker);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(buildingAddress));
                }
            } catch (Exception e) {
                Log.e(sTAG, e.getMessage());
            }
        }
    }

    private class BuildingAdapter extends RecyclerView.Adapter<BuildingHolder> {

        //instance variables
        private List<CompassDataStructures.Building> mBuildingList;

        public BuildingAdapter(List<CompassDataStructures.Building> buildings){
            mBuildingList = buildings;
        }

        @Override
        public BuildingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            return new BuildingHolder(inflater, parent, R.layout.building_list_item);
        }

        @Override
        public void onBindViewHolder(BuildingHolder holder, int position) {
            CompassDataStructures.Building building = mBuildingList.get(position);
            holder.bind(building);
        }

        @Override
        public int getItemCount() {
            return mBuildingList.size();
        }
    }
}
