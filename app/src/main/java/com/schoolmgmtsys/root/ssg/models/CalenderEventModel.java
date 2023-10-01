package com.schoolmgmtsys.root.ssg.models;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class CalenderEventModel {

    public String id;
    public String title;
    public String start;
    public String url;
    public String backgroundColor;
    public String textColor;
    public String allDay;

    public String onlyDate;

    public CalenderEventModel(String id, String title, String start, String url, String backgroundColor, String textColor, String allDay) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.url = url;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.allDay = allDay;
    }

    public CalenderEventModel() {
    }
}
