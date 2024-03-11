package com.schoolmgmtsys.root.ssg.models;

public class DashNewsModel {
    public Integer id = 0;
    public String Title = "";
    public String Type = "";
    public String Start = "";
    public boolean isNews;

    public DashNewsModel(Integer id, String Title, String Type, String Start,boolean isNews) {
        super();
        if(id != null) this.id = id;
        if(Title != null) this.Title = Title;
        if(Type != null) this.Type = Type;
        if(Start != null) this.Start = Start;
        this.isNews = isNews;
    }
}
