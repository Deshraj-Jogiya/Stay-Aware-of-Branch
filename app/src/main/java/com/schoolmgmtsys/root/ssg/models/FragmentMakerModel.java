package com.schoolmgmtsys.root.ssg.models;

public class FragmentMakerModel {
    public String FragmentTitleShowedOnTabs;
    public String FragmentTitle;
    public Object FragmentObj;

    public FragmentMakerModel(String fragmentTitle, Object fragmentObj,String FragmentTitleShowedOnTabs) {
        FragmentTitle = fragmentTitle;
        FragmentObj = fragmentObj;
        this.FragmentTitleShowedOnTabs = FragmentTitleShowedOnTabs;
    }
}
