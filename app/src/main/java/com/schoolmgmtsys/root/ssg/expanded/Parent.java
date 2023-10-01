package com.schoolmgmtsys.root.ssg.expanded;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.schoolmgmtsys.root.ssg.models.DashLeadModel;

import java.util.List;

public class Parent implements ParentListItem {

    private String mName;
    private List<DashLeadModel> mLeaders;

    public Parent(String name, List<DashLeadModel> leaders) {
        mName = name;
        mLeaders = leaders;
    }

    public String getName() {
        return mName;
    }

    @Override
    public List<?> getChildItemList() {
        return mLeaders;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
