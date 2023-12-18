package com.schoolmgmtsys.root.ssg.models;


public class DashPollItemModel {
    public String title;
    public String count;
    public String perc;


    public DashPollItemModel(String title, String count, String perc) {
        this.title = title;
        this.count = count;
        this.perc = perc;
    }
}
