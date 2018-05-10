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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class ResourceListFragment extends Fragment {

    //class variables
    private static final int sFILES_TAB_POSITION = 0;
    private static final int sFAQS_TAB_POSITION = 1;
    private static final int sVIEW_PDF_REQUEST_CODE = 0;
    private static final String sTAG = "ResourceListFragment";
    private static final String sARG_AREA_TITLE_RES_ID = "area_title_res_id";

    //instance variables
    private RecyclerView mResourceRecycler;
    private LinearLayout mMainLayout;
    private TabLayout mTabLayout;
    private TextView mResourceListTitle;
    private CompassDataLab mCompassDataLab;
    private ResourceAdapter mAdapter;
    private int mAreaTitleResId;

    //function to create a new instance of the fragment
    public static ResourceListFragment newInstance(int areaTitleResId) {
        Bundle args = new Bundle();
        args.putInt(sARG_AREA_TITLE_RES_ID, areaTitleResId);

        ResourceListFragment fragment = new ResourceListFragment();
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
        View v = inflater.inflate(R.layout.fragment_resource_list, container, false);

        //get the res id
        mAreaTitleResId = getArguments().getInt(sARG_AREA_TITLE_RES_ID, R.string.residential_students);

        //setup recycler view
        mResourceRecycler = (RecyclerView) v.findViewById(R.id.resource_list_recycler_view);
        mResourceRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        //setup main layout view
        mMainLayout = (LinearLayout) v.findViewById(R.id.resource_list_main);

        //setup the list title
        mResourceListTitle = (TextView) v.findViewById(R.id.resource_list_title);

        //setup the tabs
        mTabLayout = (TabLayout) v.findViewById(R.id.resource_tab_layout);
        mTabLayout.getTabAt(sFILES_TAB_POSITION).select();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == sFAQS_TAB_POSITION) {
                    Intent intent = QuestionListActivity.newIntent(getActivity(), mAreaTitleResId);
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
        List<CompassDataStructures.OrientationResource> resources = mCompassDataLab.getActiveResources();

        //set adapter
        if (mAdapter == null) {
            mAdapter = new ResourceAdapter(resources);
            mResourceRecycler.setAdapter(mAdapter);
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

            mResourceRecycler.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Primary));

            mResourceListTitle.setTextColor(Color.parseColor(theme.Theme_Colors.Title));
            mResourceListTitle.setShadowLayer(8, 8, 8, Color.parseColor(theme.Theme_Colors.Shadow));

            mTabLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));
            mTabLayout.setTabTextColors(Color.parseColor(theme.Theme_Colors.Text), Color.parseColor(theme.Theme_Colors.Text));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ResourceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //instance variables
        private CompassDataStructures.OrientationResource mResource;
        private ConstraintLayout mResourceItemLayout;
        private TextView mResourceName;
        private ImageView mArrowImage;

        public ResourceHolder(LayoutInflater inflater, ViewGroup parent, int resId){
            super(inflater.inflate(resId, parent, false));
            itemView.setOnClickListener(this);

            //setup text views
            mResourceItemLayout = (ConstraintLayout) itemView.findViewById(R.id.resource_item_main);
            mResourceName = (TextView) itemView.findViewById(R.id.resource_title);
            mArrowImage = (ImageView) itemView.findViewById(R.id.resource_arrow_image);
            mArrowImage.setColorFilter(getResources().getColor(R.color.white));
        }

        public void bind(CompassDataStructures.OrientationResource resource) {
            mResource = resource;

            mResourceName.setText(resource.Name);

            setColors();
        }

        //function to set colors for the UI
        private void setColors() {
            CompassDataStructures.OrientationTheme theme = mCompassDataLab.getPreferedTheme();

            if (theme != null) {
                mResourceItemLayout.setBackgroundColor(Color.parseColor(theme.Theme_Colors.Secondary));

                mResourceName.setTextColor(Color.parseColor(theme.Theme_Colors.Text));
                mArrowImage.setColorFilter(Color.parseColor(theme.Theme_Colors.Text));
            }
        }

        @Override
        public void onClick(View view) {
            //grab the cloud storage reference
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Resources");
            StorageReference resourceRef = storageRef.child(mResource.File_Name);
            File file = new File(getContext().getCacheDir(), mResource.File_Name);

            if (file.exists()) {
                viewFile();
            } else {
                resourceRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Log.i(sTAG, "success");
                    viewFile();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.i(sTAG, "failure");
                    }
                });
            }
        }

        //function to view the file
        public void viewFile() {
            //get file
            File file = new File(getContext().getCacheDir(), mResource.File_Name);

            //make uri to the file
            Uri uri = FileProvider.getUriForFile(getContext(), "com.example.collegecompass.champlaincompass", file);

            //create intent to view file
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/" + mResource.File_Type);

            //view file
            startActivity(intent);
        }
    }

    private class ResourceAdapter extends RecyclerView.Adapter<ResourceHolder> {

        //instance variables
        private List<CompassDataStructures.OrientationResource> mResourceList;

        public ResourceAdapter(List<CompassDataStructures.OrientationResource> resources){
            mResourceList = resources;
        }

        @Override
        public ResourceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ResourceHolder(inflater, parent, R.layout.resource_list_item);
        }

        @Override
        public void onBindViewHolder(ResourceHolder holder, int position) {
            CompassDataStructures.OrientationResource resource = mResourceList.get(position);
            holder.bind(resource);
        }

        @Override
        public int getItemCount() {
            return mResourceList.size();
        }
    }
}
