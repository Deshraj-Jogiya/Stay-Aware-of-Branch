package com.schoolmgmtsys.root.ssg.models;

import com.google.gson.JsonArray;

public class DashPollModel {
    public String id;
    public String pollTitle;
    public String view;
    public String voted;
    public String totalCount;
    public JsonArray items;

    public DashPollModel(String id, String pollTitle, String view, String voted, String totalCount, JsonArray items) {
        this.id = id;
        this.pollTitle = pollTitle;
        this.view = view;
        this.voted = voted;
        this.totalCount = totalCount;
        this.items = items;
    }
}
