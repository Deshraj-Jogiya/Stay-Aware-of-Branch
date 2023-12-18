package com.schoolmgmtsys.root.ssg.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.schoolmgmtsys.root.ssg.models.ClassesSchModel;
import com.schoolmgmtsys.root.ssg.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {


    private final String[] Days;
    private final HashMap<Integer, ArrayList<ClassesSchModel>> classSchMap;
    private final ArrayList<String> daysNames;

    public CustomPagerAdapter(FragmentManager fm, Context context, HashMap<Integer, ArrayList<ClassesSchModel>> classSchMap,ArrayList<String> daysNames) {
        super(fm);
        this.classSchMap = classSchMap;
        this.daysNames = daysNames;
        Days = context.getResources().getStringArray(R.array.days);
    }

    @Override
    public int getCount() {
        return Days.length;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle extras = new Bundle();

        extras.putParcelableArrayList("schArrayList", classSchMap.get(position));
        ClassesSchTabs fg = new ClassesSchTabs();
        fg.setArguments(extras);
        return fg;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(daysNames.size() > position)return daysNames.get(position);
        else return "Day "+position +1;
    }
}