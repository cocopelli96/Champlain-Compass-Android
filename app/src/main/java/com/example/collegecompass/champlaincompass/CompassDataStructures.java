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

import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CompassDataStructures {

    //class for themes
    public static class OrientationTheme implements Serializable {

        //instance variables
        public String Semester;
        public String Theme_Name;
        public String Description;
        public boolean Is_Current;
        public Logo Logo;
        public ThemeColors Theme_Colors;

        //constructor
        public OrientationTheme() {

        }
    }

    //class for logos
    public static class Logo implements Serializable {

        //instance variables
        public String File_Name;
        public String Description;

        //constructor
        public Logo() {

        }

        //constructor
        public Logo(String file_Name, String description) {
            this.File_Name = file_Name;
            this.Description = description;
        }
    }

    //class for theme colors
    public static class ThemeColors implements Serializable {

        //instance variables
        public String Primary;
        public String Secondary;
        public String Text;
        public String Text_Secondary;
        public String Title;
        public String Shadow;
        public String Text_Click;

        //constructor
        public ThemeColors() {

        }
    }

    //class for events
    public static class Event implements Serializable {

        //class variables
        private static final String sTAG = "CompassEvent";

        //instance variables
        public String Name;
        public String Description;
        public String Location;
        public String Presenter;
        public String Start_Time;
        public String End_Time;
        public List<String> Groups;

        //constructor
        public Event() {

        }

        //function to get event start date
        public Date getStartTime() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            try {
                Date date = format.parse(Start_Time);
                return date;
            } catch (ParseException e) {
                Log.e(sTAG, e.getMessage());
                return null;
            }
        }

        //function to get event end date
        public Date getEndTime() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            try {
                Date date = format.parse(End_Time);
                return date;
            } catch (ParseException e) {
                Log.e(sTAG, e.getMessage());
                return null;
            }
        }
    }

    //class for resources
    public static class OrientationResource implements Serializable {

        //instance variables
        public String Name;
        public String Description;
        public String File_Name;
        public String File_Type;
        public boolean Is_Active;

        //constructor
        public OrientationResource() {

        }
    }

    //class for questions
    public static class FrequentlyAskedQuestion implements Serializable {

        //instance variables
        public String Question;
        public String Answer;
        public boolean Is_Active;

        //constructor
        public FrequentlyAskedQuestion() {

        }
    }

    //class for presenters
    public static class Presenter implements Serializable {

        //instance variables
        public String Name;
        public String Job_Title;
        public String Bio;

        //constructor
        public Presenter() {

        }
    }

    //class for buildings
    public static class Building implements Serializable {

        //instance variables
        public String Name;
        public String Address;
        public boolean Is_Active;

        //constructor
        public Building() {

        }
    }

}
