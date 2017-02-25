package com.example.joni.beaconalerterandroid.jsonentities;

import java.util.Date;

/**
 * Created by Joni on 25.2.2017.
 */
public class Alert {
    private String title;
    private Date time;
    private boolean repeating;
    private boolean isEnabled;
    private String id;
    private boolean[] days;

    public Alert() {

    }

    public Alert(String title, Date time, boolean repeating, boolean isEnabled, String id, boolean[] days) {
        this.title = title;
        this.time = time;
        this.repeating = repeating;
        this.isEnabled = isEnabled;
        this.id = id;
        this.days = days;
    }
    /*
    public Alert() {

    }

    public Alert() {

    }
    */

    public boolean[] getDays() {return days;}

    public void setDays(boolean[] days) {this.days = days;}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public boolean isEnabled() {return isEnabled;}

    public void setIsEnabled(boolean isEnabled) {this.isEnabled = isEnabled;}

    public boolean isRepeating() {return repeating;}

    public void setRepeating(boolean repeating) {this.repeating = repeating;}

    public Date getTime() {return time;}

    public void setTime(Date time) {this.time = time;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}




}
