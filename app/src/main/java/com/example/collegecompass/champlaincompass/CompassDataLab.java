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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CompassDataLab {

    //class variables
    private static CompassDataLab sCompassDataLab;
    private static final String sTAG = "CompassDataLab";
    private static final String sPREFS_NAME = "ChamplainCompassSettings";

    //instance variables
    private Context mContext;
    private HashMap<String, CompassDataStructures.OrientationTheme> mOrientationThemes;
    private List<CompassDataStructures.FrequentlyAskedQuestion> mFrequentlyAskedQuestions;
    private List<CompassDataStructures.Event> mEvents;
    private List<CompassDataStructures.Presenter> mPresenters;
    private List<CompassDataStructures.OrientationResource> mOrientationResources;
    private List<CompassDataStructures.Building> mBuildings;
    private Date mTimestamp;

    public static CompassDataLab get(Context context) {
        //create data object if it doesn't exist
        if (sCompassDataLab == null) {
            sCompassDataLab = new CompassDataLab(context);
        }

        //if data file exists load data from this file
        if (sCompassDataLab.isCached()) {
            sCompassDataLab.loadFromJson();
        }
        return sCompassDataLab;
    }

    //constructor
    private CompassDataLab(Context context) {
        mTimestamp = getBaseDate();
        mContext = context.getApplicationContext();
        mOrientationThemes = new HashMap<String, CompassDataStructures.OrientationTheme>();
        mFrequentlyAskedQuestions = new ArrayList<CompassDataStructures.FrequentlyAskedQuestion>();
        mOrientationResources = new ArrayList<CompassDataStructures.OrientationResource>();
        mEvents = new ArrayList<CompassDataStructures.Event>();
        mPresenters = new ArrayList<CompassDataStructures.Presenter>();
        mBuildings = new ArrayList<CompassDataStructures.Building>();
    }

    //function to get current date
    private Date getBaseDate() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY) - 3);
        return time.getTime();
    }

    //function to get list of themes
    public HashMap<String, CompassDataStructures.OrientationTheme> getOrientationThemes() {
        return mOrientationThemes;
    }

    //function to set list of themes
    public void setOrientationThemes(HashMap<String, CompassDataStructures.OrientationTheme> orientationThemes) {
        mOrientationThemes = orientationThemes;
    }

    //function to get list of theme based on provided key
    public CompassDataStructures.OrientationTheme getOrientationTheme(String key) {
        return mOrientationThemes.get(key);
    }

    //function to add theme
    public void addOrientationTheme(String key, CompassDataStructures.OrientationTheme orientationTheme) {
        mOrientationThemes.put(key, orientationTheme);
    }

    //function to remove theme form data set based on provided key
    public void removeOrientationTheme(String key) {
        mOrientationThemes.remove(key);
    }

    //function to get current application theme
    public CompassDataStructures.OrientationTheme getCurrentTheme() {
        List<CompassDataStructures.OrientationTheme> themes = new ArrayList<CompassDataStructures.OrientationTheme>();

        //select current theme
        for (String key : mOrientationThemes.keySet()) {
            if (mOrientationThemes.get(key).Is_Current) {
                themes.add(mOrientationThemes.get(key));
            }
        }

        //if more than one theme selected grab the first one
        if (themes.size() > 1) {
            Collections.sort(themes, new Comparator<CompassDataStructures.OrientationTheme>() {
                @Override
                public int compare(CompassDataStructures.OrientationTheme t1, CompassDataStructures.OrientationTheme t2) {
                    return t1.Semester.compareTo(t2.Semester);
                }
            });
            return themes.get(0);
        } else if (themes.size() == 1) {
            return themes.get(0);
        } else {
            return null;
        }
    }

    //function to get basic application theme
    public CompassDataStructures.OrientationTheme getBasicTheme() {
        //get the basic theme named "FALL"
        if (mOrientationThemes.containsKey("FALL")) {
            return mOrientationThemes.get("FALL");
        } else {
            return null;
        }
    }

    //function to get preferred application theme
    public CompassDataStructures.OrientationTheme getPreferedTheme() {
        //get theme preference
        SharedPreferences settings = mContext.getSharedPreferences(sPREFS_NAME, 0);

        //grab the preferred theme
        if (settings.getBoolean("UseOrientationThemes", true)) {
            return getCurrentTheme();
        } else {
            return getBasicTheme();
        }
    }

    //function to get list of events
    public List<CompassDataStructures.Event> getEvents() {
        return mEvents;
    }

    //function to set list of events
    public void setEvents(List<CompassDataStructures.Event> events) {
        mEvents = events;
    }

    //function to add event to data set
    public void addEvent(CompassDataStructures.Event event) {
        mEvents.add(event);
    }

    //function to remove event from data set based on index
    public void removeEvent(int position) {
        mEvents.remove(position);
    }

    //function to remove event from data set based on provided event
    public void removeEvent(CompassDataStructures.Event event) {
        mEvents.remove(event);
    }

    //function to get events for specific month
    public List<CompassDataStructures.Event> getEventsForMonth(int month, int year) {
        //prepare date string
        String date = year + "-";
        if (month < 10) {
            date += "0" + month;
        } else {
            date += month;
        }
        List<CompassDataStructures.Event> eventList = new ArrayList<CompassDataStructures.Event>();

        //select events for month
        for (CompassDataStructures.Event event : mEvents) {
            if (event.Start_Time.contains(date)) {
                eventList.add(event);
            }
        }

        //sort events by time
        Collections.sort(eventList, new Comparator<CompassDataStructures.Event>() {
            @Override
            public int compare(CompassDataStructures.Event t1, CompassDataStructures.Event t2) {
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        return eventList;
    }

    //function to get events for specific date
    public List<CompassDataStructures.Event> getEventsForDay(int month, int day, int year) {
        //perpare date string
        String date = year + "-";
        if (month < 10) {
            date += "0" + month;
        } else {
            date += month;
        }
        if (day < 10) {
            date += "-0" + day;
        } else {
            date += "-" + day;
        }
        List<CompassDataStructures.Event> eventList = new ArrayList<CompassDataStructures.Event>();

        //select events for date
        for (CompassDataStructures.Event event : mEvents) {
            if (event.Start_Time.contains(date)) {
                eventList.add(event);
            }
        }

        //sort events by time
        Collections.sort(eventList, new Comparator<CompassDataStructures.Event>() {
            @Override
            public int compare(CompassDataStructures.Event t1, CompassDataStructures.Event t2) {
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        return eventList;
    }

    //function to get events for group for specific month
    public List<CompassDataStructures.Event> getEventsForMonth(String group, int month, int year) {
        //prepare date string
        String date = year + "-";
        if (month < 10) {
            date += "0" + month;
        } else {
            date += month;
        }
        List<CompassDataStructures.Event> eventList = new ArrayList<CompassDataStructures.Event>();

        //select events for group and month
        for (CompassDataStructures.Event event : mEvents) {
            if (event.Start_Time.contains(date) && event.Groups.contains(group)) {
                eventList.add(event);
            }
        }

        //sort by time of events
        Collections.sort(eventList, new Comparator<CompassDataStructures.Event>() {
            @Override
            public int compare(CompassDataStructures.Event t1, CompassDataStructures.Event t2) {
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        return eventList;
    }

    //function to get events for a group for a specific date
    public List<CompassDataStructures.Event> getEventsForDay(String group, int month, int day, int year) {
        //prepare date string
        String date = year + "-";
        if (month < 10) {
            date += "0" + month;
        } else {
            date += month;
        }
        if (day < 10) {
            date += "-0" + day;
        } else {
            date += "-" + day;
        }
        List<CompassDataStructures.Event> eventList = new ArrayList<CompassDataStructures.Event>();

        //select events for group and date
        for (CompassDataStructures.Event event : mEvents) {
            if (event.Start_Time.contains(date) && event.Groups.contains(group)) {
                eventList.add(event);
            }
        }

        //sort by time of events
        Collections.sort(eventList, new Comparator<CompassDataStructures.Event>() {
            @Override
            public int compare(CompassDataStructures.Event t1, CompassDataStructures.Event t2) {
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        return eventList;
    }

    //function to get events based on group
    public List<CompassDataStructures.Event> getEventsForGroup(String group) {
        List<CompassDataStructures.Event> eventList = new ArrayList<CompassDataStructures.Event>();

        //select events based on group
        for (CompassDataStructures.Event event : mEvents) {
            if (event.Groups.contains(group)) {
                eventList.add(event);
            }
        }

        //sort based on time of event
        Collections.sort(eventList, new Comparator<CompassDataStructures.Event>() {
            @Override
            public int compare(CompassDataStructures.Event t1, CompassDataStructures.Event t2) {
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        return eventList;
    }

    //function to get list of questions
    public List<CompassDataStructures.FrequentlyAskedQuestion> getFrequentlyAskedQuestions() {
        return mFrequentlyAskedQuestions;
    }

    //function to set list of questions
    public void setFrequentlyAskedQuestions(List<CompassDataStructures.FrequentlyAskedQuestion> frequentlyAskedQuestions) {
        mFrequentlyAskedQuestions = frequentlyAskedQuestions;
    }

    //function add question to the data set
    public void addQuestion(CompassDataStructures.FrequentlyAskedQuestion question) {
        mFrequentlyAskedQuestions.add(question);
    }

    //function to remove question from data set based on provided index
    public void removeQuestion(int position) {
        mFrequentlyAskedQuestions.remove(position);
    }

    //function to remove question from data set based on provided question
    public void removeQuestion(CompassDataStructures.FrequentlyAskedQuestion question) {
        mFrequentlyAskedQuestions.remove(question);
    }

    //function to get list of active questions
    public List<CompassDataStructures.FrequentlyAskedQuestion> getActiveQuestions() {
        List<CompassDataStructures.FrequentlyAskedQuestion> questionList = new ArrayList<CompassDataStructures.FrequentlyAskedQuestion>();

        //select active questions
        for (CompassDataStructures.FrequentlyAskedQuestion question : mFrequentlyAskedQuestions) {
            if (question.Is_Active) {
                questionList.add(question);
            }
        }

        return questionList;
    }

    //function to get list of resources
    public List<CompassDataStructures.OrientationResource> getOrientationResources() {
        return mOrientationResources;
    }

    //function to set the list of resources
    public void setOrientationResources(List<CompassDataStructures.OrientationResource> orientationResources) {
        mOrientationResources = orientationResources;
    }

    //function to add resource to the data set
    public void addResource(CompassDataStructures.OrientationResource resource) {
        mOrientationResources.add(resource);
    }

    //function to remove resource from list based on provided index
    public void removeResource(int position) {
        mOrientationResources.remove(position);
    }

    //function to remove resource from data set based on provided resource
    public void removeResource(CompassDataStructures.OrientationResource resource) {
        mOrientationResources.remove(resource);
    }

    //function to get list of active resources
    public List<CompassDataStructures.OrientationResource> getActiveResources() {
        List<CompassDataStructures.OrientationResource> resourceList = new ArrayList<CompassDataStructures.OrientationResource>();

        //select active resources
        for (CompassDataStructures.OrientationResource resource : mOrientationResources) {
            if (resource.Is_Active) {
                resourceList.add(resource);
            }
        }

        //sort by resource name
        Collections.sort(resourceList, new Comparator<CompassDataStructures.OrientationResource>() {
            @Override
            public int compare(CompassDataStructures.OrientationResource t1, CompassDataStructures.OrientationResource t2) {
                return t1.Name.compareTo(t2.Name);
            }
        });

        return resourceList;
    }

    //function to get list of presenters
    public List<CompassDataStructures.Presenter> getPresenters() {
        return mPresenters;
    }

    //function to get a present based on provided event
    public CompassDataStructures.Presenter getPresenter(CompassDataStructures.Event event) {
        //find presenter for event
        for (CompassDataStructures.Presenter presenter : mPresenters) {
            if (presenter.Name.equals(event.Presenter)) {
                return presenter;
            }
        }

        return null;
    }

    //function to set presenter list
    public void setPresenters(List<CompassDataStructures.Presenter> presenters) {
        mPresenters = presenters;
    }

    //function to add presenter to the data set
    public void addPresenter(CompassDataStructures.Presenter presenter) {
        mPresenters.add(presenter);
    }

    //function to remove presenter from the data set based on provided index
    public void removePresenter(int position) {
        mPresenters.remove(position);
    }

    //function to remove presenter from data set based on provided presenter
    public void removePresenter(CompassDataStructures.Presenter presenter) {
        mPresenters.remove(presenter);
    }

    //function to get list of buildings
    public List<CompassDataStructures.Building> getBuildings() {
        return mBuildings;
    }

    //function to set list of buildings
    public void setBuildings(List<CompassDataStructures.Building> buildings) {
        mBuildings = buildings;
    }

    //function to add building to the data set
    public void addBuilding(CompassDataStructures.Building building) {
        mBuildings.add(building);
    }

    //fucntion to remove building from the data set based on provided index
    public void removeBuilding(int position) {
        mBuildings.remove(position);
    }

    //function to remove building from the data set based on provided building
    public void removeBuilding(CompassDataStructures.Building building) {
        mBuildings.remove(building);
    }

    //function to get a list of active buildings
    public List<CompassDataStructures.Building> getActiveBuildings() {
        List<CompassDataStructures.Building> buildingList = new ArrayList<CompassDataStructures.Building>();

        //select active buildings
        for (CompassDataStructures.Building building : mBuildings) {
            if (building.Is_Active) {
                buildingList.add(building);
            }
        }

        //sort list by building name
        Collections.sort(buildingList, new Comparator<CompassDataStructures.Building>() {
            @Override
            public int compare(CompassDataStructures.Building t1, CompassDataStructures.Building t2) {
                return t1.Name.compareTo(t2.Name);
            }
        });

        return buildingList;
    }

    //function to save the data as JSON to a cache file
    public void saveAsJson() {
        //get the cache file
        File file = new File(mContext.getCacheDir(), "ChamplainCompass.json");

        try {
            //prepare to save the data
            FileOutputStream outputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            mTimestamp = Calendar.getInstance().getTime();

            //write to the file
            objectOutputStream.writeObject(mTimestamp);
            objectOutputStream.writeObject(mOrientationThemes);
            objectOutputStream.writeObject(mEvents);
            objectOutputStream.writeObject(mFrequentlyAskedQuestions);
            objectOutputStream.writeObject(mOrientationResources);
            objectOutputStream.writeObject(mPresenters);
            objectOutputStream.writeObject(mBuildings);

            //close the stream
            objectOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            Log.e(sTAG, e.getMessage());
        }
    }

    //function to load the data from JSON cache
    public void loadFromJson() {
        //get the cache file
        File file = new File(mContext.getCacheDir(), "ChamplainCompass.json");

        try {
            //prepare to pull data
            FileInputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            //write to the file
            mTimestamp = ((Date) objectInputStream.readObject());
            mOrientationThemes = (HashMap<String, CompassDataStructures.OrientationTheme>) objectInputStream.readObject();
            mEvents = (List<CompassDataStructures.Event>) objectInputStream.readObject();
            mFrequentlyAskedQuestions = (List<CompassDataStructures.FrequentlyAskedQuestion>) objectInputStream.readObject();
            mOrientationResources = (List<CompassDataStructures.OrientationResource>) objectInputStream.readObject();
            mPresenters = (List<CompassDataStructures.Presenter>) objectInputStream.readObject();
            mBuildings =  (List<CompassDataStructures.Building>) objectInputStream.readObject();

            //close the stream
            objectInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            Log.e(sTAG, e.getMessage());
        }
    }

    //function to determine if there is cached data
    public boolean isCached() {
        //get the cache file
        File file = new File(mContext.getCacheDir(), "ChamplainCompass.json");

        //prepare dates for comparison
        Date currentTime = Calendar.getInstance().getTime();
        Calendar currentCalendar = Calendar.getInstance();
        Calendar timestampCalendar = Calendar.getInstance();

        currentCalendar.setTime(currentTime);
        timestampCalendar.setTime(mTimestamp);

        //determine if file exists and if the data in the cache has expired
        return file.exists() && timestampCalendar.get(Calendar.HOUR_OF_DAY) > currentCalendar.get(Calendar.HOUR_OF_DAY) - 1 ;
    }

    //fucntion to clear the current set of data from the system
    public void clear() {
        mOrientationThemes = new HashMap<String, CompassDataStructures.OrientationTheme>();
        mFrequentlyAskedQuestions = new ArrayList<CompassDataStructures.FrequentlyAskedQuestion>();
        mOrientationResources = new ArrayList<CompassDataStructures.OrientationResource>();
        mEvents = new ArrayList<CompassDataStructures.Event>();
        mPresenters = new ArrayList<CompassDataStructures.Presenter>();
        mBuildings = new ArrayList<CompassDataStructures.Building>();
    }
}
