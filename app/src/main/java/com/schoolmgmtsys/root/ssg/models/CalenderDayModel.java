package com.schoolmgmtsys.root.ssg.models;

import java.util.ArrayList;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class CalenderDayModel {

    public String dayName;
    public Integer dayIndex;
    public String startDayNameInItsCollection;
    public String endDayNameInItsCollection;
    public ArrayList<CalenderEventModel> dayEvents = new ArrayList<>();

    public CalenderDayModel(String dayName, Integer dayIndex, String startDayNameInItsCollection, String endDayNameInItsCollection) {
        this.dayName = dayName;
        this.dayIndex = dayIndex;
        this.startDayNameInItsCollection = startDayNameInItsCollection;
        this.endDayNameInItsCollection = endDayNameInItsCollection;
    }



}
